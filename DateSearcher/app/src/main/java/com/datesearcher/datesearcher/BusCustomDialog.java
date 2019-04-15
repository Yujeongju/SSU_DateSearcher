package com.datesearcher.datesearcher;

import android.app.Dialog;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class BusCustomDialog {

    private Context context;

    public BusCustomDialog(Context context){
        this.context = context;
    }



    public void callFunction(String busNum, String dialogRoute){
        final Dialog dlg = new Dialog(context);

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dlg.setContentView(R.layout.busroute_dialog);

        final TextView BusNumDial = (TextView)dlg.findViewById(R.id.bus_num_text_dialog);
        final TextView BusRoutesDial = (TextView)dlg.findViewById(R.id.bus_route_text_dialog);
        BusRoutesDial.setMovementMethod(new ScrollingMovementMethod()); // 스크롤바
        final Button okButton = (Button)dlg.findViewById(R.id.dialog_okButton);

        BusNumDial.setText(busNum);
        BusRoutesDial.setText(dialogRoute);

        dlg.show();

        okButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                dlg.dismiss();
            }
        });
    }

}