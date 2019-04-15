package com.datesearcher.datesearcher;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class Review_recycler_adapter extends   RecyclerView.Adapter<Review_recycler_adapter.MyViewHoler> {

    private Context mContext;
    private List<Review_Tag> mData;

    public Review_recycler_adapter(Context mContext, List<Review_Tag> mData){
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyViewHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.review_tag, viewGroup, false);

        return new MyViewHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHoler myViewHoler, int i) {

        myViewHoler.card_tag.setText(String.valueOf(mData.get(i).getTag()));
        myViewHoler.card_cnt.setText(String.valueOf(mData.get(i).getTag_cnt()));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHoler extends RecyclerView.ViewHolder{

        TextView card_tag;
        TextView card_cnt;
        CardView cardView ;

        public MyViewHoler(@NonNull View itemView) {
            super(itemView);
            card_tag = (TextView)itemView.findViewById(R.id.link);
            card_cnt = (TextView)itemView.findViewById(R.id.count);

            cardView = (CardView)itemView.findViewById(R.id.review_card_view);
        }
    }


}
