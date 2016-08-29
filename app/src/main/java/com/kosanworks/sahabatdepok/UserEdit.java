package com.kosanworks.sahabatdepok;

import android.Manifest;
import android.app.Activity;
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
import android.support.design.widget.FloatingActionButton;
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
import com.kosanworks.sahabatdepok.helpers.AppConfig;
import com.kosanworks.sahabatdepok.helpers.AppController;
import com.kosanworks.sahabatdepok.helpers.SQLiteHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserEdit extends AppCompatActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 5;
    private static final int MY_REQUEST_CODE = 3;
    private static final int PICK_PHOTO_FOR_AVATAR = 4;
    private SQLiteHandler db;


    private Button btnSubmit;
    private EditText inputNama;
    private EditText inputEmail;
    private EditText inputNomer;
    private ProgressDialog pDialog;
    private ImageView imgAvatar;
    private Uri fileUri = null;
    private Uri avatarbaru = null;
    private String foto;
    private String avatarlama;


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
        avatarlama = user.get("avatar");

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        inputNama = (EditText) findViewById(R.id.input_nama);
        inputNama.setText(name);

        inputEmail = (EditText) findViewById(R.id.input_email);
        inputEmail.setText(email);

        inputNomer = (EditText) findViewById(R.id.input_telepon);
        inputNomer.setText(phone);

        imgAvatar = (ImageView) findViewById(R.id.avatar);

        Picasso.with(this)
                .load(avatarlama)
                .config(Bitmap.Config.RGB_565)
                .error(R.drawable.loading)
                .fit()
                .centerInside()
                .into(imgAvatar);

        btnSubmit = (Button) findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    submitUpdate();
                } catch (IOException e) {
                    e.printStackTrace();
                }

//              Toast.makeText(getApplicationContext(), "Mohon Maaf saat ini belum bisa merubah data anda, silahkan tunggu update selanjutnya.!", Toast.LENGTH_SHORT).show();
            }
        });

        
        FloatingActionButton galeri = (FloatingActionButton) findViewById(R.id.galeri);
        galeri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
            }
        });
        
        FloatingActionButton kamera = (FloatingActionButton) findViewById(R.id.kamera);
        kamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

    }

    private void submitUpdate() throws IOException {
        String nama = inputNama.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String nomer = inputNomer.getText().toString().trim();

        if (avatarbaru!=null) {
            Bitmap baru = MediaStore.Images.Media.getBitmap(getContentResolver(), avatarbaru);
            foto = getStringImage(baru);
//            Log.d("String foto",foto);
        }else {
            foto = "tidak berubah";
        }

//        Log.e("avatarbaru", String.valueOf(avatarbaru));


        if (!nama.isEmpty() && !email.isEmpty() && !nomer.isEmpty()) {
            updateUser(nama, email, foto, nomer);
        } else {
            Toast.makeText(getApplicationContext(), "Mohon lengkapi data anda!", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateUser(final String nama, final String email, final String avatar, final String nomer) {
        // Tag used to cancel the request
        String tag_string_req = "req_update";

        pDialog.setMessage("Updating ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPDATE_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e("EROR", "Edit Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    String error = jObj.getString("error");
                    if (error == "false") {
                        String message = jObj.getString("message");
                        String username = jObj.getJSONObject("user").getString("name");
                        String email = jObj.getJSONObject("user").getString("email");
                        String avatar = jObj.getJSONObject("user").getString("avatar");
                        String phone = jObj.getJSONObject("user").getString("phone");
                        String create_at = jObj.getJSONObject("user").getString("created_at");

                        String avatar_link = "http://188.166.189.134/avatar/".concat(avatar);
                        Log.e("avatar disimpan",avatar_link);
                        db.updateUser(email, username, avatar_link, phone, create_at);

//                        Log.d("namaa",username);
                        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();

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

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("name", nama);
                params.put("email", email);
                params.put("avatar", foto);
                params.put("phone", nomer);
//                Log.e("foto dikirim",foto);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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

    public String getStringImage(Bitmap bmp) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bmp.compress(Bitmap.CompressFormat.JPEG, 40, baos);

//        Log.e("width", String.valueOf(bmp.getWidth() +" x " + bmp.getHeight()));
        String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        return encodedImage;
    }


    private File getOutputPhotoFile() {

        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Sahabat Depok");

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e("Errror", "Failed to create storage directory.");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US).format(new Date());

        return new File(directory.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
            if (resultCode == RESULT_OK) {
//                avatarbaru = 1;
                Uri photoUri = null;
                if (data == null) {
                    photoUri = fileUri;
                    avatarbaru = fileUri;
                    Log.e("fotonya",photoUri.getPath());

                } else {
                    photoUri = data.getData();
                    avatarbaru = fileUri;
                    Log.e("fotonya",photoUri.getPath());
                }
                    showPhoto(photoUri.getPath());
            } else if (resultCode == RESULT_CANCELED) {
//                Intent i = new Intent(UserEdit.this, UserEdit.class);
//                startActivity(i);
//                finish();
            } else {
                Toast.makeText(this, "Callout for image capture failed!",
                        Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            Uri photoUri = null;
            if (data == null) {
                //Display an error
//                Log.e("data","gak ada nih");
                return;
            } else {
                photoUri = data.getData();
                avatarbaru = photoUri;
//                Log.e("fotonya",photoUri.getPath());

                imgAvatar.setImageURI(photoUri);

            }
        }
    }

    private void showPhoto(String photoUri) {
        File imageFile = new File(photoUri);
        if (imageFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(photoUri, options);
            imgAvatar.setImageBitmap(bitmap);
        }
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
