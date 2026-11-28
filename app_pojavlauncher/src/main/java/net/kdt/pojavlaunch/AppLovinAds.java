package net.kdt.pojavlaunch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

/**
 * AppLovin ads for the Cloud Launcher APK.
 *
 * <p>AppLovin (MAX) is the ad network used for this APK. The SDK is initialised once from
 * {@link PojavApplication}. On launch, {@link SplashActivity} requests a full-screen interstitial
 * ad and only routes to the launcher once the ad has been shown and dismissed (or failed, with a
 * safety-net timeout). A second interstitial is shown at natural break points such as a game
 * launch.
 *
 * <p>The App Key and interstitial ad unit ID come from the AppLovin dashboard
 * (AppLovin.com → Monetize → Your Apps → App Key, and Ad Units → Interstitial). Replace the
 * placeholder values below with the real ones before releasing.
 *
 * <p>Every call is defensive: ads are an income stream, not a launcher feature, and a failure
 * anywhere here must never break launching or navigation.
 */
public final class AppLovinAds {

    /**
     * AppLovin App Key from the dashboard (Monetize → Your Apps → App Key).
     * PLACEHOLDER — replace with your real App Key before release.
     */
    public static final String APP_KEY = "YOUR_APPLOVIN_APP_KEY";

    /**
     * AppLovin interstitial ad unit ID from the dashboard (Monetize → Your Apps → Ad Units →
     * Interstitial → Ad Unit ID). PLACEHOLDER — replace with your real ad unit ID before release.
     */
    public static final String INTERSTITIAL_AD_UNIT_ID = "YOUR_APPLOVIN_INTERSTITIAL_AD_UNIT_ID";

    private static final String TAG = "AppLovinAds";

    private static volatile boolean sInitialised;

    /** Minimal callback for the ad lifecycle so callers can gate on show/hide/fail. */
    public interface InterstitialCallback {
        void onAdDisplayed();

        void onAdHidden();

        void onAdFailed();
    }

    private AppLovinAds() {}

    /**
     * Initialise the AppLovin SDK with the Cloud Launcher's App Key. Safe to call more than once;
     * only the first call does work.
     */
    public static void init(Context context) {
        if (sInitialised) return;
        sInitialised = true;
        try {
            AppLovinSdk.getInstance(context.getApplicationContext())
                    .setMediationProvider("max");
            AppLovinSdk.initializeSdk(context.getApplicationContext(),
                    new AppLovinSdk.SdkInitializationListener() {
                        @Override
                        public void onSdkInitialized(@NonNull AppLovinSdkConfiguration configuration) {
                            Log.i(TAG, "AppLovin SDK initialised with app key");
                        }
                    });
            Log.i(TAG, "AppLovin SDK initialisation requested");
        } catch (Throwable t) {
            // Roll the flag back so a later entry point can try again.
            sInitialised = false;
            Log.w(TAG, "AppLovin SDK initialisation failed", t);
        }
    }

    /**
     * Show the interstitial ad now, with explicit display callbacks. Used by {@link SplashActivity}
     * so it knows exactly when the ad is displayed, dismissed or failed — letting it hold the
     * launcher behind the ad.
     *
     * <p>A fresh {@link MaxInterstitialAd} is created against the given activity each call
     * (the constructor requires an {@code Activity}, and the launcher's two ad moments live in
     * different activities, so a single shared instance would hold a stale context). If a loaded
     * ad is ready it is shown immediately; otherwise a fresh ad is requested and shown the moment
     * it arrives. {@code callback.onAdFailed} is always invoked if the ad cannot be shown, so the
     * caller never waits forever.
     */
    public static void showInterstitial(Activity activity, InterstitialCallback callback) {
        try {
            MaxInterstitialAd ad = new MaxInterstitialAd(INTERSTITIAL_AD_UNIT_ID, activity);
            ad.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(@NonNull MaxAd ad) {
                    Log.d(TAG, "Interstitial ad loaded");
                    if (ad.isReady()) {
                        if (ad.showAd(activity)) {
                            Log.d(TAG, "Interstitial shown from cached ad");
                        }
                    }
                }

                @Override
                public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                    Log.w(TAG, "Interstitial load failed: " + error.getMessage());
                    callback.onAdFailed();
                }

                @Override
                public void onAdDisplayed(@NonNull MaxAd ad) {
                    callback.onAdDisplayed();
                }

                @Override
                public void onAdHidden(@NonNull MaxAd ad) {
                    callback.onAdHidden();
                }

                @Override
                public void onAdClicked(@NonNull MaxAd ad) {
                    // Clicking the ad is fine; onAdHidden fires afterwards.
                }

                @Override
                public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
                    Log.w(TAG, "Interstitial display failed: " + error.getMessage());
                    // Reload so a subsequent attempt can show, and tell the caller to proceed.
                    ad.loadAd();
                    callback.onAdFailed();
                }
            });
            // Kick off a load (also covers the case where a cached ad is already ready).
            ad.loadAd();
            if (ad.isReady()) {
                ad.showAd(activity);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Interstitial show failed", t);
            callback.onAdFailed();
        }
    }

    /**
     * Show the interstitial now with no callback (used at natural break points such as a game
     * launch). If a loaded ad is ready it is shown immediately; otherwise a fresh ad is requested
     * and shown the moment it arrives.
     */
    public static void showInterstitial(Activity activity) {
        try {
            MaxInterstitialAd ad = new MaxInterstitialAd(INTERSTITIAL_AD_UNIT_ID, activity);
            ad.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(@NonNull MaxAd ad) {
                    if (ad.isReady() && ad.showAd(activity)) {
                        Log.d(TAG, "Interstitial shown from on-demand load");
                    }
                }

                @Override
                public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                    Log.w(TAG, "Interstitial load failed: " + error.getMessage());
                }

                @Override
                public void onAdDisplayed(@NonNull MaxAd ad) {}

                @Override
                public void onAdHidden(@NonNull MaxAd ad) {}

                @Override
                public void onAdClicked(@NonNull MaxAd ad) {}

                @Override
                public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
                    ad.loadAd();
                }
            });
            ad.loadAd();
            if (ad.isReady()) {
                ad.showAd(activity);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Interstitial show failed", t);
        }
    }

    /** Preload the interstitial so it is ready at a natural break point. */
    public static void preload(Activity activity) {
        try {
            MaxInterstitialAd ad = new MaxInterstitialAd(INTERSTITIAL_AD_UNIT_ID, activity);
            ad.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(@NonNull MaxAd ad) {
                    Log.d(TAG, "Interstitial preloaded and ready");
                }

                @Override
                public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                    Log.w(TAG, "Interstitial preload failed: " + error.getMessage());
                }

                @Override
                public void onAdDisplayed(@NonNull MaxAd ad) {}

                @Override
                public void onAdClicked(@NonNull MaxAd ad) {}

                @Override
                public void onAdHidden(@NonNull MaxAd ad) {}

                @Override
                public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
                    ad.loadAd();
                }
            });
            ad.loadAd();
        } catch (Throwable t) {
            Log.w(TAG, "Interstitial preload failed", t);
        }
    }
}
