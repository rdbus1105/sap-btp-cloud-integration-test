import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import java.util.Base64

/**
 * Converts JSON to XML for General Splitter.
 */
def Message processData(Message message) {

    def json = new JsonSlurper().parse(message.getBody(java.io.Reader))
    def items = json?.value ?: []
    message.setProperty('UnreadMailCount', String.valueOf(items.size()))

    def xml = new StringBuilder('<?xml version="1.0" encoding="UTF-8"?><Messages>')
    items.each { mail ->
        def bodyText = mail?.body?.content?.toString() ?: mail?.bodyPreview?.toString() ?: ''
        def bodyType = mail?.body?.contentType?.toString() ?: 'text'

        xml.append('<Message>')
        xml.append('<Id>').append(escapeXml(mail.id?.toString())).append('</Id>')
        xml.append('<Subject>').append(escapeXml(mail.subject?.toString())).append('</Subject>')
        xml.append('<HasAttachments>').append(escapeXml(String.valueOf(mail.hasAttachments))).append('</HasAttachments>')
        xml.append('<ReceivedDateTime>').append(escapeXml(mail.receivedDateTime?.toString())).append('</ReceivedDateTime>')
        xml.append('<BodyPreview>').append(escapeXml(mail.bodyPreview?.toString())).append('</BodyPreview>')
        xml.append('<BodyContentType>').append(escapeXml(bodyType)).append('</BodyContentType>')
        xml.append('<BodyContent>').append(Base64.encoder.encodeToString(bodyText.getBytes('UTF-8'))).append('</BodyContent>')
        xml.append('<FromName>').append(escapeXml(mail?.from?.emailAddress?.name?.toString())).append('</FromName>')
        xml.append('<FromAddress>').append(escapeXml(mail?.from?.emailAddress?.address?.toString())).append('</FromAddress>')
        xml.append('</Message>')
    }
    xml.append('</Messages>')
    message.setBody(xml.toString())
    return message
}

private static String escapeXml(String value) {
    if (value == null) return ''
    return value.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
        .replace('"', '&quot;').replace("'", '&apos;')
}
