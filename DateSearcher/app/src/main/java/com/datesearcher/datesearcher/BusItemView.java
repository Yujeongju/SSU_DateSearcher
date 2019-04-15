package com.datesearcher.datesearcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BusItemView extends LinearLayout {
    TextView busNumber;
    TextView busRoute;

    public BusItemView(Context context){
        super(context);
        init(context);
    }
    private BusItemView(Context context, AttributeSet attrs){
        super(context,attrs);
        init(context);
    }
    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.bus_view_item, this, true);

        busNumber = (TextView) findViewById(R.id.Bus_Number);
        busRoute = (TextView) findViewById(R.id.Bus_Route);
    }
    void setBusNumber(String busNumber){ this.busNumber.setText(busNumber);}
    void setBusRoute(String busRoute){ this.busRoute.setText(busRoute);}

}
