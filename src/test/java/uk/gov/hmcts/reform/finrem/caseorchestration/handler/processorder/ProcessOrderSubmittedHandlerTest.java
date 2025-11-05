package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;

import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ProcessOrderSubmittedHandlerTest {

    @Mock
    private HearingDocumentService hearingDocumentService;
    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @Mock
    private ManageHearingsCorresponder manageHearingsCorresponder;

    @InjectMocks
    private ProcessOrderSubmittedHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.PROCESS_ORDER);
    }

    @Test
    void givenMhProcessOrderEvent_whenHandling_thenSendMhNotifications() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().getManageHearingsWrapper().setIsAddHearingChosen(YesOrNo.YES);
        callbackRequest.setEventType(EventType.PROCESS_ORDER);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(hearingDocumentService, never()).sendInitialHearingCorrespondence(any(FinremCaseDetails.class), any());
        verify(additionalHearingDocumentService, never()).sendAdditionalHearingDocuments(any(), any(FinremCaseDetails.class));
        verify(manageHearingsCorresponder).sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);
    }

    @Test
    void givenMhProcessOrderEvent_whenHandlingWithNoHearing_thenNoMhNotifications() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().getManageHearingsWrapper().setIsAddHearingChosen(YesOrNo.NO);
        callbackRequest.setEventType(EventType.PROCESS_ORDER);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(hearingDocumentService, never()).sendInitialHearingCorrespondence(any(FinremCaseDetails.class), any());
        verify(additionalHearingDocumentService, never()).sendAdditionalHearingDocuments(any(), any(FinremCaseDetails.class));
        verify(manageHearingsCorresponder, never()).sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L)
            .data(FinremCaseData.builder().directionDetailsCollection(of(DirectionDetailCollection.builder()
                .value(DirectionDetail.builder().isAnotherHearingYN(YesOrNo.YES).build()).build()))
                .partiesOnCase(getParties()).build()).build();
        return FinremCallbackRequest.builder().eventType(EventType.SEND_ORDER)
            .caseDetails(caseDetails).build();
    }

    private DynamicMultiSelectList getParties() {

        List<DynamicMultiSelectListElement> list = new ArrayList<>();
        partyList().forEach(role -> list.add(getElementList(role)));

        return DynamicMultiSelectList.builder()
            .value(of(DynamicMultiSelectListElement.builder()
                .code(CaseRole.APP_SOLICITOR.getCcdCode())
                .label(CaseRole.APP_SOLICITOR.getCcdCode())
                .build()))
            .listItems(list)
            .build();
    }

    private List<String> partyList() {
        return of(CaseRole.APP_SOLICITOR.getCcdCode(),
            CaseRole.RESP_SOLICITOR.getCcdCode(), CaseRole.INTVR_SOLICITOR_1.getCcdCode(), CaseRole.INTVR_SOLICITOR_2.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_3.getCcdCode(), CaseRole.INTVR_SOLICITOR_4.getCcdCode());
    }

    private DynamicMultiSelectListElement getElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }

}
