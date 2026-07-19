import com.sap.gateway.ip.core.customdev.util.Message

def Message filterSupplierRecordsOnly(Message message) {

    def root = new XmlSlurper().parseText(message.getBody(String.class))

    def totalRecords = root.A_BusinessPartnerType.size()
    def missingSupplierBPs = []
    def validSupplierBPs = []

    root.A_BusinessPartnerType.each {
        if (it.Supplier.text().trim().isEmpty()) {
            missingSupplierBPs << it.BusinessPartner.text()
        } else {
            validSupplierBPs << it.BusinessPartner.text()
        }
    }

    // Remove invalid records
    root.A_BusinessPartnerType.findAll {
        it.Supplier.text().trim().isEmpty()
    }.each {
        it.replaceNode {}
    }

    def missingList = missingSupplierBPs.join(",")
    def validList   = validSupplierBPs.join(",")

    message.setProperty("MISSING_SUPPLIER_BPs",       missingList)
    message.setProperty("MISSING_SUPPLIER_COUNT",      missingSupplierBPs.size().toString())
    message.setProperty("VALID_SUPPLIER_BPs",          validList)
    message.setProperty("VALID_SUPPLIER_COUNT",        validSupplierBPs.size().toString())
    message.setProperty("TOTAL_BP_COUNT",              totalRecords.toString())

    // Set error flag instead of throwing
    if (missingSupplierBPs.size() == totalRecords) {
        def errorMsg = "All ${totalRecords} records filtered out — no BP with valid Supplier found. " +
                       "BPs missing Supplier [${missingSupplierBPs.size()}]: ${missingList}"

        message.setProperty("FILTER_ERROR",     "true")
        message.setProperty("FILTER_ERROR_MSG", errorMsg)

        // Log to MPL so it's visible in monitoring
        def messageLog = messageLogFactory.getMessageLog(message)
        messageLog?.addCustomHeaderProperty("Filter_Error",     "ALL_RECORDS_FILTERED")
        messageLog?.addCustomHeaderProperty("Missing_BP_Count", missingSupplierBPs.size().toString())
        messageLog?.addCustomHeaderProperty("Missing_BPs",      missingList)

        return message  
    }

    // Log stats to MPL
    def messageLog = messageLogFactory.getMessageLog(message)
    // messageLog?.addCustomHeaderProperty("Total_BP_Count",   totalRecords.toString())
    // messageLog?.addCustomHeaderProperty("Valid_BP_Count",   validSupplierBPs.size().toString())
    // messageLog?.addCustomHeaderProperty("Missing_BP_Count", missingSupplierBPs.size().toString())
    // messageLog?.addCustomHeaderProperty("Missing_BPs",      missingList)

    def output = groovy.xml.XmlUtil.serialize(
        new groovy.xml.StreamingMarkupBuilder().bind { mkp.yield root }
    )

    message.setBody(output)
    return message
}