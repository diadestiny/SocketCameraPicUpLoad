package com.hanyuzhou.testchat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "MainActivity001";
    private static final String HOST = "10.34.7.186";
    private static final int POST = 4714;
    private EditText mEditText;
    private Button mButton;
    private String sendMesg;
    private Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditText = (EditText) this.findViewById(R.id.mEditText);
        mButton = (Button) this.findViewById(R.id.mButton);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.mButton){

            sendMesg = mEditText.getText().toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG,"test001");
                        socket = new Socket(HOST, POST);
                        Log.d(TAG,"test002");
                        //向服务器发送数据
                        PrintWriter send = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8")));
                        send.println(sendMesg);
                        send.flush();

                        //接受服务端数据
                        BufferedReader recv = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String recvMsg = recv.readLine();
                        if (recvMsg != null) {
                            Log.e(TAG,"返回的内容是:"+recvMsg);
                        } else {
                        }

                        //释放资源
                        send.close();
                        recv.close();
                        socket.close();
                    } catch (Exception e) {
                        Log.d(TAG,"test003");
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
