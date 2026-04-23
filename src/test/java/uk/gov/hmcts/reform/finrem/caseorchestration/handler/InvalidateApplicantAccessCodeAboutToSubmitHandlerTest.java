package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

class InvalidateApplicantAccessCodeAboutToSubmitHandlerTest {

    private InvalidateAccessCodeService invalidateAccessCodeService;

    private InvalidateApplicantAccessCodeAboutToSubmitHandler handler;

    @BeforeEach
    void setUp() {
        invalidateAccessCodeService = mock(InvalidateAccessCodeService.class);
        FinremCaseDetailsMapper finremCaseDetailsMapper = mock(FinremCaseDetailsMapper.class);

        handler = new InvalidateApplicantAccessCodeAboutToSubmitHandler(
            finremCaseDetailsMapper,
            invalidateAccessCodeService
        );
    }

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.INVALIDATE_APPLICANT_ACCESS_CODE);
    }

    @Test
    void shouldMergeApplicantAccessCodesAndReturnUpdatedCaseData() {
        AccessCodeCollection beforeCode =
            accessCode(UUID.randomUUID());

        AccessCodeCollection mergedCode =
            accessCode(UUID.randomUUID());

        FinremCaseData beforeData = FinremCaseData.builder()
            .applicantAccessCodes(List.of(beforeCode))
            .build();

        FinremCaseData currentData = FinremCaseData.builder()
            .applicantAccessCodes(List.of())
            .build();

        FinremCaseDetails beforeDetails = FinremCaseDetails.builder()
            .data(beforeData)
            .build();

        FinremCaseDetails currentDetails = FinremCaseDetails.builder()
            .data(currentData)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(currentDetails)
            .caseDetailsBefore(beforeDetails)
            .build();

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(List.of(mergedCode));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, "auth");

        assertThat(response.getData().getApplicantAccessCodes())
            .containsExactly(mergedCode);

        ArgumentCaptor<List<AccessCodeCollection>> beforeCaptor =
            ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<AccessCodeCollection>> currentCaptor =
            ArgumentCaptor.forClass(List.class);

        verify(invalidateAccessCodeService).mergeForInvalidation(
            beforeCaptor.capture(),
            currentCaptor.capture()
        );

        assertThat(beforeCaptor.getValue()).containsExactly(beforeCode);
        assertThat(currentCaptor.getValue()).isEmpty();
    }

    @Test
    void shouldHandleNullAccessCodeListsGracefully() {
        FinremCaseData beforeData = FinremCaseData.builder().build();
        FinremCaseData currentData = FinremCaseData.builder().build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder()
                .caseDetails(FinremCaseDetails.builder().data(currentData).build())
                .caseDetailsBefore(FinremCaseDetails.builder().data(beforeData).build())
                .build();

        when(invalidateAccessCodeService.mergeForInvalidation(List.of(), List.of()))
            .thenReturn(List.of());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, "auth");

        assertThat(response.getData().getApplicantAccessCodes()).isEmpty();
    }

    private AccessCodeCollection accessCode(UUID id) {
        return AccessCodeCollection.builder()
            .id(id)
            .build();
    }
}
