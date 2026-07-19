import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.*

def Message processData(Message message) {

    def body = message.getBody(String)
    def xml = new XmlSlurper(false, false).parseText(body)

    def errorList = []
    def successList = []

    xml.'**'.findAll { it.name() == 'record' }.each { rec ->

        def err = rec.errormessage?.text()

        if (err && err.trim()) {
            errorList << rec
        } else {
            successList << rec
        }
    }

    // Convert to XML strings
    def errorXml = new StringWriter()
    def successXml = new StringWriter()

    def errorBuilder = new MarkupBuilder(errorXml)
    def successBuilder = new MarkupBuilder(successXml)

    errorBuilder.records {
        errorList.each { r ->
            record {
                r.children().each { c ->
                    if (c.name()) {
                        "${c.name()}"(c.text())
                    }
                }
            }
        }
    }

    successBuilder.records {
        successList.each { r ->
            record {
                r.children().each { c ->
                    if (c.name()) {
                        "${c.name()}"(c.text())
                    }
                }
            }
        }
    }

    // 🔥 Set CPI properties
    message.setProperty("errorRecords", errorXml.toString())
    message.setProperty("successRecords", successXml.toString())

    return message
}