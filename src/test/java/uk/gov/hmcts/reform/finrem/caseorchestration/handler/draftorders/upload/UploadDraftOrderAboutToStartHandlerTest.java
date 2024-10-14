package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
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

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

    @Test
    void shouldPopulateAgreedDraftOrderFlowProperties() {
        FinremCaseData caseData = spy(new FinremCaseData());

        DynamicList expectedDynamicList = DynamicList.builder().listItems(List.of(
            DynamicListElement.builder().label("test").code(UUID.randomUUID().toString()).build()
        )).build();

        when(caseData.getApplicantLastName()).thenReturn("Hello");
        when(caseData.getFullApplicantName()).thenReturn("Hello World");
        when(caseData.getRespondentLastName()).thenReturn("Hey");
        when(caseData.getRespondentFullName()).thenReturn("Hello Respondent");
        when(hearingService.generateSelectableHearingsAsDynamicList(any())).thenReturn(expectedDynamicList);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(FinremCallbackRequestFactory.from(caseData),
            AUTH_TOKEN);

        DraftOrdersWrapper draftOrdersWrapper = response.getData().getDraftOrdersWrapper();
        assertThat(draftOrdersWrapper.getUploadAgreedDraftOrder().getConfirmUploadedDocuments()).isEqualTo(
            DynamicMultiSelectList.builder().listItems(List.of(DynamicMultiSelectListElement.builder()
                .label("I confirm the uploaded documents are for the Hello v Hey case")
                .code("1")
                .build())).build()
        );
        assertThat(draftOrdersWrapper.getUploadAgreedDraftOrder().getHearingDetails()).isEqualTo(expectedDynamicList);
        assertThat(draftOrdersWrapper.getUploadAgreedDraftOrder().getUploadParty()).isEqualTo(
            DynamicRadioList.builder()
                .listItems(List.of(
                    DynamicRadioListElement.builder().label("The applicant, Hello World").code("theApplicant").build(),
                    DynamicRadioListElement.builder().label("The respondent, Hello Respondent").code("theRespondent").build()
                )).build()
        );
    }
}
