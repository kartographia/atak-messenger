package atak.messenger;
import java.math.BigDecimal;

public class Vertex {

    private BigDecimal lat;
    private BigDecimal lon;

    public Vertex(double lat, double lon){
        this.lat = BigDecimal.valueOf(lat);
        this.lon = BigDecimal.valueOf(lon);
    }

    public String toXML(){
        StringBuilder xml = new StringBuilder();
        xml.append("<vertex");
        xml.append(" lat=\""); xml.append(lat); xml.append("\"");
        xml.append(" lon=\""); xml.append(lon); xml.append("\"");
        xml.append("/>");
        return xml.toString();
    }
}