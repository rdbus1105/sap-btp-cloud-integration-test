importClass(com.sap.gateway.ip.core.customdev.util.Message);
importClass(java.lang.String);

function processData(message) {
    // Get body
    var body = message.getBody(String);
    
    // Parse JSON
    var data = JSON.parse(body);
    
    // Convert object to array
    if (!Array.isArray(data.compositeRequest)) {
        data.compositeRequest = [data.compositeRequest];
    }
    
    // Fix booleans
    if (data.allOrNone === "false") {
        data.allOrNone = false;
    }
    
    // Set body back
    message.setBody(JSON.stringify(data));
    
    return message;
}