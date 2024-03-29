package com.hanyuzhou.testchat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final int TAKE_PHOTO = 1 ;
    private static final String TAG = "MainActivity001";
    private String HOST = "";
    private static final int POST = 4714;
    private int flag=0;
    private ImageView picture;
    private Button mSend;
    private Button mTakePhoto;
    private EditText editText;
    private Socket socket;
    private Uri imageuri;
    private File outputImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }
    private void initUI(){
        mSend = (Button) this.findViewById(R.id.send);
        mTakePhoto = this.findViewById(R.id.take_photo);
        picture = this.findViewById(R.id.picture);
        editText=this.findViewById(R.id.edit);
        mSend.setOnClickListener(this);
        mTakePhoto.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.take_photo){
            flag = 1;
            outputImage = new File(getExternalCacheDir(),"output_image.jpg");
            try {
                if(outputImage.exists()){
                    outputImage.delete();
                }
                outputImage.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(Build.VERSION.SDK_INT>=24){
                imageuri = FileProvider.getUriForFile(MainActivity.this,"com.example.cameraalbumtest.fileprovider", outputImage);
            }else{
                imageuri = Uri.fromFile(outputImage);
            }
            //启动相机程序
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri);
            startActivityForResult(intent,TAKE_PHOTO);

        }
        if(v.getId()==R.id.send ){
            if(flag==0){
                Toast.makeText(MainActivity.this,"请先拍照后上传",Toast.LENGTH_SHORT).show();
            }
            else {
                if(TextUtils.isEmpty(editText.getText())) {
                    Toast.makeText(MainActivity.this,"请先输入本机电脑的ip",Toast.LENGTH_SHORT).show();
                }
                else  {
                    Toast.makeText(MainActivity.this,"照片上传成功",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket = new Socket(editText.getText().toString(), POST);
                                //读取图片文件并上传
                                FileInputStream fis = new FileInputStream(outputImage);
                                OutputStream os = socket.getOutputStream();
                                byte[] buff = new byte[1024];
                                int length = 0;
                                while((length=fis.read(buff))!=-1)
                                {
                                    os.write(buff,0,length);
                                }

                                socket.shutdownOutput();//关闭socket的输出流
                                //读取响应数据
                                InputStream is =socket.getInputStream();
                                byte[] readbuff= new byte[1024];
                                length  = is.read(readbuff);
                                String message = new String(readbuff,0,length);
                                Log.d(TAG,message);

                                //释放资源
                                fis.close();
                                socket.close();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        }

        }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode==RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageuri));
                        picture.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:break;
        }
    }

}
