package com.datesearcher.datesearcher;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

public class DataBase extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "myTable.db";
    public int check = 1;
    SQLiteDatabase database;

    public DataBase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create DB
        String sql = "create table myTable(id integer primary key autoincrement, Latitude DOUBLE, Longitude DOUBLE, Keyword VARCHAR(20));";
        database = db;
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "drop table mytable;"; // 테이블 드랍
        db.execSQL(sql);
        onCreate(db); // 다시 테이블 생성
    }

    public void insertData(double latitude, double longitude, String keyword){
        Log.i("keyword", "lan = " +latitude + "long = " + longitude);
        SQLiteDatabase db = getWritableDatabase();


        Log.i("제발 insert", " " + latitude +","+longitude);

        boolean result = checkDuplication(latitude, longitude, db);


        if(result == true) {
            db.execSQL("INSERT INTO myTable VALUES(null, '" + latitude + "', '" + longitude + "','" + keyword + "');");
            db.close();
            check = 1;
        }
        else if(result == false){
            check = 0;
        }
    }

    public void delete(double latitude, double longitude){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteDatabase database = getWritableDatabase();
        String keyword;
        double epsilon = 0.001;
        double dbLatitude, dbLongitude;
        Cursor cursor = db.rawQuery("SELECT * FROM myTable", null);
        while(cursor.moveToNext()){
            dbLatitude = cursor.getDouble(1);
            dbLongitude = cursor.getDouble(2);
            if((Math.abs(latitude - dbLatitude) <= epsilon)&&(Math.abs(longitude - dbLongitude) <= epsilon)){
                keyword = cursor.getString(3);
                database.execSQL("DELETE FROM myTable WHERE Latitude="+dbLatitude+ " and Longitude="+dbLongitude+";");
                db.close();
                database.close();
            }

        }

    }
    public void deleteAll(){ //database 내용 모두 삭제
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DELETE FROM myTable");
        database.close();
    }

    public LocationData[] getAllLocations(){ //
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM myTable", null);
        int dataCnt = getLocationCnt();
        LocationData[] locationData = new LocationData[dataCnt+1];
        double dbLatitude, dbLongitude;
        String keyword;
        //DB내용 가져오기
        int cnt = 0;
        if(dataCnt != 0) {
            while (cursor.moveToNext()) {

                dbLatitude = cursor.getDouble(1);
                dbLongitude = cursor.getDouble(2);
                keyword = cursor.getString(3);

                locationData[cnt] = new LocationData();

                locationData[cnt].latitude = dbLatitude;
                locationData[cnt].longitude = dbLongitude;
                locationData[cnt].keyword = keyword;

                cnt++;
            }
        }
        return locationData;
    }

    public int getLocationCnt(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Latitude, Longitude, Keyword FROM myTable", null);
        Log.i("위치개수", "in getLocationCnt");
        int dataCnt = cursor.getCount();
        Log.i("위치개수", ""+dataCnt);
        return dataCnt;
    }


    //중복 제거 함수
    public boolean checkDuplication(double latitude, double longitude, SQLiteDatabase db){
        db= getReadableDatabase();
        double dbLatitude, dbLongitude;
        latitude = Double.parseDouble(String.format("%.3f", latitude)); //위도 소수점 네자리까지 비교
        longitude = Double.parseDouble(String.format("%.3f", longitude)); //경도 소수점 네자리까지 비교
        Cursor cursor = db.rawQuery("SELECT * FROM myTable", null);
        while(cursor.moveToNext()){
            dbLatitude = cursor.getDouble(1);
            dbLongitude = cursor.getDouble(2);
            dbLatitude = Double.parseDouble(String.format("%.3f", dbLatitude)); //위도 소수점 네자리까지 비교
            dbLongitude = Double.parseDouble(String.format("%.3f", dbLongitude)); //경도 소수점 네자리까지 비교
            if((Double.compare(latitude,dbLatitude)==0)&&(Double.compare(longitude,dbLongitude)==0)){
                return false;
            }
        }
        return true;
    }

    public String getKeyword(double latitude, double longitude) {
        String getKeyword;
        SQLiteDatabase db = getReadableDatabase();
        String keyword = null;
        double epsilon = 0.001;
        double dbLatitude, dbLongitude;
        Cursor cursor = db.rawQuery("SELECT * FROM myTable", null);
        while (cursor.moveToNext()) {
            dbLatitude = cursor.getDouble(1);
            dbLongitude = cursor.getDouble(2);
            if ((Math.abs(latitude - dbLatitude) <= epsilon) && (Math.abs(longitude - dbLongitude) <= epsilon)) {
                keyword = cursor.getString(3);
            }
        }
        Log.i("키워드 젭알", " " + keyword);
        return keyword;
    }
}
