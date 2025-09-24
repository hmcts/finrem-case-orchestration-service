package uk.gov.hmcts.reform.finrem.caseorchestration.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that wraps incoming requests and logs the request body.
 * This filter is only expected to be used during development.
 *
 * <p>
 * This bean is only enabled if the property 'finrem.request-body-logger' is set to true in application.properties.
 */
@Component
@ConditionalOnProperty(prefix = "finrem.request-body-logger", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class HttpRequestBodyLoggerFilter implements Filter {

    private final ObjectMapper objectMapper;

    /**
     * Wraps the incoming request, logs the request body, and forwards the request.
     *
     * @param servletRequest  the incoming ServletRequest
     * @param servletResponse the outgoing ServletResponse
     * @param filterChain     the filter chain
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {

        BufferedBodyHttpServletRequestWrapper requestWrapper = new BufferedBodyHttpServletRequestWrapper((HttpServletRequest) servletRequest);
        logRequestBody(((HttpServletRequest) servletRequest).getRequestURI(), requestWrapper.getRequestBody());
        filterChain.doFilter(requestWrapper, servletResponse);
    }

    /**
     * Pretty prints and logs the request body if it is valid JSON.
     *
     * @param requestBody the raw request body
     */
    private void logRequestBody(String contextPath, String requestBody) {
        ObjectWriter prettyPrinter = objectMapper.writerWithDefaultPrettyPrinter();

        try {
            Object json = objectMapper.readValue(requestBody, Object.class);
            log.debug("{} request body:\n{}", contextPath, prettyPrinter.writeValueAsString(json));
        } catch (IOException e) {
            log.debug("{} request body (non-JSON):\n{}", contextPath, requestBody);
        }
    }
}
