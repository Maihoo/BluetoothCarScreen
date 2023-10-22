package com.example.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;

public class PopColor extends Activity {

    Button btn10, btn20, btn30, btn40, btn50, btnconf;
    int colour;
    int size = 60;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupcolor);

        ColorPickerView colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(int color, boolean fromUser) {
                colour = color;
            }
        });

        btn10 = (Button) findViewById(R.id.button_10c);
        btn20 = (Button) findViewById(R.id.button_20c);
        btn30 = (Button) findViewById(R.id.button_30c);
        btn40 = (Button) findViewById(R.id.button_40c);
        btn50 = (Button) findViewById(R.id.button_50c);

        btn10.setBackgroundColor(Color.GRAY);
        btn20.setBackgroundColor(Color.GRAY);
        btn30.setBackgroundColor(Color.WHITE);
        btn40.setBackgroundColor(Color.GRAY);
        btn50.setBackgroundColor(Color.GRAY);

        btn10.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                size = 20;
                btn10.setBackgroundColor(Color.WHITE);
                btn20.setBackgroundColor(Color.GRAY);
                btn30.setBackgroundColor(Color.GRAY);
                btn40.setBackgroundColor(Color.GRAY);
                btn50.setBackgroundColor(Color.GRAY);
            }
        });
        btn20.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                size = 40;
                btn10.setBackgroundColor(Color.GRAY);
                btn20.setBackgroundColor(Color.WHITE);
                btn30.setBackgroundColor(Color.GRAY);
                btn40.setBackgroundColor(Color.GRAY);
                btn50.setBackgroundColor(Color.GRAY);
            }
        });
        btn30.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                size = 60;
                btn10.setBackgroundColor(Color.GRAY);
                btn20.setBackgroundColor(Color.GRAY);
                btn30.setBackgroundColor(Color.WHITE);
                btn40.setBackgroundColor(Color.GRAY);
                btn50.setBackgroundColor(Color.GRAY);
            }
        });
        btn40.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                size = 80;
                btn10.setBackgroundColor(Color.GRAY);
                btn20.setBackgroundColor(Color.GRAY);
                btn30.setBackgroundColor(Color.GRAY);
                btn40.setBackgroundColor(Color.WHITE);
                btn50.setBackgroundColor(Color.GRAY);
            }
        });
        btn50.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                size = 100;
                btn10.setBackgroundColor(Color.GRAY);
                btn20.setBackgroundColor(Color.GRAY);
                btn30.setBackgroundColor(Color.GRAY);
                btn40.setBackgroundColor(Color.GRAY);
                btn50.setBackgroundColor(Color.WHITE);
            }
        });

        btnconf = (Button) findViewById(R.id.button_confirmc);
        btnconf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra("color", "" + colour + ":" + (size));
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        Log.e("ree", "pause");
        Intent i = new Intent();
        i.putExtra("color", "" + colour + ":" + (size));
        setResult(RESULT_OK, i);
        finish();
        super.onPause();
    }/*

    @Override
    protected void onDestroy() {
        Log.e("ree", "destroy");
        Intent i = new Intent();
        i.putExtra("size", colour);
        setResult(RESULT_OK, i);
        finish();
        super.onDestroy();

    }

    @Override
    protected void onStop() {
        Log.e("ree", "stop");
        Intent i = new Intent();
        i.putExtra("size", colour);
        setResult(RESULT_OK, i);
        finish();
        super.onStop();

    }*/

    @Override
    public void onBackPressed() {
        Log.e("ree", "back");
        Intent i = new Intent();
        i.putExtra("color", "" + colour + ":" + (size));
        setResult(RESULT_OK, i);
        super.onBackPressed();
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        View view = getWindow().getDecorView();
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
        lp.gravity = Gravity.BOTTOM;
        getWindowManager().updateViewLayout(view, lp);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int width = dm.widthPixels;
        int height = 1200;
        lp.width = 900;
        lp.height = 1200;
        getWindow().setLayout(width, height);
        //this.setFinishOnTouchOutside(true);
        PopupWindow attachmentPopup = new PopupWindow(this);
        attachmentPopup.showAsDropDown(findViewById(R.id.button_color), -50, 0);
    }
}
