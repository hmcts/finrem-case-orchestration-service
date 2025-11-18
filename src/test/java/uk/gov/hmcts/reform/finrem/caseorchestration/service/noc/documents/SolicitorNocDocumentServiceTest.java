package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorNocDocumentServiceTest extends NocDocumentServiceBaseTestSetup {

    @InjectMocks
    SolicitorNocDocumentService solicitorNocDocumentService;

    @Before
    @Override
    public void setUpTest() {
        super.setUpTest();
        solicitorNocDocumentService =
            new SolicitorNocDocumentService(genericDocumentService, objectMapper, documentConfiguration);
    }

    @Test
    public void shouldGenerateLitigantSolicitorAddedDocuments() {
        when(documentConfiguration.getNocLetterNotificationSolicitorTemplate()).thenReturn(DOC_TEMPLATE);
        when(documentConfiguration.getNocLetterNotificationSolicitorFileName()).thenReturn(DOC_FILENAME);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            notiicationLettersDetailsMapCaptor.capture(),
            eq(DOC_TEMPLATE),
            eq(DOC_FILENAME),
            eq(CONTESTED))).thenReturn(new CaseDocument());
        CaseDocument caseDocument = solicitorNocDocumentService.generateNoticeOfChangeLetter(
            AUTH_TOKEN, noticeOfChangeLetterDetails, CONTESTED);

        Map placeholdersMap = notiicationLettersDetailsMapCaptor.getValue();
        assertPlaceHoldersMap(placeholdersMap);
        assertAndVerifyDocumentsAreGenerated(caseDocument);
    }
}
