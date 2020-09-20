package com.jelly.baselibrary.widget.gif;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;

import com.jelly.baselibrary.utils.LogUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

//import com.mediatek.gifDecoder.GifDecoder;

@SuppressLint("AppCompatCustomView")
@RemoteView
public class GifImageView extends ImageView {
    private static final String TAG = "GifImageView";
    private Context mContext;

    private static final String STORE_PIC_TAG = "storePic";
    private static int MAX_WIDTH = 360;
    private static int MAX_HEIGHT = 640;

    /**
     * @param context The Context to attach
     */
    public GifImageView(Context context) {
        super(context);
        this.mContext = context;

        //add for gif animation
        initForGif();
    }

    /**
     * @param context The Context to attach
     * @param attrs   The attribute set
     */
    public GifImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        //add for gif animation
        initForGif();
    }

    /**
     * @param context  The Context to attach
     * @param attrs    The attribute set
     * @param defStyle The used style
     */
    public GifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;

        //add for gif animation
        initForGif();
    }


    /**
     * Register a callback to be invoked when this view is clicked. If this view is not
     * clickable, it becomes clickable.
     *
     * @param action An action be insert into Intent for startService.
     */
    public void setOnClickIntent(final String action) {
        this.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                final float appScale = v.getContext().getResources().getCompatibilityInfo().applicationScale;
//                final int[] pos = new int[2];
//                v.getLocationOnScreen(pos);
//
//                final Rect rect = new Rect();
//                rect.left = (int) (pos[0] * appScale + 0.5f);
//                rect.top = (int) (pos[1] * appScale + 0.5f);
//                rect.right = (int) ((pos[0] + v.getWidth()) * appScale + 0.5f);
//                rect.bottom = (int) ((pos[1] + v.getHeight()) * appScale + 0.5f);
//
//                final Intent intent = new Intent();
//                intent.setAction(action);
//                intent.putExtra("widgetX", pos[0]);
//                intent.putExtra("widgetY", pos[1]);
//                intent.putExtra("widgetWidth", v.getWidth());
//                intent.putExtra("widgetHeight", v.getHeight());
//                intent.setSourceBounds(rect);
//                mContext.startService(intent);
            }
        });
    }

    /**
     * @see android.view.View#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /**
     * @param flag An flag to start or stop the animation of an ImageView. true for start, and false for stop.
     */
    public void setAnimationRunning(boolean flag) {
        Drawable drawable = this.getDrawable();
        if (flag) {
            if (drawable != null && (drawable instanceof AnimationDrawable)) {
                AnimationDrawable tempAD = (AnimationDrawable) drawable;
                if (!tempAD.isRunning())
                    tempAD.start();
            } else {
                ImageView iv = null;
                ViewParent parent = this.getParent();
                if (parent instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) parent;
                    iv = (ImageView) vg.findViewWithTag(STORE_PIC_TAG);
                } else {
                    LogUtil.getInstance().d("test", "ViewParent is not a ViewGroup!");
                    return;
                }

                if (iv != null && (iv instanceof ImageView)) {
                    Drawable d = iv.getBackground();
                    if (d != null && d instanceof AnimationDrawable) {
                        AnimationDrawable ad = (AnimationDrawable) d;
                        this.setImageDrawable(ad);
                        ad.start();
                    }
                } else {
                    LogUtil.getInstance().d("com.mediatek.sns", "Iv is null");
                }
            }
        }
        //stop animation
        else {
            if (drawable != null && (drawable instanceof AnimationDrawable)) {
                AnimationDrawable tempAD = (AnimationDrawable) drawable;
                if (tempAD.isRunning())
                    tempAD.stop();
            }
            ImageView iv = null;
            ViewParent parent = this.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) parent;
                iv = (ImageView) vg.findViewWithTag(STORE_PIC_TAG);
            } else {
                LogUtil.getInstance().d("test", "ViewParent is not a ViewGroup!");
                return;
            }

            if (iv != null && (iv instanceof ImageView)) {
                Drawable d = iv.getDrawable();
                if (d != null /*&& d instanceof BitmapDrawable*/) {
                    this.setImageDrawable(d);
                }
            } else {
                LogUtil.getInstance().d("com.mediatek.sns", "Iv is null");
            }
        }
    }


    private boolean mSetFromGif = false;
    protected int mResourceId;
    protected Uri mUri = null;
    private boolean mResGif = false;
    private boolean mUriGif = false;
    protected InputStream mGifStream = null;
    private ImageView self;
    private int mCurrGifFrame = 0;
    private Thread mAnimationThread;
    //protected GifDecoder mGifDecoder = null;
    protected GifOpenHelper mIGifDecoder = null;
    // This is used to stop the worker thread.
    volatile boolean mAbort = false;
    protected Handler mHandler = new Handler();

    protected void initForGif() {
        self = this;
    }

    private void closeGifStream() {
        //close previous gif stream if any
        try {
            if (null != mGifStream) {
                mGifStream.close();
                mGifStream = null;
            }
        } catch (IOException e) {
            LogUtil.getInstance().d(TAG, "Close GIF InputStream failed, e=" + e);
        }
    }

    private void openGifStream() {
        //close previous gif stream if any
        closeGifStream();
        if (mUriGif == mResGif) {
            LogUtil.getInstance().d(TAG, "openGifStream:not correct status!");
            return;
        }
        if (mResGif) {
            try {
                //Open GIF resource as inputStream
                LogUtil.getInstance().d(TAG, "openGifStream:open new gif strem from " + mResourceId);
                mGifStream = mContext.getResources().openRawResource(mResourceId);
            } catch (Resources.NotFoundException e) {
                LogUtil.getInstance().d(TAG, "Open GIF resource as InputStream failed");
            }
        } else {
            try {
                LogUtil.getInstance().d(TAG, "openGifStream:open new gif strem from " + mUri);
                mGifStream = mContext.getContentResolver().openInputStream(mUri);
            } catch (IOException e) {
                LogUtil.getInstance().d(TAG, "Open GIF URI as InputStream failed");
            }
        }
    }

    /**
     * Sets a drawable as the content of this GifImageView.
     *
     * @param resId the resource identifier of the the drawable
     */
    @SuppressLint("ResourceType")
    public void setImageResource(int resId) {
        LogUtil.getInstance().i(TAG, "setImageResource:abort previous gif animation if any");
        abortAnimationThread();

        InputStream imageStream = null;
        byte[] buffer = new byte[4];
        boolean isGifImage = false;
        try {
            imageStream = mContext.getResources().openRawResource(resId);
            if (3 != imageStream.read(buffer, 0, 3)) {
                LogUtil.getInstance().w(TAG, "can't read data from resource inputstream");
                isGifImage = false;
            } else if (buffer[0] == 'G' && buffer[1] == 'I' && buffer[2] == 'F') {
                isGifImage = true;
            } else {
                isGifImage = false;
            }

            imageStream.close();
            imageStream = null;
        } catch (IOException e) {
            LogUtil.getInstance().d(TAG, "" + e);
        }

        if (false == isGifImage) {
            super.setImageResource(resId);
            return;
        }
        //for gif image resource, we should play animation
        mResourceId = resId;

        //recorded gif stream as a Resource
        mUriGif = false;
        mResGif = true;

        //display the first frame of the gif image
//      super.setImageResource(resId);

        startAnimationThread();
    }

    private class Bound {
        private int width;
        private int height;
    }

    /**
     * corresponding orientation of EXIF to degrees.
     */
    private int getExifRotation(int orientation) {
        int degrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                degrees = 0;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                degrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    // Rotates the bitmap by the specified degree.
    // If a new bitmap is created, the original bitmap is recycled.
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees,
                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                b.setHasAlpha(true);
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

    private Bound decodeBoundsInfo(Uri uri) {
        InputStream input = null;
        Bound bound = new Bound();
        try {
            input = mContext.getContentResolver().openInputStream(uri);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, opt);
            bound.width = opt.outWidth;
            bound.height = opt.outHeight;
            if (bound.width > bound.height) {
                int temp = bound.width;
                int temp2 = MAX_WIDTH;
                MAX_WIDTH = MAX_HEIGHT;
                MAX_HEIGHT = MAX_WIDTH;
                bound.width = bound.height;
                bound.height = bound.width;
            }
        } catch (FileNotFoundException e) {
            // Ignore
            LogUtil.getInstance().e(TAG, "FileNotFoundException e：" + e.toString());
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    LogUtil.getInstance().e(TAG, e.toString());
                }
            }
        }

        return bound;
    }


    private Bitmap resizeAndRotateImage(Uri uri) {
        // here we need compress the image to 640*480 max limit 
        // and rotate it if it has rotation degree.
        Bitmap finalImage = null;
        // get the rotation degree;
        Bound bound = decodeBoundsInfo(uri);
        int imageWidth = bound.width;
        int imageHeight = bound.height;

        int scaleFactor = 1;
//        int degree = decodeDegreeInfo(uri);
//
//        if (degree == 90 || degree == 270) {
//            imageWidth = bound.height;
//            imageHeight = bound.width;
//        }

        if (imageWidth > MAX_WIDTH || imageHeight > MAX_HEIGHT) {
            LogUtil.getInstance().d(TAG, "Image need resize: " + imageWidth + "," + imageHeight);
            do {
                scaleFactor *= 2;
            }
            while ((imageWidth / scaleFactor > MAX_WIDTH) || (imageHeight / scaleFactor > MAX_HEIGHT));
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scaleFactor;
        InputStream input = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);
            if (input != null) {
                try {
                    finalImage = BitmapFactory.decodeStream(input, null, options);
                } catch (OutOfMemoryError ex) {
                    // decode fail because of no memory, return null to invoke default setImageUri.
                    return null;
                }
                if (finalImage == null) {
                    //decode fail, return null will invoke setImageUri, this method can handle bad pictures.
                    return null;
                }
//                try {
//                    finalImage = Bitmap.createScaledBitmap(finalImage, bound.width / scaleFactor,
//                            bound.height / scaleFactor, false);
//                }catch (OutOfMemoryError ex) {
//                    // We have no memory to scale. go on process the original bitmap.
//                }
                //finalImage = rotate(finalImage, degree);
            }
        } catch (FileNotFoundException e) {
            LogUtil.getInstance().e(TAG, e.toString());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    LogUtil.getInstance().e(TAG, e.toString());
                }
            }
        }

        return finalImage;
    }

    /**
     * Sets the content of this ImageView to the specified Uri.
     *
     * @param uri The Uri of an image
     */
    public void setImageURI(Uri uri) {
        LogUtil.getInstance().v(TAG, "setImageURI(uri=" + uri + ")" + " //this=" + this);
        abortAnimationThread();

        if (null == uri) {
            LogUtil.getInstance().d(TAG, "setImageURI:follow ImageView's routin for " + uri);
            super.setImageURI(uri);
            return;
        }

        InputStream imageStream = null;
        byte[] buffer = new byte[4];
        boolean isGifImage = false;
        Bitmap finalImage = null;
        try {
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)
                    || uri.getScheme().equals(ContentResolver.SCHEME_FILE)
                    || uri.getScheme().equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                imageStream = mContext.getContentResolver().openInputStream(uri);
                if (3 != imageStream.read(buffer, 0, 3)) {
                    LogUtil.getInstance().w(TAG, "can't read data from uri inputstream");
                    isGifImage = false;
                } else if (buffer[0] == 'G' && buffer[1] == 'I' && buffer[2] == 'F') {
                    isGifImage = true;
                } else {
                    isGifImage = false;
                    finalImage = resizeAndRotateImage(uri);
                }

                imageStream.close();
                imageStream = null;
            } else {
                LogUtil.getInstance().w(TAG, "Uncoped uri scheme,call ImageView.setImageURI()");
                isGifImage = false;
            }
        } catch (IOException e) {
            LogUtil.getInstance().d(TAG, "" + e);
        }

        LogUtil.getInstance().i(TAG, "setImageURI:isGifImage=" + isGifImage + " //this=" + this);

        if (false == isGifImage) {
            LogUtil.getInstance().d(TAG, "setImageURI:follow ImageView's routin for " + uri);
            if (finalImage != null) {
                super.setImageBitmap(finalImage);
            } else {
                super.setImageURI(uri);
            }
            return;
        }

        //for gif image source, we should play animation
        LogUtil.getInstance().d(TAG, "setImageUri:synchroized lock, start gif animation");
        mUri = uri;

        //recorded gif stream as a Uri
        mUriGif = true;
        mResGif = false;

        //display the first frame of the gif image
