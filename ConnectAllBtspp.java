import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.*;

public class ConnectAllBtspp {
  public static void main(String[] args) throws IOException, InterruptedException {
    ServicesSearch.main(args);
    for(Enumeration en = ServicesSearch.serviceFound.elements(); en.hasMoreElements(); ) {
      String url = (String)en.nextElement();
      url = url.replaceAll(";.*", "");
      ConnectThread cth = new ConnectThread(url);
      Thread th = new Thread(cth);
      th.start();
      Thread.sleep(5000);
    }
    System.out.println();
    System.out.println("[ConnectAllBtSpp] All Connection started.");
    System.out.println();
  }
}

class ConnectThread implements Runnable {
  private String url;
  public ConnectThread(String _url) {
    url = _url;
  }
  public void run() {
    String args[] = {this.url};
    try {
      BtPcClient.main(args);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}

