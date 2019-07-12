package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class RespondToOrderDataTest {

    private String json = " {\n"
            + "          \"id\": \"1\",\n"
            + "          \"value\": {\n"
            + "            \"DocumentType\": \"AmendedConsentOrder\",\n"
            + "            \"DocumentLink\": {\n"
            + "              \"document_url\": \"http://doc1\",\n"
            + "              \"document_filename\": \"doc1\",\n"
            + "              \"document_binary_url\": \"http://doc1.binary\"\n"
            + "            },\n"
            + "            \"DocumentDateAdded\": \"2010-01-02\",\n"
            + "            \"DocumentFileName\": \"file1\"\n"
            + "          }\n"
            + "        }";

    @Test
    public void shouldConvertRespondToOrderDataObject() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        RespondToOrderData respondToOrderData = objectMapper.readValue(json, RespondToOrderData.class);
        assertThat(respondToOrderData, is(notNullValue()));
        assertThat(respondToOrderData.getId(), is("1"));
        RespondToOrder respondToOrder = respondToOrderData.getRespondToOrder();
        assertThat(respondToOrder, is(notNullValue()));
        assertThat(respondToOrder.getDocumentAdded(), is(notNullValue()));
        assertThat(respondToOrder.getDocumentType(), is("AmendedConsentOrder"));
        CaseDocument documentLink = respondToOrder.getDocumentLink();
        assertThat(documentLink, is(notNullValue()));
        assertThat(documentLink.getDocumentUrl(), is("http://doc1"));
        assertThat(documentLink.getDocumentFilename(), is("doc1"));
        assertThat(documentLink.getDocumentBinaryUrl(), is("http://doc1.binary"));


    }
}