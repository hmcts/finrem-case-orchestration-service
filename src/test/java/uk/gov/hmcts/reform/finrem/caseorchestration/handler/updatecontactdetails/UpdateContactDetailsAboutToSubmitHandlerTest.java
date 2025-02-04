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
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.CONTESTED;
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

        verify(updateContactDetailsService, times(1)).persistOrgPolicies(finremCaseData, request.getCaseDetailsBefore().getData());
        verify(onlineFormDocumentService, never()).generateContestedMiniForm(any(), any());
        verify(nocWorkflowService, never()).handleNoticeOfChangeWorkflow(any(), any(), any());
    }

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
        verify(updateContactDetailsService, times(1)).persistOrgPolicies(finremCaseData, request.getCaseDetailsBefore().getData());
        verify(nocWorkflowService, never()).handleNoticeOfChangeWorkflow(any(), any(), any());

        if (CaseType.CONTESTED.equals(caseType)) {
            verify(onlineFormDocumentService, times(1)).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());
        }

        if (CaseType.CONSENTED.equals(caseType)) {
            verify(onlineFormDocumentService, never()).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());
        }
    }

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
        verify(updateContactDetailsService, times(1)).persistOrgPolicies(finremCaseData, request.getCaseDetailsBefore().getData());
        verify(nocWorkflowService, never()).handleNoticeOfChangeWorkflow(any(), any(), any());

        if (CaseType.CONTESTED.equals(caseType)) {
            verify(onlineFormDocumentService, times(1)).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());
        }

        if (CaseType.CONSENTED.equals(caseType)) {
            verify(onlineFormDocumentService, never()).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());
        }
    }

    @ParameterizedTest
    @EnumSource(CaseType.class)
    void shouldHandleRepresentationChangeWhenUpdateIncludesRepresentativeChange(CaseType caseType) {

        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .updateIncludesRepresentativeChange(YesOrNo.YES)
                .build()).build();

        FinremCallbackRequest request = createRequest(caseType, finremCaseData);

        CaseDetails caseDetails = CaseDetails.builder().build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().caseTypeId(CONTESTED).build();

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

        verify(onlineFormDocumentService, never()).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());
        verify(nocWorkflowService, times(1)).handleNoticeOfChangeWorkflow(caseDetails, AUTH_TOKEN, caseDetailsBefore);
    }

    @Test
    void testPopulateInRefugeQuestionsCalled() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        // MockedStatic is closed after the try resources block
        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {

            handler.handle(callbackRequest, AUTH_TOKEN);
            // Check that updateRespondentInRefugeTab is called with our case details instance
            mockedStatic.verify(() -> RefugeWrapperUtils.updateApplicantInRefugeTab(caseDetails), times(1));
            mockedStatic.verify(() -> RefugeWrapperUtils.updateRespondentInRefugeTab(caseDetails), times(1));
        }
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
                .builder()
                .eventType(EventType.UPDATE_CONTACT_DETAILS)
                .caseDetails(FinremCaseDetails.builder().id(123L)
                        .data(new FinremCaseData()).build())
                .caseDetailsBefore(FinremCaseDetails.builder().id(123L)
                    .data(new FinremCaseData()).build())
                .build();
    }

    private FinremCallbackRequest createRequest(CaseType caseType, FinremCaseData finremCaseData) {

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(1727874196328932L, caseType, finremCaseData);
        FinremCaseDetails finremCaseDetailsBefore = new FinremCaseDetails();
        request.setCaseDetailsBefore(finremCaseDetailsBefore);
        return request;
    }
}
