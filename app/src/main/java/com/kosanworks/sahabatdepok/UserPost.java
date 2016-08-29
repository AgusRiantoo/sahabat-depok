package com.kosanworks.sahabatdepok;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.kosanworks.sahabatdepok.helpers.AppConfig;
import com.kosanworks.sahabatdepok.helpers.AppController;
import com.kosanworks.sahabatdepok.helpers.SQLiteHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserPost extends AppCompatActivity {


    private static final String TAG = "CallCamera";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;
    private static final int PLACE_PICKER_FLAG = 1;
    private static final int MY_REQUEST_CODE = 3;
    Uri fileUri = null;
    ImageView photoImage = null;
    private PlacePicker.IntentBuilder builder;
    private ProgressDialog pDialog;
    private Bitmap bitmap2;
    private SQLiteHandler db;
    private EditText txtnama, txtketerangan, txtLokasi;
    private String latitude;
    private String longitude;
    private LatLng latLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_laporan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photoImage = (ImageView) findViewById(R.id.foto);
        txtnama = (EditText) findViewById(R.id.input_judul);
        txtketerangan = (EditText) findViewById(R.id.input_keterangan);
        txtLokasi = (EditText) findViewById(R.id.input_lokasi);
        db = new SQLiteHandler(getApplicationContext());
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        takePicture();
        Button submit = (Button) findViewById(R.id.btn_submit);

        txtLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Toast.makeText(UserPost.this,"Silahkan tekan icon pick location untuk menentukan alamat",Toast.LENGTH_SHORT).show();
                }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    submitPost();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    public String getStringImage(Bitmap bmp) {

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();

//        bmp.compress(Bitmap.CompressFormat.JPEG, 40, baos);
//        2,048 x 1536 2mp 2,592 x 1944
        Bitmap photo = bmp;
        photo = Bitmap.createScaledBitmap(photo, 2592, 1994, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 40, baos);

            /*
            * encode image to base64 so that it can be picked by saveImage.php file
            * */
        Log.e("width", String.valueOf(bmp.getWidth() +" x " + bmp.getHeight()));
        String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);


//        byte[] imageBytes = baos.toByteArray();
//        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private File getOutputPhotoFile() {

        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Sahabat Depok");

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to create storage directory.");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US).format(new Date());

        return new File(directory.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");
    }


    private boolean submitPost() throws IOException {
//        Log.e("pencet", "dipencet");
        String inputNama = txtnama.getText().toString().trim();
        String inputKeterangan = txtketerangan.getText().toString().trim();
        String inputLokasi = txtLokasi.getText().toString().trim();

        bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);

        String foto = getStringImage(bitmap2);


        if (!foto.isEmpty() && !inputNama.isEmpty() && !inputKeterangan.isEmpty() && !inputLokasi.isEmpty() && !latitude.isEmpty() && !longitude.isEmpty()) {

            posting(inputNama, inputKeterangan, inputLokasi, foto, latitude, longitude);
        } else {
            Toast.makeText(getApplicationContext(), "Mohon lengkapi data anda!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    public void getPlace(View view) {
        try {
            builder = new PlacePicker.IntentBuilder();
            Intent intent = builder.build(UserPost.this);
            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, PLACE_PICKER_FLAG);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), UserPost.this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(UserPost.this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();

        }
    }

    public void retake(View view) {
        takePicture();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
                takePicture();
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
                Toast.makeText(this, "Permission Denied!!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }
    }

    private boolean takePicture() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_REQUEST_CODE);

            } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //            Log.v(TAG, "Permission is granted");
                //
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_REQUEST_CODE);
                return true;
            } else {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = Uri.fromFile(getOutputPhotoFile());
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ);
            }
        } else {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = Uri.fromFile(getOutputPhotoFile());
            i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ);
        }
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
//            }


        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PLACE_PICKER_FLAG:
                    Place place = PlacePicker.getPlace(data, this);
                    txtLokasi.setText(place.getAddress());
                    latLng = place.getLatLng();
                    latitude = String.valueOf(latLng.latitude);
                    longitude = String.valueOf(latLng.longitude);
//                    Log.e("latitude", latitude);
//                    Log.e("ALAMAT", String.valueOf(txtLokasi.getText()));
                    break;
            }
        }

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = null;
                if (data == null) {
                    // A known bug here! The image should have saved in fileUri
//                    Toast.makeText(this, "Image saved successfully", Toast.LENGTH_LONG).show();
                    photoUri = fileUri;

                } else {
                    photoUri = data.getData();
//                    Toast.makeText(this, "Image saved successfully in: " + data.getData(),
//                            Toast.LENGTH_LONG).show();
                }

                showPhoto(photoUri.getPath());
            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(UserPost.this, MainActivity.class);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(this, "Callout for image capture failed!",
                        Toast.LENGTH_LONG).show();

            }
        }
    }


    private void showPhoto(String photoUri) {
        File imageFile = new File(photoUri);
        if (imageFile.exists()) {

            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(photoUri, options);


//            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//            BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmap);
            photoImage.setImageBitmap(bitmap);
        }
    }


    private void posting(final String inputNama, final String inputKeterangan, final String inputLokasi, final String foto, final String latitude, final String longitude) {
        // Tag used to cancel the request
        String tag_string_req = "req_posting";

        pDialog.setMessage("Melaporkan ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_POST_lAPORAN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
//                Log.d("EROR", "Laporan Response: " + response.toString());
                hideDialog();
                Toast.makeText(UserPost.this, "Berhasil mengirim laporan", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(UserPost.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.d("EROR", "Laporan Response: " + error.networkResponse.data);
//                Log.d("EROR", "Laporan Response: " + error.networkResponse.headers);
//                Log.d("EROR", "Laporan Response: " + error.networkResponse.notModified);
//                Log.d("EROR", "Laporan Response: " + error.networkResponse.statusCode);
//                Log.e("ERROR", "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Ukuran foto terlalu besar mohon kecilkan resolusi kamera anda.. ",
                        Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("judul_laporan", inputNama);
                params.put("konten_laporan", inputKeterangan);
                params.put("nama_daerah", inputLokasi);
                params.put("foto", foto);
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                params.put("kategori_id", "1");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
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

}