package com.datesearcher.datesearcher;

import android.util.Log;

public class BusItem {
    private String busNumber;
    private String busRoute;
    private String dialogRoute;

    BusItem(String busNumber, String busRoute, String Route){
        this.busNumber = ""+busNumber;
        this.busRoute = ""+busRoute;
        this.dialogRoute = ""+ Route;
        Log.d("버스아이템 확인" ,"busNumber"+busNumber);
    }
    String getBusNumber(){ return busNumber; }
    String getBusRoute(){ return busRoute; }
    String getDialogRoute(){ return dialogRoute; }
    }

