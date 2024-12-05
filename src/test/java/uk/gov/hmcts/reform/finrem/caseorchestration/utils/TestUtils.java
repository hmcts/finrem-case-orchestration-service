package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.InputStream;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DATA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DETAILS;

public abstract class TestUtils {

    /**
     * Builds a {@link CaseDetails} object from a JSON resource file.
     *
     * <p>This utility method reads a JSON file from the classpath and maps its content to
     * a {@link CaseDetails} object using the provided {@link ObjectMapper}. It is primarily
     * designed for use in tests to quickly create {@link CaseDetails} instances from test data.
     *
     * @param objectMapper the {@link ObjectMapper} to use for JSON deserialization
     * @param testJson the path to the JSON resource file on the classpath
     * @return the {@link CaseDetails} object deserialized from the JSON content
     * @throws RuntimeException if an error occurs while reading or deserializing the JSON resource
     */
    public static CaseDetails buildCaseDetailsFromJson(ObjectMapper objectMapper, String testJson) {
        try (InputStream resourceAsStream = TestUtils.class.getResourceAsStream(testJson)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts the case data from the provided placeholders map.
     *
     * <p>This utility method is the recommended approach for retrieving case data
     * in tests, replacing the older {@code getCaseData(Map)} method. It ensures
     * consistent and maintainable test utility practices.
     *
     * @param placeholdersMap the map containing placeholders, including case details
     * @return the extracted case data as a map
     * @throws IllegalArgumentException if the placeholders map does not contain expected keys
     */
    public static Map<String, Object> getCaseData(Map<String, Object> placeholdersMap) {
        if (placeholdersMap == null || !placeholdersMap.containsKey(CASE_DETAILS)) {
            throw new IllegalArgumentException("Placeholders map must contain a key for CASE_DETAILS");
        }
        Map<String, Object> actualCaseDetails = (Map<String, Object>) placeholdersMap.get(CASE_DETAILS);
        if (actualCaseDetails == null || !actualCaseDetails.containsKey(CASE_DATA)) {
            throw new IllegalArgumentException("Case details must contain a key for CASE_DATA");
        }
        return (Map<String, Object>) actualCaseDetails.get(CASE_DATA);
    }
}
