package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsAboutToSubmitHandlerTest {

    @InjectMocks
    private UpdateContactDetailsAboutToSubmitHandler handler;

    @Mock
    private UpdateContactDetailsService updateContactDetailsService;

    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @Mock
    private UpdateRepresentationWorkflowService nocWorkflowService;

    @Mock
    private FinremCaseDetailsMapper detailsMapper;

    @Test
    void shouldHandleContestedAndConsentedCaseTypes() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPDATE_CONTACT_DETAILS),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPDATE_CONTACT_DETAILS)
        );
    }

    /*
    * Passed CONTESTED, CONSENTED and UNKNOWN Case types.
    * Case data set as representation NOT changed.
    * Check that the representation, NOC and miniform A methods are never called.
    * Check that the existing org policies are persisted.
    */
    @ParameterizedTest
    @EnumSource(CaseType.class)
    void givenNoRepresentationChangeAndNoHiddenAddresses_handle(CaseType caseType) {

        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .updateIncludesRepresentativeChange(YesOrNo.NO)
                .build()).build();

        FinremCallbackRequest request = createRequest(caseType, finremCaseData);

        handler.handle(request, AUTH_TOKEN);

        verify(updateContactDetailsService).persistOrgPolicies(finremCaseData, request.getCaseDetailsBefore().getData());
        verify(updateContactDetailsService, never()).handleRepresentationChange(finremCaseData, caseType);
        verify(onlineFormDocumentService, never()).generateContestedMiniForm(any(), any());
        verify(nocWorkflowService, never()).handleNoticeOfChangeWorkflow(any(), any(), any());
    }

    /*
     * Passed CONTESTED, CONSENTED and UNKNOWN Case types.
     * Case data set as representation NOT changed and the respondent address is hidden.
     * Check that the representation and NOC methods are never called.
     * Check that the existing org policies are persisted and miniform A generated for CONTESTED only.
     */
    @ParameterizedTest
    @EnumSource(CaseType.class)
    void givenNoRepresentationChangeAndRespondentHasHiddenAddresses_handle(CaseType caseType) {

        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .respondentAddressHiddenFromApplicant(YesOrNo.YES)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .updateIncludesRepresentativeChange(YesOrNo.NO)
                .build()).build();

        FinremCallbackRequest request = createRequest(caseType, finremCaseData);

        var response = handler.handle(request, AUTH_TOKEN);

        assertNotNull(response);
        verify(updateContactDetailsService).persistOrgPolicies(finremCaseData, request.getCaseDetailsBefore().getData());
        verify(updateContactDetailsService, never()).handleRepresentationChange(finremCaseData, caseType);
        verify(nocWorkflowService, never()).handleNoticeOfChangeWorkflow(any(), any(), any());

        checkGenerateContestedMiniFormCalledForContested(caseType, request);
    }

    /*
     * Passed CONTESTED, CONSENTED and UNKNOWN Case types.
     * Case data set as representation NOT changed and the applicant address is hidden.
     * Check that the representation and NOC methods are never called.
     * Check that the existing org policies are persisted and miniform A generated for CONTESTED only.
     */
    @ParameterizedTest
    @EnumSource(CaseType.class)
    void givenNoRepresentationChangeAndApplicantHasHiddenAddresses_handle(CaseType caseType) {

        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantAddressHiddenFromRespondent(YesOrNo.YES)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .updateIncludesRepresentativeChange(YesOrNo.NO)
                .build()).build();

        FinremCallbackRequest request = createRequest(caseType, finremCaseData);

        var response = handler.handle(request, AUTH_TOKEN);

        assertNotNull(response);
        verify(updateContactDetailsService).persistOrgPolicies(finremCaseData, request.getCaseDetailsBefore().getData());
        verify(updateContactDetailsService, never()).handleRepresentationChange(finremCaseData, caseType);
        verify(nocWorkflowService, never()).handleNoticeOfChangeWorkflow(any(), any(), any());

        checkGenerateContestedMiniFormCalledForContested(caseType, request);
    }

    /*
     * Passed CONTESTED, CONSENTED and UNKNOWN Case types.
     * Case data set as representation changed and both party addresses hidden.
     * Check that the representation and NOC methods are called once.
     * Check that the existing org policies are NOT persisted
     * Check that the miniform A is generated for CONTESTED only.
     */
    @ParameterizedTest
    @EnumSource(CaseType.class)
    void shouldHandleRepresentationChangeAndHiddenAddresses(CaseType caseType) {

        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .applicantAddressHiddenFromRespondent(YesOrNo.YES)
                .respondentAddressHiddenFromApplicant(YesOrNo.YES)
                .updateIncludesRepresentativeChange(YesOrNo.YES)
                .build()).build();

        FinremCallbackRequest request = createRequest(caseType, finremCaseData);

        CaseDetails caseDetails = CaseDetails.builder().build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().caseTypeId(caseType.toString()).build();

        AboutToStartOrSubmitCallbackResponse nocWorkflowResponse = AboutToStartOrSubmitCallbackResponse
            .builder().data(new HashMap<>()).build();

        when(detailsMapper.mapToCaseDetails(request.getCaseDetails())).thenReturn(caseDetails);
        when(detailsMapper.mapToCaseDetails(request.getCaseDetailsBefore())).thenReturn(caseDetailsBefore);
        when(nocWorkflowService.handleNoticeOfChangeWorkflow(caseDetails, AUTH_TOKEN, caseDetailsBefore))
            .thenReturn(nocWorkflowResponse);
        when(detailsMapper.mapToFinremCaseData(nocWorkflowResponse.getData(), caseDetails.getCaseTypeId()))
            .thenReturn(finremCaseData);

        var response = handler.handle(request, AUTH_TOKEN);

        assertNotNull(response);

        verify(updateContactDetailsService).handleRepresentationChange(finremCaseData, caseType);
        verify(nocWorkflowService).handleNoticeOfChangeWorkflow(caseDetails, AUTH_TOKEN, caseDetailsBefore);
        verify(updateContactDetailsService, never()).persistOrgPolicies(finremCaseData, request.getCaseDetailsBefore().getData());

        checkGenerateContestedMiniFormCalledForContested(caseType, request);
    }

    @Test
    void testPopulateInRefugeQuestionsCalled() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        // MockedStatic is closed after the try resources block
        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {

            handler.handle(callbackRequest, AUTH_TOKEN);
            // Check that updateRespondentInRefugeTab is called with our case details instance
            mockedStatic.verify(() -> RefugeWrapperUtils.updateApplicantInRefugeTab(caseDetails));
            mockedStatic.verify(() -> RefugeWrapperUtils.updateRespondentInRefugeTab(caseDetails));
        }
    }

    @Test
    void givenInvalidOrganisationPolicy_whenHandle_thenReturnsValidationError() {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(caseDetails.getData()).thenReturn(finremCaseData);

        try (MockedStatic<ContactDetailsValidator> mockedStatic = mockStatic(ContactDetailsValidator.class)) {
            mockedStatic.when(() -> ContactDetailsValidator.validateOrganisationPolicy(finremCaseData))
                .thenReturn(List.of("VALIDATION FAILED"));

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
            mockedStatic.verify(() -> ContactDetailsValidator.validateOrganisationPolicy(finremCaseData));
            assertThat(response.getErrors()).containsExactly("VALIDATION FAILED");
        }
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.UPDATE_CONTACT_DETAILS)
            .caseDetails(FinremCaseDetails.builder().id(Long.valueOf(CASE_ID))
                .data(new FinremCaseData()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(Long.valueOf(CASE_ID))
                .data(new FinremCaseData()).build())
            .build();
    }

    private FinremCallbackRequest createRequest(CaseType caseType, FinremCaseData finremCaseData) {

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseType, finremCaseData);
        FinremCaseDetails finremCaseDetailsBefore = new FinremCaseDetails();
        request.setCaseDetailsBefore(finremCaseDetailsBefore);
        return request;
    }

    private void checkGenerateContestedMiniFormCalledForContested(CaseType caseType, FinremCallbackRequest request) {

        if (CaseType.CONTESTED.equals(caseType)) {
            verify(onlineFormDocumentService).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());

        } else if (CaseType.CONSENTED.equals(caseType)) {
            verify(onlineFormDocumentService, never()).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());

        } else {
            verify(onlineFormDocumentService, never()).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());
        }
    }
}
