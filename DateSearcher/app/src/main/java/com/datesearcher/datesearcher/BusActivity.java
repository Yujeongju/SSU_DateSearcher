package com.datesearcher.datesearcher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


public class BusActivity extends AppCompatActivity {
    //  좌표를 넘겨받아서 버스 정보 출력
    String arr[];
    String Route;
    String serviceKey = "7qY9R6SPmblJyJyl9Hm4yLMLIV3LMRcbjTkkAGIVlReRfyjKJbI1wU1Ae39WjB5g2v2kD15W6zbZ0wlEjq7gyA%3D%3D";
    String StartStation, EndStation;
    private Handler set_busitem_handler, set_BusStation_list_handler;
    BusAdapter adapter;
    private ArrayAdapter<String> dialogAdapter;

//다이얼로그    private AlertDialog.Builder alert_Bus;
    BusCustomDialog busCustomDialog = new BusCustomDialog(BusActivity.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);

        final ListView listView = (ListView) findViewById(R.id.bus_list);
        final TextView busKeyword = (TextView) findViewById(R.id.bus_keyword);
        busKeyword.setSelected(true);
        adapter = new BusAdapter();

//다이얼로그    alert_Bus = new AlertDialog.Builder(BusActivity.this);


//        adapter.addItem(new BusItem("752", "숭실대입구 -> 이수역"));
//        adapter.addItem(new BusItem("751", "숭실대입구 -> 이수역"));
//        adapter.addItem(new BusItem("753", "숭실대입구 -> 이수역"));

//        listView.setAdapter(adapter);
        set_busitem_handler = new Handler(){    //  서브스레드에서 넘어온 값을 리스트에 띄운다.
            public void handleMessage(Message msg){
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        };

        Intent intent = getIntent();
        final double startXpos = intent.getDoubleExtra("startXpos", 0);
        final double startYpos = intent.getDoubleExtra("startYpos", 0);
        final String startKeyword = intent.getStringExtra("startKeyword");
        final double endXpos = intent.getDoubleExtra("endXpos", 0);
        final double endYpos = intent.getDoubleExtra("endYpos", 0);
        final String endKeyword = intent.getStringExtra("endKeyword");
        Log.d("endkeyword ", endKeyword+"123");
        //todo 중복되는 버스가 없을 때 처리.
        busKeyword.setText(startKeyword + " -> " + endKeyword);

        Log.d("test",startXpos+" asd1 "+startYpos);
        new Thread() {
            @Override
            public void run() {
                  arr = BusSearchProcess(startXpos, startYpos, endXpos, endYpos);
                  for(int i=1; i<arr.length; i=i+2){
                      System.out.println("BusItem"+arr[i]);
                      Route = getStationByRouteList(Integer.parseInt(arr[0]));  // 경로를 따온다.
                      adapter.addItem(new BusItem(arr[i],StartStation+" -> "+EndStation, Route));
                      //todo 버스번호는 완료했고, 출발지랑 목적지 넣어주고, 다이얼로그!
                  }
                  set_busitem_handler.sendEmptyMessage(0);


//                Log.d("버스정류장 고유번호",getStationByPosList(0,0 ));
//                Log.d("지나가는 버스번호",getRouteByStationList(0));
//                Log.d("버스의 노선정보",getStationByRouteList(0));
            }
        }.start();



        listView.setAdapter(adapter);

    }
    private String[] BusSearchProcess(double X_pos1, double Y_pos1, double X_pos2, double Y_pos2){
        // test 숭실대학교
//        X_pos1 = 126.956881;
//        Y_pos1 =  37.495064;
//        X_pos2 = 126.960129;
//        Y_pos2 =  37.495008;

        StringBuffer sb = new StringBuffer();//  버스정류장 정보를 저장하는 배열
        String[] BusStation1; // 짝수 인덱스에는 ID, 홀수 인덱스에는 정류소명
        String[] BusStation2;
        String[] BusNum1;     // 짝수 인덱스에는 ID, 홀수 인덱스에는 버스번호명
        String[] BusNum2;
        ArrayList<String> BusNum_final = new ArrayList();
        Iterator iterator = BusNum_final.iterator();

        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ첫번째 좌표의 버스정류장ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
        Log.d("test",X_pos1+" asd1 "+Y_pos1);
        sb.append(getStationByPosList(X_pos1,Y_pos1));  // 좌표를 이용해 주변 정류장을 조회한다.
        BusStation1 = sb.toString().split("#");
        StartStation = BusStation1[1];

        sb.delete(0,sb.capacity());
        sb.append(getRouteByStationList(Integer.parseInt(BusStation1[0])));//  지나가는 버스를 조회한다.

//        Log.d("BusNum1",sb.toString());
        BusNum1 = sb.toString().split("#");

        sb.delete(0,sb.capacity());

        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ두번째 좌표의 버스정류장ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
        sb.append(getStationByPosList(X_pos2,Y_pos2));
        BusStation2 = sb.toString().split("#");
        EndStation = BusStation2[1];
//        StartStation = BusStation2[1];

        sb.delete(0,sb.capacity());
        sb.append(getRouteByStationList(Integer.parseInt(BusStation2[0])));

//        Log.d("BusNum2",sb.toString());
        BusNum2 = sb.toString().split("#");

        sb.delete(0,sb.capacity());

        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ중복 검사ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ //

        for(int i=0; i<BusNum1.length; i++) {
//            Log.d("BusNum1", BusNum1[i]);
            for (int j = 0; j<BusNum2.length; j++) {
//                Log.d("BusNum2",BusNum2[j]);
                  Log.d("중복을 확인합니다. ", BusNum1[i]+BusNum2[j]);
                if (BusNum1[i].equals(BusNum2[j])) {  // 중복되는 버스 번호 검사
                    BusNum_final.add(BusNum1[i]);
                    Log.d("중복", "중복입니다.");
                }
//                else {
//                    System.out.println("중복이 아닙니다");
//                }
            }
        }
//        for(int k=0; k<BusNum_final.size(); k++) {
//            Log.d("중복되는버스값",BusNum_final.get(k).toString());
//        }
        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ버스 노선 출력ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

        String[] arr = BusNum_final.toArray(new String[0]);
//        System.out.println("size"+BusNum_final.size());
//        for(int k=0; k<BusNum_final.size(); k=k+2) {
//            getStationByRouteList(Integer.parseInt(arr[k]));
//            Log.d("test",""+k);
//        }

        return arr;
    }


