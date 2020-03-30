package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.LINE_SEPARATOR;

public class LetterAddressHelperTest {

    private LetterAddressHelper letterAddressHelper = new LetterAddressHelper();

    @Test
    public void testAddressIsCorrectlyFormatterForLetterPrinting() throws Exception {

        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", "50 Applicant Street");
        testAddressMap.put("AddressLine2", "Second Address Line");
        testAddressMap.put("AddressLine3", "Third Address Line");
        testAddressMap.put("County", "Greater London");
        testAddressMap.put("Country", "England");
        testAddressMap.put("PostTown", "London");
        testAddressMap.put("PostCode", "SW1");

        String formattedAddress = letterAddressHelper.formatAddressForLetterPrinting(testAddressMap);

        String expectedAddress = "50 Applicant Street" + LINE_SEPARATOR
            + "Second Address Line" + LINE_SEPARATOR
            + "Third Address Line" + LINE_SEPARATOR
            + "Greater London" + LINE_SEPARATOR
            + "England" + LINE_SEPARATOR
            + "London" + LINE_SEPARATOR
            + "SW1";

        assertThat(formattedAddress, is(expectedAddress));
    }
}