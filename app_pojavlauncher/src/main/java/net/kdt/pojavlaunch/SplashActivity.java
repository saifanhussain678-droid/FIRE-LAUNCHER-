package net.kdt.pojavlaunch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;

/**
 * Entry point shown the moment the launcher icon is tapped.
 *
 * <p>This activity immediately requests a full-screen Start.io interstitial ad and holds the
 * launcher behind it: the launcher is only started once the ad has been dismissed
 * ({@link #adHidden}) or has failed to appear ({@link #adNotDisplayed}). A safety-net timer
 * guarantees the launcher opens even if no ad is ever served, so the user is never stranded.
 *
 * <p>The SDK's deprecated {@code showSplash} API is deliberately not used here; the reliable,
 * supported path is an interstitial driven through an {@link AdDisplayListener}.
 */
public class SplashActivity extends Activity implements AdDisplayListener {

    private static final String TAG = "SplashActivity";

    /** Safety net: if no ad is displayed within this window, proceed to the launcher anyway. */
    private static final long FALLBACK_MS = 10000L;

    private boolean mAdShown;
    private boolean mProceeded;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mFallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // A minimal placeholder so the screen is never blank while the ad loads or fails.
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);

        TextView label = new TextView(this);
        label.setText(R.string.aerix_app_label);
        label.setTextColor(Color.WHITE);
        label.setGravity(Gravity.CENTER);
        root.addView(label, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mFallback = this::proceed;
        mHandler.postDelayed(mFallback, FALLBACK_MS);

        // Request the launch ad. The display listener drives the gate.
        StartIoAds.showInterstitial(this, this);
    }

    @Override
    public void adDisplayed(Ad ad) {
        mAdShown = true;
    }

    @Override
    public void adHidden(Ad ad) {
        proceed();
    }

    @Override
    public void adClicked(Ad ad) {
        // Clicking the ad is fine; the ad will hide itself and fire adHidden afterwards.
    }

    @Override
    public void adNotDisplayed(Ad ad) {
        // Ad failed to load or could not be shown: don't block the launcher.
        proceed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mFallback);
    }

    /** Transition to the storage-check / launcher flow exactly once. */
    private void proceed() {
        if (mProceeded) return;
        mProceeded = true;
        mHandler.removeCallbacks(mFallback);
        Intent intent = new Intent(this, TestStorageActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // If the ad is up, let it handle the back press; otherwise allow leaving straight to launcher.
        if (mAdShown) {
            super.onBackPressed();
        } else {
            proceed();
        }
    }
}
