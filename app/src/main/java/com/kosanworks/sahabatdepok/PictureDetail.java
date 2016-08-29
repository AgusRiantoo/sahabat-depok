package com.kosanworks.sahabatdepok;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class PictureDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_picture_detail);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_picture_detail);

    /* adapt the image to the size of the display */
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(),R.drawable.avatar),size.x,size.y,true);

    /* fill the background ImageView with the resized image */
        ImageView pic = (ImageView) findViewById(R.id.picture);

        String url = getIntent().getStringExtra("url");
        if (url != null){
            Picasso.with(this)
                    .load(url)
                    .config(Bitmap.Config.RGB_565)
                    .error(R.drawable.loading)
                    .fit()
                    .centerInside()
                    .into(pic);
        }else {
            pic.setImageBitmap(bmp);
        }
    }
}
