package api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;
import java.io.*;
import java.net.*;

public class EventfulAPIGetter implements APIGetter<Event> {

    private String key;
    private String endpoint;
    public static final String API_URL = "http://api.eventful.com";

    // pre: given a valid authentication String and the desired endpoint,
    // post: constructs and returns a interactive client-side object that allows get requests
    //      to said endpoint.
    public EventfulAPIGetter(String key, String endpoint) {
        this.key = key;
        this.endpoint = endpoint;
    }

    // pre: given a map of parameters (mapping type to value) for the query that
    //      follows the Eventful api formatting
    // post: returns the XML string of results for the given query
    public Event[] query(Map<String, String> parameters) throws Exception {
        String paramString = paramString(parameters);
        URL url = new URL(API_URL + endpoint + paramString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        InputStreamReader input;
        try {
            input = new InputStreamReader(con.getInputStream());
        } catch (Exception e) {
            con.disconnect();
            throw new IllegalArgumentException("The parameters passed in were not valid");
        }
        BufferedReader in = new BufferedReader(input);
        String query = readQuery(in);
        con.disconnect();
        return processXML(query);
    }

    public String paramString(Map<String, String> parameters) {
        String params = "?app_key=" + key + "&";
        int i = parameters.keySet().size();
        for (String type : parameters.keySet()) {
            params += type + "=" + parameters.get(type);
            if (i != 1) { params += "&"; }
            i--;
        }
        return params;
    }

    public void changeKey(String newKey) { key = newKey; }

    public void setEndpoint(String newEndpoint) {
        endpoint = newEndpoint;
    }

    public static void main(String[] args) throws Exception {
        EventfulAPIGetter events = new EventfulAPIGetter("zDsLqMh4NJdQtWsw", "/rest/events/search");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("keywords", "books");
        parameters.put("location", "San+Diego");
        System.out.println(Arrays.toString(events.query(parameters)));
    }



    public Event[] processXML(String xml) throws Exception {
        Document doc = loadXMLFromString(xml);
        return getEvents(doc);
    }

    public Event[] getEvents(Document doc){
        Event[] ret;
        NodeList events = doc.getElementsByTagName("event");
        ret = new Event[events.getLength()];
        for (int i =0; i < events.getLength(); i++){
            Node cur = events.item(i);
            if (cur.getNodeType() == Node.ELEMENT_NODE){
                Element event = (Element) cur;
                ret[i] = new Event(getField(event, "title"),
                        getField(event, "venue_name"),
                        getField(event, "venue_address"),
                        getField(event, "description"),
                        getField(event, "start_time"),
                        getField(event, "stop_time"),
                        getField(event, "url"));
            }
        }

        return ret;
    }

    private String getField(Element e, String field){
        return e.getElementsByTagName(field)
                .item(0)
                .getTextContent();
    }



    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

}
