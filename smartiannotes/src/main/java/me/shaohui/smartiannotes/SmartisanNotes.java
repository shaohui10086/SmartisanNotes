package me.shaohui.smartiannotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.Pair;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by shaohui on 2016/10/22.
 */

public class SmartisanNotes {

    private static final int DEFAULT_BG_COLOR = Color.parseColor("#FFFCF6");
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#695C4E");
    private static final int DEFAULT_AUTHOR_COLOR = Color.parseColor("#D5D0C7");
    private static final int DEFAULT_FRAME_COLOR = Color.parseColor("#EBE6D9");

    private static final String CACHE_FILE_NAME = "SmartisanNotes";

    private static final int FRAME_BORDER = 30;
    private static final int FRAME_INTERVAL = 6;
    private static final int IMAGE_BORDER = 8;

    private int mHeight;
    private Context mContext;
    private String mAuthor = "via Smartisan Notes";

    private TextPaint mTextPaint;
    private Paint mFramePaint;
    private Paint mAuthorPaint;
    private Paint mImageBgPaint;

    private Bitmap mBitmap;
    private Canvas mCanvas;

    private SmartisanNotes(Context context) {
        mContext = context;
        init();
    }

    public static SmartisanNotes with(Context context) {
        return new SmartisanNotes(context);
    }

    public SmartisanNotes author(String author) {
        mAuthor = author;
        return this;
    }

    private void init() {
        mTextPaint = new TextPaint();
        mTextPaint.setColor(DEFAULT_TEXT_COLOR);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(36);
        mTextPaint.setStyle(Paint.Style.FILL);

        mFramePaint = new Paint();
        mFramePaint.setColor(DEFAULT_FRAME_COLOR);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(2);

        mAuthorPaint = new Paint();
        mAuthorPaint.setTextSize(24);
        mAuthorPaint.setTextAlign(Paint.Align.CENTER);
        mAuthorPaint.setAntiAlias(true);
        mAuthorPaint.setColor(DEFAULT_AUTHOR_COLOR);

        mImageBgPaint = new Paint();
        mImageBgPaint.setStyle(Paint.Style.FILL);
        mImageBgPaint.setColor(Color.WHITE);
        mImageBgPaint.setShadowLayer(4, 0, 0, Color.parseColor("#1A000000"));
    }

