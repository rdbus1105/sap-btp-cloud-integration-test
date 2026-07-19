import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.gateway.ip.core.customdev.util.AttachmentWrapper
import groovy.json.JsonSlurper
import javax.mail.util.ByteArrayDataSource
import javax.activation.DataHandler
import java.util.Base64

/**
 * Build Attachments
 */
def Message processData(Message message) {

    def json = new JsonSlurper().parse(message.getBody(java.io.Reader))
    final Set<String> allowedExt = ['.pdf', '.tif', '.tiff', '.png', '.xml', '.jpg', '.jpeg'] as Set
    int count = 0
    Map<String, Integer> nameOccurrence = [:]

    (json?.value ?: []).each { att ->
        if (!att?.contentBytes) return
        String name = att.name?.toString() ?: 'attachment'
        if (name.contains('#')) return
        int dot = name.lastIndexOf('.')
        if (dot < 0) return
        if (!allowedExt.contains(name.substring(dot).toLowerCase())) return

        byte[] bytes = Base64.decoder.decode(att.contentBytes.toString())
        String contentType = resolveContentType(name, att.contentType?.toString())
        def ds = new ByteArrayDataSource(bytes, contentType)
        def wrapper = new AttachmentWrapper(new DataHandler(ds))

        String attachmentKey = uniqueAttachmentKey(name, nameOccurrence)
        message.addAttachmentObject(attachmentKey, wrapper)
        count++
    }

    message.setProperty('AttachmentCount', String.valueOf(count))
    message.setProperty('HasAttachment', count > 0 ? 'true' : 'false')
    return message
}

/**
 * Handle Files with the same name
 */
private static String uniqueAttachmentKey(String name, Map<String, Integer> nameOccurrence) {
    int occurrence = (nameOccurrence[name] ?: 0) + 1
    nameOccurrence[name] = occurrence
    if (occurrence == 1) {
        return name
    }
    return name + ('\u200B' * (occurrence - 1))
}

private static String resolveContentType(String filename, String graphContentType) {
    if (graphContentType && graphContentType != 'application/octet-stream') {
        return graphContentType
    }
    String lower = filename.toLowerCase()
    if (lower.endsWith('.pdf')) return 'application/pdf'
    if (lower.endsWith('.png')) return 'image/png'
    if (lower.endsWith('.jpg') || lower.endsWith('.jpeg')) return 'image/jpeg'
    if (lower.endsWith('.tif') || lower.endsWith('.tiff')) return 'image/tiff'
    if (lower.endsWith('.xml')) return 'application/xml'
    return 'application/octet-stream'
}
