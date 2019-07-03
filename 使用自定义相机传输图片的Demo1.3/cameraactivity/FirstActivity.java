package com.example.cameraactivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_first);
        Button button = findViewById(R.id.btn1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstActivity.this,SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            AlertDialog alert=new AlertDialog.Builder(FirstActivity.this).create();
            alert.setTitle("你确定退出本应用吗?");
            alert.setCancelable(false);
            //添加取消按钮
            alert.setButton(DialogInterface.BUTTON_NEGATIVE,"否",new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            //添加"确定"按钮
            alert.setButton(DialogInterface.BUTTON_POSITIVE,"是", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                }
            });
            alert.show();
        }
        return super.onKeyDown(keyCode, event);
    }

}
