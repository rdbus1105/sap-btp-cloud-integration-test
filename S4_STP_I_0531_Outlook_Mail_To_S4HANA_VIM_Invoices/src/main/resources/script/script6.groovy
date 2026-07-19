import com.sap.gateway.ip.core.customdev.util.Message
//import groovy.xml.XmlSlurper
import java.util.Base64

/**
 * Set Properties
 */
def Message processData(Message message) {

    def root = new XmlSlurper().parse(message.getBody(java.io.Reader))
    def mail = root.name() == 'Message' ? root : root.Message

    message.setProperty('MessageId', mail.Id.text())
    message.setProperty('Subject', mail.Subject.text())
    message.setProperty('HasAttachments', mail.HasAttachments.text())
    message.setProperty('ReceivedDateTime', mail.ReceivedDateTime.text())
    message.setProperty('FromName', mail.FromName.text())
    message.setProperty('FromAddress', mail.FromAddress.text())

    String graphBodyType = mail.BodyContentType.text() ?: 'text'
    String mailContentType = graphBodyType.equalsIgnoreCase('html') ? 'text/html' : 'text/plain'
    message.setProperty('MailContentType', mailContentType)

    String bodyContent = ''
    String encoded = mail.BodyContent.text()
    if (encoded) {
        bodyContent = new String(Base64.decoder.decode(encoded), 'UTF-8')
    } else {
        bodyContent = mail.BodyPreview.text() ?: ''
    }

    message.setProperty('BodyContent', bodyContent)
    message.setBody(bodyContent)
    return message
}
