import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class com implements Runnable{

    public static void main(String[] args) {
        Thread myThread = new Thread(new com());
        myThread.start();

    }

    @Override
    public void run() {
        try {
            @SuppressWarnings("resource")
            ServerSocket serverSocket = new ServerSocket(4714);
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("accept");
                try {
                    //接收客户端的数据
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(),"utf-8"));
                    String str = in.readLine();
                    System.out.println("read:" + str);

                    //返回数据给客户端
                    PrintWriter pout = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "utf-8"));
                    pout.println("返回数据给客户端 : " + "你好科协");
                    System.out.println("after send in server");
                    pout.close();
                    in.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                } finally {
                    client.close();
                    System.out.println("close");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


}
