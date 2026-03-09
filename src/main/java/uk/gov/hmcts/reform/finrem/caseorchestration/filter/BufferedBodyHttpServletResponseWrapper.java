package uk.gov.hmcts.reform.finrem.caseorchestration.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

class BufferedBodyHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    public BufferedBodyHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (outputStream == null) {
            outputStream = new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // No implementation needed
                }

                @Override
                public void write(int b) {
                    buffer.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) {
                    buffer.write(b, off, len);
                }
            };
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), StandardCharsets.UTF_8), true);
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        if (outputStream != null) {
            outputStream.flush();
        }
    }

    public String getResponseBody() {
        if (writer != null) {
            writer.flush();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /**
     * Copies the buffered response body to the real response output stream
     * so the client actually receives the response.
     */
    public void copyBodyToResponse() throws IOException {
        byte[] body = buffer.toByteArray();
        HttpServletResponse rawResponse = (HttpServletResponse) getResponse();
        rawResponse.setContentLength(body.length);
        rawResponse.getOutputStream().write(body);
        rawResponse.getOutputStream().flush();
    }
}