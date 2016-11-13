import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import javax.microedition.io.OutputConnection;

public class BtPcClient {

    public static void main(String[] args) throws Exception {
        String connectionURL = "";
        String relayIP = "127.0.0.1";
        String relayPort = "12345";

        if ((args != null) && (args.length > 0)) {
            connectionURL = args[0];
        }else{
            System.out.println("usage: java BtPcClient btspp://1234ABCD5678:1");
            return;
        }

        if ((args != null) && (args.length > 1)) {
            relayIP = args[1];
        }
        if ((args != null) && (args.length > 2)) {
            relayPort = args[2];
        }

        System.out.println(connectionURL);
        System.out.println("Connecting...");

        // サーバー側のデバイスからコネクションを取得する
        Connection connection = Connector.open(connectionURL);
        if (connection == null) {
            System.out.println("Connect fail");
            System.exit(1);
        }
        System.out.println("Connected");

        // TCP接続
        Socket socket = null;
        try{
          System.out.println("Connect TCP "+relayIP+":"+relayPort);
          socket = new Socket(relayIP, Integer.parseInt(relayPort));
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        OutputStream os = ((OutputConnection)connection).openOutputStream();
        InputStream is = ((InputConnection)connection).openInputStream();

        if ( null == socket ) {
          BtRelayThread btrthread = new BtRelayThread(is);
          Thread th = new Thread(btrthread);
          th.start();
        } else {
          OutputStream sos = socket.getOutputStream();
          InputStream sis = socket.getInputStream();
          BtRelayThread btrthread = new BtRelayThread(is, sos);
          BtRelayThread btrthreadr = new BtRelayThread(sis, os);
          Thread th = new Thread(btrthread);
          Thread thr = new Thread(btrthreadr);
          th.start();
          thr.start();
          th.join();
          thr.join();
        }

        if ( null == socket )
        {
          Thread.sleep(500);
          System.out.println("Sending...");
          sendSDLStart(os);
          Thread.sleep(500);
        }

        Thread.sleep(5000);

        System.out.println("Closing...");
        try {
            if (null != connection) {
                os.close();
                is.close();
                connection.close();
            }
        } catch (Exception ioe) {
        }

        if (socket != null ) socket.close();
        System.out.println("Finished");
    }

    static void sendSDLStart(OutputStream os) {
        byte[] sendBuff = {0x40, 0x07, 0x02, 0x01, 0x00, 0x00, 0x00, 0x04,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01};
        try {
            os.write(sendBuff);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static byte[] receive(InputStream is) {
        byte[] recvBuff = new byte[4096];
        int n=0;
        try {
            n = is.read(recvBuff);
            System.out.print("read:");
            System.out.println(n);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Output HexDump
        for (int i = 0; i < n; i++) {
            System.out.printf(" %02x", recvBuff[i]);
            if (15==(i%16))System.out.println(); 
        }
        System.out.println();

        // Output Ascii
        for (int i = 0; i < n; i++) {
            if( 0x20 <= recvBuff[i] && recvBuff[i] <= 0x7e)
            {
            System.out.printf("%c", recvBuff[i]);
            }else{
            System.out.print("?");
            }
        }
        System.out.println();

        return recvBuff;
    }
}

class BtRelayThread implements Runnable {
  private InputStream is;
  private OutputStream os;
  public BtRelayThread(InputStream _is) {
    is = _is;
    os = null;
  }
  public BtRelayThread(InputStream _is, OutputStream _os) {
    is = _is;
    os = _os;
  }
  public void run(){
    System.out.println("start");
    byte[] recvBuff = new byte[4096];
    int n=0;
    for(;;)
    {
        try {
            n = is.read(recvBuff);
            System.out.print("read:");
            System.out.println(n);
        } catch (IOException e) {
            e.printStackTrace();
            break;
        }
        if ( n<=0 )
        {
            System.out.println("Close InputStream.");
            break;
        }

        // Output HexDump
        for (int i = 0; i < n; i++) {
            System.out.printf(" %02x", recvBuff[i]);
            if (15==(i%16))System.out.println();
        }
        System.out.println();

        // Output Ascii
        for (int i = 0; i < n; i++) {
            if( 0x20 <= recvBuff[i] && recvBuff[i] <= 0x7e)
            {
            System.out.printf("%c", recvBuff[i]);
            }else{
            System.out.print("?");
            }
        }
        System.out.println();
        if ( os != null )
        {
          try {
            os.write(recvBuff, 0, n);
          } catch (Exception e) {
            e.printStackTrace();
            os = null;
          }
        }
    }
  }
}
