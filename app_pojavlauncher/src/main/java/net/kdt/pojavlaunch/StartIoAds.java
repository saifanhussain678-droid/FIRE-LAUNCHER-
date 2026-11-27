package net.kdt.pojavlaunch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

/**
 * Start.io (formerly StartApp) ads for the Cloud Launcher APK.
 *
 * <p>App ID 210869947 is the publisher app registered for this APK on the Start.io portal.
 * The SDK is initialised once from {@link PojavApplication}. On launch, {@link SplashActivity}
 * requests a full-screen interstitial ad through {@link #showInterstitial(Activity, AdDisplayListener)}
 * and only routes to the launcher once the ad has been shown and dismissed (or failed, with a
 * safety-net timeout). A second interstitial is shown at natural break points such as a game
 * launch.
 *
 * <p>Note: the SDK's {@code showSplash} API is deprecated in 5.3.2 and is a silent no-op when the
 * publisher has not enabled a splash placement, so the launch ad is driven as an interstitial
 * with explicit display listeners instead — that is the supported, reliable path.
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
     * Initialise the Start.io SDK with the Cloud Launcher's app ID and enable consent
     * right after initialisation, as the SDK requires consent to serve ads.
     * Safe to call more than once; only the first call does work.
     */
    public static void init(Context context) {
        if (sInitialised) return;
        sInitialised = true;
        try {
            StartAppSDK.initParams(context.getApplicationContext(), APP_ID).init();
            // Enable the Start.io consent flag; without it the SDK will not target
            // (and may not serve) ads.
            StartAppSDK.enableConsent(context.getApplicationContext(), true);
            // Test ads for debug/nightly builds. Without this the SDK requests real ads;
            // if the app is not yet approved for production on the Start.io portal (or has
            // no inventory), the request fails and no ad ever shows. Test ads let the
            // integration be verified end-to-end. Remove/disable for a production release.
            if (BuildConfig.DEBUG) {
                StartAppSDK.setTestAdsEnabled(true);
                Log.i(TAG, "Start.io test ads enabled (debug build)");
            }
            Log.i(TAG, "Start.io SDK initialised with app ID " + APP_ID);
        } catch (Throwable t) {
            // Roll the flag back so a later entry point can try again.
            sInitialised = false;
            Log.w(TAG, "Start.io SDK initialisation failed", t);
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

    /**
     * Show the interstitial ad now, with explicit display callbacks. Used by {@link SplashActivity}
     * so it knows exactly when the ad is displayed, dismissed or failed — letting it hold the
     * launcher behind the ad without any deprecated splash API.
     *
     * <p>If a loaded ad is cached it is shown immediately; otherwise a fresh ad is requested and
     * shown the moment it arrives. {@code listener.adNotDisplayed} is always invoked if the ad
     * cannot be shown, so the caller never waits forever.
     */
    public static void showInterstitial(Activity activity, AdDisplayListener listener) {
        try {
            StartAppAd existing = ad(activity);
            if (existing != null && existing.showAd(listener)) {
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
                        if (onDemand.showAd(listener)) {
                            Log.d(TAG, "Interstitial shown from on-demand load");
                        }
                    } catch (Throwable t) {
                        Log.w(TAG, "Interstitial show after load failed", t);
                        listener.adNotDisplayed(ad);
                    }
                }

                @Override
                public void onFailedToReceiveAd(@Nullable Ad ad) {
                    Log.w(TAG, "Interstitial on-demand load failed: "
                            + (ad == null ? "unknown error" : ad.getErrorMessage()));
                    listener.adNotDisplayed(ad);
                }
            });
            Log.d(TAG, "Interstitial not ready; requested on demand");
        } catch (Throwable t) {
            Log.w(TAG, "Interstitial show failed", t);
            listener.adNotDisplayed(null);
        }
    }

    /**
     * Show the interstitial now with no display listener (used at natural break points such as a
     * game launch). If a loaded ad is cached it is shown immediately; otherwise a fresh ad is
     * requested and shown the moment it arrives.
     */
    public static void showInterstitial(Activity activity) {
        try {
            StartAppAd existing = ad(activity);
            if (existing != null && existing.showAd()) {
                Log.d(TAG, "Interstitial shown from cached ad");
                return;
            }
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

    /** Preload the interstitial so it is ready at a natural break point. */
    public static void preload(Activity activity) {
        try {
            ad(activity);
        } catch (Throwable t) {
            Log.w(TAG, "Interstitial preload failed", t);
        }
    }
}
