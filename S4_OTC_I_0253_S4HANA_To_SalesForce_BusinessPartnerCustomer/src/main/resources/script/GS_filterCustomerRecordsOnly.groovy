import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.XmlUtil

def Message filterCustomerRecordsOnly(Message message) {
    def root  = new XmlParser(false, false).parseText(message.getBody(String.class))
    def bps   = root.'**'.findAll { it.name() == 'A_BusinessPartnerType' }
    def total = bps.size()
    def missing = []

    bps.findAll { it.'Customer'.text().trim().isEmpty() }.each {
        missing << it.'BusinessPartner'.text()
        it.parent().remove(it)
    }

    def missingList = missing.join(",").toString()
    message.setProperty("MISSING_CUSTOMER_BPs",   missingList)
    message.setProperty("MISSING_CUSTOMER_COUNT", missing.size().toString())
    message.setProperty("TOTAL_BP_COUNT",         total.toString())

    def log = messageLogFactory.getMessageLog(message)

    if (missing.size() == total) {
        message.setProperty("FILTER_ERROR",     "true")
        message.setProperty("FILTER_ERROR_MSG",
    "All ${total} records filtered out — no BP with valid Customer. Missing: ${missingList}".toString())
        log?.addCustomHeaderProperty("Filter_Error",     "ALL_RECORDS_FILTERED")
        log?.addCustomHeaderProperty("Missing_BP_Count", missing.size().toString())
        log?.addCustomHeaderProperty("Missing_BPs",      missingList)
    }

    message.setBody(XmlUtil.serialize(root))   // always serialize
    return message
}