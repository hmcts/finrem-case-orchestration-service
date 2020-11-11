package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class RespondToOrderTest {

    private String json = "{\n"
        + "            \"DocumentType\": \"AmendedConsentOrder\",\n"
        + "            \"DocumentLink\": {\n"
        + "              \"document_url\": \"http://doc1\",\n"
        + "              \"document_filename\": \"doc1\",\n"
        + "              \"document_binary_url\": \"http://doc1/binary\"\n"
        + "            },\n"
        + "            \"DocumentDateAdded\": \"2010-01-02\",\n"
        + "            \"DocumentFileName\": \"file1\"\n"
        + "          }";

    @Test
    public void shouldPopulateRespondToOrderObject() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RespondToOrder respondToOrder = objectMapper.readValue(json, RespondToOrder.class);
        assertThat(respondToOrder, is(notNullValue()));
        CaseDocument documentLink = respondToOrder.getDocumentLink();
        assertThat(documentLink, is(notNullValue()));
        assertThat(documentLink.getDocumentUrl(), is("http://doc1"));
        assertThat(documentLink.getDocumentFilename(), is("doc1"));
        assertThat(documentLink.getDocumentBinaryUrl(), is("http://doc1/binary"));
        assertThat(respondToOrder.getDocumentType(), is("AmendedConsentOrder"));
        assertThat(respondToOrder.getDocumentAdded(), is(notNullValue()));
    }
}