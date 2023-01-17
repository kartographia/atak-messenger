package atak.messenger;

//Java includes
import java.io.*;
import java.util.*;

//SSL stuff
import java.net.Socket;
import javax.net.ssl.*;
import java.security.cert.*;
import java.security.KeyStore;
import java.security.KeyStoreSpi;


//******************************************************************************
//**  ATAK Messenger
//******************************************************************************
/**
 *  Used to send Cursor-On-Target (COT) messages to an ATAK server
 *
 ******************************************************************************/

public class Messenger {

    private String host;
    private Integer port;
    private KeyManager km;
    private TrustManager tm;
    private SocketWriter writer;
    private Contact contact;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
    public Messenger(String host, int port, java.io.File clientCert, java.io.File serverCert) throws Exception {
        this.host = host;
        this.port = port;
        this.km = getKeyManager(clientCert, "atakatak");
        this.tm = getTrustManager(serverCert);
        this.contact = new Contact("PETER B");
    }


  //**************************************************************************
  //** connect
  //**************************************************************************
    public void connect() throws Exception {
        connect(null);
    }


  //**************************************************************************
  //** connect
  //**************************************************************************
    public void connect(Callback callback) throws Exception {



      //Configure SSL
        SSLContext context = SSLContext.getInstance("SSL"); //SSLv3 vs TLS
	context.init(new KeyManager[]{km}, new TrustManager[]{tm}, null);
	SSLSocketFactory factory = context.getSocketFactory();



      //Open SSL socket to the server
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.addHandshakeCompletedListener(
            new HandshakeCompletedListener() {
                public void handshakeCompleted(HandshakeCompletedEvent ev) {

                    System.out.println("Connected to ATAK Server at " + ev.getSession().getPeerHost());

//                  //Print connection metadata
//                    System.out.println("Handshake finished!");
//                    System.out.println("\t CipherSuite:" + ev.getCipherSuite());
//                    System.out.println("\t SessionId " + ev.getSession());
//                    System.out.println("\t PeerHost " + ev.getSession().getPeerHost());



                  //Start sending messages to the server
                    try{


                      //Create socket writer
                        writer = new SocketWriter(ev.getSocket());
                        new Thread(writer).start();


                      //Identify this client
                        Event event = new Event(new Date(), new Point(0,0));
                        event.addDetail(new Device());
                        event.addDetail(contact);
                        writer.write(event.toXML());



                      //Send ping messages every 30 seconds
                        new java.util.Timer().scheduleAtFixedRate(
                            new java.util.TimerTask(){
                                public void run(){

                                    Event event = new Event(new Date(), new Point(0,0));
                                    event.setUID(event.getUID()+"-ping");
                                    event.setType("t-x-c-t");
                                    writer.write(event.toXML());
                                }
                            },
                            3000, //delay
                            30000 //interval
                        );



                        if (callback!=null){
                            new Thread(new Runnable(){
                                public void run(){
                                    callback.exec();
                                }
                            }).start();
                        }


                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        );
        socket.startHandshake();


//      //Create socket reader
//        SocketReader reader = new SocketReader(socket);
//        Thread t1 = new Thread(reader);
//        t1.start();
//        t1.join();

    }


  //**************************************************************************
  //** send
  //**************************************************************************
    public void send(Event event){
        writer.write(event.toXML());
    }


  //**************************************************************************
  //** Callback
  //**************************************************************************
    public static class Callback {
        public void exec(){
        }
    }


  //**************************************************************************
  //** SocketReader
  //**************************************************************************
    private static class SocketReader implements Runnable {

        private Socket socket;

        public SocketReader(Socket socket) throws Exception {
            this.socket = socket;
        }

        public void run() {
            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                boolean isChatOver = false;
                while(!isChatOver) {
                    if (in.ready()) {
                        Thread.sleep(5000);
                    }
                    String nextChat = in.readLine();
                    System.out.println("server says : " + nextChat);
                    if("bye".equalsIgnoreCase(nextChat.trim())) {
                        System.out.println("**************************************Closing Session.*********************************************");
                        isChatOver = true;
                    }
                }
                in.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }


  //**************************************************************************
  //** SocketWriter
  //**************************************************************************
    private static class SocketWriter implements Runnable {

        private List pool;
        private OutputStreamWriter writer;


        public SocketWriter(Socket socket) throws Exception {
            this.pool = new LinkedList();
            this.writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        }

        public void write(String str){
            synchronized (pool) {
                pool.add(str);
                pool.notifyAll();
            }
        }

        public void run() {
            while (true) {

                Object obj = null;
                synchronized (pool) {
                    while (pool.isEmpty()) {
                        try {
                            pool.wait();
                        }
                        catch (InterruptedException e) {
                            return;
                        }
                    }
                    obj = pool.get(0);
                    if (obj!=null) pool.remove(0);
                    pool.notifyAll();
                }

                if (obj!=null){
                    try{

                        String msg = obj.toString();
                        writer.write(msg);
                        writer.flush();

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }
    }


  //**************************************************************************
  //** getKeyManager
  //**************************************************************************
  /** Returns a KeyManager for a given p12 file
   *  @param file P12 file containing client certificate and private key
   */
    private static KeyManager getKeyManager(java.io.File file, String password) throws Exception {

      //Convert password to char array
        char[] pw = password.toCharArray();


      //Create keystore
        FileInputStream is = new FileInputStream(file);
        KeyStore keystore = KeyStore.getInstance("pkcs12");
        keystore.load(is, pw);
        is.close();


      //Count aliases found in the keystore
        Enumeration<String> aliases = keystore.aliases();
        int n = 0;
        String alias = null;
        while (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
            n++;
            System.out.println("HttpsCert Alias is \"" + alias + "\"");
        }


      //Throw an error if there aren't any aliases in the keystore
        if (n == 0) throw new IllegalArgumentException(
                "Certificate doesn't contain any aliases");


      //Throw an error if there is more than one alias in the keystore
        if (n > 1) throw new IllegalArgumentException(
                "Certificate contains " + n +
                " aliases - we can only use files with one alias!");


      //Validate client certificate
        java.security.cert.Certificate cert = keystore.getCertificate(alias);
        java.security.cert.X509Certificate xcert = (java.security.cert.X509Certificate) cert;
        xcert.checkValidity();


      //Get and return key manager
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	kmf.init(keystore, pw);
        KeyManager[] km = kmf.getKeyManagers();
        return km[0];
    }


  //**************************************************************************
  //** getTrustManager
  //**************************************************************************
  /** Returns a X509TrustManager for a given PEM file
   *  @param file PEM file containing "X.509" certificates for the server. You
   *  can convert a p12 to a pem using openssl like this:
   *  openssl pkcs12 -in server.p12 -out server.pem -nodes
   */
    private static X509TrustManager getTrustManager(java.io.File file){

      //Create custom trust manager
        X509TrustManager tm = new X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {

                X509Certificate[] trustedCerts = new X509Certificate[1];
                try{
                    InputStream is = new FileInputStream(file);
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
                    is.close();
                    trustedCerts[0] = cert;
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                return trustedCerts;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

                boolean match = false;
                try{
                    InputStream is = new FileInputStream(file);
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
                    is.close();

                    for (X509Certificate c : chain){
                        if (c.equals(cert)){
                            match = true;
                        }
                    }
                }
                catch(Exception e){
                    throw new CertificateException();
                }

                if (!match) throw new CertificateException();
            }

        };


      //Print metadata
        for (X509Certificate c: ((X509TrustManager)tm).getAcceptedIssuers()){
            System.out.println("Cert issuer: " + c.getIssuerX500Principal());
        }


        return tm;

    }



  //**************************************************************************
  //** testP12
  //**************************************************************************
  /** Used to create a keystore for a given p12 file. Contains code used to
   *  check for aliases using reflection. Apparently Java can't handle certain
   *  p12 files like the server p12 file generated by taky
   *  @param file P12 file
   */
    private static KeyStore testP12(java.io.File file, String password) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        KeyStore keystore = KeyStore.getInstance("pkcs12");
        keystore.load(fis, password.toCharArray());
        fis.close();

//        try {
//
//            Enumeration<String> aliases = keystore.aliases();
//            if (!aliases.hasMoreElements()){
//		java.lang.reflect.Field field;
////		KeyStoreSpi keyStoreVeritable;
//
//                field = keystore.getClass().getDeclaredField("keyStoreSpi");
//                field.setAccessible(true);
////                keyStoreVeritable = (KeyStoreSpi) field.get(keystore);
//                Object keyStoreVeritable = field.get(keystore);
//
//                System.out.println(field.get(keystore)); //sun.security.pkcs12.PKCS12KeyStore$DualFormatPKCS12
//
//
////                Collection entries;
//                String alias, hashCode;
//                X509Certificate[] certificates;
//
//
//                System.out.println(keyStoreVeritable.getClass());
//
//
//              //If keyStoreVeritable is instance of DualFormatPKCS12, get keystore from super (KeyStoreDelegator)
//                if ("sun.security.pkcs12.PKCS12KeyStore$DualFormatPKCS12".equals(keyStoreVeritable.getClass().getName())) {
//
//                    field = keyStoreVeritable.getClass().getSuperclass().getDeclaredField("keystore");
//                    field.setAccessible(true);
//
//                    keyStoreVeritable = field.get(keyStoreVeritable);
//                    System.out.println(keyStoreVeritable);
//
//                }
//
//                //keyStoreVeritable.getClass().getField(alias)
//
//
//                field = keyStoreVeritable.getClass().getDeclaredField("certEntries");
//                System.out.println(field);
//                field.setAccessible(true);
//
//
//
//                ArrayList entries = (ArrayList) field.get(keyStoreVeritable);
//                System.out.println(entries.size());
//
////                entries = (Collection) field.get(keyStoreVeritable);
////
////                for (Object entry : entries) {
////                    System.out.println(entry);
////
////                        field = entry.getClass().getDeclaredField("certChain");
////                        field.setAccessible(true);
////                        certificates = (X509Certificate[])field.get(entry);
////
////                        hashCode = Integer.toString(certificates[0].hashCode());
////
////                        field = entry.getClass().getDeclaredField("alias");
////                        field.setAccessible(true);
////                        alias = (String)field.get(entry);
////
////                        if(!alias.equals(hashCode)) {
////                                field.set(entry, alias.concat(" - ").concat(hashCode));
////                        } // if
////                } // for
//
//
//            }
//            while (aliases.hasMoreElements()) {
//                String alias = aliases.nextElement();
//                System.out.println("alias: " + alias + " (" + keystore.isCertificateEntry(alias) + ")");
//
//                if (keystore.isCertificateEntry(alias)) {
//                    System.out.println(keystore.getCertificate(alias));
//                }
//            }
//
//
//
//
//            // This class retrieves the most-trusted CAs from the keystore
//            PKIXParameters params = new PKIXParameters(keystore);
//
//            // Get the set of trust anchors, which contain the most-trusted CA certificates
//            Iterator it = params.getTrustAnchors().iterator();
//            while( it.hasNext() ) {
//                TrustAnchor ta = (TrustAnchor)it.next();
//                // Get certificate
//                X509Certificate cert = ta.getTrustedCert();
//                System.out.println(cert);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        return keystore;
    }

}