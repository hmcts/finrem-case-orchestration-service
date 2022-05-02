package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LitigantSolicitorRemovedDocumentServiceTest extends NocDocumentServiceBaseTest {

    @InjectMocks
    LitigantSolicitorRemovedNocDocumentService litigantSolicitorRemovedNocDocumentService;

    @Before
    public void setUpTest() {
        litigantSolicitorRemovedNocDocumentService =
            new LitigantSolicitorRemovedNocDocumentService(genericDocumentService, objectMapper, documentConfiguration);
    }

    @Test
    public void shouldGenerateLitigantSolicitorAddedDocuments() {
        when(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedTemplate()).thenReturn(DOC_TEMPLATE);
        when(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedFileName()).thenReturn(DOC_FILENAME);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(AUTH_TOKEN, notiicationLettersDetailsMap, DOC_TEMPLATE,
            DOC_FILENAME)).thenReturn(new CaseDocument());
        CaseDocument caseDocument =
            litigantSolicitorRemovedNocDocumentService.generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetails);

        assertAndVerifyDocumentsAreGenerated(caseDocument);
    }
}