import com.sap.gateway.ip.core.customdev.util.Message

def Message handleCompositeAPIFailures(Message message) {
    def root = new XmlSlurper().parseText(message.getBody(String))
    def q = { String s -> '"' + (s ?: '').replace('"', '""') + '"' }
    def failed = []
    def succ = [:].withDefault { 0 }
    root.result.each { r ->
        def s = r.Sobject.text().trim()
        if (r.success.text().trim() == 'true') { succ[s]++; return }
        failed << [s, s ? r."${s}".text().trim() : '',
                   r.errors.statusCode.text().trim(),
                   r.errors.message.text().trim().replaceAll(/\s+/, ' ')]
    }
    def fail = failed.countBy { it[0] }
    def csv = failed ? (["Sobject,${failed[0][0] ?: 'ExternalId'},StatusCode,Message"]
                      + failed.collect { it.collect(q).join(',') }).join('\n') : ''

    message.setProperty('SFDC_Body_Empty', csv ? 'false' : 'true')
    if (failed) message.setProperty('SFDC_Failed_RefIds', failed*.getAt(1).join('\n'))

    def log = messageLogFactory.getMessageLog(message)
    if (log) {
        ['Customer', 'Vendor'].each { t ->
            def s = succ[t] ?: 0
            def f = fail[t] ?: 0
            if (s + f) log.addCustomHeaderProperty("SFDC_Total_${t}_Count",   (s + f).toString())
            if (s)     log.addCustomHeaderProperty("SFDC_Success_${t}_Count", s.toString())
            if (f)     log.addCustomHeaderProperty("SFDC_Failed_${t}_Count",  f.toString())
        }
        if (failed) log.addAttachmentAsString('SFDC_Posting_Failures', csv, 'text/csv')
    }

    message.setBody(csv)
    return message
}