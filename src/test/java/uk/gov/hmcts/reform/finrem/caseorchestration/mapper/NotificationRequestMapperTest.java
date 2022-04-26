package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_OF_REPRESENTATIVES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

public class NotificationRequestMapperTest extends BaseServiceTest {

    @Autowired
    NotificationRequestMapper notificationRequestMapper;

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForConsentedJourney() {
        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(
            getConsentedCallbackRequest().getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
    }

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForContestedJourney() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForAppSolicitor(
            callbackRequest.getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("nottingham", notificationRequest.getSelectedCourt());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForConsentedJourney() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForRespSolicitor(
            callbackRequest.getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForContestedJourney() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        NotificationRequest notificationRequest = notificationRequestMapper.createNotificationRequestForRespSolicitor(
            callbackRequest.getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("nottingham", notificationRequest.getSelectedCourt());
    }

    @Test
    public void givenApplicantNoticeOfChangeOnContestedCaseWhenGetNotificationRequestForNoticeOfChangeCalledThenReturnNotificationRequestToAddedSolicitorOnLatestChangeOfRepresentation() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(CHANGE_OF_REPRESENTATIVES, getChangeOfRepresentationApplicantSolicitor());

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is("testSolicitor3@test.com"));
        assertThat(notificationRequest.getName(), is("TestSolicitor3 Name"));
        assertThat(notificationRequest.getCaseType(), is("contested"));
    }

    private ChangeOfRepresentatives getChangeOfRepresentationApplicantSolicitor() {
        List<Element<ChangeOfRepresentation>> changeOfRepresentation = Stream.of(
            element(UUID.randomUUID(), ChangeOfRepresentation.builder()
                .party("Applicant")
                .clientName("TestClient Name")
                .via("Notice of Change")
                .by("TestSolicitor2 Name")
                .date(LocalDate.now().minusDays(5))
                .added(ChangedRepresentative.builder()
                    .email("testSolicitor2@test.com")
                    .name("TestSolicitor2 Name")
                    .organisation(Organisation.builder().build())
                    .build())
                .removed(ChangedRepresentative.builder()
                    .email("testSolicitor1@test.com")
                    .name("TestSolicitor1 Name")
                    .organisation(Organisation.builder().build())
                    .build())
                .build()),
            element(UUID.randomUUID(), ChangeOfRepresentation.builder()
                .party("Applicant")
                .clientName("TestClient Name")
                .via("Notice of Change")
                .by("TestSolicitor3 Name")
                .date(LocalDate.now())
                .added(ChangedRepresentative.builder()
                    .email("testSolicitor3@test.com")
                    .name("TestSolicitor3 Name")
                    .organisation(Organisation.builder().build())
                    .build())
                .removed(ChangedRepresentative.builder()
                    .email("testSolicitor2@test.com")
                    .name("TestSolicitor2 Name")
                    .organisation(Organisation.builder().build())
                    .build())
                .build())
            ).collect(Collectors.toList());
        return ChangeOfRepresentatives.builder()
            .changeOfRepresentation(changeOfRepresentation)
                .build();
    }
}
