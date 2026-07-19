import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def props = message.getProperties()
    
    def initialLoad = props.get("InitialLoad")?.toString()?.trim()
    def dateNow     = props.get("dateNow")?.toString()?.trim()
    def supplierID  = props.get("SupplierID")?.toString()?.trim()
    def customerID  = props.get("CustomerID")?.toString()?.trim()
    
    def salesforceCustomQuery = ""
    
    if (customerID) {
        // Split by comma, trim each value, and build filter conditions
        def customerList = customerID.split(",").collect { it.trim() }.findAll { it }
        def customerFilter = customerList.collect { "Customer eq '${it}'" }.join(" and ")
        salesforceCustomQuery = "&\$filter=${customerFilter}"
        
    } else if (supplierID) {
        // Split by comma, trim each value, and build filter conditions
        def supplierList = supplierID.split(",").collect { it.trim() }.findAll { it }
        def supplierFilter = supplierList.collect { "Supplier eq '${it}'" }.join(" and ")
        salesforceCustomQuery = "&\$filter=${supplierFilter}"
        
    } else if (initialLoad?.equalsIgnoreCase("false")) {
        salesforceCustomQuery = "&\$filter=LastChangeDate ge datetime'${dateNow}'"
    }
    
    message.setProperty("SalesforceCustomQuery", salesforceCustomQuery.toString())
    
    return message
}