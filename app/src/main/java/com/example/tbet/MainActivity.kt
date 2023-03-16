package com.example.tbet

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.webkit.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main)


        val webView = findViewById<WebView>(R.id.web);
        webView.webChromeClient = object : WebChromeClient(){};
        webView.webViewClient = object : WebViewClient(){};

        val webSettings: WebSettings = webView.getSettings();
        webSettings.javaScriptEnabled = true;
        webSettings.javaScriptCanOpenWindowsAutomatically = true;
        webSettings.domStorageEnabled = true;
        webSettings.useWideViewPort = true;
        webSettings.loadWithOverviewMode = true;

        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        val splashScreen = findViewById<LinearLayout>(R.id.splash_screen_layout);

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                splashScreen.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }
        }

        webView.loadUrl("https://tmichezo.co.tz/");
    }
}