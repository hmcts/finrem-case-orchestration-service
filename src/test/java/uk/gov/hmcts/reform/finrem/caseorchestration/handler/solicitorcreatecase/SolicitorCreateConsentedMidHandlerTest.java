package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateConsentedMidHandlerTest {

    @InjectMocks
    private SolicitorCreateConsentedMidHandler handler;

    @Mock
    private ConsentOrderService consentOrderService;

    @Mock
    private InternationalPostalService postalService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    private static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    private static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    private static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.SOLICITOR_CREATE);
    }

    @Test
    void handle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(finremCallbackRequest.getCaseDetails());

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(consentOrderService).performCheck(callbackRequest, AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    @Test
    void givenConsentedCase_WhenNotEmptyPostCode_thenHandlerWillShowNoErrorMessage() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseData data = finremCallbackRequest.getCaseDetails().getData();

        data.getContactDetailsWrapper().setSolicitorAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 1AA"
        ));

        data.getContactDetailsWrapper().setApplicantAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 1AA"
        ));

        data.getContactDetailsWrapper().setRespondentSolicitorAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 2AA"
        ));

        data.getContactDetailsWrapper().setRespondentAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 2AA"
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(finremCallbackRequest.getCaseDetails());

        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).isEmpty();
    }

    @Test
    void givenConsentedCase_WhenEmptyApplicantSolicitorPostCode_thenHandlerWillShowErrorMessage() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseData data = finremCallbackRequest.getCaseDetails().getData();

        data.getContactDetailsWrapper().setApplicantSolicitorAddress(new Address());

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(finremCallbackRequest.getCaseDetails());

        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors())
            .containsExactly(APPLICANT_SOLICITOR_POSTCODE_ERROR);
    }

    @Test
    void givenConsentedCase_WhenEmptyApplicantPostCode_thenHandlerWillShowErrorMessage() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseData data = finremCallbackRequest.getCaseDetails().getData();

        data.getContactDetailsWrapper().setApplicantAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(finremCallbackRequest.getCaseDetails());

        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors())
            .containsExactly(APPLICANT_POSTCODE_ERROR);
    }

    @Test
    void givenConsentedCase_WhenEmptyRespondentSolicitorPostCode_thenHandlerWillShowErrorMessage() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseData data = finremCallbackRequest.getCaseDetails().getData();

        data.getContactDetailsWrapper().setRespondentSolicitorAddress(new Address());

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(finremCallbackRequest.getCaseDetails());

        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors())
            .containsExactly(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
    }

    @Test
    void givenConsentedCase_WhenEmptyRespondentPostCode_thenHandlerWillShowErrorMessage() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseData data = finremCallbackRequest.getCaseDetails().getData();

        data.getContactDetailsWrapper().setRespondentAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(finremCallbackRequest.getCaseDetails());

        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors())
            .containsExactlyInAnyOrder(RESPONDENT_POSTCODE_ERROR);
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        return FinremCallbackRequestFactory.from(EventType.SOLICITOR_CREATE,
            FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder().eventId(EventType.SOLICITOR_CREATE.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }
}
