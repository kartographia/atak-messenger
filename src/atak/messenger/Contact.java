package atak.messenger;

public class Contact implements Detail {

    private String name;
    private String role = "Team Member";
    private String group = "Cyan";
    private String endpoint = "*:-1:stcp";

    public Contact(String name){
        this.name = name;
    }


    public String toXML(){
        StringBuilder xml = new StringBuilder();

        xml.append("<contact");
        xml.append(" callsign=\""); xml.append(name); xml.append("\"");
        xml.append(" endpoint=\""); xml.append(endpoint); xml.append("\"");
        xml.append("/>");

        xml.append("<uid");
        xml.append(" Droid=\""); xml.append(name); xml.append("\"");
        xml.append("/>");

        xml.append("<__group");
        xml.append(" name=\""); xml.append(group); xml.append("\"");
        xml.append(" role=\""); xml.append(role); xml.append("\"");
        xml.append("/>");

        return xml.toString();
    }
}