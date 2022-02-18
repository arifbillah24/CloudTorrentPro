package com.cloud.torrent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class activitywebview  extends AppCompatActivity {

    private WebView mywebview;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setMax(100);
        String url = "https://24x7files.com/buy.php";
        mywebview = (WebView) findViewById(R.id.webView2);
        mywebview.getSettings().setJavaScriptEnabled(true);
        mywebview.getSettings().setUseWideViewPort(true);
        mywebview.getSettings().setDomStorageEnabled(true);
        mywebview.loadUrl(url);

        mywebview.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                mywebview.loadUrl("file:///android_asset/no-internet.html");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(100);

                if (mywebview.getProgress() == 100) {


                    mywebview.setVisibility(View.VISIBLE);

                    findViewById(R.id.progressBar2).setVisibility(View.GONE);
                }
            }
        });

        }

    @Override
    public void onBackPressed() {

        if (mywebview.canGoBack()) {
            mywebview.goBack();
        }

        else {
            Intent intent = new Intent(activitywebview.this,
                    LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
