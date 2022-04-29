package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddedNocLetterGeneratorTest extends NocLetterGeneratorBaseTest {

    @InjectMocks
    SolicitorNocLetterGenerator solicitorAddedNocLetterGenerator;

    @Before
    public void setUpTest() {
        solicitorAddedNocLetterGenerator =
            new SolicitorNocLetterGenerator(genericDocumentService, objectMapper, documentConfiguration);
    }

    @Test
    public void shouldGenerateLitigantSolicitorAddedDocuments() {
        when(documentConfiguration.getNocLetterNotificationSolicitorTemplate()).thenReturn(DOC_TEMPLATE);
        when(documentConfiguration.getNocLetterNotificationSolicitorFileName()).thenReturn(DOC_FILENAME);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(AUTH_TOKEN, notiicationLettersDetailsMap, DOC_TEMPLATE,
            DOC_FILENAME)).thenReturn(new CaseDocument());
        CaseDocument caseDocument = solicitorAddedNocLetterGenerator.generateNoticeOfLetter(AUTH_TOKEN, noticeOfChangeLetterDetails);

        assertAndVerifyDocumentsAreGenerated(caseDocument);
    }
}