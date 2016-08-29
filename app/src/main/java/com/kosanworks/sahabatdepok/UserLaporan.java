package com.kosanworks.sahabatdepok;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kosanworks.sahabatdepok.helpers.AppConfig;
import com.kosanworks.sahabatdepok.helpers.AppController;
import com.kosanworks.sahabatdepok.helpers.SQLiteHandler;
import com.kosanworks.sahabatdepok.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLaporan extends AppCompatActivity {

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;
    private Toolbar toolbar;
    private SQLiteHandler db;
    private SessionManager session;
    private ProgressDialog pDialog;
    private UserLaporanAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_laporan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pDialog = new ProgressDialog(this);

        pDialog.setCancelable(false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setEnabled(true);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tampiLaporan();
                onItemsLoadComplete();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserLaporan.this, UserPost.class);
                startActivity(i);
            }
        });

        tampiLaporan();

    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void tampiLaporan() {
        hideDialog();
        String tag_get_laporan = "get_laporan";
        pDialog.setMessage("Memproses...");
        showDialog();

        final StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_LAPORAN_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
//                Log.e("Laporan", "Laporan Response : " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    String error = jObj.getString("error");

                    if (error == "false") {

                        String message = jObj.getString("laporan");
//                        Log.e("laporan",message);

                        List<LaporanData> data = new ArrayList<>();
                        JSONArray jsonarray = new JSONArray(message);

                        for (int i = 0; i < jsonarray.length(); i++) {
                            LaporanData orderData = new LaporanData();

                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            orderData.id = jsonobject.getString("id");
                            orderData.nama = jsonobject.getString("user_id");
                            orderData.judul_laporan = jsonobject.getString("judul_laporan");
                            orderData.konten_laporan = jsonobject.getString("konten_laporan");
                            orderData.latitude = jsonobject.getString("latitude");
                            orderData.longitude = jsonobject.getString("longitude");
                            orderData.nama_daerah = jsonobject.getString("nama_daerah");
                            orderData.status = jsonobject.getString("status");
                            orderData.foto = jsonobject.getString("foto");
                            orderData.create_at = jsonobject.getString("created_at");

                            data.add(orderData);
                            recyclerView.setVisibility(View.VISIBLE);
                            mAdapter = new UserLaporanAdapter(UserLaporan.this, data);
                            recyclerView.setAdapter(mAdapter);
                            hideDialog();
                        }

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e("data", "data kosong");
                        Toast.makeText(UserLaporan.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(UserLaporan.this, "Json error: ", Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("jancok", "Error : " + error.getMessage());
                Toast.makeText(UserLaporan.this, "Gagal Terhubung ke Server", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            //Fungsi untuk ngirim request GET ke api
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                db = new SQLiteHandler(getApplicationContext());
                HashMap<String, String> user = db.getUserDetails();
                //ambil token yang disimpan dalam SQLite
                String token = user.get("token");

                //Tambahkan custom header yang berisi token
                String auth = "Bearer "
                        + token;

                Log.e("token", "token :" + auth);
                headers.put("Authorization", auth);
                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_get_laporan);
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
}
