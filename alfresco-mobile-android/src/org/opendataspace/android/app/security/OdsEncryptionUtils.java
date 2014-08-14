package org.opendataspace.android.app.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.exception.AlfrescoAppException;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.opendataspace.android.ui.logging.OdsLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class OdsEncryptionUtils
{
    private static final String TAG = OdsEncryptionUtils.class.getName();

    private static final byte[] SALT = { 0x0A, 0x02, 0x13, 0x3C, 0x3B, 0x0F, 0x1A };

    private static final int COUNT = 10;

    private static final byte[] REFERENCE_DATA = "AlfrescoCrypto".getBytes(Charset.forName("UTF-8"));

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final int KEY_LENGTH = 128;

    private static final String KEYSTORE_FILE = "keystore";

    private static final String DEFAULT_ALIAS = "local_files";

    private static SecretKey info = null;

    private static final int MAX_BUFFER_SIZE = 10240;

    private static ArrayList<String> filesDecrypted = null;

    private static final String DECRYPTION_EXTENSION = ".utmp";

    private static ArrayList<String> filesEncrypted = null;

    private static final String ENCRYPTION_EXTENSION = ".etmp";

    private static final String DEFAULT_PASSWORD = "ocya1QNlUI%4gn3b^6ZnLzFZ";

    private OdsEncryptionUtils()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DETECTION
    // ///////////////////////////////////////////////////////////////////////////
    public static boolean isEncrypted(String filename)
    {
        return (filename.endsWith(DECRYPTION_EXTENSION) || filename.endsWith(ENCRYPTION_EXTENSION));
    }

    public static boolean isEncrypted(Context ctxt, String filename) throws IOException
    {
        File source = new File(filename);
        InputStream sourceFile = testInputStream(new FileInputStream(source), getKey());

        if (sourceFile != null)
        {
            sourceFile.close();
            return true;
        }

        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DECRYPTION
    // ///////////////////////////////////////////////////////////////////////////
    public static boolean decryptFile(Context ctxt, String filename) throws AlfrescoAppException
    {
        return decryptFile(ctxt, filename, null);
    }

    public static boolean decryptFile(Context ctxt, String filename, String newFilename) throws AlfrescoAppException
    {
        boolean ret = true;
        OutputStream destStream = null;
        InputStream sourceStream = null;
        try
        {
            if (isEncrypted(ctxt, filename))
            {
                File source = new File(filename);
                long size = source.length();
                long copied = 0;
                File dest = new File(newFilename != null ? newFilename : filename + ".utmp");
                sourceStream = wrapCipherInputStream(new FileInputStream(source), getKey());
                destStream = new FileOutputStream(dest);
                int nBytes = 0;

                byte[] buffer = new byte[MAX_BUFFER_SIZE];

                OdsLog.i(TAG, "Decrypting file " + filename);

                while (size - copied > 0)
                {
                    if (size - copied < MAX_BUFFER_SIZE)
                    {
                        buffer = new byte[(int) (size - copied)];
                    }
                    nBytes = sourceStream.read(buffer);
                    if (nBytes == -1)
                    {
                        break;
                    }
                    else if (nBytes > 0)
                    {
                        destStream.write(buffer);
                    }
                }

                sourceStream.close();
                destStream.close();

                if (newFilename == null)
                {
                    if (source.delete())
                    {
                        // Rename decrypted file to original name.
                        if (!(ret = dest.renameTo(source)))
                        {
                            OdsLog.e(TAG, "Cannot rename decrypted file " + dest.getName());
                        }
                    }
                    else
                    {
                        OdsLog.e(TAG, "Cannot delete original file " + source.getName());
                        dest.delete();
                        ret = false;
                    }
                }
            }
            else
            {
                OdsLog.w(TAG, "File is already decrypted: " + filename);
                return true;
            }
        }
        catch (Exception e)
        {
            IOUtils.closeStream(sourceStream);
            IOUtils.closeStream(destStream);
            throw new AlfrescoAppException(-1, e);
        }
        return ret;
    }

    /*
     * Encrypt an entire folder, recursively if required. Rollback is
     * implemented if any failures occur. NOTE: This method is not thread-safe.
     */
    public static boolean decryptFiles(Context ctxt, String sourceFolder, boolean recursive)
    {
        boolean startPoint = false;
        boolean result = true;

        if (filesDecrypted == null)
        {
            filesDecrypted = new ArrayList<String>();
            startPoint = true;
        }
        try
        {
            File f = new File(sourceFolder);
            File file[] = f.listFiles();

            for (int i = 0; i < file.length; i++)
            {
                File sourceFile = file[i];
                String destFilename = file[i].getPath() + DECRYPTION_EXTENSION;

                if (!sourceFile.isHidden())
                {
                    if (sourceFile.isFile())
                    {
                        result = decryptFile(ctxt, sourceFile.getPath(), destFilename);
                        if (result)
                        {
                            filesDecrypted.add(sourceFile.getPath());
                        }
                    }
                    else
                    {
                        if (sourceFile.isDirectory() && recursive && !sourceFile.getName().equals(".")
                                && !sourceFile.getName().equals(".."))
                        {
                            result = decryptFiles(ctxt, sourceFile.getPath(), recursive);
                        }
                    }

                    if (!result)
                    {
                        if (filesDecrypted != null)
                        {
                            OdsLog.e(TAG, "Folder decryption failed for " + sourceFile.getName());

                            // Remove the decrypted versions done so far.
                            OdsLog.d(TAG, "Decryption rollback in progress...");
                            for (int j = 0; j < filesDecrypted.size(); j++)
                            {
                                if (new File(filesDecrypted.get(j) + DECRYPTION_EXTENSION).delete())
                                {
                                    OdsLog.w(TAG, "Deleted decrypted version of " + filesDecrypted.get(j));
                                }
                            }
                            filesDecrypted.clear();
                            filesDecrypted = null;
                        }

                        break;
                    }
                }
            }

            if (result && startPoint)
            {
                // Whole folder decrypt succeeded. Move over to new decrypted
                // versions.
                File src = null, dest = null, tempSrc = null;
                Uri uri = null;
                Cursor favoriteCursor = null;
                ContentValues cValues = null;
                int statut = 0;

                for (int j = 0; j < filesDecrypted.size(); j++)
                {
                    src = new File(filesDecrypted.get(j));
                    dest = new File(filesDecrypted.get(j) + DECRYPTION_EXTENSION);

                    //
                    // Two-stage delete for failsafe operation.
                    //
                    tempSrc = new File(filesDecrypted.get(j) + ".mov");
                    if (src.renameTo(tempSrc))
                    {
                        // Put decrypted version in originals place.
                        if (dest.renameTo(src))
                        {
                            // Delete the original decrypted temp file.
                            if (!tempSrc.delete())
                            {
                                OdsLog.w(TAG, "Could not delete original file " + tempSrc.getPath());
                            }

                            // If the file lives in Sync folder
                            if (StorageManager.isSynchroFile(ctxt, src))
                            {
                                try
                                {
                                    favoriteCursor = ctxt.getContentResolver().query(
                                            SynchroProvider.CONTENT_URI,
                                            SynchroSchema.COLUMN_ALL,
                                            SynchroSchema.COLUMN_LOCAL_URI + " LIKE '" + Uri.fromFile(src).toString()
                                            + "%'", null, null);

                                    if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToFirst())
                                    {
                                        statut = favoriteCursor.getInt(SynchroSchema.COLUMN_STATUS_ID);
                                        if (statut != SyncOperation.STATUS_MODIFIED)
                                        {
                                            uri = Uri.parse(SynchroProvider.CONTENT_URI + "/"
                                                    + favoriteCursor.getLong(SynchroSchema.COLUMN_ID_ID));
                                            if (cValues == null)
                                            {
                                                cValues = new ContentValues();
                                            }
                                            cValues.put(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP,
                                                    src.lastModified());
                                            ctxt.getContentResolver().update(uri, cValues, null, null);
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                                finally
                                {
                                    if (favoriteCursor != null)
                                    {
                                        favoriteCursor.close();
                                    }
                                }
                            }
                        }
                        else
                        {
                            tempSrc.renameTo(src);
                        }
                    }
                }
                filesDecrypted.clear();
                filesDecrypted = null;
            }

            return result;
        }
        catch (Exception e)
        {
            OdsLog.exw(TAG, e);
            return false;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ENCRYPTION
    // ///////////////////////////////////////////////////////////////////////////
    /*
     * Encrypt file in place, leaving original file unencrypted.
     */
    public static boolean encryptFile(Context ctxt, String filename, boolean nuke) throws AlfrescoAppException
    {
        return encryptFile(ctxt, filename, null, nuke);
    }

    /*
     * Encrypt file in place, leaving no trace of original unencrypted data.
     * filename file to encrypt nuke whether to zero the original unencrypted
     * file before attempting its deletion, for additional security.
     */
    public static boolean encryptFile(Context ctxt, String filename, String newFilename, boolean nuke) throws AlfrescoAppException
    {
        boolean ret = true;
        OutputStream destStream = null;
        InputStream sourceStream = null;
        try
        {

            if (!isEncrypted(ctxt, filename))
            {
                File source = new File(filename);
                long size = source.length();
                long copied = 0;
                File dest = new File(newFilename != null ? newFilename : filename + ".etmp");
                sourceStream = new FileInputStream(source);
                destStream = wrapCipherOutputStream(new FileOutputStream(dest), getKey());
                int nBytes = 0;
                byte buffer[] = new byte[MAX_BUFFER_SIZE];

                OdsLog.i(TAG, "Encrypting file " + filename);

                while (size - copied > 0)
                {
                    if (size - copied < MAX_BUFFER_SIZE)
                    {
                        buffer = new byte[(int) (size - copied)];
                    }
                    nBytes = sourceStream.read(buffer);
                    if (nBytes == -1)
                    {
                        break;
                    }
                    else if (nBytes > 0)
                    {
                        destStream.write(buffer);
                    }
                }

                sourceStream.close();
                destStream.flush();
                destStream.close();

                if (newFilename == null)
                {
                    if (nuke)
                    {
                        nukeFile(source, size);
                    }

                    if (source.delete())
                    {
                        // Rename encrypted file to original name.
                        if (!(ret = dest.renameTo(source)))
                        {
                            OdsLog.e(TAG, "Cannot rename encrypted file " + dest.getName());
                        }
                    }
                    else
                    {
                        OdsLog.e(TAG, "Cannot delete original file " + source.getName());

                        dest.delete();
                        ret = false;
                    }
                }

                return ret;
            }
            else
            {
                OdsLog.w(TAG, "File is already encrypted: " + filename);
                return true;
            }
        }
        catch (Exception e)
        {
            IOUtils.closeStream(sourceStream);
            IOUtils.closeStream(destStream);
            throw new AlfrescoAppException(-1, e);
        }
    }

    /*
     * Encrypt an entire folder, recursively if required. Rollback is
     * implemented if any failures occur. NOTE: This method is not thread-safe.
     */
    public static boolean encryptFiles(Context ctxt, String sourceFolder, boolean recursive)
    {
        boolean startPoint = false;
        boolean result = true;

        if (filesEncrypted == null)
        {
            filesEncrypted = new ArrayList<String>();
            startPoint = true;
        }
        try
        {
            File f = new File(sourceFolder);
            File file[] = f.listFiles();

            for (int i = 0; i < file.length; i++)
            {
                File sourceFile = file[i];
                String destFilename = file[i].getPath() + ENCRYPTION_EXTENSION;

                if (!sourceFile.isHidden())
                {
                    if (sourceFile.isFile())
                    {
                        result = encryptFile(ctxt, sourceFile.getPath(), destFilename, true);
                        if (result)
                        {
                            filesEncrypted.add(sourceFile.getPath());
                        }
                    }
                    else
                    {
                        if (sourceFile.isDirectory() && recursive && !sourceFile.getName().equals(".")
                                && !sourceFile.getName().equals(".."))
                        {
                            result = encryptFiles(ctxt, sourceFile.getPath(), recursive);
                        }
                    }

                    if (!result)
                    {
                        if (filesEncrypted != null)
                        {
                            OdsLog.e(TAG, "Folder encryption failed for " + sourceFile.getName());

                            // Remove the encrypted versions done so far.
                            OdsLog.i(TAG, "Encryption rollback in progress...");
                            for (int j = 0; j < filesEncrypted.size(); j++)
                            {
                                if (new File(filesEncrypted.get(j) + ENCRYPTION_EXTENSION).delete())
                                {
                                    OdsLog.i(TAG, "Deleted encrypted version of " + filesEncrypted.get(j));
                                }
                            }
                            filesEncrypted.clear();
                            filesEncrypted = null;
                        }

                        break;
                    }
                }
            }

            if (result && startPoint)
            {
                // Whole folder encrypt succeeded. Move over to new encrypted
                // versions.

                for (int j = 0; j < filesEncrypted.size(); j++)
                {
                    File src = new File(filesEncrypted.get(j));
                    File dest = new File(filesEncrypted.get(j) + ENCRYPTION_EXTENSION);

                    //
                    // Two-stage delete for failsafe operation.
                    //
                    File tempSrc = new File(filesEncrypted.get(j) + ".mov");
                    if (src.renameTo(tempSrc))
                    {
                        // Put encrypted version in originals place.
                        if (dest.renameTo(src))
                        {
                            // Delete the original unencrypted temp file.
                            if (!tempSrc.delete())
                            {
                                // At least rename it out of the way with a temp
                                // extension, and nuke its content.
                                OdsLog.w(TAG, "Could not delete original file. Nuking and renaming it " + tempSrc.getPath());
                                nukeFile(tempSrc, -1);
                            }
                        }
                        else
                        {
                            tempSrc.renameTo(src);
                        }
                    }
                }
                filesEncrypted.clear();
                filesEncrypted = null;
            }

            return result;
        }
        catch (Exception e)
        {
            OdsLog.e(TAG, "Error during folder encryption: " + e.getMessage());
            OdsLog.exw(TAG, e);

            return false;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private static InputStream testInputStream(InputStream streamIn, SecretKey key)
    {
        try
        {
            Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
            pbeCipher.init(Cipher.DECRYPT_MODE, key);

            int count = streamIn.read();
            if (count <= 0 || count > 1024) { return null; }

            byte[] input = new byte[count];
            streamIn.read(input);
            pbeCipher.doFinal(input).toString().contains(Arrays.toString(REFERENCE_DATA));

            return new CipherInputStream(streamIn, pbeCipher);
        }
        catch (IOException io)
        {
            // nothing
        }
        catch (GeneralSecurityException ge)
        {
            OdsLog.exw(TAG, ge);
        }

        return null;
    }

    public static OutputStream wrapCipherOutputStream(OutputStream streamOut, SecretKey key) throws IOException,
    GeneralSecurityException
    {
        Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);

        /*
         * Write predefined data first (see the reading code)
         */
        byte[] output = pbeCipher.doFinal(REFERENCE_DATA);
        streamOut.write(output.length);
        streamOut.write(output);

        return new CipherOutputStream(streamOut, pbeCipher);
    }

    private static InputStream wrapCipherInputStream(InputStream streamIn, SecretKey key) throws IOException,
    GeneralSecurityException
    {
        Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
        pbeCipher.init(Cipher.DECRYPT_MODE, key);

        /*
         * Read a predefined data block. If the password is incorrect, we'll get
         * a security exception here. Without this, we will only get an
         * IOException later when reading the CipherInputStream, which is not
         * specific enough for a good error message.
         */
        int count = streamIn.read();
        if (count <= 0 || count > 1024) { throw new IOException("Bad encrypted file"); }

        byte[] input = new byte[count];
        streamIn.read(input);
        pbeCipher.doFinal(input);

        return new CipherInputStream(streamIn, pbeCipher);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // KEY GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    private static SecretKey getKey()
    {
        return info;
    }

    public static boolean checkKey(Context ctx, String password)
    {
        try
        {
            KeyStore ks = loadKeyStore(ctx);
            KeyStore.Entry ke = ks.getEntry(DEFAULT_ALIAS, new KeyStore.PasswordProtection(password.toCharArray()));

            if (ke instanceof KeyStore.SecretKeyEntry)
            {
                info = ((KeyStore.SecretKeyEntry) ke).getSecretKey();
                return true;
            }
        }
        catch (Exception ex)
        {
            // nothing
        }

        return false;
    }

    public static boolean generateKey(Context ctx, String password)
    {
        try
        {
            KeyStore ks = loadKeyStore(ctx);

            if (ks.containsAlias(DEFAULT_ALIAS))
            {
                ks.deleteEntry(DEFAULT_ALIAS);
            }

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, COUNT, KEY_LENGTH);
            SecretKey key = new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded(), "AES");

            ks.setEntry(DEFAULT_ALIAS, new KeyStore.SecretKeyEntry(key), new KeyStore.PasswordProtection(password.toCharArray()));
            saveKeyStore(ctx, ks);
            info = key;
            return true;
        }
        catch (Exception ex)
        {
            OdsLog.ex(TAG, ex);
        }

        return false;
    }

    public static boolean removeKey(Context ctx, String password)
    {
        try
        {
            KeyStore ks = loadKeyStore(ctx);

            if (ks.containsAlias(DEFAULT_ALIAS))
            {
                KeyStore.Entry ke = ks.getEntry(DEFAULT_ALIAS, new KeyStore.PasswordProtection(password.toCharArray()));

                if (ke instanceof KeyStore.SecretKeyEntry)
                {
                    ks.deleteEntry(DEFAULT_ALIAS);
                    saveKeyStore(ctx, ks);
                }
                else
                {
                    return false;
                }
            }

            info = null;
            return true;
        }
        catch (Exception ex)
        {
            OdsLog.ex(TAG, ex);
        }

        return false;
    }

    private static KeyStore loadKeyStore(Context ctx) throws Exception
    {
        KeyStore ks = KeyStore.getInstance("BKS");

        FileInputStream fis = null;
        try
        {
            fis = ctx.openFileInput(KEYSTORE_FILE);
        }
        catch (FileNotFoundException e)
        {
            fis = null;
        }

        ks.load(fis, DEFAULT_PASSWORD.toCharArray()); // null will initialize empty keystore
        return ks;
    }

    private static void saveKeyStore(Context ctx, KeyStore ks) throws Exception
    {
        ks.store(ctx.openFileOutput(KEYSTORE_FILE, Context.MODE_PRIVATE), DEFAULT_PASSWORD.toCharArray());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CLEANER
    // ///////////////////////////////////////////////////////////////////////////
    /*
     * Nuke a file with zero's.
     */
    public static void nukeFile(File source, long size) throws Exception
    {
        if (size <= 0)
        {
            size = source.length();
        }

        byte zeros[] = new byte[MAX_BUFFER_SIZE];
        OutputStream destroyFile = new FileOutputStream(source);
        long chunks = (size + MAX_BUFFER_SIZE - 1) / MAX_BUFFER_SIZE;

        for (long i = 0; i < chunks; i++)
        {
            destroyFile.write(zeros);
        }

        destroyFile.flush();
        destroyFile.close();
    }

    public static boolean hasKey()
    {
        return getKey() != null;
    }
}

