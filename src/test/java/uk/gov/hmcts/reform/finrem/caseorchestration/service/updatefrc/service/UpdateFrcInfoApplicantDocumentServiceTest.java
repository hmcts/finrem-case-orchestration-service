package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFrcInfoApplicantDocumentServiceTest extends BaseUpdateFrcInfoDocumentServiceTest {

    private static final String APP_LITIGANT_URL = "appLitigantUrl";
    private static final String APP_SOLICITOR_URL = "appSolicitorUrl";

    @InjectMocks
    UpdateFrcInfoApplicantDocumentService updateFrcInfoApplicantDocumentService;

    @Before
    public void setUp() {
        super.setUp();
    }

    private void setUpLitigantMockContext() {
        when(documentConfiguration.getUpdateFRCInformationLitigantTemplate()).thenReturn(LIT_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationLitigantFilename()).thenReturn(LIT_DOC_FILENAME);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(LIT_DOC_TEMPLATE),
            eq(LIT_DOC_FILENAME)))
            .thenReturn(new CaseDocument(APP_LITIGANT_URL, LIT_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpSolicitorMockContext() {
        when(documentConfiguration.getUpdateFRCInformationSolicitorTemplate()).thenReturn(SOL_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationSolicitorFilename()).thenReturn(SOL_DOC_FILENAME);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(SOL_DOC_TEMPLATE),
            eq(SOL_DOC_FILENAME)))
            .thenReturn(new CaseDocument(APP_SOLICITOR_URL, SOL_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpNoLetterMockContext() {
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
    }

    @Test
    public void givenApplicantRequiresLetterNotification_whenGetUpdateFrcInfoLetter_thenReturnApplicantLetter() {
        setUpLitigantMockContext();
        Optional<CaseDocument> applicantLetter = updateFrcInfoApplicantDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(applicantLetter.isPresent());
        assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue());
        assertAndVerifyDocumentIsGenerated(applicantLetter.get());
        assertEquals(applicantLetter.get().getDocumentFilename(), LIT_DOC_FILENAME);
        assertEquals(applicantLetter.get().getDocumentUrl(), APP_LITIGANT_URL);
    }

    @Test
    public void givenAppSolicitorRequiresLetterNotification_whenGetUpdateFrcInfoLetter_thenReturnAppSolLetter() {
        setUpSolicitorMockContext();
        Optional<CaseDocument> appSolLetter = updateFrcInfoApplicantDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(appSolLetter.isPresent());
        assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue());
        assertAndVerifyDocumentIsGenerated(appSolLetter.get());
        assertEquals(appSolLetter.get().getDocumentFilename(), SOL_DOC_FILENAME);
        assertEquals(appSolLetter.get().getDocumentUrl(), APP_SOLICITOR_URL);
    }

    @Test
    public void givenNoLetterNotificationsRequired_whenGetUpdateFrcInfoLetter_thenReturnEmptyOptional() {
        setUpNoLetterMockContext();
        Optional<CaseDocument> shouldBeEmpty = updateFrcInfoApplicantDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(shouldBeEmpty.isEmpty());
    }
}
