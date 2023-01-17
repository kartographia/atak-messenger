package atak.messenger;

public class Device implements Detail {

    private String name = "ATAK MESSENGER";
    private String platform = "ATAK MESSENGER";
    private String os = "25";
    private String version = "1.0.0";


    public String toXML(){
        StringBuilder xml = new StringBuilder();

        xml.append("<takv");

        xml.append(" device=\""); xml.append(name); xml.append("\"");
        xml.append(" platform=\""); xml.append(platform); xml.append("\"");
        xml.append(" os=\""); xml.append(os); xml.append("\"");
        xml.append(" version=\""); xml.append(version); xml.append("\"");

        xml.append("/>");
        return xml.toString();
    }

}