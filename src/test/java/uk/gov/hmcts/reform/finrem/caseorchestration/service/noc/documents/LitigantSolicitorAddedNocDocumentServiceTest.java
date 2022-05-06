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

@RunWith(MockitoJUnitRunner.class)
public class LitigantSolicitorAddedNocDocumentServiceTest extends NocDocumentServiceBaseTest {

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
        when(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()).thenReturn(DOC_TEMPLATE);
        when(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()).thenReturn(DOC_FILENAME);
        when(
            genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), notiicationLettersDetailsMapCaptor.capture(), eq(DOC_TEMPLATE),
                eq(DOC_FILENAME))).thenReturn(new CaseDocument());
        CaseDocument caseDocument =
            litigantSolicitorAddedNocLetterGenerator.generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetails);

        Map placeholdersMap = notiicationLettersDetailsMapCaptor.getValue();
        assertPlaceHoldersMap(placeholdersMap);
        assertAndVerifyDocumentsAreGenerated(caseDocument);
    }


}