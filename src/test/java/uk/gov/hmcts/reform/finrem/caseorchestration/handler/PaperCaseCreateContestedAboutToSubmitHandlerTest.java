package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;


@ExtendWith(MockitoExtension.class)
class PaperCaseCreateContestedAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON = "/fixtures/contested/validate-hearing-successfully.json";
    private PaperCaseCreateContestedAboutToSubmitHandler handler;
    @Mock
    private IdamService idamService;
    @Mock
    private CaseFlagsService caseFlagsService;

    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new PaperCaseCreateContestedAboutToSubmitHandler(finremCaseDetailsMapper, caseFlagsService, idamService);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.NEW_PAPER_CASE);
    }

    @Test
    void shouldSuccessfullyReturnAsAdminConsentedPaperCase() {

        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_HWF_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        assertEquals(caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole(), CaseRole.APP_SOLICITOR.getCcdCode());
        assertEquals(caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole(), CaseRole.RESP_SOLICITOR.getCcdCode());
        assertEquals(YES_VALUE, caseData.getContactDetailsWrapper().getIsAdmin());
        assertEquals(YesOrNo.YES, caseData.getFastTrackDecision());
        assertEquals(YesOrNo.YES, caseData.getPaperApplication());
    }

    @Test
    void shouldSuccessfullyReturnNotAsAdminConsentedPaperCase() {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        assertEquals(caseData.getContactDetailsWrapper().getIsAdmin(), NO_VALUE);
        assertEquals(caseData.getFastTrackDecision(), YesOrNo.NO);
        assertEquals(caseData.getPaperApplication(), YesOrNo.YES);
        assertTrue(caseData.isApplicantRepresentedByASolicitor());
    }

    @Test
    void shouldSuccessfullyReturnAsAdminContestedPaperCase() {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_HWF_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        assertEquals(caseData.getContactDetailsWrapper().getIsAdmin(), YES_VALUE);
        assertEquals(caseData.getFastTrackDecision(), YesOrNo.YES);
        assertEquals(caseData.getPaperApplication(), YesOrNo.YES);
    }

    @Test
    void shouldSuccessfullyReturnNotAsAdminContestedPaperCase() {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        assertEquals(caseData.getContactDetailsWrapper().getIsAdmin(), NO_VALUE);
        assertEquals(caseData.getFastTrackDecision(), YesOrNo.NO);
        assertEquals(caseData.getPaperApplication(), YesOrNo.YES);
        assertTrue(caseData.isApplicantRepresentedByASolicitor());
    }
}
