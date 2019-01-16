package com.trybe.trybe.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.trybe.trybe.R;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import io.branch.referral.Branch;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

@ReportsCrashes(
        httpMethod = HttpSender.Method.POST,
        customReportContent = {ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.STACK_TRACE, ReportField.LOGCAT},
        reportType = HttpSender.Type.FORM,
        formUri = Config.Server.SERVER_ERROR_REPORTING_URL,
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
)
public class BaseApplication extends Application {

    private static BaseApplication mInstance;

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        Branch.getAutoInstance(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        ACRA.init(this);
    }
}
