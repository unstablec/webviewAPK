package TMichezo

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tbet.R
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var proxyManager: ProxyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
        proxyManager = ProxyManager(this)
        
        // Initialize default proxy configuration
        proxyManager.initializeDefaultProxy()
        
        var versionAppInstall = VersionApp(1,"0.5.0",UpgradeType.Minor,"First release","2023-03-27T20:49:28.680665Z","2023-03-27T20:49:28.680665Z",);

        setContentView(R.layout.activity_main)


        val webView = findViewById<WebView>(R.id.web);

        val thisIsAppOld = sharedPreferences.getBoolean("thisIsOldApp",false);
        if(!thisIsAppOld){

            getStatusVersionAppHttp(webView, versionAppInstall);
        }else{
            showModal(webView,"La nueva version de la app ha sido descargada","Puedes encontrarla en tu carpeta de descargas en un administrador de archivos en el menu de tu telefono, debes desinstalar manualmente esta version", false, false,true)
        }

        webView.webChromeClient = object : WebChromeClient(){};
        
        // Configure proxy for WebView
        proxyManager.configureWebViewProxy(webView)
        
        // Use custom WebViewClient with proxy support
        webView.webViewClient = ProxyWebViewClient(proxyManager)

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

        // Test proxy connection before loading the main URL
        proxyManager.testProxyConnection { success, message ->
            runOnUiThread {
                if (success) {
                    Log.d("MainActivity", "Proxy test successful: $message")
                    // Load the main URL through proxy
                    webView.loadUrl("https://senkuro.com/")
                } else {
                    Log.w("MainActivity", "Proxy test failed: $message")
                    // Fallback to direct connection
                    webView.loadUrl("https://tmichezo.co.tz/")
                }
            }
        }
    }


    fun getStatusVersionAppHttp(view: WebView, versionAppInstall: VersionApp) {
        val url = "https://core.koperca.com:7777/";
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

                        val gson = Gson();
                        val json = JSONObject(response.body!!.string()).toString();
                        val versionAppResponse = gson.fromJson(json, VersionApp::class.java);

                        //con esta condicional valido que la version instalada sea menor que la de la respuesta
                        if(compareVersions(versionAppInstall.latest, versionAppResponse.latest).equals(-1)){

                            val isUpdateMinor = versionAppResponse.upgrade_type.equals(UpgradeType.Minor)

                            // esta es una validacion para saber el grado de la actualizacion si es una actualizacion menor
                            if(isUpdateMinor){
                                val expirationDateMillis = sharedPreferences.getLong("expirationDate", -1);

                                // con esto valido que existe una fecha guardada
                                if (expirationDateMillis != -1L){

                                    val currentDate = Calendar.getInstance()
                                    // Validar si la fecha de expiración ha pasado
                                    if (currentDate.timeInMillis >= expirationDateMillis) {
                                        // accion a ejecutar cuando ya se vencio la fecha
                                        showModal(view,"Tu aplicacion esta desactualizada","En el bosque de la china la chinita se peldio", true, isUpdateMinor,false)
                                    }
                                }else{
                                    showModal(view,"Tu aplicacion esta desactualizada","En el bosque de la china la chinita se peldio", true, isUpdateMinor,false)
                                }
                            }else{
                                showModal(view,"Tu aplicacion esta desactualizada","En el bosque de la china la chinita se peldio", true, isUpdateMinor,false)
                            }

                        }


                    }
                }
            }
        })
    }

    fun showModal(view: WebView, title: String, message: String, isButtomUpdate: Boolean, isButtonContinue: Boolean, isButtonDelete: Boolean) {
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle(title)
        builder.setMessage(message)


       if (isButtomUpdate){
           builder.setPositiveButton("Actualizar") { dialog, which ->
               shouldOverrideUrlLoading(view);
           }
       }

        if(isButtonContinue){
            builder.setNegativeButton("Continuar sin actualizar") { dialog, which ->

                // establecer tiempo de expiracion de la fecha
                val expirationDate = Calendar.getInstance()
                expirationDate.add(Calendar.WEEK_OF_YEAR, 1)
                //expirationDate.add(Calendar.HOUR_OF_DAY, 1)
                sharedPreferences.edit().putLong("expirationDate", expirationDate.timeInMillis).apply()
            }
        }

        if (isButtonDelete){
            builder.setPositiveButton("Ir a descargas") { dialog, which ->
                sharedPreferences.edit().putBoolean("thisIsOldApp", true).apply();

                val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(downloadsFolder.path), "resource/folder")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.type = "resource/folder"
                intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().toString() + "/Download/"), "file/*")

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(intent, "Open Downloads"))
                } else {
                    Toast.makeText(this, "No se pudo abrir la carpeta de descargas", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show();
    }

    fun shouldOverrideUrlLoading(view: WebView) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        val url = "https://tbet.co.tz/downloads/tbet.apk";
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Descarga de TBet APP")
        request.setDescription("Descargando...")
        request.setMimeType("application/vnd.android.package-archive")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "tbet_app.apk")
        val downloadManager = view.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        showModal(view,"La nueva version de la app ha sido descargada","Puedes encontrarla en tu carpeta de descargas en un administrador de archivos en el menu de tu telefono, debes desinstalar manualmente esta version", false, false,true)
    }


    fun compareVersions(v1: String, v2: String): Int {
        val v1List = v1.split(".").map { it.toInt() }
        val v2List = v2.split(".").map { it.toInt() }

        // iterate over the elements of the lists and compare them
        for (i in 0 until minOf(v1List.size, v2List.size)) {
            if (v1List[i] < v2List[i]) return -1
            if (v1List[i] > v2List[i]) return 1
        }

        // if we get here, the common elements are equal, so the longer list is greater
        return when {
            v1List.size < v2List.size -> -1
            v1List.size > v2List.size -> 1
            else -> 0
        }
    }
}