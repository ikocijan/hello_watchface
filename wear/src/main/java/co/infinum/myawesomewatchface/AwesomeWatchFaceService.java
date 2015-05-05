package co.infinum.myawesomewatchface;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.SurfaceHolder;

import java.util.concurrent.TimeUnit;

/**
 * @author Koc
 *         ivan.kocijan@infinum.hr
 * @since 03.05.15.
 */
public class AwesomeWatchFaceService extends CanvasWatchFaceService {


    @Override
    public Engine onCreateEngine() {
        return new AwesomeWatchFaceEngine();
    }

    private class AwesomeWatchFaceEngine extends Engine {

        private final int WATCH_FACE_BACKGROUND = Color.parseColor("#222222");

        private final int INFINUM_RED = Color.parseColor("#ec2125");

        private Bitmap infinum;

        private final long TICK_PERIOD = TimeUnit.SECONDS.toMillis(1);

        private Handler timer = new Handler(Looper.myLooper());

        private Paint timePaint;

        private boolean mLowBitAmbient;

        private boolean mBurnInProtection;

        private Runnable timeRunnable = new Runnable() {
            @Override
            public void run() {

                if (isVisible() && !isInAmbientMode()) {

                    invalidate();
                    long timeMs = System.currentTimeMillis();
                    long delayMs = TICK_PERIOD - (timeMs % TICK_PERIOD);

                    timer.postDelayed(this, delayMs);
                }
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            timePaint = new Paint();
            timePaint.setColor(INFINUM_RED);
            timePaint.setTextSize(50);
            timePaint.setAntiAlias(true);

            infinum = BitmapFactory.decodeResource(getResources(),
                    R.drawable.infinum_logo_no_text
            );

            startTimer();
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            startTimer();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (inAmbientMode) {
                timePaint.setColor(Color.GRAY);
                timePaint.setAntiAlias(false);
            } else {
                timePaint.setColor(INFINUM_RED);
                timePaint.setAntiAlias(true);
            }

            invalidate();
            startTimer();

        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION,
                    false);

        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            // Called every minute ONLY when the watch is in ambient mode
        }


        private void startTimer() {
            timer.removeCallbacks(timeRunnable);
            if (isVisible() && !isInAmbientMode()) {
                timer.post(timeRunnable);
            }
        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            DateTimeZone defaultZone = DateTimeZone.getDefault();
            DateTime time = new DateTime(defaultZone);

            canvas.drawColor(WATCH_FACE_BACKGROUND);

            String hourOfDay = Integer.toString(time.getHourOfDay());
            String minute = Integer.toString(time.getMinuteOfHour());
            String second = Integer.toString(time.getSecondOfMinute());

            String timeString = hourOfDay + ":" + minute + ":" + second;

            canvas.drawText(timeString,
                    getXPosition(timeString, timePaint, bounds),
                    getYPosition(timeString, timePaint, bounds),
                    timePaint);

            canvas.drawBitmap(infinum,
                    getInfinumXPosition(infinum, bounds),
                    getInfinumYPosition(infinum, bounds),
                    null);
        }

        private float getXPosition(String text, Paint hoursPaint, Rect bounds) {
            Rect textBounds = new Rect();
            hoursPaint.getTextBounds(text, 0, text.length(), textBounds);
            return bounds.exactCenterX() - (textBounds.width() / 2f);
        }

        private float getYPosition(String text, Paint hoursPaint, Rect bounds) {
            Rect textBounds = new Rect();
            hoursPaint.getTextBounds(text, 0, text.length(), textBounds);
            int textHeight = textBounds.height();
            return bounds.exactCenterY() + (textHeight / 2f);
        }

        private float getInfinumXPosition(Bitmap bitmap, Rect bounds) {
            return bounds.exactCenterX() - (bitmap.getWidth() / 2);
        }

        private float getInfinumYPosition(Bitmap bitmap, Rect bounds) {
            return (bounds.exactCenterY() / 2) - (bitmap.getHeight() / 2);
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            timer.removeCallbacks(timeRunnable);

        }

    }


}
