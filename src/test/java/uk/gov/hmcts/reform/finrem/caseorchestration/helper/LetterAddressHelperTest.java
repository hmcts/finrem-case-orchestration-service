package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.LINE_SEPARATOR;

public class LetterAddressHelperTest {

    private ObjectMapper mapper = new ObjectMapper();

    private LetterAddressHelper letterAddressHelper = new LetterAddressHelper();

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream =
                     getClass().getResourceAsStream("/fixtures/bulk-print.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    @Test
    public void testAddressIsCorrectlyFormatterForLetterPrinting() throws Exception {

        Map<String, Object> testAddress = (Map<String, Object>) caseDetails().getData().get("applicantAddress");

        String formattedAddress = letterAddressHelper.formatAddressForLetterPrinting(testAddress);

        String expectedAddress = "1 Victoria Street" + LINE_SEPARATOR
            + "Westminster" + LINE_SEPARATOR
            + "" + LINE_SEPARATOR
            + "Greater London" + LINE_SEPARATOR
            + "UK" + LINE_SEPARATOR
            + "London" + LINE_SEPARATOR
            + "SE1";

        assertThat(formattedAddress, is(expectedAddress));
    }
}