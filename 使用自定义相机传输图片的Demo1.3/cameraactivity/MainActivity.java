package com.example.cameraactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {

    private SurfaceView mSurfaceView;
    private Socket socket;
    private Camera camera;
    private Button mtakePic;
    private final static String TAG="MainActivity001";
    private String host="";
    private final static int PORT = 4714;
    private final static int PORT2 = 5714;
    private File tempFile;
    private Handler handler;
    private Button mClose;
    private Socket socket2;
    private boolean flag=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        permmsion();
        findView();
        init();

    }


    private void permmsion(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
        }else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},1);
        }
    }
    private void findView() {
        mtakePic = findViewById(R.id.btn);
        mClose = findViewById(R.id.close);
        mSurfaceView  = findViewById(R.id.surfaceView);
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("请输入本机IP:")
                .setView(editText)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"已连接ip为:"+editText.getText().toString(),Toast.LENGTH_SHORT).show();
                        host = editText.getText().toString();
                    }
                })
                .show();
    }

    private void init() {
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().setKeepScreenOn(true);
        mSurfaceView.getHolder().addCallback(new MySurfaceViewCallback());
        mSurfaceView.setFocusable(true);
        mtakePic.setOnClickListener(this);
        mClose.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn){
            Toast.makeText(MainActivity.this,"开始上传",Toast.LENGTH_SHORT).show();
            handler= new Handler();
            handler.postDelayed(runnable1,3000);
            handler.postDelayed(runnable2,1000);
        }

        if(v.getId()==R.id.close){
            AlertDialog alert=new AlertDialog.Builder(MainActivity.this).create();
            alert.setTitle("是否返回转账界面?");
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
    }



    private class MySurfaceViewCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            initCamera();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if(success){
                        camera.cancelAutoFocus();
                    }
                }
            });
       }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            try {
                camera = Camera.open(1);
                initCamera();
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null){
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    }

    private void initCamera() {
        Camera.Parameters parameters = camera.getParameters();// 设置相机参数
        Camera.Size maxPictureSize = parameters.getSupportedPictureSizes().get(0);
        Camera.Size maxPreviewSize = parameters.getSupportedPreviewSizes().get(0);
        for (int i = 0; i < parameters.getSupportedPreviewSizes().size(); i++) {
            Camera.Size s = parameters.getSupportedPreviewSizes().get(i);
            if (s.width > maxPreviewSize.width) {
                maxPreviewSize = s;
            }
            if(s.width==maxPreviewSize.width&&s.height>maxPreviewSize.height){
                maxPreviewSize = s;
            }

        }
        for (int i = 0; i < parameters.getSupportedPictureSizes().size(); i++) {
            Camera.Size s = parameters.getSupportedPictureSizes().get(i);
            if (s.width > maxPictureSize.width) {
                maxPictureSize = s;
            }
            if(s.width==maxPictureSize.width&&s.height>maxPictureSize.height){
                maxPictureSize = s;
            }
        }
        parameters.setPictureSize(maxPictureSize.width,maxPictureSize.height);
        parameters.setPreviewSize(maxPreviewSize.width,maxPreviewSize.height);
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setJpegQuality(80);
        //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(parameters);
        //camera.cancelAutoFocus();
        camera.setDisplayOrientation(90);
        camera.startPreview();

    }

    private class MyPictureCallback implements Camera.PictureCallback{
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            FileOutputStream fos = null;
            tempFile = new File(getExternalCacheDir(),"output_image.jpg");
            try {
                fos = new FileOutputStream(tempFile);
                fos.write(data);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                camera.startPreview();
                if(fos!=null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            if(camera!=null){
                camera.takePicture(null,null,new MyPictureCallback());
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(flag==true){
                        try {
                            if(tempFile.exists()){
                                socket = new Socket(host, PORT);
                                //Log.d(TAG,socket.toString());
                                //读取图片文件并上传
                                FileInputStream fis = new FileInputStream(tempFile);
                                OutputStream os = socket.getOutputStream();
                                byte[] buff = new byte[5000];
                                int length = 0;
                                while((length=fis.read(buff))!=-1)
                                {
                                    os.write(buff,0,length);
                                }
                                socket.shutdownOutput();//关闭socket的输出流
                                //释放资源
                                fis.close();
                                socket.close();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    }
            }).start();
            handler.postDelayed(this,5000);
        }
    };

    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket2 = new Socket(host,PORT2);
                        if(socket2.isConnected()){
                            InputStream is = socket2.getInputStream();
                            byte[] readbuff= new byte[1024];
                            int length  = is.read(readbuff);
                            String message = new String(readbuff,0,length);
                            Log.d(TAG,message);
                            Looper.prepare();
                            Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
                            flag=false;
                            startActivity(new Intent(MainActivity.this,FirstActivity.class));
                            Looper.loop();

                            socket2.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            handler.postDelayed(this,1000);
        }
    };




}
