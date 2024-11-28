package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;

@ExtendWith(MockitoExtension.class)
public class PaperCaseCreateContestedAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON = "/fixtures/contested/validate-hearing-successfully.json";
    public static final String AUTH_TOKEN = "tokien:)";
    private PaperCaseCreateContestedAboutToSubmitHandler handler;
    @Mock
    private IdamService idamService;
    @Mock
    private CaseFlagsService caseFlagsService;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        handler = new PaperCaseCreateContestedAboutToSubmitHandler(finremCaseDetailsMapper, caseFlagsService, idamService);
    }

    @Test
    void canHandle() {
        assertTrue(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.NEW_PAPER_CASE));
    }

    @Test
    void canNotHandle() {
        assertFalse(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.NEW_PAPER_CASE));
    }

    @Test
    void canNotHandleWrongEventType() {
        assertFalse(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void canNotHandleWrongCallbackType() {
        assertFalse(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.NEW_PAPER_CASE));
    }

    @Test
    void shouldSuccessfullyReturnAsAdminConsentedPaperCase() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_HWF_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);
        when(caseData.getFastTrackDecision().isNoOrNull()).thenReturn(true);

        assertEquals(caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole(), CaseRole.APP_SOLICITOR.getCcdCode());
        assertEquals(caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole(), CaseRole.RESP_SOLICITOR.getCcdCode());
        assertEquals(caseData.getContactDetailsWrapper().getIsAdmin(), YES_VALUE);
        assertEquals(caseData.getFastTrackDecision(), YesOrNo.YES);
        assertEquals(caseData.getPaperApplication(), YesOrNo.YES);
    }

    @Test
    void shouldSuccessfullyReturnNotAsAdminConsentedPaperCase() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(caseData.getFastTrackDecision().isNoOrNull()).thenReturn(false);

        assertEquals(caseData.getContactDetailsWrapper().getIsAdmin(), NO_VALUE);
        assertEquals(caseData.getFastTrackDecision(), YesOrNo.NO);
        assertEquals(caseData.getPaperApplication(), YesOrNo.YES);
        assertTrue(caseData.isApplicantRepresentedByASolicitor());
    }

    @Test
    void shouldSuccessfullyReturnAsAdminContestedPaperCase() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_HWF_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);
        when(caseData.getFastTrackDecision().isNoOrNull()).thenReturn(true);

        assertEquals(caseData.getContactDetailsWrapper().getIsAdmin(), YES_VALUE);
        assertEquals(caseData.getFastTrackDecision(), YesOrNo.YES);
        assertEquals(caseData.getPaperApplication(), YesOrNo.YES);
    }

    @Test
    void shouldSuccessfullyReturnNotAsAdminContestedPaperCase() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_HWF_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);

        assertEquals(caseData.getContactDetailsWrapper().getIsAdmin(), NO_VALUE);
        assertEquals(caseData.getFastTrackDecision(), YesOrNo.NO);
        assertEquals(caseData.getPaperApplication(), YesOrNo.YES);
        assertTrue(caseData.isApplicantRepresentedByASolicitor());
    }

}