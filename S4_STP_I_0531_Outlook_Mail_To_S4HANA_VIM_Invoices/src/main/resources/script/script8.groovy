import com.sap.gateway.ip.core.customdev.util.Message

/**
 * Set Header before sending
 */
def Message processData(Message message) {

    def body = message.getProperty('BodyContent')
    message.setBody(body ?: '')

    def contentType = message.getProperty('MailContentType') ?: 'text/plain'
    message.setHeader('Content-Type', contentType)
    return message
}
