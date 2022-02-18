package com.cloud.torrent;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cloud.torrent.app.AppConfig;
import com.cloud.torrent.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class ForgotActivity extends AppCompatActivity {

    public static boolean checkInternetConnection(Context context) {

        ConnectivityManager con_manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected());
    }


    private EditText inputForgot;
    private Button btnforgotpwd;
    private ProgressDialog pDialog;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);

        inputForgot = (EditText) findViewById(R.id.inputforgot);
        btnforgotpwd = (Button) findViewById(R.id.btnforgot);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);



        btnforgotpwd.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputForgot.getText().toString().trim();

                if (!ForgotActivity.checkInternetConnection(ForgotActivity.this))

                {
                    Toast.makeText(getApplicationContext(),
                            "Check Your Internet Connection!", Toast.LENGTH_LONG).show();
                }

                // Check for empty data in the form
                else if (!email.isEmpty() ) {
                    // login user
                    checkforgot(email);


                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the Email address!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

    }

    private void checkforgot(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "req_forgot";

        pDialog.setMessage("Please wait ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_FORGOT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Forgot Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    String successMsg = jObj.getString("success_msg");
                    Toast.makeText(getApplicationContext(),
                            successMsg, Toast.LENGTH_LONG)
                            .show();
                }catch (JSONException e)
                { e.printStackTrace(); }

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (error) {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },  new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Forgot Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(ForgotActivity.this,
                LoginActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        String whatsappjoin = getString(R.string.whatsapp);

        //noinspection SimplifiableIfStatement
        if (id == R.id.close_app) {

            System.exit(0);
        }

        else if (id == R.id.whatsappjoin) {

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappjoin)));
        }

        return super.onOptionsItemSelected(item);
    }

}
