package com.kosanworks.sahabatdepok;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
    private TextInputLayout inputLayoutName, inputLayoutEmail, inputLayoutPassword,inputLayoutNomer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_nama);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputLayoutNomer = (TextInputLayout) findViewById(R.id.input_layout_nomor);

        inputNama = (EditText) findViewById(R.id.input_nama);
        inputEmail = (EditText) findViewById(R.id.input_email);
        inputPassword = (EditText) findViewById(R.id.input_password);
        inputNomer = (EditText) findViewById(R.id.input_nomer);

        inputNama.addTextChangedListener(new MyTextWatcher(inputNama));
        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));
        inputNomer.addTextChangedListener(new MyTextWatcher(inputNomer));

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
                submitForm();

            }
        });

    }

    /**
     * Validating form
     */
    private void submitForm() {
        if (!validateName()) {
            return;
        }

        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }

        if (!validateNomer()) {
            return;
        }
        String nama = inputNama.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String nomer = inputNomer.getText().toString().trim();

        if (!nama.isEmpty() && !email.isEmpty() && !password.isEmpty() && !nomer.isEmpty()) {
            registerUser(nama, email, password, nomer);
        } else {
            Toast.makeText(getApplicationContext(), "Mohon lengkapi data anda!", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(getApplicationContext(), "Thank You!", Toast.LENGTH_SHORT).show();
    }


    private boolean validateName() {
        if (inputNama.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(inputNama);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }


    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateNomer() {
        if (inputNomer.getText().toString().trim().isEmpty()) {
            inputLayoutNomer.setError(getString(R.string.err_msg_password));
            requestFocus(inputNomer);
            return false;
        } else {
            inputLayoutNomer.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_nama:
                    validateName();
                    break;
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
            }
        }
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
//                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Email terdaftar.", Toast.LENGTH_LONG).show();

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Gagal terhubung ke server, pastikan anda memiliki koneksi internet!", Toast.LENGTH_LONG).show();
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

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void backlogin(View view) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

}
