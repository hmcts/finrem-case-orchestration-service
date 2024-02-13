package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CaseDocumentCleanerTest {

    private static final String RESOURCE = "/fixtures/contested-updateFrcInformation-twoCourtListsA.json";
    ObjectMapper objectMapper = new ObjectMapper();
    CaseDocumentCleaner caseDocumentCleaner = new CaseDocumentCleaner();

    FinremCaseDetails finremCaseDetails;


    @Before
    public void setUp() throws URISyntaxException, IOException {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        finremCaseDetails = buildCaseDetailsFromJson(RESOURCE);
    }


    @Test
    public void shouldReplaceCaseDocumentByFindingMatchingDocUrl() throws JsonProcessingException {

        CaseDocument replacementCaseDoc = CaseDocument.builder()
            .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/03413320-c0bb-4571-a7bf-8078417bnc24")
            .documentBinaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/03413320-c0bb-4571-a7bf-8078417bnc24/binary")
            .documentFilename("replacementDoc.pdf")
            .build();

        String docUrlToReplace = "http://dm-store-aat.service.core-compute-aat.internal/documents/3bd00934-36f7-4375-b18e-8fd8bdf67280";
        assertThat(finremCaseDetails.getData().getMiniFormA().getDocumentUrl(), is(docUrlToReplace
        ));


        finremCaseDetails = caseDocumentCleaner.removeOrReplaceCaseDocumentFromFinremCaseDetails(finremCaseDetails,
            docUrlToReplace,
            replacementCaseDoc);

        assertThat(finremCaseDetails.getData().getMiniFormA().getDocumentUrl(), is(replacementCaseDoc.getDocumentUrl()
        ));
        assertThat(finremCaseDetails.getData().getMiniFormA().getDocumentFilename(), is(replacementCaseDoc.getDocumentFilename()
        ));
        assertThat(finremCaseDetails.getData().getMiniFormA().getDocumentBinaryUrl(), is(replacementCaseDoc.getDocumentBinaryUrl()
        ));

    }

    @Test
    public void shouldReplaceMultipleCaseDocumentsByFindingMatchingDocUrl() throws IOException {

        CaseDocument replacementCaseDoc = CaseDocument.builder()
            .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/03413320-c0bb-4571-a7bf-8078417bnc24")
            .documentBinaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/03413320-c0bb-4571-a7bf-8078417bnc24/binary")
            .documentFilename("replacementDoc.pdf")
            .build();

        String docUrlToReplace = "http://dm-store-aat.service.core-compute-aat.internal/documents/03413320-c0bb-4571-a7bf-8078417ac556";

        finremCaseDetails.getData().getCopyOfPaperFormA().forEach(copyOfPaperFormA -> {
            CaseDocument paperFormADoc = copyOfPaperFormA.getValue().getUploadedDocument();
            assertThat(paperFormADoc.getDocumentUrl(), is(docUrlToReplace));
        });


        finremCaseDetails = caseDocumentCleaner.removeOrReplaceCaseDocumentFromFinremCaseDetails(finremCaseDetails,
            docUrlToReplace,
            replacementCaseDoc);

        finremCaseDetails.getData().getCopyOfPaperFormA().forEach(copyOfPaperFormA -> {
            CaseDocument paperFormADoc = copyOfPaperFormA.getValue().getUploadedDocument();
            assertThat(paperFormADoc.getDocumentUrl(), is(replacementCaseDoc.getDocumentUrl()));
            assertThat(paperFormADoc.getDocumentFilename(), is(replacementCaseDoc.getDocumentFilename()));
            assertThat(paperFormADoc.getDocumentBinaryUrl(), is(replacementCaseDoc.getDocumentBinaryUrl()));
        });
    }



    private FinremCaseDetails buildCaseDetailsFromJson(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
