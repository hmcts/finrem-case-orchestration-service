package uk.gov.hmcts.reform.finrem.caseorchestration.handler.shareselecteddocuments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.MANAGE_SCANNED_DOCS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SHARE_SELECTED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class ShareSelectedDocumentsMidEventHandlerTest {

    private ShareSelectedDocumentsMidEventHandler handler;
    private AssignCaseAccessService assignCaseAccessService;
    private IntervenerShareDocumentsService intervenerShareDocumentsService;

    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper mapper = mock(FinremCaseDetailsMapper.class);
        assignCaseAccessService = mock(AssignCaseAccessService.class);
        intervenerShareDocumentsService = mock(IntervenerShareDocumentsService.class);

        handler = new ShareSelectedDocumentsMidEventHandler(mapper, assignCaseAccessService,
            intervenerShareDocumentsService);
    }

    @ParameterizedTest
    @MethodSource
    void testCanHandle(CallbackType callbackType, CaseType caseType, EventType eventType, boolean expected) {
        assertThat(handler.canHandle(callbackType, caseType, eventType)).isEqualTo(expected);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            Arguments.of(ABOUT_TO_START, CONTESTED, SHARE_SELECTED_DOCUMENTS, false),
            Arguments.of(MID_EVENT, CONTESTED, SHARE_SELECTED_DOCUMENTS, true),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, SHARE_SELECTED_DOCUMENTS, false),
            Arguments.of(SUBMITTED, CONTESTED, SHARE_SELECTED_DOCUMENTS, false),

            Arguments.of(ABOUT_TO_START, CONTESTED, MANAGE_SCANNED_DOCS, false),
            Arguments.of(MID_EVENT, CONTESTED, MANAGE_SCANNED_DOCS, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, MANAGE_SCANNED_DOCS, false),
            Arguments.of(SUBMITTED, CONTESTED, MANAGE_SCANNED_DOCS, false),

            Arguments.of(ABOUT_TO_START, CONSENTED, SHARE_SELECTED_DOCUMENTS, false),
            Arguments.of(MID_EVENT, CONSENTED, SHARE_SELECTED_DOCUMENTS, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, SHARE_SELECTED_DOCUMENTS, false),
            Arguments.of(SUBMITTED, CONSENTED, SHARE_SELECTED_DOCUMENTS, false)
        );
    }

    @Test
    void givenValidSolicitorRolesSelections_whenMidEventCalled_thenNoErrors() {
        String userAuthorisation = "some-token";
        FinremCaseData caseData = new FinremCaseData();
        FinremCallbackRequest request = createRequest(caseData);
        when(assignCaseAccessService.getActiveUser(String.valueOf(request.getCaseDetails().getId()), userAuthorisation))
            .thenReturn("Willie Armstrong");
        when(intervenerShareDocumentsService.checkThatApplicantAndRespondentAreBothSelected(caseData))
            .thenReturn(new ArrayList<>());

        var response = handler.handle(request, userAuthorisation);

        assertThat(response.getErrors().isEmpty()).isTrue();
        verify(intervenerShareDocumentsService, times(1))
            .checkThatApplicantAndRespondentAreBothSelected(caseData);
        verifyNoMoreInteractions(intervenerShareDocumentsService);
    }

    @Test
    void givenInvalidSolicitorRolesSelections_whenMidEventCalled_thenNoErrors() {
        String userAuthorisation = "some-token";
        String expectedErrorMessage = "Documents must be shared with either both applicant and respondent or neither";
        FinremCaseData caseData = new FinremCaseData();
        FinremCallbackRequest request = createRequest(caseData);
        when(assignCaseAccessService.getActiveUser(String.valueOf(request.getCaseDetails().getId()), userAuthorisation))
            .thenReturn("Willie Armstrong");
        when(intervenerShareDocumentsService.checkThatApplicantAndRespondentAreBothSelected(caseData))
            .thenReturn(List.of(expectedErrorMessage));

        var response = handler.handle(request, userAuthorisation);

        List<String> errors = response.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).isEqualTo(expectedErrorMessage);
        verify(intervenerShareDocumentsService, times(1))
            .checkThatApplicantAndRespondentAreBothSelected(caseData);
        verifyNoMoreInteractions(intervenerShareDocumentsService);
    }

    private FinremCallbackRequest createRequest(FinremCaseData caseData) {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(123L)
            .data(caseData)
            .build();
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }
}
