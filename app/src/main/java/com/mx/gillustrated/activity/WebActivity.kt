package com.mx.gillustrated.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R

class WebActivity: BaseActivity() {

    @BindView(R.id.web_view)
    lateinit var webView: WebView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        ButterKnife.bind(this)
        initWebView()
        loadWebPage()
    }

    private fun initWebView(){
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.javaScriptCanOpenWindowsAutomatically=true
        webSettings.setSupportZoom(true)
        webSettings.defaultTextEncodingName = "utf-8"
        webView.addJavascriptInterface(JavaScriptInterface(this), "AndroidFunction")
        webView.webChromeClient = MyWebChromeClient() //这里不设置， alert弹不出来
        webView.webViewClient = MyWebViewClient()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun executeJavascript(view: WebView, script: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(script, null)
        } else {
            view.loadUrl(script)
        }
    }

    private fun loadWebPage(){
        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class JavaScriptInterface internal constructor(private var mContext: Context) {

        @JavascriptInterface
        fun goBack() {
            val intent = Intent(mContext, MainActivity::class.java)
            mContext.startActivity(intent)
        }
    }

    inner class MyWebViewClient : WebViewClient(){


        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            Toast.makeText(view!!.context, "LOADING", Toast.LENGTH_LONG).show()
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Toast.makeText(view!!.context, "start", Toast.LENGTH_SHORT).show()
        }
    }

    inner class MyWebChromeClient : WebChromeClient() {

        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            AlertDialog.Builder(view!!.context)
                    .setTitle("Title")
                    .setMessage(message)
                    .setPositiveButton("OK") { _: DialogInterface, _: Int -> result?.confirm() }
                    .setOnDismissListener { result?.confirm() }
                    .create()
                    .show()
            return true
        }
    }

}