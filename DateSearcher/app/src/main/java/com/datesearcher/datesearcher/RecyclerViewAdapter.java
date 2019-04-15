package com.datesearcher.datesearcher;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHoler> {


    private Context mContext;
    private List<Post> mData;
    private EditText bottom, top;
    HashTagHelper mTextHashTagHelper;
    String bottom_text="";

    public RecyclerViewAdapter(Context mContext, List<Post> mData, EditText editText, EditText editText2){
        this.mContext = mContext;
        this.mData = mData;
        bottom = editText;
        top = editText2;
    }

    @Override
    public MyViewHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.card_view_item, viewGroup, false);

        return new MyViewHoler(view);
    }

    int j;
    @Override
    public void onBindViewHolder(@NonNull MyViewHoler myViewHoler, final int i) {
        myViewHoler.card_like.setText(String.valueOf(mData.get(i).getLike()));
        myViewHoler.card_content.setText(String.valueOf(mData.get(i).getHashtag()));
        Glide.with(mContext).load(""+mData.get(i).getCard_url()).into(myViewHoler.card_img);

        mTextHashTagHelper = HashTagHelper.Creator.create(mContext.getResources().getColor(R.color.colorPrimary), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {
                Log.d("hashtag", hashTag);
                try {
                    bottom_text = bottom.getText().toString();
                }catch (Exception e){
                    bottom_text = "";
                }
                bottom.setText(bottom_text+" "+hashTag);


            }
        });
        mTextHashTagHelper.handle(myViewHoler.card_content);

        myViewHoler.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)mContext.getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(bottom.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(top.getWindowToken(), 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHoler extends RecyclerView.ViewHolder{

        TextView card_like;
        TextView card_content;
        ImageView card_img;
        CardView cardView ;

        public MyViewHoler(@NonNull View itemView) {
            super(itemView);
            card_like = (TextView)itemView.findViewById(R.id.card_like);
            card_content = (TextView)itemView.findViewById(R.id.card_content);
            card_img = (ImageView)itemView.findViewById(R.id.card_img);
            cardView = (CardView)itemView.findViewById(R.id.card_view);




        }
    }
}
