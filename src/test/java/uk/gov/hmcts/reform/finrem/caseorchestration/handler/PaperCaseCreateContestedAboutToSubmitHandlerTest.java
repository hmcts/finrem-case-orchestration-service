package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateContestedAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    @Mock
    private IdamService idamService;
    @Mock
    private CaseFlagsService caseFlagsService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    ExpressCaseService expressCaseService;

    @Spy
    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule())
    );

    @InjectMocks
    private PaperCaseCreateContestedAboutToSubmitHandler handler;

    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON = "/fixtures/contested/validate-hearing-successfully.json";

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

        assertEquals(NO_VALUE, caseData.getContactDetailsWrapper().getIsAdmin());
        assertEquals(YesOrNo.NO, caseData.getFastTrackDecision());
        assertEquals(YesOrNo.YES, caseData.getPaperApplication());
        assertTrue(caseData.isApplicantRepresentedByASolicitor());
    }

    @Test
    void shouldSuccessfullyReturnAsAdminContestedPaperCase() {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_HWF_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        assertEquals(YES_VALUE, caseData.getContactDetailsWrapper().getIsAdmin());
        assertEquals(YesOrNo.YES, caseData.getFastTrackDecision());
        assertEquals(YesOrNo.YES, caseData.getPaperApplication());
    }

    @Test
    void shouldSuccessfullyReturnNotAsAdminContestedPaperCase() {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        assertEquals(NO_VALUE, caseData.getContactDetailsWrapper().getIsAdmin());
        assertEquals(YesOrNo.NO, caseData.getFastTrackDecision());
        assertEquals(YesOrNo.YES, caseData.getPaperApplication());
        assertTrue(caseData.isApplicantRepresentedByASolicitor());
    }

    @Test
    void testUpdateRespondentInRefugeTabCalled() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        // MockedStatic is closed after the try resources block
        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {

            handler.handle(callbackRequest, AUTH_TOKEN);
            // Check that updateRespondentInRefugeTab is called with our case details instance
            mockedStatic.verify(() -> RefugeWrapperUtils.updateRespondentInRefugeTab(caseDetails), times(1));
        }
    }

    @Test
    void testUpdateApplicantInRefugeTabCalled() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        // MockedStatic is closed after the try resources block
        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {

            handler.handle(callbackRequest, AUTH_TOKEN);
            // Check that updateApplicantInRefugeTab is called with our case details instance
            mockedStatic.verify(() -> RefugeWrapperUtils.updateApplicantInRefugeTab(caseDetails), times(1));
        }
    }

    @Test
    void testSetFinancialRemediesCourtDetailsCalled() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);

        handler.handle(callbackRequest, AUTH_TOKEN);
        // Verify call to caseDataService to set PowerBI tracking fields.
        verify(caseDataService,times(1)).setFinancialRemediesCourtDetails(any(CaseDetails.class));
    }

    @Test
    void testExpressCaseServiceCalled() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(CONTESTED_HWF_JSON);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService).setExpressCaseEnrollmentStatus(caseData);
    }
}
