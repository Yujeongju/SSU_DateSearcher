package com.datesearcher.datesearcher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {

    private EditText editText, bottom_editText;
    private ImageButton info, search, tomap; //정보보기, 검색, 지도에서보기
    private AlertDialog.Builder alert, alert_map, alert_research; //정보보기 다이얼로그, 지도 다이얼로그
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> adapter; // 다이얼로그 리스트 어댑터
    private Double lat, lng;

    private String aaa_keyword, wholetext_b;
    private RecyclerView myrv;
    private RecyclerViewAdapter myAdapter;
    private String[] ps_shortcode, ps_display_url, ps_liked, ps_wholeHashtag;
    private int loop_num = 0;
    private int post_list_num = 0;

    private List<Post> postList;
    private Handler set_post_handler, progress_handler, progress2_handler, research_handler, set_info_list_handler, set_tomap_list_handler;

    private String[] all_keyword;
    private InputStream is = null;
    private JSONObject JS;
    private JSONArray temp = null;
    private int hasnext_num = 0;
    private boolean null_check = false;
    String all_result, keyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();
        aaa_keyword = intent.getStringExtra("keyword");

        editText = (EditText) findViewById(R.id.search_top_editText);
        bottom_editText = (EditText) findViewById(R.id.search_bottom_editText);
        info = (ImageButton) findViewById(R.id.search_toinfo_btn);
        search = (ImageButton) findViewById(R.id.search_search_btn);
        tomap = (ImageButton) findViewById(R.id.search_toamp_btn);
        postList = new ArrayList<>();

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.search_linear);


        alert = new AlertDialog.Builder(SearchActivity.this);
        alert_map = new AlertDialog.Builder(SearchActivity.this);
        alert_research = new AlertDialog.Builder(SearchActivity.this);
        myrv = (RecyclerView) findViewById(R.id.search_recyclerView);
        myAdapter = new RecyclerViewAdapter(this, postList, bottom_editText, editText); //어댑터 설정

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        myrv.setLayoutManager(gridLayoutManager);//3행을 가진 그리드뷰로 레이아웃을 만듬
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(bottom_editText.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                set_list(1);
                    //Toast.makeText(SearchActivity.this, "정보가 없습니다.", Toast.LENGTH_LONG);
            }
        });

        tomap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set_list(2);
                //Toast.makeText(SearchActivity.this, "정보가 없습니다.", Toast.LENGTH_LONG);

            }
        });
        set_post_handler = new Handler() {
            public void handleMessage(Message msg) {
                myrv.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
            }
        };

        progress_handler = new Handler() {
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
            }
        };

        progress2_handler = new Handler() {
            public void handleMessage(Message msg) {
                progressDialog.show();
            }
        };

        research_handler = new Handler() {
            public void handleMessage(Message msg) {
                alert_research.setTitle("추가검색");
                alert_research.setMessage("검색된 게시물은 총 " + myrv.getAdapter().getItemCount() + "개 입니다. 추가검색 하시겠습니까?");
                alert_research.show();
            }
        };

        set_info_list_handler = new Handler() {
            public void handleMessage(Message msg) {
                alert.setTitle(keyword);
                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });

                Log.d("keyword", all_result.toString());
                String[] keyword_arr = all_result.split("#"); // 검색결과 종류별로 자르기

                if (keyword_arr.length == 1) {
                    null_check = true;
                }

                Double[] mapx = new Double[keyword_arr.length];
                Double[] mapy = new Double[keyword_arr.length];

                for (int i = 1; i < keyword_arr.length; ) {
                    int j = 1;
                    adapter.add(keyword_arr[i++]); //리스트에 추가하기
                    mapx[j] = Double.parseDouble(keyword_arr[i++]);
                    mapy[j] = Double.parseDouble(keyword_arr[i++]);
                    j++;
                    adapter.notifyDataSetChanged();
                }

                alert.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String keyword_adapter = adapter.getItem(which);
                        Intent intent = new Intent(SearchActivity.this, InfoActivity.class);
                        intent.putExtra("keyword", keyword_adapter);
                        startActivity(intent);
                    }
                });

                if(null_check)
                    Toast.makeText(getApplicationContext(), "정보가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                else
                    alert.show();
            }
        };

        set_tomap_list_handler = new Handler() {
            public void handleMessage(Message msg) {
                alert_map.setTitle(keyword);
                alert_map.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });

                Log.d("keyword", all_result.toString());
                String[] keyword_arr = all_result.split("#"); // 검색결과 종류별로 자르기

                if (keyword_arr.length == 1) {
                    null_check = true;
                }

                Double[] mapx = new Double[keyword_arr.length];
                Double[] mapy = new Double[keyword_arr.length];

                for (int i = 1; i < keyword_arr.length; ) {
                    int j = 1;
                    adapter.add(keyword_arr[i++]); //리스트에 추가하기
                    mapx[j] = Double.parseDouble(keyword_arr[i++]);
                    mapy[j] = Double.parseDouble(keyword_arr[i++]);
                    j++;
                    adapter.notifyDataSetChanged();
                }

                final Double[] mapx_x = mapx;
                final Double[] mapy_y = mapy;
                //Log.d("map2","---"+mapx_x[1]+"---"+mapy_y[1]);

                alert_map.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String keyword_adapter = adapter.getItem(which);
                        //네이버에 위치정보가 등록되어 있을 때
                        if (mapx_x[which + 1] != null) {
                            //카텍좌표계를 구글맵에서 쓰는 좌표계로 바꾸는 코드
                            GeoPoint oKA = new GeoPoint(mapx_x[which + 1], mapy_y[which + 1]);
                            GeoPoint oGeo = GeoTrans.convert(GeoTrans.KATEC, GeoTrans.GEO, oKA);
                            lat = oGeo.getY();
                            lng = oGeo.getX();

                            //출력테스트
                            Log.d("map", lng + "--" + lat);

                            ////////////////////////////////////////////////////////////////////////////////////////////////

                            Intent intent = new Intent(SearchActivity.this, TMapActivity.class);
                            intent.putExtra("lng", lng);
                            intent.putExtra("lat", lat);
                            intent.putExtra("keyword", keyword_adapter);
                            startActivity(intent);
                            ////////////////////////////////////////////////////////////////////////////////////////////////
                            //lng : 경도, lat : 위도
                        } else {
                            Intent intent = new Intent(SearchActivity.this, TMapActivity.class);
                            intent.putExtra("keyword", keyword_adapter);
                            startActivity(intent);
                        }
                    }
                });

                ////////////////////////////////////////////////////////////////////////////////////////////////
                //네이버에 위치정보가 등록이 되어있지 않을 때 키워드만 지도로 넘긴다.
                if(null_check) {
                    Intent intent = new Intent(SearchActivity.this, TMapActivity.class);
                    intent.putExtra("keyword", bottom_editText.getText().toString());
                    startActivity(intent);
                }
                else
                    alert_map.show();
            }
        };

        if (aaa_keyword.equals(null))
            editText.setText("");
        else
            editText.setText(aaa_keyword);

        all_keyword = aaa_keyword.split(" ");

        alert_research.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {
                            public void run() {

                                postList.clear();
                                post_list_num = 0;
                                loop_num = 0;

                                try {
                                    if (post_list_num < 10) {
                                        progress2_handler.sendEmptyMessage(0);
                                        Log.d("Instagram", "-----------------Instagram Start--------------");
                                        getInstaInfoFromKeyword(wholetext_b, all_keyword[0]);
                                        Log.d("Instagram", "-----------------Instagram Finish--------------");

                                        Log.d("Insert_Post", "-----------------Insert_Post Start--------------");
                                        insert_post(inputData());
                                        progress_handler.sendEmptyMessage(0);
                                        Log.d("Insert_Post", "-----------------Insert_Post Finish--------------");

                                        if (hasNextPage(wholetext_b))
                                            research_handler.sendEmptyMessage(0);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                });
        alert_research.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        progressDialog = ProgressDialog.show(this, "", "데이터를 불러오는 중...", true, true);

        new Thread() {
            public void run() {
                try {
                    Log.d("Instagram", "-----------------Instagram Start--------------");
                    Instagram(all_keyword[0]);
                    Log.d("Instagram", "-----------------Instagram Finish--------------");

                    Log.d("Insert_Post", "-----------------Insert_Post Start--------------");
                    insert_post(inputData());
                    progress_handler.sendEmptyMessage(0);
                    Log.d("Insert_Post", "-----------------Insert_Post Finish--------------");

                    if (post_list_num < 10 && hasNextPage(wholetext_b))
                        research_handler.sendEmptyMessage(0);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void Instagram(String keyword) {
        System.out.println("\n-----------------Instagram2 Start--------------\n");

        try {
            //request 헤더 추가
            System.out.println("\n-----------------Instagram Connect Start--------------\n");

            Document wholeCode = Jsoup.connect("https://www.instagram.com/explore/tags/" + keyword)
                    .header("Cookie",
                            "mid=XAqVqwAEAAGZOAcVNZl-1iaW2hSI; mcd=3; fbm_124024574287414=base_domain=.instagram.com; csrftoken=8wRooGW0R3EdPKuF76Nmyzqb490i6FkE; shbid=2382; ds_user_id=3451665929; sessionid=3451665929%3AMystH0H5Ph7o6y%3A23; shbts=1545307971.0121715; rur=FTW; fbsr_124024574287414=Dg6rT2oBQliaIDKxoQAr677vxeDw76BsZaVXY2vuFic.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUURPWFc1UHAzVWtwbndZRTYyODdjejZRZlA5bklCYU1aRndkZEZ0dkJicWJ2LXZPUW5Ka0NMdlczZVBGUkxfQVk5cWlwNDM1WFBwUDZGNFVGUlRIbWF0YjZ4QnE4TEJDX0NhU1U5R3dKR3RVdU5ON3ExZ01xLVlKeW9MR0EtZTVfUW5VVUpBaHRlWDk0NnpmMzlNc09CRzlGdElXRDVnNVcwdGRJSXlQdVFTVnZPZWl4cnJQZzlzaTNZZHM0RHczdVdTX2tXeXJKUWxCTUtSYkhha1BqM1hIWEpQMXMyYmdjekVXMWdwU1RndWM1aVlVTEJTeHd3bEtiUFlwSEMtVzFRNC0zMzRTRDFfS1RsNU1mRmgxQUUwUE15cGNWdG0waXZHUTNRcGx2SUtfYzJBYksyczVDNy1qNVExQ2lva2RoXy1VNHUtYk1uX0JNeUc1OEpHV0k0SCIsImlzc3VlZF9hdCI6MTU0NTM3NzU2MCwidXNlcl9pZCI6IjEwMDAwNDg2NTA1MDI4OCJ9; urlgen=\"{\\\"219.255.158.170\\\": 9318\\054 \\\"203.253.21.84\\\": 9318}:1gaFIX:nh07qkaELxfaw5kHAOJ1uKH_QcI\"")
                    .userAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                    .get(); // 링크 연결
//                    .header("Cookie",
//                            "shbid=2382; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; ds_user_id=3451665929; mid=W3ZeWwAEAAFxRTah2gMqSLyFoUIA; mcd=3; fbm_124024574287414=base_domain=.instagram.com; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; rur=FTW; sessionid=IGSCbe1f22d109edc3691bd08d36485f9e900cac1f8b62fb2e5cf10ded0f97814a81%3A0rPYcNUdl69GWVgZVmcTdaFpnvqx1eZs%3A%7B%22_auth_user_id%22%3A3451665929%2C%22_auth_user_backend%22%3A%22accounts.backends.CaseInsensitiveModelBackend%22%2C%22_auth_user_hash%22%3A%22%22%2C%22_platform%22%3A4%2C%22_token_ver%22%3A2%2C%22_token%22%3A%223451665929%3ATls7kBIRv15DZszC7jFBZUPtxc2cgfrT%3A3a4a3e68491efeb3c447a4201458f7f0edaf37fd82695bc319512ed45ae049bc%22%2C%22last_refreshed%22%3A1534945024.2404546738%7D; fbsr_124024574287414=Rsn_jkyzvupVXCogJy4SMsU7kSm-xvudkXbRjpG6-VA.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUUNEUHRXejduZUMwYXBCZDNHbVdseXFmc1lZQUQzNU01NHFKYVZDaWlJUEx3V3lhR3dYYVVMOXU1MDRvSUFta3puYVA2ZzdLVnplRmNzekNhZW9OYlJmclRCZ0tFbHFUYXpYVkZHUGRxdmFTV3BpVW1mSU90eGpTRkRwZ1hRLUE2NDVseXFoWFAwX2d1UDBzV3I0a0E5OUFfSFoyS2JfSEpFck9CTktNZlRMUmtuQlR2QUxSY01wdW5wdi05N2ZHRnBlMTI2dFNQdVFnX0E2SzFkMU1lajloVWpKa3JiUkt0U0dMSWhlQWRPSkZZODNITW1QV3dXYjlDakdxeFdhV3FkNUxfWms5dDJhOTc5ZUNtTl82N1kwUndtbzRvdGZQb1VoN2kwUUozcGFOZnI4R3dZanJVTlBhTzhHSTlQZFNteDE0ck5yVXdjS21HWFJ2RFZMVE9XRyIsImlzc3VlZF9hdCI6MTUzNDk1MTAzNCwidXNlcl9pZCI6IjEwMDAwNDg2NTA1MDI4OCJ9; shbts=1534951070.1531627; urlgen=\"{\\\"210.93.56.23\\\": 23668\\054 \\\"121.170.57.238\\\": 4766}:1fsUtK:jqGFF0QxE9hkyQhCd-66-w5izC0\"")
//                    .userAgent(
//                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
//                    .get(); // 링크 연결

            System.out.println("\n-----------------Instagram Connect finish--------------\n");

            Elements scriptCode = wholeCode.select("body > script:eq(1)");
            String wholeText = scriptCode.toString();
            System.out.println("\n-----------------Instagram2 finish--------------\n");

            getInstaInfoFromKeyword(wholeText, keyword);    //	정보를 수집하는 함수

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getInstaInfoFromKeyword(String wholeText, String keyword) throws Exception { // keyword로 첫번째 게시물페이지의 정보를 가져옴
        System.out.println("\n-----------------getInstanInfoFromKeyword Start--------------\n");

        wholetext_b = wholeText;

        String[] shortcode = wholeText.split("\"shortcode\":\""); // 특정패턴 검색	/ 전체 문자열에서 필요한 부분만 수집하기 위해서
        String shortcode__end = new String("\",\"edge_medi");

        String[] display_url = wholeText.split("\"display_url\":\"");    // ~부터
        String url__end = new String("\",\"edg");                            // ~까지 추출

        String[] liked = wholeText.split("\"edge_liked_by\":\\{\"count\":");
        String liked__end = new String("},");

        String[] contents = new String[1000];
        contents = wholeText.split("text\":\"");
        String contents__end = new String("\"}}]},\"");

        String[] timestamp = wholeText.split("\"taken_at_timestamp\":");
        String timestamp__end = new String(",\"dimensi");

        for (int i = 1; i < display_url.length; i++) {
            try {
                int shortcode_end = shortcode[i].indexOf(shortcode__end);
                if (shortcode_end > -1) {
                    shortcode[i] = shortcode[i].substring(0, shortcode_end);
                }

                int url_end = display_url[i].indexOf(url__end);
                if (url_end > -1) {
                    display_url[i] = display_url[i].substring(0, url_end);
                }

                int like_end = liked[i].indexOf(liked__end);
                if (like_end > -1) {
                    liked[i] = liked[i].substring(0, like_end + liked__end.length() - 2);
                }


                int contents_end = contents[i].indexOf(contents__end);
                if (contents_end > -1) {
                    contents[i] = contents[i].substring(0, contents_end);
                }
                deleteEmoji(contents);
                contents[i] = unicodeConvert(contents[i]);


                int timestamp_end = timestamp[i].indexOf(timestamp__end);
                if (timestamp_end > -1) {
                    timestamp[i] = timestamp[i].substring(0, timestamp_end);
                    // Date를 위해 import java.util.*;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                contents[i] = " ";
            }

        }

        for (int i = 1; i < display_url.length; i++) {
            insertData("" + liked[i], "" + shortcode[i], "" + display_url[i], "" + URLEncoder.encode("" + contents[i], "UTF-8"));
        }

        while (true) {
            if (loop_num > 3)
                break;
            loop_num++;
            Log.d("loop", "" + loop_num);
            Log.d("hasNextPage(wholeText)", "" + hasNextPage(wholeText));
            if (hasNextPage(wholeText)) { // 로드해야 할 게시물이 남았다면
                String cur = getEndCursor(wholeText); // 현재 불러온 게시물들의 마지막을 가리키는 커서를 수집한다.
                try {
                    wholeText = getNextPage(keyword, cur); // 키워드와 커서를 이용하여 다음 게시물의 페이지 소스코드를 가져온다.
                    getInstaInfoFromKeyword(wholeText, keyword); // 소스코드에서 shortcode, 이미지링크, 좋아요수를 수집한다.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //System.out.println("\n-----------------hasNextPage finish--------------\n");

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

    public void deleteEmoji(String[] contents) { // 크롤링한 게시물들의 이모티콘 부분을 공백으로 대치하는 부분
        for (int i = 1; i < contents.length; i++) {
            Pattern emoticons = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"); // 이모티콘의 정규식을 설정해서
            Matcher emoticonsMatcher = emoticons.matcher(contents[i]);    //	여기서 찾은 다음
            contents[i] = emoticonsMatcher.replaceAll(" ");    //	공백으로 대치
        }
    }

    public boolean hasNextPage(String wholeText) { // 게시물 페이지가 마지막인지 확인하는 함수
        boolean check;
        String[] hasNextPage = wholeText.split("has_next_page\":");    //	~부터
        String end = new String(",\"end_");                            //	~까지 추출
        int hasNextPage_end = hasNextPage[1].indexOf(end);
        if (hasNextPage_end > -1) {
            hasNextPage[1] = hasNextPage[1].substring(0, hasNextPage_end);
        }

        if (hasNextPage[1].equals("true")) {
            check = true;
        } else {
            check = false;
        }

        if (!check)
            hasnext_num = 1;

        return check;
    }

    public String getEndCursor(String wholeText) { // nextpage가 있을 때 커서를 확인하는 라인

        String[] endCursor = wholeText.split("\"end_cursor\":\"");
        String end = new String("\"},\"edges\":");
        int endCursor_end = endCursor[1].indexOf(end);
        if (endCursor_end > -1) {
            endCursor[1] = endCursor[1].substring(0, endCursor_end);
        }
        System.out.println("endCursor : " + endCursor[1]);
        if (endCursor[1].equals("null"))
            return "null";
        else
            return endCursor[1];
    }

    public String getNextPage(String keyword, String cur) throws Exception {    //	로드해야할 다음 페이지소스코드를 가져오는 함수
        String url = "https://www.instagram.com/graphql/query/?query_hash=faa8d9917120f16cec7debbd3f16929d&variables={\"tag_name\":\""
                + keyword + "\",\"first\":12,\"after\":\"" + cur + "\"}";
        // request 헤더를 포함시킴
        // System.out.println("++++++++++++++++" + url);

        System.out.println("\n-----------------getNextPage Connect Start--------------\n");

        Connection conn = Jsoup.connect(url)
                .header("Accept",
                        "*/*")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7").header("Connection", "keep-alive")
                .header("Cookie",
                        "mid=XAqVqwAEAAGZOAcVNZl-1iaW2hSI; mcd=3; fbm_124024574287414=base_domain=.instagram.com; csrftoken=8wRooGW0R3EdPKuF76Nmyzqb490i6FkE; shbid=2382; ds_user_id=3451665929; sessionid=3451665929%3AMystH0H5Ph7o6y%3A23; shbts=1545307971.0121715; rur=FTW; fbsr_124024574287414=Dg6rT2oBQliaIDKxoQAr677vxeDw76BsZaVXY2vuFic.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUURPWFc1UHAzVWtwbndZRTYyODdjejZRZlA5bklCYU1aRndkZEZ0dkJicWJ2LXZPUW5Ka0NMdlczZVBGUkxfQVk5cWlwNDM1WFBwUDZGNFVGUlRIbWF0YjZ4QnE4TEJDX0NhU1U5R3dKR3RVdU5ON3ExZ01xLVlKeW9MR0EtZTVfUW5VVUpBaHRlWDk0NnpmMzlNc09CRzlGdElXRDVnNVcwdGRJSXlQdVFTVnZPZWl4cnJQZzlzaTNZZHM0RHczdVdTX2tXeXJKUWxCTUtSYkhha1BqM1hIWEpQMXMyYmdjekVXMWdwU1RndWM1aVlVTEJTeHd3bEtiUFlwSEMtVzFRNC0zMzRTRDFfS1RsNU1mRmgxQUUwUE15cGNWdG0waXZHUTNRcGx2SUtfYzJBYksyczVDNy1qNVExQ2lva2RoXy1VNHUtYk1uX0JNeUc1OEpHV0k0SCIsImlzc3VlZF9hdCI6MTU0NTM3NzU2MCwidXNlcl9pZCI6IjEwMDAwNDg2NTA1MDI4OCJ9; urlgen=\"{\\\"219.255.158.170\\\": 9318\\054 \\\"203.253.21.84\\\": 9318}:1gaFIX:nh07qkaELxfaw5kHAOJ1uKH_QcI\"")
                .header("Host", "www.instagram.com")
                //.header("Upgrade-Insecure-Requests", "1")
                .userAgent(
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                .method(Connection.Method.GET).referrer("http://www.instagram.com").ignoreContentType(true);

        System.out.println("\n-----------------getNextPage Connect finish--------------\n");

        Document wholeCode = conn.get();
        System.out.println("\n-----------------getNextPage Connect finish1--------------\n");
        String wholeText = wholeCode.toString();
        System.out.println("\n-----------------getNextPage Connect finish2--------------\n");

        return wholeText;

    }

    private void insert_post(JSONArray temp) throws Exception {

        System.out.println("\n-----------------Insert_post Start--------------\n");
        String[] jsonName = {"likenum", "shortcode", "imageUrl", "wholeHashtag", "keyword"};
        String[][] parseData = new String[temp.length()][jsonName.length];

        System.out.println("\n-----------------JS Start--------------\n");
        for (int i = 0; i < temp.length(); i++) {
            JS = temp.getJSONObject(i);
            if (JS != null) {
                for (int j = 0; j < jsonName.length; j++) {
//                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!" + JS.getString(jsonName[j]));
                    parseData[i][j] = JS.getString(jsonName[j]);
                }
            }
        }
        System.out.println("\n-----------------JS finish--------------\n");

        System.out.println("\n-----------------JS2 Start--------------\n");
        ps_liked = new String[temp.length()];
        ps_shortcode = new String[temp.length()];
        ps_display_url = new String[temp.length()];
        ps_wholeHashtag = new String[temp.length()];

        for (int i = 0; i < temp.length(); i++) {
            if (JS != null) {
                for (int j = 0; j < jsonName.length; j++) {
//                    System.out.println("i : " + i + "j : " + j);
//                    System.out.println(parseData[i][j]);
                    if (j == 0)
                        ps_liked[i] = parseData[i][j];
                    else if (j == 1)
                        ps_shortcode[i] = parseData[i][j];
                    else if (j == 2)
                        ps_display_url[i] = parseData[i][j];
                    else if (j == 3)
                        ps_wholeHashtag[i] = parseData[i][j];
                }
            } else
                System.out.println("--------------!!!------------");
        }
        System.out.println("\n-----------------JS2 finish--------------\n");

        System.out.println("\n-----------------Glide Start--------------\n");

        for (int i = 0; i < ps_liked.length; i++) {
            Log.d("ps_i", "" + "postList" + "  " + post_list_num);
            post_list_num++;
            postList.add(new Post("" + ps_display_url[i], "좋아요♡" + ps_liked[i], "" + ps_wholeHashtag[i], "" + aaa_keyword));
            // System.out.println("\n!!!!!!!!!!!!!!!!!!!!!!!Image_URL   "+ps_display_url[i]);
        }

        set_post_handler.sendEmptyMessage(0);

        System.out.println("\n-----------------Glide finish--------------\n");
        System.out.println("\n-----------------Insert_post finish--------------\n");
    }

    private JSONArray inputData() throws IOException {
        System.out.println("\n-----------------inputData Start--------------\n");

        //sendData();
        HttpPost request = makeHttpPost("http://ryunha.cafe24.com/user_signup/sendData.php");
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpClient client = new DefaultHttpClient(params);
        ResponseHandler reshandler = new BasicResponseHandler();

        try {
            client.execute(request, reshandler);
            System.out.println("\nsendData_end");
        } catch (Exception e) {
            System.out.println("\nsendData_Exception");
            e.printStackTrace();
        }

        System.out.println("\ninputData_start");
        try {
            HttpResponse response = client.execute(request);
            HttpEntity responseResultEntity = response.getEntity();
            if (responseResultEntity != null) {
                is = responseResultEntity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                Log.d("result", sb.toString());
                String result = sb.toString();
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

    private void insertData(final String like, final String shortcode, final String imageUrl, final String wholeHashtag) throws IOException {
        // TODO Auto-generated method stub
        //System.out.println("\n-----------------InsertData Start--------------\n");
        new Thread() {

            public void run() {
                try {
                    Log.d("insertData_fun", shortcode);
                    HttpPost request = makeHttpPost(aaa_keyword, like, shortcode, imageUrl, wholeHashtag, "http://ryunha.cafe24.com/user_signup/insertData.php");
                    HttpClient client = new DefaultHttpClient();
                    ClientConnectionManager mgr = client.getConnectionManager();
                    HttpParams params = client.getParams();
                    client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
                    ResponseHandler reshandler = new BasicResponseHandler();
                    client.execute(request, reshandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


        //System.out.println("\n-----------------insertData finish--------------\n");

    }

    private HttpPost makeHttpPost(String surl) throws IOException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");
        Log.d("surl", surl);
        String[] keyword_arr = aaa_keyword.split(" ");
        String keyword_final = "";

        int i = 0;
        for (i = 0; i < keyword_arr.length - 1; i++) {
            keyword_final += "'%#" + keyword_arr[i] + "%'" + " AND wholeHashtag LIKE ";
        }
        keyword_final += "'%#" + keyword_arr[i] + "%'";

        Log.d("keyword", keyword_final);
        HttpPost request = new HttpPost(surl);
        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("keyword", URLEncoder.encode("" + keyword_final, "UTF-8")));
        request.setEntity(makeEntity(nameValue));
        System.out.println("\n-----------------makeHttpPost finish--------------\n");
        return request;
    }

    private HttpPost makeHttpPost(String keyword, String like, String shortcode, String imageUrl, String wholeHashtag, String url) throws UnsupportedEncodingException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");

        HttpPost request = new HttpPost(url);

        //System.out.println("---" + keyword + "----" + like + "----" + shortcode + "---" + imageUrl + "---" + wholeHashtag);
        //System.out.println("\n\n-----------keyword_final----------" + keyword_final);
        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("keyword", "" + URLEncoder.encode("" + keyword, "UTF-8")));
        Log.d("keyword2222", keyword);
        nameValue.add(new BasicNameValuePair("likenum", "" + like));
        nameValue.add(new BasicNameValuePair("shortcode", "" + shortcode));
        nameValue.add(new BasicNameValuePair("imageUrl", "" + imageUrl));
        nameValue.add(new BasicNameValuePair("wholeHashtag", "" + wholeHashtag));

        request.setEntity(makeEntity(nameValue));
        System.out.println("\n-----------------makeHttpPost finish--------------\n");

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

    public void onBackPressed() {
        this.finish();
    }


    public void onClick_search_search_btn_Listener(View view) {

        //가상키보드 숨기기
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("keyword", editText.getText().toString());
        this.finish();
        startActivity(intent);
    }

    private void set_list(final int check) {
        new Thread() {
            @Override
            public void run() {
                adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.select_dialog_singlechoice);
                keyword = bottom_editText.getText().toString();
                // 네이버검색결과 가져오기
                all_result = getNaverSearch(keyword);

                if (check==1){
                    set_info_list_handler.sendEmptyMessage(0);
                }
                else
                    set_tomap_list_handler.sendEmptyMessage(0);
            }
        }.start();
    }

    public String getNaverSearch(String keyword) {
        String clientID = "HyFl0TwQ0jA0PkzCxSmm";
        String clientSecret = "0ySwsnXgik";

        try {
            String text = URLEncoder.encode(keyword, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/local.xml?query=" + text; //키워드를 검색하기, xml파일로 받음
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
                        else if (tag.equals("title")) { //이름
                            xpp.next();
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("#");
                        } else if (tag.equals("mapx")) { //카텍좌표계 X
                            xpp.next();
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("#");
                        } else if (tag.equals("mapy")) { //카텍좌표계 Y
                            xpp.next();
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("#");
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
}
