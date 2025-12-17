package uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.JURISDICTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerServiceTest.CASE_ID;

@ExtendWith(MockitoExtension.class)
class CCDConcurrencyHelperTest {

    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Spy
    @InjectMocks
    private CCDConcurrencyHelper helper;

    @BeforeEach
    void setup() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
    }

    @Nested
    class StartAndSubmitEvent {
        String eventId = "sample-event";
        String eventToken = "t-xyz";

        @BeforeEach
        void setUp() {
            when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
            when(systemUserService.getSysUserTokenUid()).thenReturn(TEST_USER_ID);
        }

        @ParameterizedTest
        @EnumSource(value = CaseType.class)
        void shouldStartEvent(CaseType caseType) {
            helper.startEvent(caseType, CASE_ID, eventId);

            verify(coreCaseDataApi).startEventForCaseWorker(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID,
                JURISDICTION, caseType.getCcdType(), Long.toString(CASE_ID), eventId);
        }

        @ParameterizedTest
        @EnumSource(value = CaseType.class)
        void shouldSubmitEventWithoutCaseData(CaseType caseType) {
            StartEventResponse startEventResponse = StartEventResponse.builder()
                .eventId(eventId)
                .token(eventToken)
                .caseDetails(CaseDetails.builder().data(emptyMap()).build())
                .build();

            helper.submitEvent(startEventResponse, caseType, CASE_ID, emptyMap());

            verify(coreCaseDataApi).submitEventForCaseWorker(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID, JURISDICTION,
                caseType.getCcdType(), Long.toString(CASE_ID), true,
                buildCaseDataContent(eventId, eventToken, emptyMap()));
        }

        @ParameterizedTest
        @EnumSource(value = CaseType.class)
        void shouldSubmitEventWithCaseData(CaseType caseType) {
            StartEventResponse startEventResponse = StartEventResponse.builder()
                .eventId(eventId)
                .token(eventToken)
                .caseDetails(CaseDetails.builder().data(Map.of("id", 12345L)).build())
                .build();

            Map<String, Object> updates = Map.of("caseName", "new case name");

            helper.submitEvent(startEventResponse, caseType, CASE_ID, updates);

            verify(coreCaseDataApi).submitEventForCaseWorker(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID, JURISDICTION,
                caseType.getCcdType(), Long.toString(CASE_ID), true,
                buildCaseDataContent(eventId, eventToken, updates));
        }
    }

    private CaseDataContent buildCaseDataContent(String eventId, String eventToken, Object eventData) {
        return CaseDataContent.builder()
            .eventToken(eventToken)
            .event(Event.builder()
                .id(eventId)
                .build())
            .data(eventData)
            .build();
    }
}
