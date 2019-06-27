package com.example.cameraactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
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
    private File tempFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
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
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("请输入本机IP:")
                .setView(editText)
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

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn){
            Toast.makeText(MainActivity.this,"开始上传",Toast.LENGTH_SHORT).show();
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
