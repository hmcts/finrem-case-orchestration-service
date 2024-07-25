package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentreader;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class DocxDocumentReader {

    public String[] getContent(byte[] bytes) throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        XWPFDocument wordDoc = new XWPFDocument(is);
        XWPFWordExtractor extractor = new XWPFWordExtractor(wordDoc);
        String content = extractor.getText();
        return content.split(System.lineSeparator());
    }
}
