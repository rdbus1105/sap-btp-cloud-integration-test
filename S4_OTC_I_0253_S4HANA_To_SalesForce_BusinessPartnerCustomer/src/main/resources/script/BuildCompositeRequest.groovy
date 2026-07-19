import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*

def Message processData(Message message) {
    def data = new JsonSlurper().parseText(message.getBody(String))

    // Normalize compositeRequest to always be a list
    def rawRequests = data."multimap:Messages"?."multimap:Message1"?.root?.collect { it.compositeRequest }
                      ?: (data.compositeRequest instanceof List ? data.compositeRequest : [data.compositeRequest])

    def requests = rawRequests.findAll { it?.body?.any { k, v -> v != null && v != "" } }

    requests*.body.each { body -> body.each { k, v -> body[k] = (v == "true") ? true : (v == "false") ? false : v } }

    message.setBody(JsonOutput.toJson([allOrNone: false, compositeRequest: requests]))
    return message
}