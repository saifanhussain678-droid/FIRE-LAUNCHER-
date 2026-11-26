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
import com.startapp.sdk.adsbase.SplashConfig;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

/**
 * Start.io (formerly StartApp) ads for the Cloud Launcher APK.
 *
 * <p>App ID 210869947 is the publisher app registered for this APK on the Start.io portal.
 * The SDK is initialised once from {@link PojavApplication}; the splash ad is shown from
 * {@link LauncherActivity#onCreate}, and the interstitial is shown at natural break points
 * (a game launch), loading on demand so it appears even if the cached ad expired.
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
     * Initialise the Start.io SDK with the Cloud Launcher's app ID and report the user
     * consent flag right after initialisation, as the SDK requires.
     * Safe to call more than once; only the first call does work.
     */
    public static void init(Context context) {
        if (sInitialised) return;
        sInitialised = true;
        try {
            StartAppSDK.init(context.getApplicationContext(), APP_ID);
            // Report the user consent flag ("pas") immediately after init: without it the
            // SDK will not target (and may not serve) ads.
            StartAppSDK.setUserConsent(context.getApplicationContext(),
                    "pas", System.currentTimeMillis(), true);
            Log.i(TAG, "Start.io SDK initialised with app ID " + APP_ID);
        } catch (Throwable t) {
            // Roll the flag back so a later entry point can try again.
            sInitialised = false;
            Log.w(TAG, "Start.io SDK initialisation failed", t);
        }
    }

    /**
     * Show the Start.io splash ad with the default template. Call from the launcher's
     * {@code onCreate} before {@code setContentView}.
     */
    public static void showSplash(Activity activity, @Nullable Bundle savedInstanceState) {
        try {
            StartAppAd.showSplash(activity, savedInstanceState, new SplashConfig());
            Log.d(TAG, "Splash ad requested");
        } catch (Throwable t) {
            Log.w(TAG, "Splash ad show failed", t);
        }
    }

    /** The shared interstitial ad instance, created lazily after {@link #init}. */
    @Nullable
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

    /**
     * Show the interstitial now. If a loaded ad is cached it is shown immediately;
     * otherwise a fresh ad is requested and shown the moment it arrives, so the ad
     * still appears even if the cached one expired before this call.
     */
    public static void showInterstitial(Activity activity) {
        try {
            StartAppAd existing = ad(activity);
            if (existing != null && existing.showAd()) {
                Log.d(TAG, "Interstitial shown from cached ad");
                return;
            }
            // No cached, ready ad: load on demand and show when it arrives.
            StartAppAd onDemand = new StartAppAd(activity.getApplicationContext());
            sAd = onDemand;
            onDemand.loadAd(new AdEventListener() {
                @Override
                public void onReceiveAd(@NonNull Ad ad) {
                    try {
                        if (onDemand.showAd()) {
                            Log.d(TAG, "Interstitial shown from on-demand load");
                        }
                    } catch (Throwable t) {
                        Log.w(TAG, "Interstitial show after load failed", t);
                    }
                }

                @Override
                public void onFailedToReceiveAd(@Nullable Ad ad) {
                    Log.w(TAG, "Interstitial on-demand load failed: "
                            + (ad == null ? "unknown error" : ad.getErrorMessage()));
                }
            });
            Log.d(TAG, "Interstitial not ready; requested on demand");
        } catch (Throwable t) {
            Log.w(TAG, "Interstitial show failed", t);
        }
    }
}
