package org.opendataspace.android.app.session;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.services.impl.AbstractDocumentFolderServiceImpl;
import org.alfresco.mobile.android.api.services.impl.AbstractPersonService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.opendataspace.android.app.R;
import org.opendataspace.android.ui.logging.OdsLog;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.thirdparty.imagezoom.ImageViewTouch;
import org.alfresco.mobile.android.ui.utils.thirdparty.DiskLruCache;
import org.alfresco.mobile.android.ui.utils.thirdparty.DiskLruCache.Editor;
import org.alfresco.mobile.android.ui.utils.thirdparty.DiskLruCache.Snapshot;
import org.alfresco.mobile.android.ui.utils.thirdparty.LruCache;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * Utility class for downloading content and display it.
 */
public class OdsRenditionManager extends RenditionManager
{

    private static final String TAG = "RenditionManager";

    private Activity context;

    private AlfrescoSession session;

    private DisplayMetrics dm;

    private LruCache<String, Bitmap> mMemoryCache;

    private DiskLruCache mDiskCache;

    // 10MB
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10;

    private static final String DISK_CACHE_SUBDIR = "renditions";

    public static final int TYPE_NODE = 0;

    public static final int TYPE_PERSON = 1;

    public static final int TYPE_WORKFLOW = 2;

    public OdsRenditionManager(Activity context, AlfrescoSession session)
    {
        super(context, session);
        this.context = context;
        this.session = session;

        dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        // Use 1/10th of the available memory for this memory cache.
        final int cacheSize = 1024 * 1024 * memClass / 10;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize)
                {
            @Override
            protected int sizeOf(String key, Bitmap bitmap)
            {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
                };

                try
                {
                    File cacheDir = StorageManager.getCacheDir(context, DISK_CACHE_SUBDIR);
                    mDiskCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
                    mDiskCache.delete();
                    mDiskCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
                }
                catch (IOException e)
                {
                    OdsLog.w(TAG, e.getMessage());
                }
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap)
    {
        if (key == null || bitmap == null) { return; }
        String hashKey = StorageManager.md5(key);
        if (getBitmapFromMemCache(hashKey) == null)
        {
            mMemoryCache.put(hashKey, bitmap);
            // Log.d(TAG, "Add MemoryCache : " + key);
        }
    }

    private void addBitmapToDiskMemoryCache(String key, ContentStream cf)
    {
        if (key == null || key.isEmpty()) { return; }
        String hashKey = StorageManager.md5(key);
        try
        {
            if (mDiskCache != null && mDiskCache.get(hashKey) == null)
            {
                Editor editor = mDiskCache.edit(hashKey);

                IOUtils.copyStream(cf.getInputStream(), editor.newOutputStream(0));
                editor.commit();
            }
            // Log.d(TAG, "Add DiskCache : " + key);
        }
        catch (Exception e)
        {
            OdsLog.exw(TAG, e);
        }
    }

    private Bitmap getBitmapFromMemCache(String key)
    {
        if (key == null || key.isEmpty()) { return null; }
        String hashKey = StorageManager.md5(key);
        return mMemoryCache.get(hashKey);
    }

    private Bitmap getBitmapFromDiskCache(String key)
    {
        return getBitmapFromDiskCache(key, null);
    }

    private Bitmap getBitmapFromDiskCache(String key, Integer preview)
    {
        if (key == null || key.isEmpty()) { return null; }
        String hashKey = StorageManager.md5(key);
        Snapshot snapshot = null;
        try
        {
            snapshot = mDiskCache.get(hashKey);
            if (snapshot != null)
            {
                // Log.d(TAG, "GET DiskCache : " + key);
                if (preview != null)
                {
                    return decodeStream(mDiskCache, hashKey, preview, dm);
                }
                else
                {
                    return decodeStream(snapshot.getInputStream(0), dm);
                }
            }
        }
        catch (IOException e)
        {
            OdsLog.exw(TAG, e);
        }
        return null;
    }

