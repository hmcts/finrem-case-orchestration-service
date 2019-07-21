package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PensionDocumentDataTest {

    private final String json = "{\n"
            + "          \"id\": \"1\",\n"
            + "          \"value\": {\n"
            + "            \"typeOfDocument\": \"pdf\",\n"
            + "            \"uploadedDocument\": {\n"
            + "              \"document_url\": \"http://file1\",\n"
            + "              \"document_filename\": \"file1.pdf\",\n"
            + "              \"document_binary_url\": \"http://file1.binary\"\n"
            + "            }\n"
            + "          }\n"
            + "        }";

    @Test
    public void shouldGetPensionDocumentData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        PensionDocumentData pensionDocumentData = objectMapper.readValue(json, PensionDocumentData.class);
        assertThat(pensionDocumentData, is(notNullValue()));

    }


}