package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

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

    @Test
    public void givenApplicantRequiresLetterNotification_whenGetUpdateFrcInfoLetter_thenReturnApplicantLetter() {
        setUpLitigantMockContext();
        Optional<Document> applicantLetter = updateFrcInfoApplicantDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(applicantLetter.isPresent());
        assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue());
        assertAndVerifyDocumentIsGenerated(applicantLetter.get());
        assertEquals(applicantLetter.get().getFilename(), LIT_DOC_FILENAME);
        assertEquals(applicantLetter.get().getUrl(), APP_LITIGANT_URL);
    }

    @Test
    public void givenAppSolicitorRequiresLetterNotification_whenGetUpdateFrcInfoLetter_thenReturnAppSolLetter() {
        setUpSolicitorMockContext();
        Optional<Document> appSolLetter = updateFrcInfoApplicantDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(appSolLetter.isPresent());
        assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue());
        assertAndVerifyDocumentIsGenerated(appSolLetter.get());
        assertEquals(appSolLetter.get().getFilename(), SOL_DOC_FILENAME);
        assertEquals(appSolLetter.get().getUrl(), APP_SOLICITOR_URL);
    }

    @Test
    public void givenNoLetterNotificationsRequired_whenGetUpdateFrcInfoLetter_thenReturnEmptyOptional() {
        setUpNoLetterMockContext();
        Optional<Document> shouldBeEmpty = updateFrcInfoApplicantDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(shouldBeEmpty.isEmpty());
    }

    private void setUpLitigantMockContext() {
        when(documentConfiguration.getUpdateFRCInformationLitigantTemplate()).thenReturn(LIT_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationLitigantFilename()).thenReturn(LIT_DOC_FILENAME);
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(LIT_DOC_TEMPLATE),
            eq(LIT_DOC_FILENAME)))
            .thenReturn(new Document(APP_LITIGANT_URL, null, LIT_DOC_FILENAME));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpSolicitorMockContext() {
        when(documentConfiguration.getUpdateFRCInformationSolicitorTemplate()).thenReturn(SOL_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationSolicitorFilename()).thenReturn(SOL_DOC_FILENAME);
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.NO);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(SOL_DOC_TEMPLATE),
            eq(SOL_DOC_FILENAME)))
            .thenReturn(new Document(APP_SOLICITOR_URL, null, SOL_DOC_FILENAME));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpNoLetterMockContext() {
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.YES);
    }
}
