import com.sap.gateway.ip.core.customdev.util.Message

/**
 * Prepare Archive Request
 */
def Message processData(Message message) {

    long delayMs = 500L
    def headers = message.getHeaders()
    if (headers != null) {
        def retryAfter = headers.get('Retry-After') ?: headers.get('retry-after')
        if (retryAfter != null) {
            try {
                delayMs = (Long.parseLong(retryAfter.toString()) * 1000L) + 500L
            } catch (Exception ignored) {
                delayMs = 2000L
            }
        }
    }

    Thread.sleep((int) Math.min(delayMs, 5000L))
    message.setBody('{"destinationId":"archive"}')
    return message
}
