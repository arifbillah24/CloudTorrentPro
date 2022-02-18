package com.cloud.torrent;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cloud.torrent.app.AppConfig;
import com.cloud.torrent.app.AppController;
import com.cloud.torrent.helper.SQLiteHandler;
import com.cloud.torrent.helper.SessionManager;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.android.volley.VolleyLog.TAG;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static boolean checkInternetConnection(Context context) {

        ConnectivityManager con_manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected());
    }


    private boolean isVisible = true;
    private ProgressBar progressBar;
    private WebView mywebview;
    private SQLiteHandler db;
    private SessionManager session;
    public static final long TIMEOUT_IN_MILLI = 1000 * 60 * 60 * 5;
    public static final String PREF_FILE = "App_Pref";
    public static final String KEY_SP_LAST_INTERACTION_TIME = "KEY_SP_LAST_INTERACTION_TIME";
    public static String surls;

    private static final int GROUP_ID = "listGroup".hashCode();
    static final String FETCH_NAMESPACE = "DownloadListActivity";

    private View mainView;
    private FileAdapter fileAdapter;
    private Fetch fetch;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (!MainActivity.checkInternetConnection(this))

        {
            mywebview = (WebView) findViewById(R.id.webView);
            mywebview.loadUrl("file:///android_asset/no-internet.html");
        } else {

            String magent = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
            mywebview = (WebView) findViewById(R.id.webView);
            mywebview.getSettings().setJavaScriptEnabled(true);
            mywebview.getSettings().setUseWideViewPort(true);
            mywebview.getSettings().setDomStorageEnabled(true);
            mywebview.getSettings().setUserAgentString(magent);
            String email = "admin@24x7files.com";
            checkcloudp(email);
        }

        getWindow().setBackgroundDrawable(null);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);


        mywebview.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                mywebview.loadUrl("file:///android_asset/no-internet.html");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                //Toast.makeText(MainActivity.this, "Please Wait...", Toast.LENGTH_SHORT).show(); This loading Dialog
                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(100);

                if (mywebview.getProgress() == 100) {


                    mywebview.setVisibility(View.VISIBLE);

                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {


                if (url.contains("/files/"))
                {
                    surls = url;
                    final FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(MainActivity.this)
                            .setDownloadConcurrentLimit(4)
                            .setHttpDownloader(new OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
                            .setNamespace(FETCH_NAMESPACE)
                            .build();
                    fetch = Fetch.Impl.getInstance(fetchConfiguration);
                    enqueueDownloads();
                }
                else if (url.contains("/download/"))

                {
                      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                      view.getContext().startActivity(intent);
                      return true;
                }

                return false;
            }


        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

    }

    private void checkcloudp(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "req_url";

    StringRequest strReq = new StringRequest(Request.Method.POST,
            AppConfig.URL_APP, new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.d(TAG, "url Response: " + response.toString());

            try {
                JSONObject jObj = new JSONObject(response);
               String successUrl = jObj.getString("urlcloudpro");
                mywebview.loadUrl(successUrl);
            }catch (JSONException e)
            { e.printStackTrace(); }

        }
    },  new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Forgot Error: " + error.getMessage());
            Toast.makeText(getApplicationContext(),
                    error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }) {

        @Override
        protected Map<String, String> getParams() {
            // Posting parameters to forgot url
            Map<String, String> params = new HashMap<String, String>();
            params.put("email", email);

            return params;
        }

    };

    // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
}

    private void checkcloudt(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "req_url";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_APP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "url Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    String cloudUrl = jObj.getString("urlcloudt");
                    mywebview.loadUrl(cloudUrl);
                }catch (JSONException e)
                { e.printStackTrace(); }

            }
        },  new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Forgot Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to forgot url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void checktorrentpc(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "req_url";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_APP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "url Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    String torrentpcUrl = jObj.getString("urltorrentpc");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(torrentpcUrl)));
                }catch (JSONException e)
                { e.printStackTrace(); }

            }
        },  new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Forgot Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to forgot url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checktorrentsc(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "req_url";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_APP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "url Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    String torrentscUrl = jObj.getString("urltorrentsc");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(torrentscUrl)));
                }catch (JSONException e)
                { e.printStackTrace(); }

            }
        },  new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Forgot Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to forgot url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mywebview.canGoBack()) {
            mywebview.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ctdm) {

                final Intent intent = new Intent(MainActivity.this, DownloadListActivity.class);
                MainActivity.this.startActivity(intent);
        }


        else if (id == R.id.close_app) {

            System.exit(0);
        } else if (id == R.id.btnLogout) {

            logoutUser();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        getSharedPreference().edit().remove(KEY_SP_LAST_INTERACTION_TIME).apply();

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        String whatsapp = getString(R.string.whatsapp);
        String cloud24x7 = getString(R.string.cloud24x7);
        String rate_this = getString(R.string.rate_this);
        String speedtest = getString(R.string.speedtest);
        String cloudp = getString(R.string.cloudpeers);


        int id = item.getItemId();

        if (id == R.id.cloudt) {
            String email = "admin@24x7files.com";
            checkcloudt(email);
        } else if (id == R.id.cloudtweb) {
            String email = "admin@24x7files.com";
            checktorrentpc(email);
        } else if (id == R.id.cloudp) {

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(cloudp)));

        } else if (id == R.id.whatsapp) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(whatsapp)));
        } else if (id == R.id.cloud) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(cloud24x7)));
        } else if (id == R.id.rate_this_app) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rate_this)));
        } else if (id == R.id.speedtest) {
            mywebview.loadUrl(speedtest);
        } else if (id == R.id.nav_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Cloud Torrent");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share));
            startActivity(shareIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (isValidLogin())
            getSharedPreference().edit().putLong(KEY_SP_LAST_INTERACTION_TIME, System.currentTimeMillis()).apply();
        else logoutUser();
    }

    public SharedPreferences getSharedPreference() {
        return getSharedPreferences(PREF_FILE, MODE_PRIVATE);
    }

    public boolean isValidLogin() {
        long last_edit_time = getSharedPreference().getLong(KEY_SP_LAST_INTERACTION_TIME, 0);
        return last_edit_time == 0 || System.currentTimeMillis() - last_edit_time < TIMEOUT_IN_MILLI;
    }


    private void enqueueDownloads() {
        final com.tonyodev.fetch2.Request requests = Data.getFetchRequestWithGroupId(GROUP_ID);
        fetch.enqueue(requests, updatedRequests -> {

            Toast.makeText(getApplicationContext(),"Download Added", Toast.LENGTH_LONG).show();

        }, error -> Timber.d("DownloadListActivity Error: %1$s", error.toString()));

    }



}


