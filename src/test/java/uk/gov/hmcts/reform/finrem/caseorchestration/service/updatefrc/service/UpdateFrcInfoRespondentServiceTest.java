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
public class UpdateFrcInfoRespondentServiceTest extends BaseUpdateFrcInfoDocumentServiceTest {

    private static final String RESP_LITIGANT_URL = "respLitigantUrl";
    private static final String RESP_SOLICITOR_URL = "respSolicitorUrl";

    @InjectMocks
    UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService;

    @Test
    public void givenRespondentRequiresLetterNotification_whenGetUpdateFrcInfoLetter_thenReturnRespondentLetter() {
        setUpLitigantMockContext();
        Optional<Document> applicantLetter = updateFrcInfoRespondentDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(applicantLetter.isPresent());
        assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue());
        assertAndVerifyDocumentIsGenerated(applicantLetter.get());
        assertEquals(applicantLetter.get().getFilename(), LIT_DOC_FILENAME);
        assertEquals(applicantLetter.get().getUrl(), RESP_LITIGANT_URL);
    }

    @Test
    public void givenRespSolicitorRequiresLetterNotification_whenGetUpdateFrcInfoLetter_thenReturnRespSolLetter() {
        setUpSolicitorMockContext();
        Optional<Document> appSolLetter = updateFrcInfoRespondentDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(appSolLetter.isPresent());
        assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue());
        assertAndVerifyDocumentIsGenerated(appSolLetter.get());
        assertEquals(appSolLetter.get().getFilename(), SOL_DOC_FILENAME);
        assertEquals(appSolLetter.get().getUrl(), RESP_SOLICITOR_URL);
    }

    @Test
    public void givenNoLetterNotificationsRequired_whenGetUpdateFrcInfoLetter_thenReturnEmptyOptional() {
        setUpNoLetterMockContext();
        Optional<Document> shouldBeEmpty = updateFrcInfoRespondentDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(shouldBeEmpty.isEmpty());
    }

    private void setUpLitigantMockContext() {
        when(documentConfiguration.getUpdateFRCInformationLitigantTemplate()).thenReturn(LIT_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationLitigantFilename()).thenReturn(LIT_DOC_FILENAME);
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(LIT_DOC_TEMPLATE),
            eq(LIT_DOC_FILENAME)))
            .thenReturn(new Document(RESP_LITIGANT_URL, LIT_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpSolicitorMockContext() {
        when(documentConfiguration.getUpdateFRCInformationSolicitorTemplate()).thenReturn(SOL_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationSolicitorFilename()).thenReturn(SOL_DOC_FILENAME);
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        caseDetails.getCaseData().setRespSolNotificationsEmailConsent(YesOrNo.NO);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(SOL_DOC_TEMPLATE),
            eq(SOL_DOC_FILENAME)))
            .thenReturn(new Document(RESP_SOLICITOR_URL, SOL_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpNoLetterMockContext() {
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        caseDetails.getCaseData().setRespSolNotificationsEmailConsent(YesOrNo.YES);
    }
}
