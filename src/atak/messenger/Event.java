package atak.messenger;
import java.util.*;
import java.text.SimpleDateFormat;

public class Event {

    private String version = "2.0";
    private String uid = "Kartographia";
    private String type = "a-f-G-U-C"; //Friendly Gnd/Combat
    private String how = "m-g"; //gps
    private java.util.Date date;
    private Location location;
    private TimeZone utc = TimeZone.getTimeZone("UTC");
    private String isoFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private long interval = 30000; //30 seconds

    private ArrayList<Detail> details;

    public Event(java.util.Date date, Location location){
        this.date = date;
        this.location = location;
        this.details = new ArrayList<>();
    }


    public void setUID(String uid){
        this.uid = uid;
    }

    public String getUID(){
        return uid;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setHow(String how){
        this.how = how;
    }


    public void addDetail(Detail detail){
        details.add(detail);
    }

    public String toXML(){

        String time = getISOString(date);
        String t2 = getISOString(new java.util.Date(date.getTime() + interval));


        StringBuilder xml = new StringBuilder();
        xml.append("<event");
        xml.append(" version=\""); xml.append(version); xml.append("\"");
        xml.append(" uid=\""); xml.append(uid); xml.append("\"");
        xml.append(" type=\""); xml.append(type); xml.append("\"");
        xml.append(" how=\""); xml.append(how); xml.append("\"");
        xml.append(" time=\""); xml.append(time); xml.append("\"");
        xml.append(" start=\""); xml.append(time); xml.append("\"");
        xml.append(" stale=\""); xml.append(t2); xml.append("\"");
        xml.append(">");

        xml.append(location.toXML());

        if (!details.isEmpty()){
            xml.append("<detail>");
            for (Detail detail : details){
                xml.append(detail.toXML());
            }
            xml.append("</detail>");
        }

        xml.append("</event>");
        return xml.toString();
    }


    private String getISOString(java.util.Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(isoFormat);
        sdf.setTimeZone(utc);
        return sdf.format(date);
    }

}