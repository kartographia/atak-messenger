# ATAK Messenger
Java library used to connect to an ATAK server and send Cursor-On-Target (COT) messages. This library has no dependencies
and is released under a permissive MIT license.

## Example Usage
The following class is used to connect to an ATAK server using a client and server certificate and relay ficticious drone 
locations every 10 seconds.

```java
package com.example;
import javaxt.json.JSONObject;
import java.util.*;

public class Test {


  //**************************************************************************
  //** main
  //**************************************************************************
    public static void main(String[] arguments) throws Exception {

      //Parse atak config
        JSONObject atakConfig = Config.get("atak").toJSONObject();
        String host = atakConfig.get("host").toString();
        Integer port = atakConfig.get("port").toInteger();
        JSONObject certs = atakConfig.get("certificates").toJSONObject();
        java.io.File clientCert = new java.io.File(certs.get("client").toString());
        java.io.File serverCert = new java.io.File(certs.get("server").toString());


      //Instantiate atak messenger
        atak.messenger.Messenger atakMessenger =
                new atak.messenger.Messenger(host, port, clientCert, serverCert);


      //Connect to the server and start sending messages
        atakMessenger.connect(new atak.messenger.Messenger.Callback(){
            public void exec(){
                runSimulation(atakMessenger);
            }
        });

    }


  //**************************************************************************
  //** runSimulation
  //**************************************************************************
  /** Used to send messages to the ATAK server at a fixed interval
   */
    private static void runSimulation(atak.messenger.Messenger atakMessenger) {

        new java.util.Timer().scheduleAtFixedRate(
            new java.util.TimerTask(){
                public void run(){

                    Random rand = new Random();
                    double x = (rand.nextInt(200) - 100) / 100.0;
                    double y = (rand.nextInt(200) - 100) / 100.0;



                    double lat = 26+x;
                    double lon = -80+y;
                    atak.messenger.Point point = new atak.messenger.Point(lat,lon);
                    Date date = new Date();


                    atak.messenger.Event event = new atak.messenger.Event(date, point);
                    event.setUID("UAV-HOSTILE");
                    event.setType("a-s-A-M-F-Q"); //drone
                    //event.setType("b-m-p-s-p-loc");
                    event.setHow("m-p");
                    //event.addDetail(new Sensor());

                    atakMessenger.send(event);


                }
            },
            3000, //delay until first message is sent (in milliseconds)
            10000 //how often to send messages (in milliseconds)
        );
    }
}
```
