import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def body = message.getBody(String)
    def xml  = new XmlSlurper().parseText(body)

    def businessPartner = message.getProperty("BusinessPartner")
    def customer        = message.getProperty("Customer")

    def errors   = []
    def warnings = []

    xml.compositeResponse.each { response ->
        def statusCode  = response.httpStatusCode.text() as Integer
        def referenceId = response.referenceId.text() ?: "Unknown"

        if (statusCode != 200 && statusCode != 201) {

            // ── sObject from referenceId since httpHeaders is empty on failure ──
            def sobject    = extractSObjectFromReferenceId(referenceId)
            def identifier = getSObjectIdentifier(sobject, businessPartner, customer)

            // ── Error detail from body ──
            def errorMsg  = response.body.message.text()   ?: "No message"
            def errorCode = response.body.errorCode.text() ?: "No errorCode"
            def fields    = response.body.fields.text()
            def fieldInfo = fields ? " | Field: ${fields}" : ""

            def errorLine = "[${sobject}] referenceId=${referenceId} | ${identifier} | " +
                            "HTTP ${statusCode} | ${errorCode}: ${errorMsg}${fieldInfo}"

            // ── 405 = missing SAP ID (data issue) → Warning ──
            // ── 400 = field/validation issue     → Error   ──
            if (statusCode == 405) {
                warnings.add(errorLine)
            } else {
                errors.add(errorLine)
            }
        }
    }

    // ── Set properties and always continue the flow ──
    message.setProperty("CompositeAPI_HasErrors",   errors   ? "true" : "false")
    message.setProperty("CompositeAPI_HasWarnings", warnings ? "true" : "false")
    message.setProperty("CompositeAPI_Errors",      errors.join("\n"))
    message.setProperty("CompositeAPI_Warnings",    warnings.join("\n"))

    return message
}

def extractSObjectFromReferenceId(String referenceId) {
    if (referenceId) {
        def idx = referenceId.lastIndexOf('_')
        if (idx > 0) return referenceId.substring(0, idx)
        return referenceId
    }
    return "Unknown"
}

def getSObjectIdentifier(String sobject, String businessPartner, String customer) {
    switch (sobject) {
        case "Account":              return "BusinessPartner=${businessPartner}"
        case "Inspection_Vendor__c": return "Customer=${customer}"
        default:                     return customer ?: businessPartner ?: "Unknown"
    }
}