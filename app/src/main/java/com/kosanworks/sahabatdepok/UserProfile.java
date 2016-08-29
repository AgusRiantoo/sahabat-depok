package com.kosanworks.sahabatdepok;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kosanworks.sahabatdepok.helpers.SQLiteHandler;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class UserProfile extends AppCompatActivity {
    private SQLiteHandler db;
    String avatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        String name = user.get("username");
        String email = user.get("email");
        String phone = user.get("phone");
        avatar = user.get("avatar");
        String create_at = user.get("create_at");

        TextView setNama = (TextView) findViewById(R.id.tvNama);
        setNama.setText(name);

        TextView setEmail = (TextView) findViewById(R.id.tvEmail);
        setEmail.setText(email);

        TextView setPhone = (TextView) findViewById(R.id.tvPhone);
        setPhone.setText(phone);

        TextView setDate = (TextView) findViewById(R.id.tvDate);
        setDate.setText(create_at);

        ImageView imgAvatar = (ImageView) findViewById(R.id.avatar);

        Picasso.with(this)
                .load(avatar)
                .config(Bitmap.Config.RGB_565)
                .error(R.drawable.avatar)
                .fit()
                .centerInside()
                .into(imgAvatar);

        FloatingActionButton edit = (FloatingActionButton) findViewById(R.id.editProfile);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserProfile.this, UserEdit.class);
                startActivity(i);
            }
        });

        FloatingActionButton laporan = (FloatingActionButton) findViewById(R.id.laporan);
        laporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserProfile.this, UserLaporan.class);
                startActivity(i);
            }
        });

    }

    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    public void detailavatar(View view){
        Intent i = new Intent(getApplicationContext(),PictureDetail.class);
        i.putExtra("url",avatar);
        startActivity(i);
    }
}
