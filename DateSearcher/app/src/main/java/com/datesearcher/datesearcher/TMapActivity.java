package com.datesearcher.datesearcher;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;

public class TMapActivity extends AppCompatActivity implements TMapView.OnLongClickListenerCallback{

    private RelativeLayout mapView;
    private TMapView tMapView;
    private Geocoder geocoder; //지오코더 -> 좌표에서 장소를, 장소에서 좌표를 알 수 있음
    private String apiKey = "0c11bf4e-c6ea-4b64-8407-bed690cbe366";
    private double latitude, longitude; //위도와 경도
    private String keyword; // 키워드
    private int check = 0; // 메인에서 시작했는지 아닌지 확인하는 변수
    private Bitmap bitmap;
    private DataBase dbHelper;
    private ImageButton deleteBtn, busBtn;
    private String getKeyword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmap);

        dbHelper = new DataBase(getApplicationContext(), "myTable.db", null, 1);

        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.poi_dot);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("lat", 0); // 다른 액티비티에서 lat을 받아옴. 안 받는다면 0
        longitude = intent.getDoubleExtra("lng", 0); // 다른 액티비티에서 lng를 받아옴. 안 받는다면 0
        keyword = intent.getStringExtra("keyword");
        geocoder = new Geocoder(this);

        Log.i("키워드 정보", "keyword" + keyword);

        try {
            check = intent.getIntExtra("check", 0); //메인에서 시작했다면 1, 아니라면 0 keyword = intent.getStringExtra("keyword"); // 키워드를 인텐트로 받음
        } catch (NullPointerException e) {
            keyword = "정보없음"; // 메인에서 시작할 경우 keyword가 null이기 때문에 익셉션 처리
        }

        mapView = findViewById(R.id.Tmap);
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey(apiKey);
        mapView.addView(tMapView);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);
        tMapView.setCompassMode(true);
        tMapView.setIconVisibility(true);
        if(dbHelper.getLocationCnt() != 0) {
            tMapView.removeAllMarkerItem();
            tMapView.removeAllTMapPolyLine();
            drawPath();
        }
        setMarkers();

        deleteBtn = (ImageButton) findViewById(R.id.DeleteBtn);
        busBtn = (ImageButton) findViewById(R.id.BusBtn);

        busBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // todo 버스 부르는 부분
                if(dbHelper.getLocationCnt() > 1){
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            TMapActivity.this,
                            android.R.layout.select_dialog_singlechoice);
                    int cnt = dbHelper.getLocationCnt();
                    final LocationData[] locationData = dbHelper.getAllLocations();

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                            TMapActivity.this);
                    alertBuilder.setTitle("경로를 선택하세요");

                    alertBuilder.setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            });


                    for(int i=0;i<cnt-1;i++){
                        String getPath =  locationData[i].keyword + "->" + locationData[i+1].keyword;
                        adapter.add(getPath);
                    }

                    // Adapter 셋팅
                    alertBuilder.setAdapter(adapter,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    String strName = adapter.getItem(id);

                                    Intent intent = new Intent(TMapActivity.this, BusActivity.class);
                                    try{
                                        intent.putExtra("startYpos", locationData[id].latitude);
                                        intent.putExtra("startXpos", locationData[id].longitude);
                                        intent.putExtra("startKeyword", locationData[id].keyword);
                                        intent.putExtra("endYpos", locationData[id+1].latitude);
                                        intent.putExtra("endXpos", locationData[id+1].longitude);
                                        intent.putExtra("endKeyword", locationData[id+1].keyword);
                                        startActivity(intent);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }


                                }
                            });
                    alertBuilder.show();


                }
                else if(dbHelper.getLocationCnt() == 1){
                    Toast.makeText(TMapActivity.this,"경로가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper.getLocationCnt() > 0) {
                    dbHelper.deleteAll();
                    tMapView.removeAllMarkerItem();
                    tMapView.removeAllTMapPolyLine();
                    Toast.makeText(TMapActivity.this, "경로를 모두 삭제하였습니다. ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (check == 1) {
            tMapView.moveToZoomPosition(126.97, 37.56);
            tMapView.setZoomLevel(15);

        } else if (longitude == 0 && latitude == 0) {
            List<Address> addressList = new ArrayList<>();//주소를 담을 리스트
            try {
                Log.d("geo","asd"+geocoder.getFromLocationName(keyword,10).toString());
                addressList = geocoder.getFromLocationName(keyword, 10); // 지오코드로 키워드의 좌표를 가져옴. 10 : 최대 검색 결과 개수
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addressList != null) {
                //좌표가 없다면
                if (addressList.size() == 0) {
                    Toast.makeText(this, "해당되는 주소가 없습니다.", Toast.LENGTH_LONG).show();
                }
                //좌표가 있다면
                else {
                    // 해당되는 주소에 마커 찍기


                    Address addr = addressList.get(0);
                    latitude = addr.getLatitude();
                    longitude = addr.getLongitude();

                    TMapMarkerItem marker = new TMapMarkerItem();
                    TMapPoint tMapPoint = new TMapPoint(latitude, longitude);
                    marker.setIcon(bitmap); // 마커 아이콘 지정
                    marker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    marker.setTMapPoint(tMapPoint); // 마커의 좌표 지정
                    marker.setName(keyword); // 마커의 타이틀 지정
                    tMapView.addMarkerItem("marker", marker); // 지도에 마커 추가
                    tMapView.moveToZoomPosition(longitude, latitude); //지도 마커 중심으로 이동
                    tMapView.setCenterPoint(longitude, latitude, false);
                    tMapView.setLocationPoint(longitude, latitude);
                    marker.setCalloutTitle(keyword); //키워드 받아서 키워드로 대체해줌
                    marker.setCanShowCallout(true);
                    marker.setAutoCalloutVisible(true);
                }

            }

        } else {

            TMapMarkerItem tMarkerItem = new TMapMarkerItem();
            TMapPoint tMapPoint = new TMapPoint(latitude, longitude);
            tMarkerItem.setIcon(bitmap); // 마커 아이콘 지정
            tMarkerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            tMarkerItem.setTMapPoint(tMapPoint); // 마커의 좌표 지정
            tMarkerItem.setName(keyword); // 마커의 타이틀 지정
            tMapView.addMarkerItem("tmarkerItem", tMarkerItem); // 지도에 마커 추가
            tMapView.moveToZoomPosition(longitude, latitude); //지도 마커 중심으로 이동 !!이거 지금 안되는 듯!!
            tMapView.setCenterPoint(longitude, latitude, false);
            tMapView.setLocationPoint(longitude, latitude);
            tMarkerItem.setCalloutTitle(keyword); //키워드 받아서 키워드로 대체해줌
            tMarkerItem.setCanShowCallout(true);
            tMarkerItem.setAutoCalloutVisible(true);
            tMapView.setOnLongClickListenerCallback(this);
        }



    }


    @Override
    public void onLongPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint) {
        if(arrayList.isEmpty() == FALSE) {//롱클릭이 일어난 장소가 마커라면
            showDialog(tMapPoint);

        }
        else{
            Log.i("이벤트 객체", "NOT MARKER"); }
    }

    public void showDialog(final TMapPoint tPoint) {
        final String[] dialogs = new String[]{"경로추가", "경로삭제", "장소검색"};

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("경로");
        alertDialog.setItems(
                dialogs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double latitude, longitude; //위도 경도
                        latitude = tPoint.getLatitude();
                        longitude = tPoint.getLongitude();

                        Log.i("위치정보확인" , "Lat = " + latitude + " Lng = " + longitude);

                        if (which == 0) {//경로추가
                            //내부 DB에 경로 추가
                            dbHelper.insertData(latitude,longitude,keyword);
                            Log.i("제발", " " + latitude +","+longitude);
                            if(dbHelper.check == 1){
                                Toast.makeText(TMapActivity.this, "경로가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                                tMapView.removeAllMarkerItem();
                                setMarkers();
                                if(dbHelper.getLocationCnt() >= 2) {
                                    tMapView.removeAllTMapPolyLine();
                                    drawPath();
                                }
                            }
                            Log.i("위치개수 CNT = ", "" + dbHelper.getLocationCnt());
                            if(dbHelper.check ==0) //중복된 경로 확인
                                Toast.makeText(TMapActivity.this, "이미 추가된 경로입니다.", Toast.LENGTH_SHORT).show();
                        } else if (which == 1) {//경로삭제
                            Toast.makeText(TMapActivity.this, "경로가 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                            Log.i("위치개수 CNT = ", "" + dbHelper.getLocationCnt());

                            if(dbHelper.getLocationCnt() >= 2){
                                tMapView.removeAllTMapPolyLine();
                            }
                            dbHelper.delete(latitude, longitude);
                            if(dbHelper.getLocationCnt() >= 2){
                                drawPath();
                            }
                            tMapView.removeAllMarkerItem();
                            setMarkers();
                            tMapView.setLocationPoint(longitude, latitude);

                            //내부 DB에 현재 경로 삭제
                        } else {//장소검색
                            String getKey = dbHelper.getKeyword(latitude, longitude);
                            Toast.makeText(TMapActivity.this, getKey, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(TMapActivity.this, InfoActivity.class);
                            intent.putExtra("keyword", getKey);
                            startActivity(intent);
                            //Info로 넘겨주기
                        }

                    }
                }
        );
        alertDialog.setNeutralButton("닫기", null).show();
    }

    public void setMarkers() {
        int dataCnt = dbHelper.getLocationCnt();
        LocationData[] allLocation = dbHelper.getAllLocations();

        for (int i = 0; i < dataCnt; i++) {
            TMapMarkerItem markerItem = new TMapMarkerItem();
            double lat = allLocation[i].latitude;
            double lng = allLocation[i].longitude;
            String keyword = allLocation[i].keyword;
            Log.i("Location " , "Lat = " + lat +"lng = " + lng);
            TMapPoint location = new TMapPoint(lat, lng);
            markerItem.setIcon(bitmap); // 마커 아이콘 지정
            markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem.setTMapPoint(location); // 마커의 좌표 지정
            markerItem.setName(keyword); // 마커의 타이틀 지정
            tMapView.addMarkerItem("markerItem" + i, markerItem); // 지도에 마커 추가
            markerItem.setCalloutTitle(keyword); //키워드 받아서 키워드로 대체해줌
            markerItem.setCanShowCallout(true);
            markerItem.setAutoCalloutVisible(true);
            tMapView.setOnLongClickListenerCallback(this);

        }
    }

    public void drawPath(){ //보행자 경로 그리기
        int dataCnt = dbHelper.getLocationCnt();
        Log.i("위치", " " + dataCnt);
        int i;
        LocationData[] allLocation = dbHelper.getAllLocations();
        TMapData tmapdata = new TMapData();

        if(dataCnt >= 2) {
            for (i = 0; i < dataCnt - 1; i++) {
                TMapPoint tMapPointStart = new TMapPoint(allLocation[i].latitude, allLocation[i].longitude); // (출발지)
                TMapPoint tMapPointEnd = new TMapPoint(allLocation[i+1].latitude, allLocation[i+1].longitude); // (목적지)

                try {
                    final int finalI = i;
                    tmapdata.findPathDataWithType(
                            TMapData.TMapPathType.PEDESTRIAN_PATH,
                            tMapPointStart, tMapPointEnd,
                            new TMapData.FindPathDataListenerCallback() {
                                @Override
                                public void onFindPathData(TMapPolyLine polyLine) {
                                    polyLine.setLineColor(Color.RED);
                                    tMapView.addTMapPolyLine("Line" + finalI, polyLine);
                                }
                            });

                } catch (Exception e) {
                    Toast.makeText(this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show(); }

            }
        }

    }

}
