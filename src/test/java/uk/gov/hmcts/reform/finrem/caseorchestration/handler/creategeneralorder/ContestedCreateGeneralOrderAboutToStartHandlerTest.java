package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralorder;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.*;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FinremDateUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.*;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.*;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class ContestedCreateGeneralOrderAboutToStartHandlerTest {
    @InjectMocks
    private ContestedCreateGeneralOrderAboutToStartHandler handler;

    @Mock
    private FinremCaseDetailsMapper mapper;

    @Mock
    private IdamService idamService;

    @Spy
    private Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @ParameterizedTest
    @MethodSource
    void testCanHandle(CallbackType callbackType, CaseType caseType, EventType eventType, boolean expected) {
        assertThat(handler.canHandle(callbackType, caseType, eventType)).isEqualTo(expected);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            // Contested
            Arguments.of(ABOUT_TO_START, CONTESTED, GENERAL_ORDER, true),
            Arguments.of(MID_EVENT, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(SUBMITTED, CONTESTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_START, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, true),
            Arguments.of(MID_EVENT, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(SUBMITTED, CONTESTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),

            // Consented
            Arguments.of(ABOUT_TO_START, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(MID_EVENT, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, GENERAL_ORDER, false),
            Arguments.of(SUBMITTED, CONSENTED, GENERAL_ORDER, false),

            Arguments.of(ABOUT_TO_START, CONSENTED, GENERAL_ORDER_CONSENT_IN_CONTESTED, false),
            Arguments.of(ABOUT_TO_START, CONTESTED, ASSIGN_DOCUMENT_CATEGORIES, false),
            Arguments.of(ABOUT_TO_START, CONSENTED, ASSIGN_DOCUMENT_CATEGORIES, false)
        );
    }

    @Test
    void testHandle() {
        String judgeFullName = "Judge Daisy Nixon";
        String judgeSurname = "Nixon";
        String authorisationToken = "some-token";
        when(idamService.getIdamFullName(authorisationToken)).thenReturn(judgeFullName);
        when(idamService.getIdamSurname(authorisationToken)).thenReturn(judgeSurname);
        FinremCallbackRequest callbackRequest = createRequest(createCaseData());

        var response = handler.handle(callbackRequest, authorisationToken);

        FinremCaseData caseData = response.getData();
        assertThat(caseData.getGeneralOrderWrapper().getGeneralOrderCreatedBy()).isEqualTo(judgeFullName);
        assertThat(caseData.getGeneralOrderWrapper().getGeneralOrderRecitals()).isNull();
        assertThat(caseData.getGeneralOrderWrapper().getGeneralOrderJudgeType()).isNull();
        assertThat(caseData.getGeneralOrderWrapper().getGeneralOrderJudgeName()).isEqualTo(judgeSurname);
        assertThat(FinremDateUtils.getDateFormatter().format(caseData.getGeneralOrderWrapper().getGeneralOrderDate()))
            .isEqualTo("1970-01-01");
        assertThat(caseData.getGeneralOrderWrapper().getGeneralOrderBodyText()).isNull();
        assertThat(caseData.getGeneralOrderWrapper().getGeneralOrderPreviewDocument()).isNull();
        assertThat(caseData.getGeneralOrderWrapper().getGeneralOrderAddressTo()).isNull();
    }

    private FinremCaseData createCaseData() {
        String testData = "Test data";
        FinremCaseData caseData = FinremCaseData.builder().build();
        caseData.getGeneralOrderWrapper().setGeneralOrderCreatedBy(testData);
        caseData.getGeneralOrderWrapper().setGeneralOrderRecitals(testData);
        caseData.getGeneralOrderWrapper().setGeneralOrderJudgeType(JudgeType.DEPUTY_DISTRICT_JUDGE);
        caseData.getGeneralOrderWrapper().setGeneralOrderJudgeName(testData);
        caseData.getGeneralOrderWrapper().setGeneralOrderDate(null);
        caseData.getGeneralOrderWrapper().setGeneralOrderBodyText(testData);
        caseData.getGeneralOrderWrapper().setGeneralOrderPreviewDocument(mock(CaseDocument.class));
        caseData.getGeneralOrderWrapper().setGeneralOrderAddressTo(mock(GeneralOrderAddressTo.class));

        return caseData;
    }

    private FinremCallbackRequest createRequest(FinremCaseData caseData) {
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CONTESTED)
                .data(caseData)
                .build())
            .build();
    }
}
