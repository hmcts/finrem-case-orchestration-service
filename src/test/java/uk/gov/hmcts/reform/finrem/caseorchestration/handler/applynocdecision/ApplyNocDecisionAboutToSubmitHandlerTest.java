package uk.gov.hmcts.reform.finrem.caseorchestration.handler.applynocdecision;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandleAnyCaseType;

@ExtendWith(MockitoExtension.class)
class ApplyNocDecisionAboutToSubmitHandlerTest {

    private static RepresentationUpdateHistoryCollection applicantRepresentationUpdateHistory() {
        return representationUpdateHistoryCollection(true);
    }

    private static RepresentationUpdateHistoryCollection respondentRepresentationUpdateHistory() {
        return representationUpdateHistoryCollection(false);
    }

    private static  RepresentationUpdateHistoryCollection representationUpdateHistoryCollection(boolean isApplicant) {
        return RepresentationUpdateHistoryCollection.builder()
            .id(UUID.randomUUID())
            .value(RepresentationUpdate.builder()
                .party(isApplicant
                    ? CCDConfigConstant.APPLICANT
                    : CCDConfigConstant.RESPONDENT)
                .build())
            .build();
    }

    @Mock
    private GenerateCoverSheetService generateCoverSheetService;

    @InjectMocks
    private ApplyNocDecisionAboutToSubmitHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandleAnyCaseType(handler, CallbackType.ABOUT_TO_SUBMIT, EventType.APPLY_NOC_DECISION);
    }

    @Test
    void givenEmptyRepresentationUpdateHistory_whenHandled_noCoversheetGenerated() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verifyNoInteractions(generateCoverSheetService);
    }

    static Stream<Arguments> givenLatestApplicantRepresentationUpdateHistory_whenHandled_applicantCoversheetRegenerated() {
        return Stream.of(
            Arguments.of(List.of(
                respondentRepresentationUpdateHistory(),
                applicantRepresentationUpdateHistory()
            )),
            Arguments.of(List.of(
                applicantRepresentationUpdateHistory()
            )),
            Arguments.of(List.of(
                applicantRepresentationUpdateHistory(),
                applicantRepresentationUpdateHistory()
            )),
            Arguments.of(List.of(
                applicantRepresentationUpdateHistory(),
                respondentRepresentationUpdateHistory(),
                applicantRepresentationUpdateHistory()
            ))
        );
    }

    @MethodSource
    @ParameterizedTest
    void givenLatestApplicantRepresentationUpdateHistory_whenHandled_applicantCoversheetRegenerated(
        List<RepresentationUpdateHistoryCollection> representationUpdateHistory
    ) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .representationUpdateHistory(representationUpdateHistory)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(callbackRequest.getCaseDetails(), AUTH_TOKEN);
        verify(generateCoverSheetService, never()).generateAndSetRespondentCoverSheet(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    static Stream<Arguments> givenLatestRespondentRepresentationUpdateHistory_whenHandled_applicantCoversheetRegenerated() {
        return Stream.of(
            Arguments.of(List.of(
                applicantRepresentationUpdateHistory(),
                respondentRepresentationUpdateHistory()
            )),
            Arguments.of(List.of(
                respondentRepresentationUpdateHistory()
            )),
            Arguments.of(List.of(
                respondentRepresentationUpdateHistory(),
                respondentRepresentationUpdateHistory()
            )),
            Arguments.of(List.of(
                respondentRepresentationUpdateHistory(),
                applicantRepresentationUpdateHistory(),
                respondentRepresentationUpdateHistory()
            ))
        );
    }

    @MethodSource
    @ParameterizedTest
    void givenLatestRespondentRepresentationUpdateHistory_whenHandled_applicantCoversheetRegenerated(
        List<RepresentationUpdateHistoryCollection> representationUpdateHistory
    ) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .representationUpdateHistory(representationUpdateHistory)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(generateCoverSheetService, never()).generateAndSetApplicantCoverSheet(callbackRequest.getCaseDetails(), AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }
}
