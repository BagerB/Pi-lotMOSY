package dev.pi_lot.frisch.pi_lotmjpeg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
    private MjpegViewThread thread;
    private MjpegInputStream mjpegInputStream = null;
    private String sourceUrl;
    private boolean mediaRun = false;
    private boolean surfaceDone = false;
    private boolean doubleImageMode = false;
    private int dispWidth;
    private int dispHeight;
    private Bitmap bitmap;
    private Rect mainRect;
    private Rect leftRect;
    private Rect rightRect;
    private int resolutionWidth = 640;
    private int resolutionHeight = 480;
    private int resolutionWidthOffset = 160;
    private int resolutionHeightOffset = -(resolutionHeight / 2);
    private Canvas canvas;
    private Paint paint;
    boolean initrun = true;

    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;

        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
            mSurfaceHolder = surfaceHolder;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;

                checkdispsize();

                mainRect = mainRect();
                leftRect = leftRect();
                rightRect = rightRect();
            }
        }

        public void run() {
            mainRect = mainRect();
            leftRect = leftRect();
            rightRect = rightRect();
            mjpegInputStream = MjpegInputStream.read(sourceUrl);
            canvas = null;
            paint = new Paint();
            while (mjpegInputStream != null && mediaRun) {
                if (surfaceDone) {
                    try {
                        canvas = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {
                            try {
                                bitmap = mjpegInputStream.readMjpegFrame();
                                if (doubleImageMode) {
                                    canvas.drawColor(Color.BLACK);
                                    canvas.drawBitmap(bitmap, null, leftRect, paint);
                                    canvas.drawBitmap(bitmap, null, rightRect, paint);
                                } else {
                                    canvas.drawBitmap(bitmap, null, mainRect, paint);
                                }
                            } catch (IOException e) {
                                Log.e("Error", e.getMessage(), e);
                            }
                        }
                    } finally {
                        if (canvas != null)
                            mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    private void init(Context context) {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        dispWidth = getWidth();
        dispHeight = getHeight();
    }

    public void startPlayback() {
        mediaRun = true;
        thread.start();
    }

    public void stopPlayback() {
        mediaRun = false;
        thread.interrupt();
    }

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        thread.setSurfaceSize(w, h);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        stopPlayback();
    }

    public MjpegView(Context context) {
        super(context);
        init(context);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
    }

    public void setSource(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        startPlayback();
    }

    public void setResolution(int width, int height) {
        this.resolutionWidth = width;
        this.resolutionHeight = height;
        resolutionHeightOffset = -(resolutionHeight / 2);
    }

    public void addWidthOffset(int add) {
        if (resolutionWidthOffset < dispWidth / 2 - resolutionWidth && doubleImageMode) {
            resolutionWidthOffset = resolutionWidthOffset + add;
            leftRect = leftRect();
            rightRect = rightRect();
        }
    }

    public void subWidthOffset(int sub) {
        if (resolutionWidthOffset > 0 && doubleImageMode) {
            resolutionWidthOffset = resolutionWidthOffset - sub;
            leftRect = leftRect();
            rightRect = rightRect();
        }
    }

    public void toggleDoubleImageMode() {
        doubleImageMode = !doubleImageMode;
    }

    private Rect mainRect() {
        return new Rect(0, 0, dispWidth, dispHeight);
    }

    private Rect leftRect() {

        int widthCenter = dispWidth / 2;
        int heightCenter = dispHeight / 2;
        int left = widthCenter - resolutionWidth - resolutionWidthOffset;
        int right = widthCenter - resolutionWidthOffset;
        int top = heightCenter + resolutionHeightOffset;
        int bottom = heightCenter + resolutionHeightOffset + resolutionHeight;
        return new Rect(left, top, right, bottom);

    }

    private Rect rightRect() {
        int widthCenter = dispWidth / 2;
        int heightCenter = dispHeight / 2;
        int left = widthCenter + resolutionWidthOffset;
        int right = widthCenter + resolutionWidth + resolutionWidthOffset;
        int top = heightCenter + resolutionHeightOffset;
        int bottom = heightCenter + resolutionHeightOffset + resolutionHeight;
        return new Rect(left, top, right, bottom);
    }

    private void checkdispsize() {
        if (initrun == true) {
            if (dispWidth < 1620 || dispHeight < 780) {
                resolutionWidth = resolutionWidth / 2;
                resolutionHeight = resolutionHeight / 2;
                resolutionWidthOffset = resolutionWidthOffset / 2;
                resolutionHeightOffset = -(resolutionHeight / 2);
            }
            initrun = false;
        }
    }
}