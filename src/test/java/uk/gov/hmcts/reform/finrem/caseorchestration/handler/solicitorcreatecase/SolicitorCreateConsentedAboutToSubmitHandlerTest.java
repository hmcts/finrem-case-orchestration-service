package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.CreateCaseMandatoryDataValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateConsentedAboutToSubmitHandlerTest {

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";

    @InjectMocks
    private SolicitorCreateConsentedAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderService consentOrderService;
    @Mock
    private IdamService idamService;
    @Mock
    private CaseFlagsService caseFlagsService;

    @Mock
    private CreateCaseMandatoryDataValidator createCaseMandatoryDataValidator;

    @Test
    void given_case_whenEvent_type_is_solicitorCreate_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SOLICITOR_CREATE));
    }

    @Test
    void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_CREATE));
    }

    @Test
    void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE));
    }

    @Test
    void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE));
    }


    @Test
    void givenCase_whenRequestToUpdateLatestConsentOrderAndUserDoNotHaveAdminRole_thenHandlerCanHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(idamService.isUserRoleAdmin(any())).thenReturn(false);
        when(consentOrderService.getLatestConsentOrderData(any(FinremCallbackRequest.class))).thenReturn(caseDocument());
        when(createCaseMandatoryDataValidator.validate(callbackRequest.getCaseDetails().getData()))
            .thenReturn(Collections.emptyList());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(response.getData().getLatestConsentOrder());
        assertEquals(YesOrNo.YES, response.getData().getContactDetailsWrapper().getApplicantRepresented());
        verify(idamService).isUserRoleAdmin(any());
        verify(consentOrderService).getLatestConsentOrderData(any(FinremCallbackRequest.class));
        verify(caseFlagsService).setCaseFlagInformation(callbackRequest.getCaseDetails());
    }


    @Test
    void givenCase_whenRequestToUpdateLatestConsentOrderAndUserDoHaveAdminRole_thenHandlerCanHandle() {
        when(consentOrderService.getLatestConsentOrderData(any(FinremCallbackRequest.class))).thenReturn(caseDocument());
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(idamService.isUserRoleAdmin(any())).thenReturn(true);
        when(createCaseMandatoryDataValidator.validate(callbackRequest.getCaseDetails().getData()))
            .thenReturn(Collections.emptyList());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(response.getData().getLatestConsentOrder());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantRepresented());
        verify(idamService).isUserRoleAdmin(any());
        verify(consentOrderService).getLatestConsentOrderData(any(FinremCallbackRequest.class));
        verify(caseFlagsService).setCaseFlagInformation(callbackRequest.getCaseDetails());
    }

    @Test
    void givenCase_whenMandatoryDataValidationFails_thenReturnsErrors() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(createCaseMandatoryDataValidator.validate(callbackRequest.getCaseDetails().getData()))
            .thenReturn(List.of("Validation failed"));

        var response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo("Validation failed");
        assertThat(response.getData()).isNotNull();
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SOLICITOR_CREATE)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .build();
    }
}