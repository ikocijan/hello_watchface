package co.infinum.myawesomewatchface;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

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
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.concurrent.TimeUnit;

import co.infinum.shared_code.Constants;

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


    private class AwesomeWatchFaceEngine extends Engine implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        private int watchFaceBackground = Color.parseColor("#222222");

        private final int INFINUM_RED = Color.parseColor("#ec2125");

        private Bitmap infinum;

        private final long TICK_PERIOD = TimeUnit.SECONDS.toMillis(1);

        private Handler timer = new Handler(Looper.myLooper());

        private Paint timePaint;

        private boolean mLowBitAmbient;

        private boolean mBurnInProtection;

        private GoogleApiClient googleApiClient;

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

            googleApiClient = new GoogleApiClient.Builder(AwesomeWatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

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

            if (visible) {

                googleApiClient.connect();

            } else {

                disconnectGoogleApiClient();

            }

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

            canvas.drawColor(watchFaceBackground);

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
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(googleApiClient, onDataChangedListener);
            Wearable.DataApi.getDataItems(googleApiClient).setResultCallback(onConnectedResultCallback);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            timer.removeCallbacks(timeRunnable);
            disconnectGoogleApiClient();

        }

        private void disconnectGoogleApiClient() {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(googleApiClient, onDataChangedListener);
                googleApiClient.disconnect();
            }
        }


        @Override
        public void onConnectionSuspended(int i) {
            Log.d("koc", "onConnectionSuspended");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        private final DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {

                for (DataEvent event : dataEvents) {

                    if (event.getType() == DataEvent.TYPE_CHANGED) {

                        DataItem item = event.getDataItem();
                        getInfoFromDataItem(item);

                    }
                }

                dataEvents.release();
            }
        };

        private final ResultCallback<DataItemBuffer> onConnectedResultCallback =
                new ResultCallback<DataItemBuffer>() {

            @Override
            public void onResult(DataItemBuffer dataItems) {

                for (DataItem item : dataItems) {
                    getInfoFromDataItem(item);
                }

                dataItems.release();
            }
        };

        private void getInfoFromDataItem(DataItem item) {

            if (item.getUri().getPath().equals(Constants.MAP_REQUEST_PATH)) {

                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                if (dataMap.containsKey(Constants.KEY_BACKGROUND_COLOR)) {
                    watchFaceBackground = dataMap.getInt(Constants.KEY_BACKGROUND_COLOR);
                    invalidate();
                }

            }

        }

    }


}
