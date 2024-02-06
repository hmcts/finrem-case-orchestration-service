package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class JsonRemoveParentObjectExampleTest {

    private static final String RESOURCE = "/fixtures/contested-updateFrcInformation-twoCourtListsA.json";
    ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode requestContent;


    @Test
    public void shouldParseJson() throws URISyntaxException, IOException {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(RESOURCE).toURI()));

        FinremCaseDetails finremCaseDetails = buildCaseDetailsFromJson(RESOURCE);

        RemoveCaseDocumentNodeFromCaseData removeCaseDocumentNodeFromCaseData = new RemoveCaseDocumentNodeFromCaseData();
        objectMapper.convertValue(requestContent, JsonNode.class);
//        caseDocumentJsonFinder.removeCaseDocumentFromJson(requestContent, "http://dm-store-aat.service.core-compute-aat.internal/documents/03413320-c0bb-4571-a7bf-8078417ac556", finremCaseDetails);
        removeCaseDocumentNodeFromCaseData.removeCaseDocumentFromFinremCaseDetails(finremCaseDetails,
            "http://dm-store-aat.service.core-compute-aat.internal/documents/7f87da57-fb57-4307-bc9f-79ce9b2496d6");

    }


    private FinremCaseDetails buildCaseDetailsFromJson(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
