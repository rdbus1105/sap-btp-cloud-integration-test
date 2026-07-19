import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.MarkupBuilder

def Message processData(Message msg) {
    def root = new XmlSlurper().parseText(msg.getBody(String))
    def req  = new XmlSlurper().parseText(msg.getProperty('SFDC_Request_Body') as String)

    def records = req.records.list()
    def writer  = new StringWriter()
    def xml     = new MarkupBuilder(writer)
    xml.mkp.xmlDeclaration(version: '1.0', encoding: 'UTF-8')
    xml.results {
        root.result.eachWithIndex { r, i ->
            def rec   = i < records.size() ? records[i] : null
            def cust  = rec ? rec.SAP_External_Id__c.text().trim() : ''
            def vend  = rec ? rec.Vendor_Number__c.text().trim()   : ''
            def label = cust ? 'Customer' : (vend ? 'Vendor' : '')
            def extId = cust ?: vend

            result {
                created(r.created.text())
                if (r.errors.size()) {
                    errors {
                        fields(r.errors.fields.text())
                        message(r.errors.message.text())
                        statusCode(r.errors.statusCode.text())
                    }
                }
                id(r.id.text())
                Sobject(label)
                if (label) "${label}"(extId)
                success(r.success.text())
            }
        }
    }

    msg.setBody(writer.toString())
    return msg
}