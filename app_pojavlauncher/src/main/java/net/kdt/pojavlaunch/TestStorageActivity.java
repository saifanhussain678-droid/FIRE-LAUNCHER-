package net.kdt.pojavlaunch;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.tasks.AsyncAssetManager;

import net.kdt.pojavlaunch.R;

public class TestStorageActivity extends Activity {
    private final int REQUEST_STORAGE_REQUEST_CODE = 1;
    private AlertDialog mPermissionRequestDialog;
    private boolean mPermsRequired = false;
    private boolean mPermsDialogShown = false;

    // Start.io splash-ad gating. The splash ad is shown on this entry activity first;
    // the launcher only opens once the ad has been dismissed. mSplashWasShown records that
    // the ad covered the activity (we paused for it); the gate opens on the following resume.
    private boolean mSplashGateOpen = false;
    private boolean mSplashWasShown = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    /** Safety net: if no splash ad ever shows, don't block the launcher indefinitely. */
    private static final long SPLASH_FALLBACK_MS = 6000L;
    private Runnable mFallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPermsDialogShown = false;
        // Show the Start.io splash ad on the entry activity, before anything else happens,
        // so the ad is the first thing the user sees when they tap the launcher icon.
        StartIoAds.showSplash(this, savedInstanceState);
        if(Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29 && !isStorageAllowed(this)) {
            mPermsRequired = true;
        }
        mFallback = () -> {
            // No splash ad ever appeared (no pause happened): proceed so the launcher still opens.
            if (!mSplashGateOpen && !mSplashWasShown) proceedAfterSplash();
        };
        mHandler.postDelayed(mFallback, SPLASH_FALLBACK_MS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mSplashGateOpen) {
            if (mSplashWasShown) {
                // The splash ad was shown (we paused for it) and has now been dismissed.
                mSplashGateOpen = true;
            } else {
                // First resume right after onCreate; the splash is still being requested.
                return;
            }
        }
        if(!mPermsRequired) {
            // Storage is fine: go to the launcher.
            exit();
            return;
        }
        if(!mPermsDialogShown) requestStoragePermission();
        else showRerequestDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mSplashGateOpen) {
            // The splash ad is covering the activity -> remember it was shown.
            mSplashWasShown = true;
        }
        if(mPermissionRequestDialog != null) mPermissionRequestDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mFallback);
    }

    private void proceedAfterSplash() {
        mHandler.removeCallbacks(mFallback);
        mSplashGateOpen = true;
        if(!mPermsRequired) {
            exit();
            return;
        }
        if(!mPermsDialogShown) requestStoragePermission();
        else showRerequestDialog();
    }

    private void showRerequestDialog() {
        if(mPermissionRequestDialog != null) mPermissionRequestDialog.dismiss();
        mPermissionRequestDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.global_error)
                .setMessage(R.string.toast_permission_denied)
                .setPositiveButton(android.R.string.ok,(d,i)->requestStoragePermission())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermsRequired = false;
                exit();
            } else {
                mPermsDialogShown = true;
                showRerequestDialog();
            }
        }
    }

    public static boolean isStorageAllowed(Context context) {
        //Getting the permission status
        int result1 = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);

        //If permission is granted returning true
        return result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_REQUEST_CODE);
    }

    private void exit() {
        if(!Tools.checkStorageRoot(this)) {
            startActivity(new Intent(this, MissingStorageActivity.class));
            finish();
            return;
        }
        //Initialize constants (implicitly) and preferences after we confirm that we have storage.
        LauncherPreferences.loadPreferences(this);
        AsyncAssetManager.unpackComponents(this);
        AsyncAssetManager.unpackSingleFiles(this);

        Intent intent =  new Intent(this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }
}
