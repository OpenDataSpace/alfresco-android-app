/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * <p/>
 * This file is part of Alfresco Mobile for Android.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.capture;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.opendataspace.android.app.R;
import org.opendataspace.android.ui.logging.OdsLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AudioCapture extends DeviceCapture
{
    public static final String TAG = "AudioCapture";

    private static final long serialVersionUID = 1L;

    public AudioCapture(Activity parent, Folder folder)
    {
        this(parent, folder, null);
    }

    public AudioCapture(Activity parent, Folder folder, File parentFolder)
    {
        super(parent, folder, parentFolder);
        // Default MIME type if it cannot be retrieved from Uri later.
        mimeType = "audio/3gpp";
    }

    @Override
    public boolean hasDevice()
    {
        return (parentActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE));
    }

    @Override
    public boolean captureData()
    {
        if (hasDevice())
        {
            try
            {
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                parentActivity.startActivityForResult(intent, getRequestCode());
            }
            catch (Exception e)
            {
                MessengerManager.showLongToast(context, context.getString(R.string.no_voice_recorder));
                OdsLog.exw(TAG, e);
                return false;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected void payloadCaptured(int requestCode, int resultCode, Intent data)
    {
        Uri savedUri = data.getData();

        File folder = parentFolder;
        if (folder != null)
        {
            String filePath = getAudioFilePathFromUri(savedUri);
            String fileType = getAudioFileTypeFromUri(savedUri);
            String newFilePath =
                    folder.getPath() + "/" + createFilename("AUDIO", filePath.substring(filePath.lastIndexOf(".") + 1));

            copyFile(filePath, newFilePath);

            parentActivity.getContentResolver().delete(savedUri, null, null);
            //noinspection ResultOfMethodCallIgnored
            (new File(filePath)).delete();

            payload = new File(newFilePath);

            if (!fileType.isEmpty())
            {
                mimeType = fileType;
            }
        }
        else
        {
            MessengerManager.showLongToast(parentActivity, parentActivity.getString(R.string.sdinaccessible));
        }
    }

    private String getAudioFilePathFromUri(Uri uri)
    {
        Cursor cursor = parentActivity.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
            String res = (index != -1 ? cursor.getString(index) : "");
            cursor.close();
            return res;
        }
        else
        {
            return "";
        }
    }

    private String getAudioFileTypeFromUri(Uri uri)
    {
        Cursor cursor = parentActivity.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE);
            String res = (index != -1 ? cursor.getString(index) : "");
            cursor.close();
            return res;
        }
        else
        {
            return "";
        }
    }

    private void copyFile(String fileName, String newFileName)
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = new FileInputStream(fileName);
            out = new FileOutputStream(newFileName);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
        }
        catch (Exception ignored)
        {

        }
        finally
        {
            IOUtils.closeStream(in);
            IOUtils.closeStream(out);
        }

    }
}
