import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.Date;
import java.util.TimeZone;

def Message processData(Message message) 
{   
    def prop_timezone = message.getProperty("Timezone");
    prop_timezone = '\'' + prop_timezone  + '\''
    def timezone = TimeZone.getTimeZone("prop_timezone")
    message.setProperty("datetimestamp", new Date().format("yyyy-MM-dd'T00:00:00'",timezone)); 
    message.setProperty("timestamp", new Date().format("'PT'HH'H'mm'M'ss'S'",timezone)); 
return message
}
