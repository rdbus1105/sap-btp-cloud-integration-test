import com.sap.it.api.mapping.*;

def String sanitize(String arg1){
    return arg1.replaceAll('[^a-zA-Z0-9_]', '_')
}