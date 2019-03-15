package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DocumentDataTest extends DocumentTypeTest {
    private DocumentData documentData;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        documentData = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/document-data.json").toURI()), DocumentData.class);
        doc = documentData.getDocumentType();
    }

    @Test
    public void shouldCreateDocumentDataFromJson() {
        assertThat(documentData.getId(), is("1"));
    }
}