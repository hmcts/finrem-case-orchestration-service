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
        + "            \"typeOfDocument\": \"Form P1\",\n"
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
        PensionTypeCollection pensionCollectionData = objectMapper.readValue(json, PensionTypeCollection.class);
        assertThat(pensionCollectionData, is(notNullValue()));
        assertThat(pensionCollectionData.getId(), is("1"));
        PensionType typedCaseDocument = pensionCollectionData.getTypedCaseDocument();
        assertThat(typedCaseDocument.getTypeOfDocument(), is(PensionDocumentType.FORM_P1));
        assertThat(typedCaseDocument.getPensionDocument(), is(notNullValue()));
        CaseDocument pensionDocument = typedCaseDocument.getPensionDocument();
        assertThat(pensionDocument.getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(pensionDocument.getDocumentUrl(), is("http://file1"));
        assertThat(pensionDocument.getDocumentFilename(), is("file1.pdf"));
    }

}