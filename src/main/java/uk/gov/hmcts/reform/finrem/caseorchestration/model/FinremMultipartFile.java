package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Builder
public class FinremMultipartFile implements MultipartFile {

    private String name;
    private byte[] content;
    private String contentType;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getOriginalFilename() {
        return this.name;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return this.content;
    }

    @Override
    public InputStream getInputStream()  {
        return null;
    }

    @Override
    public void transferTo(File dest) throws IllegalStateException {

    }
}
