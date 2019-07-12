package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AmendedConsentOrderTest {

    private String json = "{\n"
            + "            \"amendedConsentOrder\": {\n"
            + "              \"document_url\": \"http://doc1\",\n"
            + "              \"document_filename\": \"amendedConsentOrder.docx\",\n"
            + "              \"document_binary_url\": \"http://doc1/binary\"\n"
            + "            },\n"
            + "            \"amendedConsentOrderDate\": \"2018-10-10\"\n"
            + "          }\n";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldPopulateAmendedConsentOrderObject() throws Exception {
        AmendedConsentOrder amendedConsentOrder = objectMapper.readValue(json, AmendedConsentOrder.class);

        assertThat(amendedConsentOrder.getAmendedConsentOrderDate(), is(notNullValue()));
        CaseDocument amendedConsentOrder1 = amendedConsentOrder.getAmendedConsentOrder();
        assertThat(amendedConsentOrder1.getDocumentUrl(), is("http://doc1"));
        assertThat(amendedConsentOrder1.getDocumentFilename(), is("amendedConsentOrder.docx"));
        assertThat(amendedConsentOrder1.getDocumentBinaryUrl(), is("http://doc1/binary"));

    }
}