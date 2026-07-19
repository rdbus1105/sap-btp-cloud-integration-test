import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.Date;
import java.util.TimeZone;

def Message processData(Message message) 
{   
    def prop_timezone = message.getProperty("Timezone");
    prop_timezone = '\'' + prop_timezone  + '\''
    def timezone = TimeZone.getTimeZone("CET")
    message.setProperty("datetimestamp", new Date().format("yyyy-MM-dd'T'HH:mm:ss.sss",timezone)); 
    message.setProperty("timestamp", new Date().format("'PT'HH'H'mm'M'ss'S'",timezone)); 
return message
}
