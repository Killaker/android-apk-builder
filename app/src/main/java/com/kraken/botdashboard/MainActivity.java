package com.kraken.botdashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    public static final String PREF_NAME   = "bot_prefs";
    public static final String PREF_URL    = "bot_url";
    public static final String DEFAULT_URL = "http://192.168.2.104:8001";

    private WebView          webView;
    private SwipeRefreshLayout swipeRefresh;
    private SharedPreferences prefs;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs        = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        webView      = findViewById(R.id.webview);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        // ── Configuración WebView ──────────────────────────────────────────
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) swipeRefresh.setRefreshing(false);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String url = req.getUrl().toString();
                // Deja que el WebView maneje URLs internas; abre externas en browser
                if (url.startsWith(getBotUrl())) {
                    return false;
                }
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                view.loadData(buildErrorPage(failingUrl), "text/html", "UTF-8");
            }
        });

        swipeRefresh.setOnRefreshListener(() -> webView.reload());
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright);

        loadDashboard();
    }

    // ── Menú ───────────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            webView.reload();
            return true;
        }
        if (id == R.id.menu_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 1);
            return true;
        }
        if (id == R.id.menu_home) {
            loadDashboard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 1 && res == RESULT_OK) {
            Toast.makeText(this, "URL actualizada", Toast.LENGTH_SHORT).show();
            loadDashboard();
        }
    }

    // ── Navegación ─────────────────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void loadDashboard() {
        webView.loadUrl(getBotUrl());
    }

    String getBotUrl() {
        return prefs.getString(PREF_URL, DEFAULT_URL);
    }

    private String buildErrorPage(String url) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'/>"
            + "<style>body{background:#111;color:#f55;font-family:sans-serif;"
            + "display:flex;flex-direction:column;align-items:center;"
            + "justify-content:center;height:100vh;margin:0;}"
            + "h2{margin-bottom:.5em;}p{color:#aaa;font-size:.9em;}</style></head>"
            + "<body><h2>&#9888; Sin conexión</h2>"
            + "<p>No se pudo conectar a:</p><p><b>" + url + "</b></p>"
            + "<p>Comprueba que el bot está corriendo y que estás en la misma red Wi-Fi.</p>"
            + "<button onclick='location.reload()' "
            + "style='margin-top:1em;padding:.6em 1.5em;background:#444;"
            + "color:#fff;border:none;border-radius:6px;font-size:1em;cursor:pointer'>"
            + "Reintentar</button></body></html>";
    }
}
