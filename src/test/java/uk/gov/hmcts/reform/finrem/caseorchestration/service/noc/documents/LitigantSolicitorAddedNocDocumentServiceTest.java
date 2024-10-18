package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@RunWith(MockitoJUnitRunner.class)
public class LitigantSolicitorAddedNocDocumentServiceTest extends NocDocumentServiceBaseTestSetup {

    public static final String CASE_ID = "1234";
    @InjectMocks
    LitigantSolicitorAddedNocDocumentService litigantSolicitorAddedNocLetterGenerator;

    @Before
    public void setUpTest() {
        super.setUpTest();
        litigantSolicitorAddedNocLetterGenerator =
            new LitigantSolicitorAddedNocDocumentService(genericDocumentService, objectMapper, documentConfiguration);
    }

    @Test
    public void shouldGenerateLitigantSolicitorAddedDocuments() {
        lenient().when(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()).thenReturn(DOC_TEMPLATE);
        lenient().when(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()).thenReturn(DOC_FILENAME);
        lenient().when(
            genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), notiicationLettersDetailsMapCaptor.capture(), eq(DOC_TEMPLATE),
                eq(DOC_FILENAME), eq(CASE_ID))).thenReturn(new CaseDocument());
        CaseDocument caseDocument =
            litigantSolicitorAddedNocLetterGenerator.generateNoticeOfChangeLetter(
                AUTH_TOKEN, noticeOfChangeLetterDetails, CASE_ID);

        Map placeholdersMap = notiicationLettersDetailsMapCaptor.getValue();
        assertPlaceHoldersMap(placeholdersMap);
        assertAndVerifyDocumentsAreGenerated(caseDocument);
    }


}