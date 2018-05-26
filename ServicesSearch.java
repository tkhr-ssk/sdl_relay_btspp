import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.*;

/**
 *
 * Minimal Services Search example.
 */
public class ServicesSearch {

    public static final Vector<String> serviceFound = new Vector<String>();

    public static void main(String[] args) throws IOException, InterruptedException {

        RemoteDevice remoteDevice = null;
        Vector<RemoteDevice> remoteDeviceList = new Vector<RemoteDevice>();
        if ((args != null) && (args.length > 0)) {
            // アドレスが指定された場合、登録済みのデバイスから一致するデバイスを検索する
            RemoteDevice[] list = LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
            if ( list == null )
            {
                System.out.println("Not Found Devices. " +args[0]);
            }
            for( int i=0; i< list.length; i++) {
              System.out.println("Preknown: " + list[i].getBluetoothAddress() + " " + list[i].getFriendlyName(false));
              if( args[0].equals(list[i].getBluetoothAddress()) || args[0].equals("0") )
              {
                remoteDevice = list[i];
                remoteDeviceList.add(list[i]);
                continue;
              }
            }
            if ( remoteDevice == null )
            {
               System.out.println("Not Found: "+args[0]);
               return;
            }
        }
        else {
          // First run RemoteDeviceDiscovery and use discoved device
          RemoteDeviceDiscovery.main(null);
        }

        serviceFound.clear();

        UUID serviceUUID = new UUID("936DA01F9ABD4D9D80C702AF85C822A8", false);
        if ((args != null) && (args.length > 1)) {
            serviceUUID = new UUID(args[1], false);
        }
        System.out.println("ServiceUUID:"+serviceUUID);

        final Object serviceSearchCompletedEvent = new Object();

        DiscoveryListener listener = new DiscoveryListener() {

            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            }

            public void inquiryCompleted(int discType) {
            }

            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                for (int i = 0; i < servRecord.length; i++) {
                    String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (url == null) {
                        continue;
                    }
                    serviceFound.add(url);
                    DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
                    if (serviceName != null) {
                        System.out.println("service " + serviceName.getValue() + " found " + url);
                    } else {
                        System.out.println("service found " + url);
                    }
                }
            }

            public void serviceSearchCompleted(int transID, int respCode) {
                System.out.println("service search completed!");
                synchronized(serviceSearchCompletedEvent){
                    serviceSearchCompletedEvent.notifyAll();
                }
            }

        };

        UUID[] searchUuidSet = new UUID[] { serviceUUID };
        int[] attrIDs =  new int[] {
                0x0100 // Service name
        };

        if ( remoteDevice != null )
        {
            for(Enumeration en = remoteDeviceList.elements(); en.hasMoreElements(); ) {
                RemoteDevice btDevice = (RemoteDevice)en.nextElement();
                synchronized(serviceSearchCompletedEvent) {
                    System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
                    LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
                    serviceSearchCompletedEvent.wait();
                }
            }
            return;
        }

        for(Enumeration en = RemoteDeviceDiscovery.devicesDiscovered.elements(); en.hasMoreElements(); ) {
            RemoteDevice btDevice = (RemoteDevice)en.nextElement();

            synchronized(serviceSearchCompletedEvent) {
                System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
                LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
                serviceSearchCompletedEvent.wait();
            }
        }

    }

}

