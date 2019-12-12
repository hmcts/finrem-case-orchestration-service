package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DocumentValidationResponseTest {

    @Test
    public void properties() {
        DocumentValidationResponse response = documentValidationResponse();

        assertThat(response.getMimeType(), is("application/xml"));
        assertThat(response.getErrors(), hasItem("Invalid file type"));
    }

    private DocumentValidationResponse documentValidationResponse() {
        return DocumentValidationResponse.builder()
                .errors(Collections.singletonList("Invalid file type"))
                .mimeType("application/xml").build();
    }

    @Test
    public void equality() {
        DocumentValidationResponse response = documentValidationResponse();
        DocumentValidationResponse response1 = documentValidationResponse();

        assertThat(response, is(equalTo(response1)));
    }
}