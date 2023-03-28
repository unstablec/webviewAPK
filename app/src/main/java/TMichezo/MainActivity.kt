package TMichezo

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.*
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tbet.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main)


        val webView = findViewById<WebView>(R.id.web);
        getStatusVersionAppHttp(webView);
        webView.webChromeClient = object : WebChromeClient(){};
        webView.webViewClient = object : WebViewClient(){};

        val webSettings: WebSettings = webView.getSettings();
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;

        if(connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected){
            webSettings.cacheMode = WebSettings.LOAD_DEFAULT;

        }else{
            webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK;
        }

        webSettings.setAppCacheEnabled(true)
        webSettings.setAppCachePath(applicationContext.cacheDir.absolutePath)
        webSettings.javaScriptEnabled = true;
        webSettings.javaScriptCanOpenWindowsAutomatically = true;
        webSettings.domStorageEnabled = true;
        webSettings.allowFileAccess = true
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


    fun getStatusVersionAppHttp(view: WebView) {
        val url = "https://jsonplaceholder.typicode.com/todos/1";
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el error aquí
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val json = JSONObject(response.body!!.string());
                        println(json);

                        val builder = AlertDialog.Builder(view.context)
                        builder.setTitle("Tu aplicacion esta desactualizada")
                        builder.setMessage("En el bosque de la china la chinita se peldio")
                        builder.setPositiveButton("Actualizar") { dialog, which ->
                            shouldOverrideUrlLoading(view);
                        }
                        builder.setNegativeButton("Continuar sin actualizar") { dialog, which ->
                            // Acción al hacer clic en el botón "Cancelar"
                        }
                        val dialog = builder.create()
                        dialog.show();

                    }
                }
            }
        })
    }

    fun shouldOverrideUrlLoading(view: WebView): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        val url = "https://tbet.co.tz/downloads/tbet.apk";
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Descarga de APK")
        request.setDescription("Descargando...")
        request.setMimeType("application/vnd.android.package-archive")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "nombre_del_archivo.apk")
        val downloadManager = view.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        return true
        
    }
}