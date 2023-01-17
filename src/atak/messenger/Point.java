package atak.messenger;
import java.math.BigDecimal;

public class Point implements Location {

    private BigDecimal lat; //y
    private BigDecimal lon; //x
    private BigDecimal hae; //elevation above MSL?
    private BigDecimal ce; //horizontal circular error
    private BigDecimal le; //vertical linear error

    public Point(double lat, double lon){
        this(BigDecimal.valueOf(lat), BigDecimal.valueOf(lon), BigDecimal.valueOf(0));
    }

    public Point(BigDecimal lat, BigDecimal lon, BigDecimal z){
        this.lat = lat;
        this.lon = lon;
        this.hae = z;
        this.ce = new BigDecimal(9999999.0);
        this.le = new BigDecimal(9999999.0);
    }

    public String toXML(){
        StringBuilder xml = new StringBuilder();
        xml.append("<point");
        xml.append(" lat=\""); xml.append(lat); xml.append("\"");
        xml.append(" lon=\""); xml.append(lon); xml.append("\"");
        xml.append(" hae=\""); xml.append(hae); xml.append("\"");
        xml.append(" ce=\""); xml.append(ce); xml.append("\"");
        xml.append(" le=\""); xml.append(le); xml.append("\"");
        xml.append("/>");
        return xml.toString();
    }
}