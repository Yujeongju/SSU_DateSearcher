package com.datesearcher.datesearcher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;


public class ReviewActivity extends AppCompatActivity {

    TextView review_keyword;
    RecyclerView list;
    Review_recycler_adapter list_ap;
    List<Review_Tag> taglist;
    String keyword,string_start, string_finish;

    Handler handler, handler2, handler3;
    ProgressDialog loading_dia;

    InputStream is = null;
    JSONObject JS;
    JSONArray temp = null;

    HttpPost request;
    HttpClient client;
    ResponseHandler reshandler;
    HttpEntity responseResultEntity;
    HttpResponse response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // 로딩 다이얼로그 보이기
        handler = new Handler() {
            public void handleMessage(Message msg) {
                loading_dia = ProgressDialog.show(ReviewActivity.this, "로딩중", "데이터 로딩 중입니다...");
                loading_dia.setCancelable(true);
            }
        };

        // 로딩 다이얼로그 숨기기
        handler2 = new Handler() {
            public void handleMessage(Message msg) {
                //Toast.makeText(MainActivity.this, "데이터를 로딩 완료", Toast.LENGTH_LONG).show();
                loading_dia.dismiss();
            }
        };

        handler3 = new Handler() {
            public void handleMessage(Message msg) {
                list.setAdapter(list_ap);
                list_ap.notifyDataSetChanged();
            }
        };

        taglist = new ArrayList<>();
        list = (RecyclerView) findViewById(R.id.review_list);
        list_ap = new Review_recycler_adapter(this, taglist);
        review_keyword = (TextView)findViewById(R.id.review_keyword);

        Intent intent = getIntent();
        keyword = intent.getStringExtra("keyword");
        review_keyword.setText(keyword);
        
        string_start = "19700101";
        string_finish = "20300101";

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        list.setLayoutManager(gridLayoutManager);

