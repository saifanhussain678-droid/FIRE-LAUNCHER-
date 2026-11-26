package net.kdt.pojavlaunch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

/**
 * Start.io (formerly StartApp) ads for the Cloud Launcher APK.
 *
 * <p>App ID 210869947 is the publisher app registered for this APK on the Start.io portal.
 * The SDK is initialised once from {@link PojavApplication}; splash ads are shown automatically
 * by the SDK on app start, the interstitial is preloaded while the launcher sits idle and then
 * shown at natural break points (before a game launch), and the exit ad is shown when the user
 * leaves through the back button.
 *
 * <p>Every call is defensive: ads are an income stream, not a launcher feature, and a failure
 * anywhere here must never break launching or navigation.
 */
public final class StartIoAds {

    /** Start.io app ID for the Cloud Launcher APK (Start.io publisher portal → My Apps). */
    public static final String APP_ID = "210869947";

    private static final String TAG = "StartIoAds";

    private static volatile boolean sInitialised;
    private static volatile StartAppAd sAd;

    private StartIoAds() {}

    /**
     * Initialise the Start.io SDK with the Cloud Launcher's app ID.
     * Safe to call more than once; only the first call does work.
     */
    public static void init(Context context) {
        if (sInitialised) return;
        sInitialised = true;
        try {
            StartAppSDK.initParams(context.getApplicationContext(), APP_ID).init();
            Log.i(TAG, "Start.io SDK initialised with app ID " + APP_ID);
        } catch (Throwable t) {
            // Roll the flag back so a later entry point can try again.
            sInitialised = false;
            Log.w(TAG, "Start.io SDK initialisation failed", t);
        }
    }

    /** The shared interstitial ad instance, created lazily after {@link #init}. */
    private static StartAppAd ad(Context context) {
        StartAppAd instance = sAd;
        if (instance == null) {
            synchronized (StartIoAds.class) {
                instance = sAd;
                if (instance == null) {
                    instance = new StartAppAd(context.getApplicationContext());
                    sAd = instance;
                    instance.loadAd(new AdEventListener() {
                        @Override
                        public void onReceiveAd(@NonNull Ad ad) {
                            Log.d(TAG, "Interstitial ad loaded");
                        }

                        @Override
                        public void onFailedToReceiveAd(@Nullable Ad ad) {
                            Log.w(TAG, "Interstitial ad load failed: "
                                    + (ad == null ? "unknown error" : ad.getErrorMessage()));
                        }
                    });
                }
            }
        }
        return instance;
    }

    /** Preload the interstitial so it is ready at a natural break point. */
    public static void preload(Activity activity) {
        try {
            ad(activity);
        } catch (Throwable t) {
            Log.w(TAG, "Interstitial preload failed", t);
        }
    }

    /** Show the interstitial if it has finished loading; a no-op when it is not ready yet. */
    public static void showInterstitial(Activity activity) {
        try {
            if (ad(activity).showAd()) {
                Log.d(TAG, "Interstitial ad shown");
            } else {
                Log.d(TAG, "Interstitial ad not ready yet");
            }
        } catch (Throwable t) {
            Log.w(TAG, "Interstitial ad show failed", t);
        }
    }

    /** Show the exit ad, if one is available, before the activity finishes. */
    public static void onBackPressed(Activity activity) {
        try {
            ad(activity).onBackPressed();
        } catch (Throwable t) {
            Log.w(TAG, "Exit ad failed", t);
        }
    }

    /**
     * Forward the activity lifecycle to the ad instance so it keeps its state across pauses,
     * configuration changes and restores.
     */
    public static void onResume(Activity activity) {
        try { ad(activity).onResume(); } catch (Throwable ignored) {}
    }

    public static void onPause(Activity activity) {
        try { ad(activity).onPause(); } catch (Throwable ignored) {}
    }

    public static void onSaveInstanceState(Activity activity, Bundle outState) {
        try { ad(activity).onSaveInstanceState(outState); } catch (Throwable ignored) {}
    }

    public static void onRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        try { ad(activity).onRestoreInstanceState(savedInstanceState); } catch (Throwable ignored) {}
    }
}
