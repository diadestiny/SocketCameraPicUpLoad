package com.example.cameraactivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private SurfaceView mSurfaceView;
    private Socket socket;
    private Camera camera;
    private Button mtakePic;
    private String TAG="MainActivity001";
    private final static String HOST="192.168.43.142";
    private final static int PORT = 4714;
    private File tempFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        permmsion();
        findView();
        init();
        mtakePic.setOnClickListener(this);
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
        mSurfaceView  = findViewById(R.id.surfaceView);
    }

    private void init() {
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().setKeepScreenOn(true);
        mSurfaceView.getHolder().addCallback(new MySurfaceViewCallback());

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn){
            Timer timer =new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(camera!=null){
                        camera.takePicture(null,null,new MyPictureCallback());
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(tempFile.exists()){
                                    socket = new Socket(HOST, PORT);
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
                                    Log.d(TAG,"test");

                                    socket.shutdownOutput();//关闭socket的输出流
                                    //读取响应数据
                                    InputStream is =socket.getInputStream();
                                    byte[] readbuff= new byte[5000];
                                    length  = is.read(readbuff);
                                    String message = new String(readbuff,0,length);

                                    //释放资源
                                    fis.close();
                                    socket.close();
                                }
                                else{

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            },0,5000);
        }
    }

    private class MySurfaceViewCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            initCamera();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open(1);
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
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setJpegQuality(50);

        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
        camera.startPreview();
        //camera.cancelAutoFocus();
    }



    private class MyPictureCallback implements Camera.PictureCallback{
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            FileOutputStream fos = null;
            tempFile = new File(getExternalCacheDir(),"output_image.jpg");
            //Log.d(TAG,getExternalCacheDir().toString());
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

}
