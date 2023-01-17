package atak.messenger;

public class Sensor implements Detail {


    private boolean hideFov = false;
    private double fov = 65;
    private double azimuth = 0;
    private double range = 100000;
    private double red = 1.0;
    private double green = 1.0;
    private double blue = 0.5;
    private double fovAlpha = 0.6;

    public void setRange(double range){
        this.range = range;
    }

    public void setAzimuth(double azimuth){
        this.azimuth = azimuth;
    }

    public void setFOV(double fov){
        this.fov = fov;
    }

    public String toXML(){
        StringBuilder xml = new StringBuilder();

        xml.append("<sensor");

        xml.append(" fovGreen=\""); xml.append(green); xml.append("\"");
        xml.append(" fovBlue=\""); xml.append(blue); xml.append("\"");
        xml.append(" fovRed=\""); xml.append(red); xml.append("\"");

        xml.append(" range=\""); xml.append(range); xml.append("\"");
        xml.append(" azimuth=\""); xml.append(azimuth); xml.append("\"");

        xml.append(" displayMagneticReference=\"0\"");

        xml.append(" fov=\""); xml.append(fov); xml.append("\"");
        xml.append(" hideFov=\""); xml.append(hideFov); xml.append("\"");
        xml.append(" fovAlpha=\""); xml.append(fovAlpha); xml.append("\"");

        xml.append("/>");

        xml.append("<color argb=\"-1\"/>");

        return xml.toString();
    }

}