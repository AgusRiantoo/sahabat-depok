package com.kosanworks.sahabatdepok;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kosanworks.sahabatdepok.helpers.AppConfig;
import com.kosanworks.sahabatdepok.helpers.AppController;
import com.kosanworks.sahabatdepok.helpers.SQLiteHandler;
import com.kosanworks.sahabatdepok.helpers.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DaftarActivity extends AppCompatActivity {

    private Button btnRegister;
    private EditText inputNama;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputNomer;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);

        inputNama = (EditText) findViewById(R.id.input_nama);
        inputEmail = (EditText) findViewById(R.id.input_email);
        inputPassword = (EditText) findViewById(R.id.input_password);
        inputNomer = (EditText) findViewById(R.id.input_nomer);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        if (session.isLoggedIn()) {
            Intent i = new Intent(DaftarActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        btnRegister = (Button) findViewById(R.id.btn_daftar);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = inputNama.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String nomer = inputNomer.getText().toString().trim();

                if (!nama.isEmpty() && !email.isEmpty() && !password.isEmpty() && !nomer.isEmpty()) {
                    registerUser(nama, email, password, nomer);
                } else {
                    Toast.makeText(getApplicationContext(), "Mohon lengkapi data anda!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void registerUser(final String nama, final String email, final String password, final String nomer) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("EROR", "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    String error = jObj.getString("error");
                    if (error == "false") {
                        session.setLogin(true);
                        String token = jObj.getString("token");
                        String username = jObj.getString("username");
                        String email = jObj.getString("email");
                        String avatar = jObj.getString("avatar_link");
                        String phone = jObj.getString("phone");
                        String create_at = jObj.getJSONObject("created").getString("date");

                        db.addUser(token, email, username, avatar, phone, create_at);

                        Toast.makeText(getApplicationContext(), "Berhasil mendaftarkan akun!", Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(DaftarActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", nama);
                params.put("email", email);
                params.put("password", password);
                params.put("phone", nomer);
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

}