    private void initCanvas(String text, Drawable drawable) {

        int height = getTextBegin() * 2 + 100;

        StaticLayout textLayout =
                new StaticLayout(text, mTextPaint, getTextWidth(), Layout.Alignment.ALIGN_NORMAL,
                        1.8f, 0f, true);
        height += textLayout.getHeight();

        if (drawable != null) {
            height += handleImageHeight(drawable).second + 30;
        }
        setHeight(height);

        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    public SmartisanNotes draw(String text) {
        return draw(text, null);
    }

    public SmartisanNotes draw(String text, Drawable drawable) {

        initCanvas(text, drawable);

        mCanvas.drawColor(DEFAULT_BG_COLOR);

        // draw frame

        Path framePath = new Path();
        // 外框
        framePath.addRect(FRAME_BORDER, FRAME_BORDER, getWidth() - FRAME_BORDER,
                getFrameHeight() - FRAME_BORDER, Path.Direction.CW);
        // 边角
        framePath.addRect(FRAME_BORDER - FRAME_INTERVAL, FRAME_BORDER - FRAME_INTERVAL,
                FRAME_BORDER, FRAME_BORDER, Path.Direction.CW);
        framePath.addRect(getWidth() - FRAME_BORDER, FRAME_BORDER - FRAME_INTERVAL,
                getWidth() - FRAME_BORDER + FRAME_INTERVAL, FRAME_BORDER, Path.Direction.CW);
        framePath.addRect(getWidth() - FRAME_BORDER, getFrameHeight() - FRAME_BORDER,
                getWidth() - FRAME_BORDER + FRAME_INTERVAL,
                getFrameHeight() - FRAME_BORDER + FRAME_INTERVAL, Path.Direction.CW);
        framePath.addRect(FRAME_BORDER - FRAME_INTERVAL, getFrameHeight() - FRAME_BORDER,
                FRAME_BORDER, getFrameHeight() - FRAME_BORDER + FRAME_INTERVAL, Path.Direction.CW);
        // 内框
        framePath.addRect(FRAME_BORDER + FRAME_INTERVAL, FRAME_BORDER + FRAME_INTERVAL,
                getWidth() - FRAME_BORDER - FRAME_INTERVAL,
                getFrameHeight() - FRAME_BORDER - FRAME_INTERVAL, Path.Direction.CW);

        mCanvas.drawPath(framePath, mFramePaint);

        // draw Text
        StaticLayout textLayout =
                new StaticLayout(text, mTextPaint, getTextWidth(), Layout.Alignment.ALIGN_NORMAL,
                        1.8f, 0f, true);
        mCanvas.save();
        mCanvas.translate(getTextBegin(), getTextBegin());
        textLayout.draw(mCanvas);
        mCanvas.restore();

        // draw image
        if (drawable != null) {
            int imageStartY = getTextBegin() + textLayout.getHeight() + 16;
            mCanvas.save();
            Pair<Boolean, Float> pair = handleImageHeight(drawable);
            if (pair.first) {
                mCanvas.translate((getWidth() - getImageWidth()) / 2, imageStartY);
                mCanvas.drawRect(0, 0, getImageWidth() + IMAGE_BORDER, pair.second + IMAGE_BORDER,
                        mImageBgPaint);
                drawable.setBounds(IMAGE_BORDER, IMAGE_BORDER, getImageWidth(),
                        pair.second.intValue());
                drawable.draw(mCanvas);
            } else {
                mCanvas.translate((getWidth() - drawable.getIntrinsicWidth()) / 2, imageStartY);
                mCanvas.drawRect(0, 0, drawable.getIntrinsicWidth() + IMAGE_BORDER,
                        drawable.getIntrinsicHeight() + IMAGE_BORDER, mImageBgPaint);
                drawable.setBounds(IMAGE_BORDER, IMAGE_BORDER, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
                drawable.draw(mCanvas);
            }
            mCanvas.restore();
        }

        // draw author text
        mCanvas.drawText(mAuthor, getWidth() / 2, getHeight() - 50, mAuthorPaint);

        return this;
    }

    private Pair<Boolean, Float> handleImageHeight(Drawable drawable) {
        if (drawable.getIntrinsicWidth() > getImageWidth()) {
            float scale = drawable.getIntrinsicWidth() / (float) getImageWidth();
            float height = drawable.getIntrinsicHeight() / scale;
            return Pair.create(true, height);
        } else {
            return Pair.create(false, drawable.getIntrinsicHeight() / 1f);
        }
    }

    public Bitmap asBitmap() {
        if (mBitmap == null) {
            throw new IllegalArgumentException("You must call draw method before call this method");
        }
        return mBitmap;
    }

    public File saveCacheFile() {
        File cacheFile = mContext.getCacheDir();
        File file = new File(cacheFile, CACHE_FILE_NAME);
        if (file.exists() || file.mkdirs()) {
            File result = new File(file, System.currentTimeMillis() + ".jpg");
            return saveFile(result);
        }
        return null;
    }

    public File savePublicFile() {
        File resultDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                CACHE_FILE_NAME);
        resultDir.mkdirs();
        File result = new File(resultDir, System.currentTimeMillis() + ".jpg");

        saveFile(result);

        notifyGallery(result);

        return result;
    }

    private void notifyGallery(File file) {
        MediaScannerConnection.scanFile(mContext, new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    public File saveFile(File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getWidth() {
        return 990;
    }

    private void setHeight(int height) {
        mHeight = height;
    }

    private int getHeight() {
        return mHeight;
    }

    private int getFrameHeight() {
        return getHeight() - 100;
    }

    private int getImageWidth() {
        return getWidth() - getTextBegin() * 2 - IMAGE_BORDER * 2;
    }

    private int getTextBegin() {
        return (int) (FRAME_BORDER + FRAME_INTERVAL + 60 + mFramePaint.getStrokeWidth() * 2);
    }

    private int getTextWidth() {
        return getWidth() - getTextBegin() * 2;
    }
}
