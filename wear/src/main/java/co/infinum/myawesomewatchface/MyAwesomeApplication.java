package co.infinum.myawesomewatchface;

import net.danlew.android.joda.JodaTimeAndroid;

import android.app.Application;

/**
 * @author Koc
 *         ivan.kocijan@infinum.hr
 * @since 04.05.15.
 */
public class MyAwesomeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
