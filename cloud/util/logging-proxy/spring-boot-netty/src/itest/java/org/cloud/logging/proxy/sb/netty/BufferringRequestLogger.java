package org.cloud.logging.proxy.sb.netty;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.stereotype.Component;

@Component("requestLogger")
class BufferringRequestLogger extends AsyncLoggingProxyApp.RequestLogger {
    CircularFifoQueue<String> queue = new CircularFifoQueue<>(10);

    @Override
    void logRequest(String request) {
        queue.add(request);
        super.logRequest(request);
    }
}
