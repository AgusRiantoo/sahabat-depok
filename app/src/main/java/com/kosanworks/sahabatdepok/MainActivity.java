package com.kosanworks.sahabatdepok;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SQLiteHandler db;
    private SessionManager session;
    private ProgressDialog pDialog;
    private LaporanAdapter mAdapter;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, UserPost.class);
                startActivity(i);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

//        viewPager = (ViewPager) findViewById(R.id.viewpager);
//        setupViewPager(viewPager);
//
//        tabLayout = (TabLayout) findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(viewPager);
//
//        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);


        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        String name = user.get("username");
        String email = user.get("email");
        String ava = user.get("avatar");
        TextView txtname = (TextView) header.findViewById(R.id.username);
        txtname.setText(name);

        TextView txtemail = (TextView) header.findViewById(R.id.email);
        txtemail.setText(email);

        ImageView avatar = (ImageView) header.findViewById(R.id.avatar);

        Picasso.with(this)
                .load(ava)
                .config(Bitmap.Config.RGB_565)
                .error(R.drawable.avatar)
                .fit()
                .centerInside()
                .into(avatar);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tampiLaporan();
                onItemsLoadComplete();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        tampiLaporan();

//        recyclerView.setAdapter(mAdapter);

    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }


    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();

        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        this.finish();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//        adapter.addFragment(new Timeline(), "Timeline");
        adapter.addFragment(new Informasi(), "Berita");
        adapter.addFragment(new Berita(), "Informasi");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
        if (id == R.id.action_settings) {
            tampiLaporan();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_lapor) {
            Intent i = new Intent(this, UserPost.class);
            startActivity(i);
        } else if (id == R.id.nav_laporan) {
            Intent i = new Intent(this, UserLaporan.class);
            startActivity(i);

        } else if (id == R.id.nav_profil) {
            Intent i = new Intent(this, UserProfile.class);
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    private void tampiLaporan() {
        hideDialog();
        String tag_get_laporan = "get_laporan";
        pDialog.setMessage("Memproses...");
        showDialog();

        final StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_DATA, new Response.Listener<String>() {

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
                            LaporanData laporanData = new LaporanData();

                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            laporanData.id = jsonobject.getString("id");
                            laporanData.nama = jsonobject.getString("name");
                            laporanData.avatar = jsonobject.getString("avatar");
                            laporanData.judul_laporan = jsonobject.getString("judul_laporan");
                            laporanData.konten_laporan = jsonobject.getString("konten_laporan");
                            laporanData.latitude = jsonobject.getString("latitude");
                            laporanData.longitude = jsonobject.getString("longitude");
                            laporanData.nama_daerah = jsonobject.getString("nama_daerah");
                            laporanData.status = jsonobject.getString("status");
                            laporanData.foto = jsonobject.getString("foto");
                            laporanData.create_at = jsonobject.getString("created_at");
                            data.add(laporanData);

                            recyclerView.setVisibility(View.VISIBLE);
                            mAdapter = new LaporanAdapter(MainActivity.this, data);
                            recyclerView.setAdapter(mAdapter);
                            hideDialog();
                        }

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("message");
//                        Log.e("data", "data kosong");
                        Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Json error: ", Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.e("Error", "Error : " + error.getMessage());
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.fab), "Failed connecting to server", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showDialog();
                                tampiLaporan();
                            }
                        });


                snackbar.show();
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
        };
// Adding request to request queue
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

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
