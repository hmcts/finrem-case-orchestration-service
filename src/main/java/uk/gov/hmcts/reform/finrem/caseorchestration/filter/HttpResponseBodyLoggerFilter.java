package uk.gov.hmcts.reform.finrem.caseorchestration.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that wraps outgoing responses and logs the response body.
 * This filter is only expected to be used during development.
 *
 * <p>
 * This bean is only enabled if the property 'finrem.response-body-logger' is set to true in application.properties.
 */
@Component
@ConditionalOnProperty(prefix = "finrem.response-body-logger", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class HttpResponseBodyLoggerFilter implements Filter {

    private final ObjectMapper objectMapper;

    /**
     * Wraps the outgoing response, forwards the request through the filter chain,
     * then logs the response body and copies it to the real response.
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

        BufferedBodyHttpServletResponseWrapper responseWrapper =
            new BufferedBodyHttpServletResponseWrapper((HttpServletResponse) servletResponse);

        filterChain.doFilter(servletRequest, responseWrapper);

        String requestUri = ((HttpServletRequest) servletRequest).getRequestURI();
        logResponseBody(requestUri, responseWrapper.getResponseBody());

        responseWrapper.copyBodyToResponse();
    }

    /**
     * Pretty prints and logs the response body if it is valid JSON.
     *
     * @param contextPath  the request URI for context in the log message
     * @param responseBody the raw response body
     */
    private void logResponseBody(String contextPath, String responseBody) {
        ObjectWriter prettyPrinter = objectMapper.writerWithDefaultPrettyPrinter();

        try {
            Object json = objectMapper.readValue(responseBody, Object.class);
            log.debug("{} response body:\n{}", contextPath, prettyPrinter.writeValueAsString(json));
        } catch (IOException e) {
            log.debug("{} response body (non-JSON):\n{}", contextPath, responseBody);
        }
    }
}