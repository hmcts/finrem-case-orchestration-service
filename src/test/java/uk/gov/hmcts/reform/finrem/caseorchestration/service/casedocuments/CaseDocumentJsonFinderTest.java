package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class CaseDocumentJsonFinderTest {

    private static final String RESOURCE = "/fixtures/contested-updateFrcInformation-twoCourtListsA.json";
    private static final String CASE_DETAILS_KEY = "case_details";
    ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode requestContent;


    @Test
    public void shouldParseJson() throws URISyntaxException, IOException {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(RESOURCE).toURI()));
        FinremCaseDetails caseDetails = objectMapper.convertValue(requestContent.get(CASE_DETAILS_KEY), FinremCaseDetails.class);
        String finRemCaseDetailsAsString = objectMapper.writeValueAsString(caseDetails);
        requestContent = objectMapper.readTree(finRemCaseDetailsAsString);

        CaseDocumentJsonFinder caseDocumentJsonFinder = new CaseDocumentJsonFinder();
        caseDocumentJsonFinder.findCaseDocumentJson(requestContent, "OnlineForm.pdf");
    }

}
