package com.example.tbet

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main)


        val visor = findViewById<WebView>(R.id.web);
        visor.webChromeClient = object : WebChromeClient(){};
        visor.webViewClient = object : WebViewClient(){};

        val webSettings: WebSettings = visor.getSettings();
        webSettings.javaScriptEnabled = true;
        webSettings.javaScriptCanOpenWindowsAutomatically = true;
        webSettings.domStorageEnabled = true;
        webSettings.useWideViewPort = true;
        webSettings.loadWithOverviewMode = true;

        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            visor.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        visor.loadUrl("https://tmichezo.co.tz/");
    }
}