//      super.setImageURI(uri);

        startAnimationThread();
    }

    public void setImageUriNotGif(Uri uri) {
        LogUtil.getInstance().v(TAG, "setImageUriNotGif(uri=" + uri + ")" + " //this=" + this);
        if (null == uri) {
            LogUtil.getInstance().d(TAG, "setImageURI:follow ImageView's routin for " + uri);
            super.setImageURI(uri);
            return;
        }
        Bitmap finalImage = resizeAndRotateImage(uri);
        if (finalImage != null) {
            super.setImageBitmap(finalImage);
        } else {
            super.setImageURI(uri);
        }
    }

    public void setImageBitmap(Bitmap bm) {
        if (!mSetFromGif) {
            abortAnimationThread();
        }
        super.setImageBitmap(bm);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (!mSetFromGif) {
            abortAnimationThread();
        }
        super.setImageDrawable(drawable);
    }

    private void startAnimationThread() {
        if (mAnimationThread != null) {
            return;
        }

        mAbort = false;
        Thread t = new Thread(new GifThread());
        t.setName("gif-animation");
        t.start();
        mAnimationThread = t;
    }

    private void abortAnimationThread() {
        LogUtil.getInstance().v(TAG, "abortAnimationThread()");
        if (mAnimationThread == null) {
            LogUtil.getInstance().d(TAG, "abortAnimationThread:thread null");
            return;
        }

        mAbort = true;
        try {
            //wake up the thread if it is sleeping
            mAnimationThread.interrupt();
            //mAnimationThread.join(500);
            //mAnimationThread.interrupt();
            mAnimationThread.join();
        } catch (InterruptedException ex) {
            LogUtil.getInstance().d(TAG, "abortAnimationThread:join interrupted");
        }
        mAnimationThread = null;

        if (null != mCurrentRunnable) {
            LogUtil.getInstance().d(TAG, "abortAnimationThread:remove " + mCurrentRunnable);
            //remove any pending Runnable in the message queue.
            mHandler.removeCallbacks(mCurrentRunnable);
            mCurrentRunnable = null;
        }
    }

    protected class GifThread implements Runnable {
        public void run() {
            gifAnimation();
        }//run
    }//GifThread

    private void gifAnimation() {
        try {
            //open gif stream
            LogUtil.getInstance().v(TAG, "gifAnimation:call openGifStream()");
            openGifStream();
            if (null == mGifStream) {
                LogUtil.getInstance().w(TAG, "gifAnimation:got null mGifStream");
                return;
            }
            if (mAbort) {
                LogUtil.getInstance().v(TAG, "gifAnimation:after open stream:thread aborted");
                return;
            }
            //mGifDecoder = new GifDecoder(mGifStream);
            //MTK Framework refactory: call factory to create the GifDecoder
            mIGifDecoder = null;
            try {
                mIGifDecoder = new GifOpenHelper();
                mIGifDecoder.read(mGifStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mAbort) {
                LogUtil.getInstance().v(TAG, "gifAnimation:after new GifDecoder:thread aborted");
                return;
            }

            if (null == mIGifDecoder) {
                LogUtil.getInstance().w(TAG, "Decode GIF resource failed");
                //when we are sure gif animation is not available, we try to
                //display a static gif image.
                showFirstGifFrame();
                return;
            }
            if (0 == mIGifDecoder.getFrameCount()) {
                LogUtil.getInstance().d(TAG, "gifAnimation:decode gif stream fails");
                //mGifDecoder.close();//no need as Movie implements finalize
                mIGifDecoder = null;
                //when we are sure gif animation is not available, we try to
                //display a static gif image.
                showFirstGifFrame();
                return;
            }

            long frameDuration = 0;
            int totalFrameCount = mIGifDecoder.getFrameCount();
            mCurrGifFrame = 0;

            while (true) {
                //load current bitmap
                Bitmap gifFrame = mIGifDecoder.getFrame(mCurrGifFrame);

                if (mAbort) {
                    LogUtil.getInstance().v(TAG, "gifAnimation:after decode:thread aborted");
                    break;
                }

                //post current bitmap to UI thread
                mCurrentRunnable = new GifFrameRunnable(gifFrame);
                mHandler.post(mCurrentRunnable);

                //load GiF frame bitmap
                frameDuration = (long) mIGifDecoder.getDelay(mCurrGifFrame);
                LogUtil.getInstance().v(TAG, "sleep for " + frameDuration + " ms for frame " +
                        mCurrGifFrame + " //this=" + self);
                //add by huangjm@frameDuration若为0，会导致循环调用很频繁，出现gif动画卡顿现象(正常gif不会为0)
                if (frameDuration == 0) {
                    frameDuration = 60;
                }
                if (mAbort) {
                    LogUtil.getInstance().v(TAG, "gifAnimation:animating:thread aborted");
                    break;
                }

                try {
                    Thread.sleep(frameDuration);
                } catch (InterruptedException ex) {
                    LogUtil.getInstance().v(TAG, "gifAnimation:sleeping interrupted");
                }

                //if thread is cancelled after wait, then break
                if (1 == totalFrameCount) {
                    LogUtil.getInstance().w(TAG, "gifAnim:single frame, cancel");
                    break;
                }
                mCurrGifFrame = (mCurrGifFrame + 1) % totalFrameCount;
            }//while
        } finally {
            //close GifDecoder when exit the thread
            if (mIGifDecoder != null) {
                //mGifDecoder.close();//no need as Movie implements finalize
                mIGifDecoder = null;
            }//if
            //close gif stream when finish gif animation
            closeGifStream();
        }
    }

    GifFrameRunnable mCurrentRunnable = null;

    class GifFrameRunnable implements Runnable {
        Bitmap mFrame;

        GifFrameRunnable(Bitmap b) {
            mFrame = b;
        }

        public void run() {
            mSetFromGif = true;
            LogUtil.getInstance().w(TAG, "GifFrameRunnable:run:call setImageBitmap(mFrame=" + mFrame + ")");
            setImageBitmap(mFrame);
            //invalidate();
            mSetFromGif = false;
        }
    }

    private void showFirstGifFrame() {
        openGifStream();
        if (null == mGifStream) return;
        Bitmap firstFrame = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        try {
            firstFrame = BitmapFactory.decodeStream(mGifStream, null, options);
        } catch (OutOfMemoryError ex) {
            LogUtil.getInstance().d(TAG, "showFirstGifFrame:OOM when decoding");
            return;
        }
        if (mAbort) {
            LogUtil.getInstance().v(TAG, "showFirstGifFrame:thread aborted");
            return;
        }
        if (null != firstFrame) {
            //post current bitmap to UI thread
            mCurrentRunnable = new GifFrameRunnable(firstFrame);
            mHandler.post(mCurrentRunnable);
        } else {
            LogUtil.getInstance().w(TAG, "showFirstGifFrame:failed to decode first frame!");
        }
    }
}
