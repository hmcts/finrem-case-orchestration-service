package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFrcInfoRespondentServiceTest extends BaseUpdateFrcInfoDocumentServiceSetup {

    private static final String RESP_LITIGANT_URL = "respLitigantUrl";
    private static final String RESP_SOLICITOR_URL = "respSolicitorUrl";

    @InjectMocks
    UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService;

    @Test
    void givenRespondentRequiresLetterNotification_whenGetUpdateFrcInfoLetter_thenReturnRespondentLetter() {
        setUpLitigantMockContext();
        Optional<CaseDocument> applicantLetter = updateFrcInfoRespondentDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(applicantLetter.isPresent());
        assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue());
        assertAndVerifyDocumentIsGenerated(applicantLetter.get());
        assertEquals(LIT_DOC_FILENAME, applicantLetter.get().getDocumentFilename());
        assertEquals(RESP_LITIGANT_URL, applicantLetter.get().getDocumentUrl());
    }

    @Test
    void givenRespSolicitorRequiresLetterNotification_whenGetUpdateFrcInfoLetter_thenReturnRespSolLetter() {
        setUpSolicitorMockContext();
        Optional<CaseDocument> appSolLetter = updateFrcInfoRespondentDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(appSolLetter.isPresent());
        assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue());
        assertAndVerifyDocumentIsGenerated(appSolLetter.get());
        assertEquals(SOL_DOC_FILENAME, appSolLetter.get().getDocumentFilename());
        assertEquals(RESP_SOLICITOR_URL, appSolLetter.get().getDocumentUrl());
    }

    @Test
    void givenNoLetterNotificationsRequired_whenGetUpdateFrcInfoLetter_thenReturnEmptyOptional() {
        setUpNoLetterMockContext();
        Optional<CaseDocument> shouldBeEmpty = updateFrcInfoRespondentDocumentService
            .getUpdateFrcInfoLetter(caseDetails, AUTH_TOKEN);

        assertTrue(shouldBeEmpty.isEmpty());
    }

    private void setUpLitigantMockContext() {
        when(documentConfiguration.getUpdateFRCInformationLitigantTemplate()).thenReturn(LIT_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationLitigantFilename()).thenReturn(LIT_DOC_FILENAME);
        when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(LIT_DOC_TEMPLATE),
            eq(LIT_DOC_FILENAME), eq("1234")))
            .thenReturn(new CaseDocument(RESP_LITIGANT_URL, LIT_DOC_FILENAME, null, "", null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpSolicitorMockContext() {
        when(documentConfiguration.getUpdateFRCInformationSolicitorTemplate()).thenReturn(SOL_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationSolicitorFilename()).thenReturn(SOL_DOC_FILENAME);
        when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(SOL_DOC_TEMPLATE),
            eq(SOL_DOC_FILENAME), eq("1234")))
            .thenReturn(new CaseDocument(RESP_SOLICITOR_URL, SOL_DOC_FILENAME, null, "", null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpNoLetterMockContext() {
        when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
    }
}
