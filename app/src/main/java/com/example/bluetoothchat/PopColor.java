package com.example.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
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

    Button btnconf;
    int colour;

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
        btnconf = (Button) findViewById(R.id.button_confirm2);
        btnconf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra("color", colour);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        Log.e("ree", "pause");
        Intent i = new Intent();
        i.putExtra("size", colour);
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
        i.putExtra("size", colour);
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
        int height = 900;
        lp.width = 900;
        lp.height = 900;
        getWindow().setLayout(width, height);
        //this.setFinishOnTouchOutside(true);
        PopupWindow attachmentPopup = new PopupWindow(this);
        attachmentPopup.showAsDropDown(findViewById(R.id.button_size), -50, 0);
    }
}
