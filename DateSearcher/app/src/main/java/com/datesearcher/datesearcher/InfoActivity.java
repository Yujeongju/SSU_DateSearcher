package com.datesearcher.datesearcher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class InfoActivity extends AppCompatActivity {
    TextView keyword, info;
    ImageButton tomap, back;
    ImageView img1, img2, img3, img4;

    double mapx, mapy;
    String str, str2, image_all,aaa_keyword;
    String [] image;

    Handler img_handler;

    private String[] all_keyword;
    int length;
    InputStream is = null;
    JSONObject JS;
    JSONArray temp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        keyword = (TextView) findViewById(R.id.info_keyword);
        info = (TextView) findViewById(R.id.info_text);
        tomap = (ImageButton) findViewById(R.id.info_toMap_btn);
        back = (ImageButton) findViewById(R.id.info_back_btn);

        img1 = (ImageView) findViewById(R.id.img1);
        img2 = (ImageView) findViewById(R.id.img2);
        img3 = (ImageView) findViewById(R.id.img3);
        img4 = (ImageView) findViewById(R.id.img4);

        Intent intent = getIntent();
        aaa_keyword = intent.getStringExtra("keyword");
        keyword.setText(aaa_keyword);

        img_handler = new Handler() {
            public void handleMessage(Message msg) {

                str2 = str.substring(60+keyword.length(), str.length() - 1);
                image = image_all.split("#");
                if (str2.contains("on a null object referenc")) {
                    info.setText("정보가 없습니다\n");
                } else {
                    info.setText(str2);
                    try{
                        if (image.length >= 3) {
                            URL url = new URL(image[1]);
                            Glide.with(InfoActivity.this).load(url).into(img1);
                        }
                        if (image.length >= 4) {
                            URL url = new URL(image[2]);
                            Glide.with(InfoActivity.this).load(url).into(img2);
                        }
                        if (image.length >= 5) {
                            URL url = new URL(image[3]);
                            Glide.with(InfoActivity.this).load(url).into(img3);
                        }
                        if (image.length >= 6) {
                            URL url = new URL(image[4]);
                            Glide.with(InfoActivity.this).load(url).into(img4);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        };

        btn_action();
    }
//todo 인텐트 넘기는 부분을 찾아서 구글맵을 티맵으로 명시적 인텐트를 해주자!
    private void btn_action(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                str = getNaverSearch(keyword.getText().toString());
                image_all = getNaverImage(keyword.getText().toString()); //이미지 가져오기
                img_handler.sendEmptyMessage(0); //이미지 설정

                //Log.d("result", str2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }).start();

        GeoPoint oKA = new GeoPoint(mapx, mapy);
        final GeoPoint oGeo = GeoTrans.convert(GeoTrans.KATEC, GeoTrans.GEO, oKA);
        tomap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InfoActivity.this, TMapActivity.class);
                try {
                    startActivity(intent);
                }catch (Exception e){

                    startActivity(intent);
                }
            }
        });
    }
    public String getNaverImage(String keyword) {
        String clientID = "HyFl0TwQ0jA0PkzCxSmm";
        String clientSecret = "0ySwsnXgik";

        try {
            String text = URLEncoder.encode(keyword, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/image.xml?query=" + text + "&display=5&sort=sim";
            StringBuffer sb = new StringBuffer();

            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Naver-Client-Id", clientID);
            conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            String tag;
            xpp.setInput(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            xpp.next();
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();

                        if (tag.equals("item")) ;
                        else if (tag.equals("link")) {
                            xpp.next();
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("#\n");
                        }
                        break;
                }
                eventType = xpp.next();
            }

            return sb.toString();
        } catch (Exception e) {
            return e.toString();
        }

    }


    public String getNaverSearch(String keyword) {
        String clientID = "HyFl0TwQ0jA0PkzCxSmm";
        String clientSecret = "0ySwsnXgik";

        try {
            String text = URLEncoder.encode(keyword, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/local.xml?query=" + text + "&display=1";
            StringBuffer sb = new StringBuffer();

            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Naver-Client-Id", clientID);
            conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            String tag;
            xpp.setInput(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            xpp.next();
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();

                        if (tag.equals("item")) ;
                        else if (tag.equals("title")) {
                            xpp.next();
                            sb.append("이름 : ");
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("\n");
                        } else if (tag.equals("category")) {
                            xpp.next();
                            sb.append("업종 : ");
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("\n");
                        } else if (tag.equals("description")) {
                            xpp.next();
                            sb.append("세부설명 : ");
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("\n");
                        } else if (tag.equals("telephone")) {
                            xpp.next();
                            sb.append("연락처 : ");
                            sb.append(xpp.getText());
                            sb.append("\n");
                        } else if (tag.equals("address")) {
                            xpp.next();
                            sb.append("주소 : ");
                            sb.append(xpp.getText());
                            sb.append("\n");
                        } else if (tag.equals("mapx")) {
                            xpp.next();
                            mapx = Double.parseDouble(xpp.getText());
                        } else if (tag.equals("mapy")) {
                            xpp.next();
                            mapy = Double.parseDouble(xpp.getText());
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();
                        if (tag.equals("item")) sb.append("\n");
                        break;
                }
                eventType = xpp.next();
            }

            return sb.toString();

        } catch (Exception e) {
            return e.toString();
        }
    }

    public void onCLick_info_back_btn_Listener(View view) {
        this.finish();
    }
}
