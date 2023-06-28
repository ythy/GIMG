package com.mx.gillustrated.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.mx.gillustrated.common.MConfig
import java.io.*
import android.webkit.WebView
import com.mx.gillustrated.databinding.ActivityWebBinding


class WebActivity: BaseActivity() {

    lateinit var binding: ActivityWebBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initWebView()
        loadWebPage()
    }

    private fun initWebView(){
        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.javaScriptCanOpenWindowsAutomatically=true
        webSettings.setSupportZoom(true)

        webSettings.defaultTextEncodingName = "utf-8"
        binding.webView.addJavascriptInterface(JavaScriptInterface(this), "AndroidFunction")
        binding.webView.webChromeClient = MyWebChromeClient() //这里不设置， alert弹不出来
//        webView.systemUiVisibility = (WebView.SYSTEM_UI_FLAG_IMMERSIVE or WebView.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or WebView.SYSTEM_UI_FLAG_FULLSCREEN)
        binding.webView.webViewClient = MyWebViewClient()
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
        binding.webView.loadUrl("file:///android_asset/index.html")
    }

    inner class JavaScriptInterface internal constructor(private var mContext: Context) {

        @JavascriptInterface
        fun goBack() {
            val intent = Intent(mContext, MainActivity::class.java)
            mContext.startActivity(intent)
        }

        @JavascriptInterface
        fun getError():String {
            if (Environment.MEDIA_MOUNTED == Environment
                            .getExternalStorageState()) {
                val fileDir = File(Environment.getExternalStorageDirectory(),
                        MConfig.SD_ERROR_PATH)
                if (!fileDir.exists()) {
                    fileDir.mkdirs()
                }
                val file = File(fileDir.path, "error.txt")
                if (!file.exists()) {
                    try {
                        file.createNewFile()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                val stringArr = arrayListOf<String>()
                try {
                    val bf = BufferedReader(InputStreamReader(FileInputStream(file)))
                    while (true) {
                        val line = bf.readLine() ?: break
                        stringArr.add(0, line)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return stringArr.joinToString(" <br />")
            }
            return "权限异常"
        }

    }

    inner class MyWebViewClient : WebViewClient(){


        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            Toast.makeText(view!!.context, "LOADING", Toast.LENGTH_LONG).show()
            view.loadUrl(url ?: "")
            return true
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