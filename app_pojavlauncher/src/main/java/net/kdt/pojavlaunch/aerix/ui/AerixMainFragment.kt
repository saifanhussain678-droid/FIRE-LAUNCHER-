package net.kdt.pojavlaunch.aerix.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.aerix.bridge.*
import net.kdt.pojavlaunch.cloud.CloudBridge
import net.kdt.pojavlaunch.cloud.CloudInGameHud
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.utils.CropperUtils

class AerixMainFragment : Fragment() {
    companion object {
        const val TAG = "AerixMainFragment"
    }

    private val cropperLauncher = CropperUtils.registerCropper(this, AerixIconCropper)

    private val jarPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && isAdded) {
            runCatching { PojavNavigator.runJar(requireContext(), uri) }
        }
    }

    private val skinPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            cloudBridge?.handlePickedSkinUri(uri)
        }
    }

    private var webView: WebView? = null
    private var cloudBridge: CloudBridge? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AerixPreferences.init(context)
        val retentionDays = AerixPreferences.logRetentionDays
        Thread({ runCatching { PojavFileBridge.pruneOldLogs(retentionDays) } }, "CloudLogPrune").start()
        PojavTaskBridge.attach()
        PojavInstanceBridge.attach(context)
        PojavAccountBridge.attach(context)
        PojavCreateInstanceBridge.attach()
        AerixNav.init()
        AerixIconCropper.launcher = cropperLauncher
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val activity = requireActivity()
        CloudInGameHud.applyLauncherCanvas(activity)

        // Hide legacy bottom Pojav progress bar; our relocated Top CLOUD DOWNLOADER bar handles tasks!
        runCatching {
            val f = LauncherActivity::class.java.getDeclaredField("mProgressLayout")
            f.isAccessible = true
            (f.get(activity) as? View)?.visibility = View.GONE
        }

        val containerFrame = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFFF4FAFF.toInt())
        }

        val wv = WebView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                mediaPlaybackRequiresUserGesture = false
                cacheMode = WebSettings.LOAD_DEFAULT
                textZoom = 100
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.evaluateJavascript("window.CloudUI && CloudUI.syncFromNative()", null)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    if (!isAdded) { result?.cancel(); return true }
                    AlertDialog.Builder(requireContext())
                        .setTitle("Cloud Launcher")
                        .setMessage(message ?: "")
                        .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                        .setOnCancelListener { result?.cancel() }
                        .show()
                    return true
                }

                override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    if (!isAdded) { result?.cancel(); return true }
                    AlertDialog.Builder(requireContext())
                        .setTitle("Cloud Launcher")
                        .setMessage(message ?: "")
                        .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                        .setOnCancelListener { result?.cancel() }
                        .show()
                    return true
                }

                override fun onJsPrompt(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    defaultValue: String?,
                    result: JsPromptResult?
                ): Boolean {
                    if (!isAdded) { result?.cancel(); return true }
                    val input = EditText(requireContext()).apply {
                        setText(defaultValue ?: "")
                        setSelection(text.length)
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle(message ?: "Cloud Launcher")
                        .setView(input)
                        .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm(input.text?.toString() ?: "") }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                        .setOnCancelListener { result?.cancel() }
                        .show()
                    return true
                }
            }
        }

        val bridge = CloudBridge(
            activity = activity,
            webViewProvider = { webView },
            onPickJar = { runCatching { jarPickerLauncher.launch("*/*") } },
            onPickSkin = { runCatching { skinPickerLauncher.launch("image/*") } }
        )
        cloudBridge = bridge
        webView = wv
        wv.addJavascriptInterface(bridge, "CloudBridge")
        wv.loadUrl("file:///android_asset/cloud_ui/index.html")
        containerFrame.addView(wv)

        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentWv = webView
                if (currentWv == null) {
                    isEnabled = false
                    activity.onBackPressedDispatcher.onBackPressed()
                    return
                }
                currentWv.evaluateJavascript("(window.CloudUI && CloudUI.onBackPressed()) ? '1' : '0'") { res ->
                    if (res != "\"1\"" && res != "1") {
                        isEnabled = false
                        activity.onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        })

        return containerFrame
    }

    override fun onResume() {
        super.onResume()
        PojavInstanceBridge.reload()
        PojavAccountBridge.reload()
        PojavSettingsBridge.touch()
        PojavInstanceEditorBridge.consumeFileSelectorResult()
        ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true)
        webView?.evaluateJavascript("window.CloudUI && CloudUI.syncFromNative()", null)
    }

    override fun onDestroyView() {
        webView?.apply {
            removeJavascriptInterface("CloudBridge")
            destroy()
        }
        webView = null
        cloudBridge = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (AerixIconCropper.launcher === cropperLauncher) AerixIconCropper.launcher = null
        super.onDestroy()
    }
}
