package com.kosanworks.sahabatdepok;

/**
 * Created by ghost on 23/08/16.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;


public class UserLaporanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<LaporanData> data = Collections.emptyList();
    LaporanData current;
    int currentPos = 0;
    private Context context;
    private LayoutInflater inflater;

    // Membuat konstruktor untuk inisialisasi context dan data yang dikirim dari
    // MainActivity
    public UserLaporanAdapter(Context context, List<LaporanData> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    // Inflate layout ketika viewholder dibuat
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_2, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        LaporanData current = data.get(position);
        myHolder.TxtJudul.setText(current.judul_laporan);
        myHolder.Txtkonten.setText(current.konten_laporan);
        myHolder.Txttanggal.setText(current.create_at);
        myHolder.TxtAlamat.setText(current.nama_daerah);
        myHolder.id = current.id;

        String url = "http://188.166.189.134/foto/".concat(current.foto);
        Picasso.with(context)
                .load(url)
                .config(Bitmap.Config.RGB_565)
                .error(R.drawable.loading)
                .fit()
                .centerInside()
                .into(myHolder.foto);
    }

    // return total item List
    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView TxtJudul;
        TextView Txtkonten;
        TextView TxtAlamat;
        ImageView foto;
        TextView Txttanggal;
        String id;

        public MyHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            TxtJudul = (TextView) itemView.findViewById(R.id.tvJudul);
            Txtkonten = (TextView) itemView.findViewById(R.id.tvKonten);
            Txttanggal = (TextView) itemView.findViewById(R.id.tvDate);
            foto = (ImageView) itemView.findViewById(R.id.foto);
            TxtAlamat = (TextView) itemView.findViewById(R.id.alamat);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(v.getContext(),LaporanDetail.class);
            i.putExtra("id",id);
            v.getContext().startActivity(i);
        }
    }
}