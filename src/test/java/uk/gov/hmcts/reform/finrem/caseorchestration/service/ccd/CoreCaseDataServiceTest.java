package uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_TWO;

@ExtendWith(MockitoExtension.class)
class CoreCaseDataServiceTest {

    private static final String EVENT_ID = EventType.INTERNAL_CHANGE_UPDATE_CASE.getCcdType();

    @Mock
    private CCDConcurrencyHelper concurrencyHelper;

    @Spy
    @InjectMocks
    private CoreCaseDataService coreCaseDataService;

    @Test
    void shouldThrowExceptionInRecoverMethod() {
        assertThatThrownBy(() ->
            coreCaseDataService.recover(new Exception(), mock(CaseType.class), Long.valueOf(CASE_ID), EVENT_ID,
                caseDetails -> Map.of(), false))
            .isInstanceOf(RetryFailureException.class);
    }

    @Nested
    class StartAndSubmitEvent {

        @Test
        void shouldSynchroniseSameCase() throws InterruptedException {
            final CaseType caseType = mock(CaseType.class);
            final StartEventResponse firstStartEventRsp =
                buildStartEventResponse("event1", "firstStartEventRsp");
            final StartEventResponse secondStartEventRsp =
                buildStartEventResponse("event2", "secondStartEventRsp");

            when(concurrencyHelper.startEvent(caseType, Long.valueOf(CASE_ID), "event1"))
                .thenAnswer((Answer<StartEventResponse>) invocationOnMock -> {
                    Thread.sleep(1000);
                    return firstStartEventRsp;
                });
            when(concurrencyHelper.startEvent(caseType, Long.valueOf(CASE_ID), "event2"))
                .thenReturn(secondStartEventRsp);

            Thread t1 = new Thread(() -> coreCaseDataService.performPostSubmitCallback(
                caseType, Long.valueOf(CASE_ID), "event1", c -> Map.of("a", "b"), true));
            t1.start();
            Thread.sleep(100);

            coreCaseDataService.performPostSubmitCallback(caseType, Long.valueOf(CASE_ID), "event2", c -> Map.of("c", "d"), true);

            InOrder inOrder = inOrder(concurrencyHelper);
            inOrder.verify(concurrencyHelper).submitEvent(firstStartEventRsp, caseType, Long.valueOf(CASE_ID), Map.of("a", "b"));
            inOrder.verify(concurrencyHelper, timeout(2000)).submitEvent(secondStartEventRsp, caseType, Long.valueOf(CASE_ID), Map.of("c", "d"));
        }

        @Test
        void shouldNotSynchroniseDifferentCase() throws InterruptedException {
            final CaseType caseType = mock(CaseType.class);
            final StartEventResponse firstStartEventRsp =
                buildStartEventResponse(EVENT_ID, "firstStartEventRsp");
            final StartEventResponse secondStartEventRsp =
                buildStartEventResponse(EVENT_ID, "secondStartEventRsp");


            when(concurrencyHelper.startEvent(caseType, Long.valueOf(CASE_ID), EVENT_ID))
                .thenAnswer((Answer<StartEventResponse>) invocationOnMock -> {
                    Thread.sleep(1000);
                    return firstStartEventRsp;
                });
            when(concurrencyHelper.startEvent(caseType, Long.valueOf(CASE_ID_TWO), EVENT_ID)).thenReturn(secondStartEventRsp);

            Thread t1 = new Thread(() -> coreCaseDataService.performPostSubmitCallback(caseType, Long.valueOf(CASE_ID), EVENT_ID, c -> Map.of("a", "a"), true));

            t1.start();
            Thread.sleep(100);
            coreCaseDataService.performPostSubmitCallback(caseType, Long.valueOf(CASE_ID_TWO), EVENT_ID,  c -> Map.of("b", "b"), true);

            InOrder inOrder = inOrder(concurrencyHelper);
            inOrder.verify(concurrencyHelper).submitEvent(secondStartEventRsp, caseType, Long.valueOf(CASE_ID_TWO), Map.of("b", "b"));
            inOrder.verify(concurrencyHelper, timeout(2000)).submitEvent(firstStartEventRsp, caseType, Long.valueOf(CASE_ID), Map.of("a", "a"));
        }
    }

    private StartEventResponse buildStartEventResponse(String eventId, String eventToken) {
        return StartEventResponse.builder()
            .eventId(eventId)
            .token(eventToken)
            .caseDetails(CaseDetails.builder().data(Map.of()).build())
            .build();
    }
}
