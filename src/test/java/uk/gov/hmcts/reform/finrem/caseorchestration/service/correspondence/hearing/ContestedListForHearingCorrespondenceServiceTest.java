package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;

import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ContestedListForHearingCorrespondenceServiceTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @Mock
    private HearingDocumentService hearingDocumentService;
    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;

    @Mock
    private SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;

    @InjectMocks
    private ContestedListForHearingCorrespondenceService contestedListForHearingCorrespondenceService;

    @Test
    void givenCase_whenSchedulingFirstTime_thenSendInitialCorrespondence() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetailsBefore =
            FinremCaseDetails.builder().id(123L).data(FinremCaseData.builder().build()).build();
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore);

        contestedListForHearingCorrespondenceService.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        verify(hearingDocumentService).sendInitialHearingCorrespondence(any(FinremCaseDetails.class), any());
        verify(additionalHearingDocumentService, never()).sendAdditionalHearingDocuments(any(), any(FinremCaseDetails.class));
    }

    @Test
    void givenCase_whenSchedulingSecondTime_thenSendAdditionalHearingDocuments() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetailsBefore =
            FinremCaseDetails.builder().id(123L).data(FinremCaseData.builder().formC(CaseDocument.builder().build()).build()).build();
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore);

        contestedListForHearingCorrespondenceService.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        verify(additionalHearingDocumentService).sendAdditionalHearingDocuments(any(), any(FinremCaseDetails.class));
        verify(hearingDocumentService, never()).sendInitialHearingCorrespondence(any(FinremCaseDetails.class), any());
    }

    @Test
    void givenCase_whenCaseDetailsBeforeDoNotExist_thenSendInitialCorrespondence() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        contestedListForHearingCorrespondenceService.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        verify(hearingDocumentService).sendInitialHearingCorrespondence(any(FinremCaseDetails.class), any());
        verify(additionalHearingDocumentService, never()).sendAdditionalHearingDocuments(any(), any(FinremCaseDetails.class));
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L)
            .data(FinremCaseData.builder().partiesOnCase(getParties()).build()).build();
        return FinremCallbackRequest.builder().eventType(EventType.LIST_FOR_HEARING)
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
