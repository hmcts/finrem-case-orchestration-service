package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class GeneralApplicationDataTest {
    private final String json = "{\n"
        + "          \"id\": \"1\",\n"
        + "          \"value\": {\n"
        + "            \"generalApplicationDocument\": {\n"
        + "              \"document_url\": \"http://file1\",\n"
        + "              \"document_filename\": \"file1.pdf\",\n"
        + "              \"document_binary_url\": \"http://file1.binary\"\n"
        + "            }\n"
        + "          }\n"
        + "        }";

    @Test
    public void shouldGetGeneralApplicationData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        GeneralApplicationData generalApplicationData = objectMapper.readValue(json, GeneralApplicationData.class);

        assertThat(generalApplicationData.getId(), is("1"));

        GeneralApplication generalApplication = generalApplicationData.getGeneralApplication();
        CaseDocument pensionDocument = generalApplication.getGeneralApplicationDocument();

        assertThat(pensionDocument.getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(pensionDocument.getDocumentUrl(), is("http://file1"));
        assertThat(pensionDocument.getDocumentFilename(), is("file1.pdf"));
    }

}