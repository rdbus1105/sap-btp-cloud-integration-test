import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {

    // Get body as String
    def body = message.getBody(String) as String

    // Step 1: Fix malformed JSON (from first script)
    body = body.replaceAll(""",""]}""", "]}").replaceAll(/\"null\"/,"null")

    // Step 2: Parse JSON
    def parsed = new JsonSlurper().parseText(body)

    // Extract root
    def payload = parsed.root ?: parsed

    // Step 3: Ensure 'records' is always a list
    if (payload.records && !(payload.records instanceof List)) {
        payload.records = [payload.records]
    }

    // Convert back to JSON string
    message.setBody(JsonOutput.toJson(payload))

    return message
}