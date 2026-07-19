import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def body = (message.getBody(String) as String)
        .replaceAll(/"allOrNone"\s*:\s*"(true|false)"/, '"allOrNone":$1')

    def m = body =~ /"records"\s*:\s*\{/
    if (m.find()) {
        int open = body.indexOf('{', m.start()), i = open + 1, d = 1
        while (d > 0) { def c = body[i++]; if (c == '{') d++ else if (c == '}') d-- }
        body = body[0..<open] + '[' + body[open..<i] + ']' + body[i..-1]
    }
    message.setBody(body)
    message.setHeader('Content-Type', 'application/json')
    return message
}