import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.lang.StringBuffer;

def Message processData(Message message) {
    //Body
    def messageLog = messageLogFactory.getMessageLog(message);
    
    def logProperty = message.getProperty("PropertyLogging") as String;
    def logHeader = message.getProperty("HeaderLogging") as String;
    def logBody = message.getProperty("BodyLogging") as String;
    String content = "";
    
    if(messageLog != null) {
        messageLog.setStringProperty("Logging", "Payload");
        
        if (logProperty != null && logProperty.equalsIgnoreCase("YES")) {
            def propertyMap = message.getProperties()  as String;
            content = content + "\n" + "Message Properties" + "\n" + "\n" + propertyMap + "\n" 
        }
        
        if (logHeader != null && logHeader.equalsIgnoreCase("YES")) {
            def header = message.getHeaders() as String;
            content = content + "\n" + "Message Headers" + "\n" + "\n" +  header + "\n"
        }
        
        if (logBody == null || logBody.equalsIgnoreCase("YES")) {
            def body = message.getBody(java.lang.String) as String;
            content = content + "\n" + "Message Body" + "\n" + "\n"  + body + "\n";
        } 
        
        if (content.length() > 0) {
            messageLog.addAttachmentAsString("Source message log", content, "text/plain");    
        }
    }

    return message;
}