    @Override
    protected void display(final ImageView iv, String identifier, int initDrawableId, int type, Integer preview,
            ScaleType scaleType)
    {
        String imageKey = identifier;
        if (imageKey != null && preview != null)
        {
            imageKey = "L" + identifier;
        }
        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null)
        {
            iv.setScaleType(scaleType);
            iv.setImageBitmap(bitmap);
            if (preview != null && iv instanceof ImageViewTouch)
            {
                ((ImageViewTouch) iv).setScaleEnabled(true);
                ((ImageViewTouch) iv).setDoubleTapEnabled(true);
                ((View)iv.getTag()).setVisibility(View.GONE);
            }
            // Log.d(TAG, "Cache : " + identifier);
        }
        else if (cancelPotentialWork(identifier, iv))
        {
            final BitmapThread thread = new BitmapThread(context, session, iv, identifier, type, preview, scaleType);
            thread.setPriority(Thread.MIN_PRIORITY);
            if (preview != null)
            {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), initDrawableId);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), bm, thread);
            iv.setImageDrawable(asyncDrawable);
            if (thread.getState() == Thread.State.NEW)
            {
                thread.start();
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }

        // Log.d(TAG, "height:" + height + "width" + width);

        return inSampleSize;
    }

    private static Bitmap decodeStream(DiskLruCache mDiskCache, String snap, int requiredSize, DisplayMetrics dm)
    {
        InputStream fis = null;
        Bitmap bmp = null;
        try
        {
            fis = new BufferedInputStream(mDiskCache.get(snap).getInputStream(0));
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            // Find the correct scale value. It should be the power of 2.
            int requiredSizePx = DisplayUtils.getDPI(dm, requiredSize);
            int scale = calculateInSampleSize(o, requiredSizePx, requiredSizePx);
            //Log.d(TAG, "Scale:" + scale + " Px" + requiredSizePx + " Dpi" + dm.densityDpi);

            // decode with inSampleSize
            fis = new BufferedInputStream(mDiskCache.get(snap).getInputStream(0));
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            o.inPreferredConfig = Bitmap.Config.ARGB_8888;
            o.inTargetDensity = dm.densityDpi;
            o.inJustDecodeBounds = false;
            o.inPurgeable = true;
            bmp = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        }
        catch (Exception e)
        {
            OdsLog.exw(TAG, e);
        }
        finally
        {
            IOUtils.closeStream(fis);
        }
        return bmp;
    }

    private static Bitmap decodeStream(InputStream is, DisplayMetrics dm)
    {
        if (is == null) { return null; }
        try
        {
            BufferedInputStream bis = new BufferedInputStream(is);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inDither = false;
            o.inScaled = false;
            o.inPreferredConfig = Bitmap.Config.ARGB_8888;
            o.inTargetDensity = dm.densityDpi;
            o.inPurgeable = true;
            return BitmapFactory.decodeStream(bis, null, o);
        }
        catch (Exception e)
        {
            OdsLog.exw(TAG, e);
        }
        finally
        {
            IOUtils.closeStream(is);
        }
        return null;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    private class BitmapThread extends Thread
    {
        private final WeakReference<ImageView> imageViewReference;

        private String identifier;

        private AlfrescoSession session;

        private Activity ctxt;

        private String username;

        private String processId;

        private Integer preview;

        private ScaleType scaleType;

        public BitmapThread(Activity ctxt, AlfrescoSession session, ImageView imageView, String identifier, int type,
                Integer preview, ScaleType scaleType)
        {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            this.imageViewReference = new WeakReference<ImageView>(imageView);
            this.session = session;
            this.ctxt = ctxt;
            this.scaleType = scaleType;

            if (type == TYPE_NODE)
            {
                this.identifier = identifier;
            }
            else if (type == TYPE_PERSON)
            {
                this.username = identifier;
            }
            else if (type == TYPE_WORKFLOW)
            {
                this.processId = identifier;
            }
            this.preview = preview;
        }

        private String getBmId()
        {
            if (identifier != null)
            {
                return identifier;
            }
            else if (username != null) { return username; }
            return null;
        }

        // Decode image in background.
        @Override
        public void run()
        {
            try
            {

                if (session == null) { return; }
                if (isInterrupted()) { return; }

                Bitmap bm = null;
                ContentStream cf = null;
                String key = getBmId();
                if (preview != null)
                {
                    key = "L" + getBmId();
                }

                if (mDiskCache != null)
                {
                    bm = getBitmapFromDiskCache(key);
                }

                if (bm == null)
                {
                    if (identifier != null)
                    {
                        try
                        {
                            String renditionId = DocumentFolderService.RENDITION_THUMBNAIL;
                            if (preview != null)
                            {
                                renditionId = DocumentFolderService.RENDITION_PREVIEW;
                            }

                            if (isInterrupted()) { return; }
                            cf = ((AbstractDocumentFolderServiceImpl) session.getServiceRegistry()
                                    .getDocumentFolderService()).getRenditionStream(identifier, renditionId);
                        }
                        catch (AlfrescoServiceException e)
                        {
                            cf = null;
                        }
                    }
                    else if (username != null)
                    {
                        try
                        {
                            cf = ((AbstractPersonService) session.getServiceRegistry().getPersonService())
                                    .getAvatarStream(username);
                            key = username;
                        }
                        catch (AlfrescoServiceException e)
                        {
                            cf = null;
                        }
                    }
                    else if (processId != null)
                    {
                        try
                        {
                            cf = session.getServiceRegistry().getWorkflowService().getProcessDiagram(processId);
                            key = processId;
                        }
                        catch (AlfrescoServiceException e)
                        {
                            cf = null;
                        }
                    }
                    if (cf != null && cf.getInputStream() != null)
                    {
                        if (mDiskCache != null)
                        {
                            if (isInterrupted()) { return; }
                            addBitmapToDiskMemoryCache(key, cf);
                            bm = getBitmapFromDiskCache(key, preview);
                        }
                        else
                        {
                            bm = decodeStream(cf.getInputStream(), dm);
                        }
                    }
                }

                if (imageViewReference != null && imageViewReference.get() != null
                        && imageViewReference.get().getContext() instanceof Activity)
                {
                    addBitmapToMemoryCache(key, bm);
                    if (!isInterrupted())
                    {

                        ((Activity) imageViewReference.get().getContext()).runOnUiThread(new BitmapDisplayer(bm,
                                preview, imageViewReference, this));
                    }
                }
                else if (imageViewReference != null && imageViewReference.get() != null && ctxt != null)
                {
                    ctxt.runOnUiThread(new BitmapDisplayer(bm, preview, imageViewReference, this));
                }
            }
            catch (Exception e)
            {
                OdsLog.exw(TAG, e);
            }
        }

        public ScaleType getScaleType()
        {
            return scaleType;
        }
    }

    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        private Bitmap bitmap;

        private ImageView imageView;

        private Integer preview;

        private WeakReference<ImageView> imageViewReference;

        private BitmapThread bitmapTask;

        public BitmapDisplayer(Bitmap b, Integer p, WeakReference<ImageView> im, BitmapThread bt)
        {
            bitmap = b;
            imageView = im.get();
            preview = p;
            imageViewReference = im;
            bitmapTask = bt;
        }

        public void run()
        {
            if (imageViewReference != null && bitmap != null)
            {
                imageView = imageViewReference.get();
                imageView.setScaleType(bitmapTask.getScaleType());
                final BitmapThread bitmapWorkerTask = getBitmapThread(imageView);
                if (bitmapTask.equals(bitmapWorkerTask) && imageView != null)
                {
                    imageView.setImageBitmap(bitmap);

                    // We create preview with a shadow effect around the image.
                    if (preview != null && imageView instanceof ImageViewTouch)
                    {
                        ((ImageViewTouch) imageView).setScaleEnabled(true);
                        ((ImageViewTouch) imageView).setDoubleTapEnabled(true);
                        ((View) imageView.getTag()).setVisibility(View.GONE);
                    }
                }
            }
            else if (preview != null && bitmap == null)
            {
                imageView = imageViewReference.get();
                if (imageView != null && ((ViewGroup) imageView.getParent()).findViewById(R.id.preview_message) != null)
                {
                    ((ViewGroup) imageView.getParent()).findViewById(R.id.preview_message).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    public static class AsyncDrawable extends BitmapDrawable
    {
        private final WeakReference<BitmapThread> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapThread bitmapThread)
        {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapThread>(bitmapThread);
        }

        public BitmapThread getBitmapWorkerTask()
        {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView)
    {
        final BitmapThread bitmapWorkerTask = getBitmapThread(imageView);

        if (bitmapWorkerTask != null)
        {
            final String bitmapData = bitmapWorkerTask.identifier;
            if (bitmapData != null && !bitmapData.equals(data))
            {
                bitmapWorkerTask.interrupt();
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    private static BitmapThread getBitmapThread(ImageView imageView)
    {
        if (imageView != null)
        {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable)
            {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}
