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


public class LaporanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<LaporanData> data = Collections.emptyList();
    LaporanData current;
    int currentPos = 0;
    private Context context;
    private LayoutInflater inflater;

    // Membuat konstruktor untuk inisialisasi context dan data yang dikirim dari
    // MainActivity
    public LaporanAdapter(Context context, List<LaporanData> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    // Inflate layout ketika viewholder dibuat

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        LaporanData current = data.get(position);
        myHolder.txtNama.setText(current.nama);
        myHolder.txtJudul.setText(current.judul_laporan);
        myHolder.txtKonten.setText(current.konten_laporan);
        myHolder.txtTanggal.setText(current.create_at);
        myHolder.txtAlamat.setText(current.nama_daerah);
        myHolder.id = current.id;
        String mStatus;
        if (current.status == "1"){
            mStatus = "Sedang dalam proses";
        }else if (current.status == "3"){
            mStatus = "Sudah diselesaikan";
        }else {
            mStatus = "Menunggu konfirmasi";
        }
        myHolder.txtStatus.setText(mStatus);

        String ava = "http://188.166.189.134/avatar/".concat(current.avatar);
        Picasso.with(context)
                .load(ava)
                .config(Bitmap.Config.RGB_565)
                .error(R.drawable.macet)
                .fit()
                .centerInside()
                .into(myHolder.avatar);

        String url = "http://188.166.189.134/foto/".concat(current.foto);
        Picasso.with(context)
                .load(url)
                .config(Bitmap.Config.RGB_565)
                .error(R.drawable.macet)
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

        public TextView txtNama;
        ImageView avatar;
        TextView txtJudul;
        TextView txtKonten;
        TextView txtAlamat;
        ImageView foto;
        TextView txtTanggal;
        TextView txtStatus;
        String id;

        public MyHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            txtNama = (TextView) itemView.findViewById(R.id.tvNama);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            txtJudul = (TextView) itemView.findViewById(R.id.tvJudul);
            txtKonten = (TextView) itemView.findViewById(R.id.tvKonten);
            txtTanggal = (TextView) itemView.findViewById(R.id.tvDate);
            foto = (ImageView) itemView.findViewById(R.id.foto);
            txtAlamat = (TextView) itemView.findViewById(R.id.alamat);
            txtStatus = (TextView) itemView.findViewById(R.id.status);

            }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(v.getContext(),LaporanDetail.class);
            i.putExtra("id",id);
            v.getContext().startActivity(i);
        }
    }


    }

