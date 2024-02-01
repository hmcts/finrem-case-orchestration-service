package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
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


        RemoveCaseDocumentNodeFromCaseData caseDocumentJsonFinder = new RemoveCaseDocumentNodeFromCaseData();
        caseDocumentJsonFinder.findCaseDocumentFromJson(requestContent, "air76_quick_guide copy.pdf");
    }

}
