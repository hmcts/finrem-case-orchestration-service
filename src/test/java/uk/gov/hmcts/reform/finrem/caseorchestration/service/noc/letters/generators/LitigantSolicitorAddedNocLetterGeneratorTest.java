package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LitigantSolicitorAddedNocLetterGeneratorTest extends NocLetterGeneratorBaseTest {

    @InjectMocks
    LitigantSolicitorAddedNocLetterGenerator litigantSolicitorAddedNocLetterGenerator;

    @Before
    public void setUpTest() {
        litigantSolicitorAddedNocLetterGenerator =
            new LitigantSolicitorAddedNocLetterGenerator(genericDocumentService, objectMapper, documentConfiguration);
    }

    @Test
    public void shouldGenerateLitigantSolicitorAddedDocuments() {
        when(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()).thenReturn(DOC_TEMPLATE);
        when(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()).thenReturn(DOC_FILENAME);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(AUTH_TOKEN, notiicationLettersDetailsMap, DOC_TEMPLATE,
            DOC_FILENAME)).thenReturn(new CaseDocument());
        CaseDocument caseDocument =
            litigantSolicitorAddedNocLetterGenerator.generateNoticeOfLetter(AUTH_TOKEN, noticeOfChangeLetterDetails);

        assertAndVerifyDocumentsAreGenerated(caseDocument);
    }

}