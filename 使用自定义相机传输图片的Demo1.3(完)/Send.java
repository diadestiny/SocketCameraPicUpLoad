import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Send  {


    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    ServerSocket serverSocket = new ServerSocket(5714);

                        Socket client = serverSocket.accept();
                        //响应到客户端
                        OutputStream os = client.getOutputStream();
                        os.write(args[0].getBytes());
                        //System.out.println("accept!!");

                        client.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
