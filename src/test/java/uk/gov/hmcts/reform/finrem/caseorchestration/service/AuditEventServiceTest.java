package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseDataApiV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEventsResponse;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


public class AuditEventServiceTest extends BaseServiceTest {

    private static final String USER_TOKEN = "USER_TOKEN";
    private static final String SERVICE_TOKEN = "SERVICE_TOKEN";
    private static final String CASE_ID = "1111";
    private static final String NOC_EVENT = "nocRequest";
    private static final String fixture = "/fixtures/audit-events.json";

    @Mock
    private CaseDataApiV2 mockCaseDataApi;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private AuditEventsResponse auditEventsResponse;

    @InjectMocks
    private AuditEventService auditEventService;

    private static final LocalDateTime A_LOCAL_DATE_TIME = LocalDateTime.now();

    @Before
    public void setUp() throws IOException {
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(mockCaseDataApi.getAuditEvents(USER_TOKEN, SERVICE_TOKEN, false, CASE_ID))
            .thenReturn(auditEventsResponse);
    }

    @Test
    public void shouldGetAuditEventByName() throws IOException {
        AuditEvent expectedAuditEvent = buildAuditEvent(NOC_EVENT, A_LOCAL_DATE_TIME);
        when(mockCaseDataApi.getAuditEvents(USER_TOKEN, SERVICE_TOKEN, false, CASE_ID))
            .thenReturn(generateAuditEventsResponse());

        Optional<AuditEvent> actualAuditEvent
            = auditEventService.getLatestAuditEventByName(CASE_ID, NOC_EVENT);

        assertTrue(actualAuditEvent.isPresent());

        assertEquals(expectedAuditEvent.getId(), actualAuditEvent.get().getId());
        assertEquals(expectedAuditEvent.getUserFirstName(), actualAuditEvent.get().getUserFirstName());
        assertEquals(expectedAuditEvent.getUserLastName(), actualAuditEvent.get().getUserLastName());
    }

    @Test
    public void shouldGetLatestAuditEventWithGivenName() {
        AuditEvent expectedAuditEvent = buildAuditEvent(NOC_EVENT, A_LOCAL_DATE_TIME);

        List<AuditEvent> auditEventList = List.of(
            buildAuditEvent(NOC_EVENT, A_LOCAL_DATE_TIME.minusMinutes(3)),
            expectedAuditEvent,
            buildAuditEvent(NOC_EVENT, A_LOCAL_DATE_TIME.minusMinutes(2)));

        when(auditEventsResponse.getAuditEvents()).thenReturn(auditEventList);

        Optional<AuditEvent> actualAuditEvent
            = auditEventService.getLatestAuditEventByName(CASE_ID, NOC_EVENT);

        assertThat(actualAuditEvent).isPresent().contains(expectedAuditEvent);
    }

    @Test
    public void shouldReturnEmptyOptionalIfAuditEventWithNameCannotBeFound() {
        List<AuditEvent> auditEventList = List.of(
            buildAuditEvent("FR_updateContactDetails", A_LOCAL_DATE_TIME),
            buildAuditEvent("FR_createCase", A_LOCAL_DATE_TIME));

        when(auditEventsResponse.getAuditEvents()).thenReturn(auditEventList);

        Optional<AuditEvent> actualAuditEvent
            = auditEventService.getLatestAuditEventByName(CASE_ID, "nocRequest");

        assertThat(actualAuditEvent).isEmpty();
    }

    private AuditEvent buildAuditEvent(String eventId, LocalDateTime createdDate) {
        return AuditEvent.builder()
            .id(eventId)
            .userFirstName("Sir")
            .userLastName("Solicitor")
            .createdDate(createdDate)
            .build();
    }

    private AuditEventsResponse generateAuditEventsResponse() throws IOException {
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream(fixture)) {
            return mapper.readValue(resourceAsStream, AuditEventsResponse.class);
        }
    }
}
