package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrderAboutToStartHandlerTest {

    @InjectMocks
    private UploadDraftOrdersAboutToStartHandler handler;

    @Mock
    private HearingService hearingService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

    @Test
    void testHandle() {
        FinremCaseData caseData = spy(new FinremCaseData());

        DynamicList hearingDetails = DynamicList.builder().listItems(List.of(
            DynamicListElement.builder().label("test").code(UUID.randomUUID().toString()).build()
        )).build();

        when(caseData.getApplicantLastName()).thenReturn("Hello");
        when(caseData.getFullApplicantName()).thenReturn("Hello World");
        when(caseData.getRespondentLastName()).thenReturn("Hey");
        when(caseData.getRespondentFullName()).thenReturn("Hello Respondent");
        when(hearingService.generateSelectableHearingsAsDynamicList(any())).thenReturn(hearingDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        DraftOrdersWrapper draftOrdersWrapper = response.getData().getDraftOrdersWrapper();
        UploadAgreedDraftOrder uploadAgreedDraftOrder = draftOrdersWrapper.getUploadAgreedDraftOrder();
        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = draftOrdersWrapper.getUploadSuggestedDraftOrder();

        assertThat(uploadAgreedDraftOrder.getHearingDetails()).isEqualTo(hearingDetails);

        DynamicMultiSelectList confirmUploadedDocuments = DynamicMultiSelectList.builder()
            .listItems(List.of(DynamicMultiSelectListElement.builder()
            .label("I confirm the uploaded documents are for the Hello v Hey case")
            .code("1")
            .build())).build();
        assertThat(uploadAgreedDraftOrder.getConfirmUploadedDocuments()).isEqualTo(confirmUploadedDocuments);
        assertThat(uploadSuggestedDraftOrder.getConfirmUploadedDocuments()).isEqualTo(confirmUploadedDocuments);

        DynamicRadioList expectedUploadParty = DynamicRadioList.builder()
            .listItems(List.of(
                DynamicRadioListElement.builder()
                    .label("The applicant, Hello World").code("theApplicant").build(),
                DynamicRadioListElement.builder()
                    .label("The respondent, Hello Respondent").code("theRespondent").build()
            )).build();
        assertThat(uploadAgreedDraftOrder.getUploadParty()).isEqualTo(expectedUploadParty);
        assertThat(uploadSuggestedDraftOrder.getUploadParty()).isEqualTo(expectedUploadParty);
    }
}
