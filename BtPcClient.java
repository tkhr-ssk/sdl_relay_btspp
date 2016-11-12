import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import javax.microedition.io.OutputConnection;

public class BtPcClient {

    public static void main(String[] args) throws Exception {
        String connectionURL = "";

        if ((args != null) && (args.length > 0)) {
            connectionURL = args[0];
        }else{
            System.out.println("usage: java BtPcClient btspp://1234ABCD5678:1");
            return;
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

        OutputStream os = ((OutputConnection)connection).openOutputStream();
        InputStream is = ((InputConnection)connection).openInputStream();

        System.out.println("Receivng...");
        receive(is);

        System.out.println("Sending...");
        sendSDLStart(os);
	Thread.sleep(500);

        System.out.println("Receivng...");
        receive(is);

        System.out.println("Closing...");
        try {
            if (null != connection) {
                os.close();
                is.close();
                connection.close();
            }
        } catch (Exception ioe) {
        }

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

