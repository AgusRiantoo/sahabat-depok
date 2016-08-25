package com.kosanworks.sahabatdepok;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kosanworks.sahabatdepok.helpers.AppConfig;
import com.kosanworks.sahabatdepok.helpers.AppController;
import com.kosanworks.sahabatdepok.helpers.SQLiteHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserEdit extends AppCompatActivity {
    private SQLiteHandler db;


    private Button btnSubmit;
    private EditText inputNama;
    private EditText inputEmail;
    private EditText inputNomer;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        String name = user.get("username");
        String email = user.get("email");
        String phone = user.get("phone");
        final String avatar = user.get("avatar");

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        inputNama = (EditText) findViewById(R.id.input_nama);
        inputNama.setText(name);

        inputEmail = (EditText) findViewById(R.id.input_email);
        inputEmail.setText(email);

        inputNomer = (EditText) findViewById(R.id.input_telepon);
        inputNomer.setText(phone);

        ImageView imgAvatar = (ImageView) findViewById(R.id.avatar);

        Picasso.with(this)
                .load(avatar)
                .config(Bitmap.Config.RGB_565)
                .error(R.drawable.loading)
                .fit()
                .centerInside()
                .into(imgAvatar);

        btnSubmit = (Button) findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String nama = inputNama.getText().toString().trim();
//                String email = inputEmail.getText().toString().trim();
//                String nomer = inputNomer.getText().toString().trim();
//                String avatarbaru = "iVBORw0KGgoAAAANSUhEUgAAAJcAAABXCAIAAACGFUCaAAAAA3NCSVQICAjb4U/gAAAAEHRFWHRTb2Z0d2FyZQBTaHV0dGVyY4LQCQAABSNJREFUeNrt3X9MlHUAx/Hvc78eeLg77qdwoB5KhWTxY5iAKcJ0WaaVKUuHmkPQHCIycKKulf2BLnNiWs2cA9wpBULGYJomJZI/gsyiqSAJCAgHHHJ299wdx93TH44yyNXwx93p5/3ns8GefV/f57nv97lnQHEcR5CHx8MQQBFBEUERQRGKCIoIigiKUERQRFBEUIQigiKCIoIiFBEUERQRFKGIoIigiKAIRQRFBEUERQRFKCIoIigiKEIRQRFBEUERigiKCIoIilBEUERQRFCEIoIigiKC4hOdwN1OaMDuaOs0mtgBl/8pbIqixIxonMZXJOS7uSL18+WbbjVGTTcMYkakkjMURbl2aDiO6+1jzRZ78HiFu1+Lz4f4uXCM2jqNw8bIzNqDAmUuJ7wzz1QKRn+tB5+L/zFGJnZgpK47EP51kh7xPw54GCOsUREUXZ/Tauw3O6DoyXHGuh2b9l8wOrFffAQXjPnqscMFx3/rshCeWPPCgpWpM9UCgh6m4rrMLXHTYxYtnH/3wbKvKqtrLuTt/GAUv9DaVP7J191TV2ZlBjNWfWuXRMyH28NWnPxsSMWxb70Y73mvzL5z5Ng3VeWVJ2Kjo0Z327P2dloU4fFR4/0EhKhUWkII4Swt1QWFJ+raTPS4mKQ1i14cIyTOvjN7dhXU33ZQ4uCEt9YuDpPZfj+y91BVg8FC6NC3c7JnyO3t53T5FTWtZkoyadnmVQkiQuztR7ZmHzQ7aE1k4uols8bSFBQJISnJSWYzW1pWIfZh4uOm/XCutvhIeWjIU8krloxuPyKeGD62r3KfTrZ0fnSIUkgRwrGNhz6u6I1P2bZOcq3ks8L8n0I3xCh40rDEtA9XSkj76d15xUenTlqhNly9zpu14b2X/Jw2kYxnvf5lXmnLlGVb12hFZrO3nE/MhPCVM1JWzwkYbDi670D++fAtM9UeuFR48KfM5/PT1iRPnKg9qCs5VFR6IP9wUND4jPRVQqFwlBPNP2HD5sTQW1UfbczZ9OnJK7edtpazF53hiXNCxsgDoudGy9ov3bASQgS+Af5KsY/ymZg4ja3DYOMIIRStUMukMpWa4dmaz/zoiFy6IEKrkmu0Y2V3JjDP2y9QrVAGTpkd6dvd2GXDHXUomhZlrX9n+469J09V+/up16enennR9zPVfIJikzJjEnsul+/P37mXzonrZ00t29Nqhy7XIJOd42w3v9MVlde19Q8KBJxD6yT/fKDADRhvDfg853uPD1Wel5R2ttvxuXh3jLd3Znrq5wd0KclJvlLJA3nUI1JPnrcwrGpXfTctZWTTs3ITg0V/L2QNp78oagrOyM2YLP2jKnfb2RE/LpRIBSa90UE0gns8ScJ+cWRyuWxj9lqlQn6fmzpLc83x842tekN3R2NNVQPrGxjwdGzYYO3hyl/bDLf7ezqa9RaOEMJxHEecDrt98N/3f14TpkWQi7qyiy29RsPN9k7W47eJHrRf5Ky3euorKou7WCfhyyZELU97WSujl2fMLSwsereSJZQ45I207FcDFFPffO0X3Z6c7+2E8Bm/WAl/2LVFMZOWr3+9sKDk/VMsoQPmZGUuVj4mitSgw5UPoOob9BGhmruPXLrS6aovy/7nGT5Zd1QERQRFKD66dYs7fa0PxVES9vaxYkY0ctvmPi8AeMo8E9Q36F2zOB56B27YcTEj6u1jVQp3eQdu5Dxzx52Gu735gvdRHwdFhDUqFBEUERQRFKGIIYAigiKCIoLiY9Wf5aDaMBh+bXsAAAAASUVORK5CYII=";
//                if (!nama.isEmpty() && !email.isEmpty() && !avatarbaru.isEmpty() && !nomer.isEmpty()) {
//                    updateUser(nama, email, avatarbaru, nomer);
//                } else {
//                    Toast.makeText(getApplicationContext(), "Mohon lengkapi data anda!", Toast.LENGTH_SHORT).show();
//                }
              Toast.makeText(getApplicationContext(), "Mohon Maaf saat ini belum bisa merubah data anda, silahkan tunggu update selanjutnya.!", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void updateUser(final String nama, final String email, final String avatar, final String nomer) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPDATE_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("EROR", "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    String error = jObj.getString("error");
                    if (error == "false") {
                        String token = jObj.getString("token");
                        String username = jObj.getString("username");
                        String email = jObj.getString("email");
                        String avatar = jObj.getString("avatar_link");
                        String phone = jObj.getString("phone");
                        String create_at = jObj.getJSONObject("created").getString("date");

                        db.addUser(token, email, username, avatar, phone, create_at);

                        Toast.makeText(getApplicationContext(), "Berhasil mendaftarkan akun!", Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(UserEdit.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("message");
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
                params.put("password", avatar);
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
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
