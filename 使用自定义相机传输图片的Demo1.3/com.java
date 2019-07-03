import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class com implements Runnable {
    private int temp = 1;
    private int PORT;
    private String path;

    public static void main(String[] args) {
        Thread myThread = new Thread(new com(Integer.valueOf(args[1]), args[0]));
        myThread.start();
    }
    private com(int PORT, String path) {
        this.PORT = PORT;
        this.path = path;
    }
    @Override
    public void run() {
        int length;
        try {
            @SuppressWarnings("resource")
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("accept");

                InputStream is = client.getInputStream();
                FileOutputStream fos = new FileOutputStream(path+ "received" + temp + ".jpg");
                temp++;
                byte[] buff = new byte[1024];
                while ((length = is.read(buff)) != -1) {
                    fos.write(buff, 0, length);
                }
                fos.flush();
                //响应到客户端
                OutputStream os = client.getOutputStream();
                os.write("上传成功".getBytes());
                //释放资源
                client.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}