    private class BusAdapter extends BaseAdapter {
        private ArrayList<BusItem> items = new ArrayList();

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) { return position; }


        @Override
        // 생성되는 n개의 리스트 수 만큼 실행되어 각각의 위젯을 참조하고 설정.
        public View getView(int position, View convertView, ViewGroup parent) {
            BusItemView view = (BusItemView)convertView;

//            final TextView BusNumItem = (TextView)convertView.findViewById(R.id.Bus_Number);
//todo 이게 왜 안되는 지 알아봐야할 것 같다. 일단 다이얼로그는 뜨고, 다이얼로그에 들어가는 값을 설정해줄 수 있게 매개변수를 전달하는게 있어야할것같다.
            if(convertView == null) //화면을 벗어나는 뷰가 없다면
                view = new BusItemView(getApplicationContext());

            final BusItem item = items.get(position);
            view.setBusNumber(item.getBusNumber());
            view.setBusRoute(item.getBusRoute());

            view.busNumber.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    busCustomDialog.callFunction(item.getBusNumber(),item.getDialogRoute());
                }
            });

            return view;
        }
        void addItem(BusItem item) { items.add(item); }
    }
    private String getStationByPosList(double X_pos, double Y_pos) {   //  getStationByPosList 근접 버스정류장 탐색
        //test 숭실대학교
//        X_pos = 126.957052;
//        Y_pos =  37.495162;

        //String serviceKey = "mOzOsjLk6iyQgNu2NmDIgB7Dgk%2FYphf0JJZYjBnsshnXPxjnO4wE81eSE6S2s5q%2BYgSAxybyCKaC7LE8xYRJ7A%3D%3D";
        double radius = 100;    //  반경, 거리M
        String CallBackURL = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByPos";
        CallBackURL = CallBackURL + "?ServiceKey=" + serviceKey + "&tmX=" + X_pos + "&tmY=" + Y_pos + "&radius=" + radius;
        Log.d("CallBackURL", CallBackURL);

        try {
            StringBuffer sb = new StringBuffer();   //  버퍼 생성

            URL url = new URL(CallBackURL); //  웹 주소 저장
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  //  웹 접근

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            String tag;
            xpp.setInput(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            xpp.next(); //  다음 태그
            int eventType = xpp.getEventType(); // EventType을 획득 // 시작인지, 끝인지 판별하기 위함.

            while (eventType != XmlPullParser.END_DOCUMENT) {   //   끝이 아니라면
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();

                        if (tag.equals("arsId")) { // 버스정류장고유번호
                            xpp.next();
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("#");
                        }
                        else if (tag.equals("stationNm")) { // 버스정류장이름
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
            Log.d("exception", ""+e);
            return e.toString();
        }
    }

    private String getRouteByStationList(int arsId){ //  정류소 고유번호를 입력받아 노선 출력
        //test 숭실대학교
//        arsId = 20169;
        String CallBackURL = "http://ws.bus.go.kr/api/rest/stationinfo/getRouteByStation";
        CallBackURL = CallBackURL + "?ServiceKey=" + serviceKey + "&arsId=" + arsId;
        Log.d("CallBackURL", CallBackURL);

        try {
            StringBuffer sb = new StringBuffer();   //  버퍼 생성

            URL url = new URL(CallBackURL); //  웹 주소 저장
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  //  웹 접근

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            String tag;
            xpp.setInput(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            xpp.next(); //  다음 태그
            int eventType = xpp.getEventType(); // EventType을 획득 // 시작인지, 끝인지 판별하기 위함.

            while (eventType != XmlPullParser.END_DOCUMENT) {   //   끝이 아니라면
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();

                        if (tag.equals("busRouteId")) { // 버스고유아이디
                            xpp.next();
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("#");
                        }
                        else if (tag.equals("busRouteNm")) { // 버스번호이름
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

    private String getStationByRouteList(int busRouteId){ //  노선별 경유 정류소 조회 서비스
        //test 숭실대학교
        //busRouteId = 100100116;
        String CallBackURL = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute";
        CallBackURL = CallBackURL + "?ServiceKey=" + serviceKey + "&busRouteId=" + busRouteId;
        Log.d("RouteListURL", CallBackURL);

        try {
            StringBuffer sb = new StringBuffer();   //  버퍼 생성

            URL url = new URL(CallBackURL); //  웹 주소 저장
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  //  웹 접근

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            String tag;
            xpp.setInput(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            xpp.next(); //  다음 태그


            int eventType = xpp.getEventType(); // EventType을 획득 // 시작인지, 끝인지 판별하기 위함.

            while (eventType != XmlPullParser.END_DOCUMENT) {   //   끝이 아니라면
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();

                        if (tag.equals("stationNm")) { // 정류소이름
                            xpp.next();
                            sb.append(xpp.getText().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
                            sb.append("\n");
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
