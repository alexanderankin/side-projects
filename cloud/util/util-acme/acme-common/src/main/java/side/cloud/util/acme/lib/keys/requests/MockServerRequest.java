package side.cloud.util.acme.lib.keys.requests;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.RequestEntity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

class MockServerRequest extends HttpServletRequestWrapper {
    private final RequestEntity<String> requestEntity;

    public MockServerRequest(RequestEntity<String> requestEntity) {
        super(new EmptyRequest());
        this.requestEntity = requestEntity;
    }

    @Override
    public String getMethod() {
        return Objects.requireNonNull(requestEntity.getMethod()).name();
    }

    @Override
    public String getRequestURI() {
        return requestEntity.getUrl().getPath();
    }

    @Override
    public String getQueryString() {
        return requestEntity.getUrl().getQuery();
    }

    @Override
    public String getScheme() {
        return requestEntity.getUrl().getScheme();
    }

    @Override
    public String getServerName() {
        return requestEntity.getUrl().getHost();
    }

    @Override
    public int getServerPort() {
        int p = requestEntity.getUrl().getPort();
        if (p != -1) return p;
        return "https".equals(getScheme()) ? 443 : 80;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(requestEntity.getHeaders().headerNames());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(
                requestEntity.getHeaders().getOrDefault(name, List.of())
        );
    }

    @Override
    public String getHeader(String name) {
        return requestEntity.getHeaders().getFirst(name);
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(Objects.requireNonNull(requestEntity.getBody()).getBytes());
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(jakarta.servlet.ReadListener rl) {
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
}
