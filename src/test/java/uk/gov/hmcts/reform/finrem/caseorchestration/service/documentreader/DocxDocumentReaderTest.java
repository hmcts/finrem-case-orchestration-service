package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentreader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.getPathFromResources;

@ExtendWith(MockitoExtension.class)
class DocxDocumentReaderTest {

    @InjectMocks
    private DocxDocumentReader underTest;

    @BeforeEach
    public void setUp() {
        underTest = new DocxDocumentReader();
    }

    @Test
    void testGetContentFromADocxFile() throws IOException {
        byte[] document = Files.readAllBytes(getPathFromResources("fixtures/documentcontentvalidation/generalOrder.docx"));

        assertThat(underTest.getContent(document)).containsAnyOf("1. The applicant is Doris Duck The respondent is Davey Duck ",
            "Case No: 1708940824018030 ");
    }

    @Test
    void testGetContent_withError() throws IOException {
        byte[] document = Files.readAllBytes(getPathFromResources("fixtures/documentcontentvalidation/generalOrder.pdf"));

        assertThatThrownBy(() -> underTest.getContent(document)).isInstanceOf(Exception.class);
    }
}
