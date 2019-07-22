package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PensionCollectionDataTest {
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
    public void shouldGetPensionCollectionData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        PensionCollectionData pensionCollectionData = objectMapper.readValue(json, PensionCollectionData.class);
        assertThat(pensionCollectionData, is(notNullValue()));
        assertThat(pensionCollectionData.getId(), is("1"));
        PensionDocumentData pensionDocumentData = pensionCollectionData.getPensionDocumentData();
        assertThat(pensionDocumentData.getTypeOfDocument(), is("pdf"));
        assertThat(pensionDocumentData.getPensionDocument(), is(notNullValue()));
        CaseDocument pensionDocument = pensionDocumentData.getPensionDocument();
        assertThat(pensionDocument.getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(pensionDocument.getDocumentUrl(), is("http://file1"));
        assertThat(pensionDocument.getDocumentFilename(), is("file1.pdf"));
    }

}