        new Thread(){
            public void run(){
                try {
                    handler.sendEmptyMessage(0);
                    Instagram(keyword);
                    insert_post(inputData());
                    handler3.sendEmptyMessage(0);
                    handler2.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        
    }

    public void Instagram(String keyword) {
        try {
            //request 헤더 추가
            Document wholeCode = Jsoup.connect("https://www.instagram.com/explore/tags/" + keyword)
                    .header("Cookie",
                            "shbid=2382; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; ds_user_id=3451665929; mid=W3ZeWwAEAAFxRTah2gMqSLyFoUIA; mcd=3; fbm_124024574287414=base_domain=.instagram.com; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; rur=FTW; sessionid=IGSCbe1f22d109edc3691bd08d36485f9e900cac1f8b62fb2e5cf10ded0f97814a81%3A0rPYcNUdl69GWVgZVmcTdaFpnvqx1eZs%3A%7B%22_auth_user_id%22%3A3451665929%2C%22_auth_user_backend%22%3A%22accounts.backends.CaseInsensitiveModelBackend%22%2C%22_auth_user_hash%22%3A%22%22%2C%22_platform%22%3A4%2C%22_token_ver%22%3A2%2C%22_token%22%3A%223451665929%3ATls7kBIRv15DZszC7jFBZUPtxc2cgfrT%3A3a4a3e68491efeb3c447a4201458f7f0edaf37fd82695bc319512ed45ae049bc%22%2C%22last_refreshed%22%3A1534945024.2404546738%7D; fbsr_124024574287414=Rsn_jkyzvupVXCogJy4SMsU7kSm-xvudkXbRjpG6-VA.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUUNEUHRXejduZUMwYXBCZDNHbVdseXFmc1lZQUQzNU01NHFKYVZDaWlJUEx3V3lhR3dYYVVMOXU1MDRvSUFta3puYVA2ZzdLVnplRmNzekNhZW9OYlJmclRCZ0tFbHFUYXpYVkZHUGRxdmFTV3BpVW1mSU90eGpTRkRwZ1hRLUE2NDVseXFoWFAwX2d1UDBzV3I0a0E5OUFfSFoyS2JfSEpFck9CTktNZlRMUmtuQlR2QUxSY01wdW5wdi05N2ZHRnBlMTI2dFNQdVFnX0E2SzFkMU1lajloVWpKa3JiUkt0U0dMSWhlQWRPSkZZODNITW1QV3dXYjlDakdxeFdhV3FkNUxfWms5dDJhOTc5ZUNtTl82N1kwUndtbzRvdGZQb1VoN2kwUUozcGFOZnI4R3dZanJVTlBhTzhHSTlQZFNteDE0ck5yVXdjS21HWFJ2RFZMVE9XRyIsImlzc3VlZF9hdCI6MTUzNDk1MTAzNCwidXNlcl9pZCI6IjEwMDAwNDg2NTA1MDI4OCJ9; shbts=1534951070.1531627; urlgen=\"{\\\"210.93.56.23\\\": 23668\\054 \\\"121.170.57.238\\\": 4766}:1fsUtK:jqGFF0QxE9hkyQhCd-66-w5izC0\"")
                    .userAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
                    .get(); // 링크 연결

            Elements scriptCode = wholeCode.select("body > script:eq(1)");
            String wholeText = scriptCode.toString();

            getInstaInfoFromKeyword(wholeText);    //	정보를 수집하는 함수

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////Insta 데이터 가져오기
    public void getInstaInfoFromKeyword(String wholeText) throws Exception { // keyword로 첫번째 게시물페이지의 정보를 가져옴
        String[] shortcode = wholeText.split("\"shortcode\":\""); // 특정패턴 검색	/ 전체 문자열에서 필요한 부분만 수집하기 위해서
        String shortcode__end = new String("\",\"edge_medi");

        String[] contents = wholeText.split("text\":\"");
        String contents__end = new String("\"}}]},\"");

        String[] timestamp = wholeText.split("\"taken_at_timestamp\":");
        String timestamp__end = new String(",\"dimensi");

        for (int i = 1; i < contents.length; i++) {
            int contents_end = contents[i].indexOf(contents__end);
            if (contents_end > -1) {
                contents[i] = contents[i].substring(0, contents_end);
            }

            int shortcode_end = shortcode[i].indexOf(shortcode__end);
            if (shortcode_end > -1) {
                shortcode[i] = shortcode[i].substring(0, shortcode_end);
            }


            contents[i] = unicodeConvert(contents[i]);
            int timestamp_end = timestamp[i].indexOf(timestamp__end);
            if (timestamp_end > -1) {
                timestamp[i] = timestamp[i].substring(0, timestamp_end);
                // Date를 위해 import java.util.*;
            }
        }

        for (int i = 1; i < contents.length; i++) {
            contents[i] = contents[i].replaceAll("\\\\", "\\\\\\\\");

            connect("" + timestampConvert(timestamp[i]), "" + contents[i], "" + shortcode[i]);
        }

    }

    public String timestampConvert(String str) {
        long timestamp = Long.parseLong(str);
        Date date = new Date((long) timestamp * 1000);
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");
        String dateToString = transFormat.format(date);

        return dateToString;
    }

    private void connect(String Date, String wholeHashtag, String shortcode) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("\n-----------------connect Start--------------\n");
        request = makeHttpPost(Date, wholeHashtag, shortcode, "http://ryunha.cafe24.com/user_signup/connect.php");
        HttpParams params = new BasicHttpParams();
//        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        client = new DefaultHttpClient(params);
        reshandler = new BasicResponseHandler();

        try {
            client.execute(request, reshandler);
            System.out.println("\nconnect_end");
        } catch (IOException e) {
            System.out.println("\nconnect_Exception");
            e.printStackTrace();
        }
        System.out.println("\n----------------connect finish--------------\n");

    }

    public String unicodeConvert(String str) {
        StringBuilder sb = new StringBuilder();
        char ch;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            ch = str.charAt(i);
            if (ch == '\\' && str.charAt(i + 1) == 'u') {
                sb.append((char) Integer.parseInt(str.substring(i + 2, i + 6), 16));
                i += 5;
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////

    private void insert_post(JSONArray temp) throws Exception {
        System.out.println("\n-----------------Insert_post Start--------------\n");
        String[] jsonName = {"hashtag", "date", "keyword", "shortcode", "cnt"};
        String[][] parseData = new String[temp.length()][jsonName.length];
        JSONObject JS = null;
        System.out.println("\n-----------------JS Start--------------\n");
        for (int i = 0; i < temp.length(); i++) {
            JS = temp.getJSONObject(i);
            if (JS != null) {
                for (int j = 0; j < jsonName.length; j++) {
                    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!" + JS.getString(jsonName[j]));
                    parseData[i][j] = JS.getString(jsonName[j]);
                }
            }
        }
        System.out.println("\n-----------------JS finish--------------\n");

        System.out.println("\n-----------------JS2 Start--------------\n");

        String[] ps_hashtag = new String[temp.length()];
        String[] ps_date = new String[temp.length()];
        String[] ps_keyword = new String[temp.length()];
        String[] ps_shortcode = new String[temp.length()];
        String[] ps_count = new String[temp.length()];

        for (int i = 0; i < temp.length(); i++) {
            if (JS != null) {
                for (int j = 0; j < jsonName.length; j++) {
//                    System.out.println("i : " + i + "j : " + j);
//                    System.out.println(parseData[i][j]);
                    if (j == 0)
                        ps_hashtag[i] = parseData[i][j];
                    else if (j == 1)
                        ps_date[i] = parseData[i][j];
                    else if (j == 2)
                        ps_keyword[i] = parseData[i][j];
                    else if (j == 3)
                        ps_shortcode[i] = parseData[i][j];
                    else if (j == 4)
                        ps_count[i] = parseData[i][j];
                }
            } else
                System.out.println("--------------!!!------------");
        }
        System.out.println("\n-----------------JS2 finish--------------\n");

        System.out.println("\n-----------------Glide Start--------------\n");

        for (int i = 1; i < ps_count.length; i++) {

            System.out.println("taglist : "+ps_hashtag+"-----"+ps_count+"-----");
            taglist.add(new Review_Tag( "" + ps_hashtag[i], "" + ps_count[i]));

        }

        System.out.println("\n-----------------Glide finish--------------\n");
        System.out.println("\n-----------------Insert_post finish--------------\n");
    }

    private JSONArray inputData() throws IOException {
        System.out.println("\n-----------------inputData Start--------------\n");

        sendjson();

        System.out.println("\ninputData_start");
        try {
            response = client.execute(request);
            responseResultEntity = response.getEntity();
            if (responseResultEntity != null) {
                is = responseResultEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                String result = sb.toString();

                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!" + result);////////////////////////////////////////////////////

                JS = new JSONObject(result);
                temp = JS.getJSONArray("result");
                System.out.println("\ninputData_end");
            } else
                System.out.println("--------------NULL----------");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n-----------------inputData finish--------------\n");

        return temp;
    }

    private void sendjson() throws IOException {
        // TODO Auto-generated method stub
        System.out.println("\n-----------------sendJson Start--------------\n");
        request = makeHttpPost("" + string_start, "" + string_finish, "http://ryunha.cafe24.com/user_signup/sendjson.php");
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        client = new DefaultHttpClient(params);
        reshandler = new BasicResponseHandler();

        System.out.println("++++++++++" + string_start + "++++++++++++");
        System.out.println("++++++++++" + string_finish + "+++++++++++");

        try {
            client.execute(request, reshandler);
            System.out.println("\nsendJson_end");
        } catch (IOException e) {
            System.out.println("\nsendJson_Exception");
            e.printStackTrace();
        }
        System.out.println("\n-----------------sendJson finish--------------\n");

    }

    ///////////////////////////////////

    private HttpPost makeHttpPost(String Date, String wholeHashtag, String shortcode, String url) throws UnsupportedEncodingException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");

        HttpPost request = new HttpPost(url);

        //System.out.println("\n\n-----------keyword_final----------" + keyword_final);
        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("date", "" + Date));
        nameValue.add(new BasicNameValuePair("hashtag", "" + URLEncoder.encode("" + wholeHashtag, "UTF-8")));
        nameValue.add(new BasicNameValuePair("keyword", "" + URLEncoder.encode("" + keyword, "UTF-8")));
        nameValue.add(new BasicNameValuePair("shortcode", "" + shortcode));
        nameValue.add(new BasicNameValuePair("startDate", "" + string_start));
        nameValue.add(new BasicNameValuePair("endDate", "" + string_finish));

        request.setEntity(makeEntity(nameValue));
        System.out.println("\n-----------------makeHttpPost finish--------------\n");

        return request;
    }

    private HttpPost makeHttpPost(String url) throws UnsupportedEncodingException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");

        HttpPost request = new HttpPost(url);

        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("keyword", "" + URLEncoder.encode("" + keyword, "UTF-8")));
        request.setEntity(makeEntity(nameValue));

        return request;
    }

    private HttpPost makeHttpPost(String startDate, String EndDate, String url) throws UnsupportedEncodingException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");

        HttpPost request = new HttpPost(url);

        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("keyword", "" + URLEncoder.encode("" + keyword, "UTF-8")));
        nameValue.add(new BasicNameValuePair("startDate", "" + startDate));
        nameValue.add(new BasicNameValuePair("endDate", "" + EndDate));

        request.setEntity(makeEntity(nameValue));

        return request;
    }

    private HttpEntity makeEntity(Vector<NameValuePair> nameValue) {
        HttpEntity result = null;
        try {
            result = new UrlEncodedFormEntity(nameValue);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    ////////////////////////////////////////
    public void onCLick_search_back_btn_Listener(View view) {
        this.finish();
    }
}
