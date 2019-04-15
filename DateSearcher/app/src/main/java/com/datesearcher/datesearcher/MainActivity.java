package com.datesearcher.datesearcher;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    private SimpleDateFormat simpleDateFormat;
    private Date now_date;
    private DBHelper dbHelper;
    private ListView list;
    ImageButton main_search_btn, tomap_btn;
    public static Context CONTEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CONTEXT = this;

        main_search_btn = (ImageButton) findViewById(R.id.main_search_btn);
        tomap_btn = (ImageButton)findViewById(R.id.main_toMap_btn);

        tomap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TMapActivity.class);
                ////////////////////////////////////////
                //메인에서 실행할 경우 check를 1로 한다.
                intent.putExtra("check",1);
                startActivity(intent);
            }
        });
        editText = (EditText) findViewById(R.id.main_editText);
        list = (ListView)findViewById(R.id.main_list);

        LinearLayout linearLayout = findViewById(R.id.main_linear);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        });

        dbHelper = new DBHelper((Context) getApplicationContext(), "LASTWORD.db", null, 1);

        long now = System.currentTimeMillis();
        now_date = new Date(now);
        // 출력될 포맷 설정
        simpleDateFormat = new SimpleDateFormat("yyyy. MM. dd.");

        getResult();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapterView.getAdapter().getItem(i);
                String searcher = cursor.getString(cursor.getColumnIndex("item"));

                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("keyword", ""+searcher);
                startActivity(intent);
            }
        });
    }

    public void onClick_main_search_btn_Listner(View view){
        String item = editText.getText().toString();
        String date = simpleDateFormat.format(now_date);

        if(view != null){
            //키보드 숨기기
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }

        if(item.length()==0)
            Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
        else {
            dbHelper.insert(date, item);
            getResult();

            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("keyword", editText.getText().toString());
            startActivity(intent);
        }
        editText.setText("");
    }

    public void onClick_main_clear_Listener(View view) {
        dbHelper.delete();
        getResult();
    }

    public void getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM LASTWORD", null);
        if(cursor.getCount() >= 0){
            DBAdapter dbAdapter = new DBAdapter(this, cursor);
            list.setAdapter(dbAdapter);
        }
    }




}
