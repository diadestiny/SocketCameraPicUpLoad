import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class com implements Runnable{

    public static final int PORT = 4714;
    public static void main(String[] args) {
        Thread myThread = new Thread(new com());
        myThread.start();

    }

    @Override
    public void run() {
        try {
            @SuppressWarnings("resource")
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {

                Socket client = serverSocket.accept();
                System.out.println("accept");

                //读上传的图像数据并写入文件
                InputStream is = client.getInputStream();
                FileOutputStream fos = new FileOutputStream("C:\\Users\\lin\\Desktop\\pro\\received.jpg");

                byte[] buff = new byte[1024];
                int length = 0;

                while((length=is.read(buff))!=-1) {
                    fos.write(buff,0,length);
                }
                fos.flush();
                //响应到客户端
                OutputStream os = client.getOutputStream();
                os.write("我上传成功啦".getBytes());

                //释放资源
                client.close();
                System.out.println("我还在");

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            System.out.println("end end close");
        }

    }

}
