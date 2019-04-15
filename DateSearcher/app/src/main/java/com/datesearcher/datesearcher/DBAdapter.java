package com.datesearcher.datesearcher;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class DBAdapter extends CursorAdapter {
    public DBAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        //레이아웃은 뷰 객체로 만들기
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.listview_item, viewGroup,false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final TextView word_view = (TextView)view.findViewById(R.id.main_keyword);
        final TextView date_view = (TextView)view.findViewById(R.id.main_date);

        //커서가 가리키는 아이템 얻어서 텍스트로 등록
        word_view.setText(""+cursor.getString(cursor.getColumnIndex("item")));
        date_view.setText(""+cursor.getString(cursor.getColumnIndex("create_at")));
    }
}
