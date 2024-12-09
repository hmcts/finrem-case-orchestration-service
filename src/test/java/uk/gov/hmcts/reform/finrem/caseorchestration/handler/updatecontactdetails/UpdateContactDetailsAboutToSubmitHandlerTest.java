package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPDATE_CONTACT_DETAILS);
    }

    @Test
    void givenNoRepresentationChangeAndNoHiddenAddresses_handle() {

        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .updateIncludesRepresentativeChange(YesOrNo.NO)
                .build()).build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(1727874196328932L, CaseType.CONTESTED, finremCaseData);
        FinremCaseDetails finremCaseDetailsBefore = new FinremCaseDetails();
        request.setCaseDetailsBefore(finremCaseDetailsBefore);

        handler.handle(request, AUTH_TOKEN);

        verify(updateContactDetailsService, times(1)).persistOrgPolicies(finremCaseData, finremCaseDetailsBefore.getData());
        verify(onlineFormDocumentService, never()).generateContestedMiniForm(any(), any());
        verify(nocWorkflowService, never()).handleNoticeOfChangeWorkflow(any(), any(), any());
    }

    @Test
    void givenNoRepresentationChangeAndRespondentHasHiddenAddresses_handle() {

        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .respondentAddressHiddenFromApplicant(YesOrNo.YES)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .updateIncludesRepresentativeChange(YesOrNo.NO)
                .build()).build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(1727874196328932L, CaseType.CONTESTED, finremCaseData);
        FinremCaseDetails finremCaseDetailsBefore = new FinremCaseDetails();
        request.setCaseDetailsBefore(finremCaseDetailsBefore);

        var response = handler.handle(request, AUTH_TOKEN);

        assertNotNull(response);
        verify(updateContactDetailsService, times(1)).persistOrgPolicies(finremCaseData, finremCaseDetailsBefore.getData());
        verify(onlineFormDocumentService, times(1)).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());
        verify(nocWorkflowService, never()).handleNoticeOfChangeWorkflow(any(), any(), any());
    }

    @Test
    void givenNoRepresentationChangeAndApplicantHasHiddenAddresses_handle() {

        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantAddressHiddenFromRespondent(YesOrNo.YES)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .updateIncludesRepresentativeChange(YesOrNo.NO)
                .build()).build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(1727874196328932L, CaseType.CONTESTED, finremCaseData);
        FinremCaseDetails finremCaseDetailsBefore = new FinremCaseDetails();
        request.setCaseDetailsBefore(finremCaseDetailsBefore);

        var response = handler.handle(request, AUTH_TOKEN);

        assertNotNull(response);
        verify(updateContactDetailsService, times(1)).persistOrgPolicies(finremCaseData, finremCaseDetailsBefore.getData());
        verify(onlineFormDocumentService, times(1)).generateContestedMiniForm(AUTH_TOKEN, request.getCaseDetails());
        verify(nocWorkflowService, never()).handleNoticeOfChangeWorkflow(any(), any(), any());
    }

    @Test
    void shouldHandleRepresentationChangeWhenUpdateIncludesRepresentativeChange() {
        // Arrange
        FinremCaseData finremCaseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .updateIncludesRepresentativeChange(YesOrNo.YES)
                .build()).build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(1727874196328932L, CaseType.CONTESTED, finremCaseData);
        request.setCaseDetailsBefore(new FinremCaseDetails());

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
}
