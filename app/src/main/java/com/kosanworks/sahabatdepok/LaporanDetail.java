package com.kosanworks.sahabatdepok;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kosanworks.sahabatdepok.helpers.AppConfig;
import com.kosanworks.sahabatdepok.helpers.AppController;
import com.kosanworks.sahabatdepok.helpers.SQLiteHandler;
import com.kosanworks.sahabatdepok.helpers.SessionManager;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LaporanDetail extends AppCompatActivity implements OnMapReadyCallback {

    private SQLiteHandler db;
    private SessionManager session;
    private ProgressDialog pDialog;
    private String id,url,urlAvatar;
    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private String judul_laporan, konten_laporan, nama_daerah, status, create_at, foto = null;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        id = getIntent().getStringExtra("id");

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());

        HashMap<String, String> user = db.getUserDetails();

        hideDialog();
        String tag_get_laporan = "get_laporan";
        pDialog.setMessage("Memproses...");
        showDialog();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.direction);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LaporanDetail.this, Directions.class);
                i.putExtra("latitude",latitude);
                i.putExtra("longitude",longitude);
                i.putExtra("title",judul_laporan);
                i.putExtra("alamat",nama_daerah);
                startActivity(i);
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LAPORAN_DETAIL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
//                Log.e("Laporan", "Laporan Response : " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    String error = jObj.getString("error");

                    if (error == "false") {

                        String message = jObj.getString("laporan");
//                        Log.e("laporan", message);
                        String id = jObj.getJSONObject("laporan").getString("id");
                        String nama = jObj.getJSONObject("laporan").getString("name");
                        String avatar = jObj.getJSONObject("laporan").getString("avatar");
                        judul_laporan = jObj.getJSONObject("laporan").getString("judul_laporan");
                        konten_laporan = jObj.getJSONObject("laporan").getString("konten_laporan");
                        latitude = jObj.getJSONObject("laporan").getDouble("latitude");
                        longitude = jObj.getJSONObject("laporan").getDouble("longitude");
                        nama_daerah = jObj.getJSONObject("laporan").getString("nama_daerah");
                        status = jObj.getJSONObject("laporan").getString("status");
                        foto = jObj.getJSONObject("laporan").getString("foto");
                        create_at = jObj.getJSONObject("laporan").getString("created_at");


                        TextView judul = (TextView) findViewById(R.id.tvJudul);
                        judul.setText(judul_laporan);

                        TextView stats = (TextView) findViewById(R.id.tvStatus);

                        String mStatus;
                        if (status == "1"){
                            mStatus = "Sedang dalam proses";
                        }else if (status == "2"){
                            mStatus = "Sudah diselesaikan";
                        }else {
                            mStatus = "Menunggu konfirmasi";
                        }

                        stats.setText(mStatus);

                        TextView konten = (TextView) findViewById(R.id.tvKonten);
                        konten.setText(konten_laporan);


                        TextView txtNama = (TextView) findViewById(R.id.tvNama);
                        txtNama.setText(nama);

                        TextView alamat = (TextView) findViewById(R.id.tvAlamat);
                        alamat.setText(nama_daerah);

                        TextView date = (TextView) findViewById(R.id.tvDate);
                        date.setText(create_at);

                        ImageView imgAvatar = (ImageView) findViewById(R.id.avatar);
                        urlAvatar = "http://188.166.189.134/avatar/".concat(avatar);
                        Picasso.with(LaporanDetail.this)
                                .load(urlAvatar)
                                .config(Bitmap.Config.RGB_565)
                                .error(R.drawable.loading)
                                .fit()
                                .centerInside()
                                .into(imgAvatar);

                        ImageView imgFoto = (ImageView) findViewById(R.id.foto);
                        url = "http://188.166.189.134/foto/".concat(foto);
                        Picasso.with(LaporanDetail.this)
                                .load(url)
                                .config(Bitmap.Config.RGB_565)
                                .error(R.drawable.loading)
                                .fit()
                                .centerInside()
                                .into(imgFoto);

                        LatLng tkp = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions()
                                .title(nama_daerah)
                                .position(tkp));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tkp, 15));

                        hideDialog();
                    } else {
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(LaporanDetail.this, errorMsg, Toast.LENGTH_LONG).show();

                    }

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.e("Error", "Error : " + error.getMessage());
                Toast.makeText(LaporanDetail.this, "Gagal Terhubung ke Server", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            //Fungsi untuk ngirim request GET ke api
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                HashMap<String, String> user = db.getUserDetails();
                //ambil token yang disimpan dalam SQLite
                String token = user.get("token");

                //Tambahkan custom header yang berisi token
                String auth = "Bearer "
                        + token;

//                Log.e("token", "token :" + auth);
                headers.put("Authorization", auth);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("id", id);

                return params;
            }

        };
// Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_get_laporan);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
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
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void detail(View view){
        Intent i = new Intent(getApplicationContext(),PictureDetail.class);
        i.putExtra("url",url);
        startActivity(i);
    }

    public void detailavatar(View view){
        Intent i = new Intent(getApplicationContext(),PictureDetail.class);
        i.putExtra("url",urlAvatar);
        startActivity(i);
    }
}
