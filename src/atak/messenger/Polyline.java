package atak.messenger;
import java.util.*;

public class Polyline implements Detail {

    private ArrayList<Vertex> vertices = new ArrayList<>();
    private boolean isClosed = false;

    public Polyline(){}

    public void addVertex(Vertex vertex){
        vertices.add(vertex);
    }

    public void isClosed(boolean isClosed){
        this.isClosed = isClosed;
    }

    public String toXML(){
        StringBuilder xml = new StringBuilder();
        xml.append("<shape>");
        xml.append("<polyline");
        if (isClosed) xml.append(" closed=\"true\"");
        xml.append(">");
        for (Vertex vertex : vertices){
            xml.append(vertex.toXML());
        }
        xml.append("</polyline>");
        xml.append("</shape>");
        return xml.toString();
    }

}