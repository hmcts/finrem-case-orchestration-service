package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;

import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
@Slf4j
public class NotificationServiceTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    private static final String END_POINT = "http://localhost:8086/notify/hwfSuccessful";

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void sendHwfSuccessfulNotificationEmail() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withNoContent());
        CaseData caseData = new CaseData();
        caseData.setSolicitorEmail("test@test.com");
        caseData.setSolicitorName("Padmaja");
        caseData.setSolicitorReference("56789");
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseId("12345");
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        ccdRequest.setCaseDetails(caseDetails);

        notificationService.sendHWFSuccessfulConfirmationEmail(ccdRequest, AUTH_TOKEN);
        mockServer.verify();
    }

    @Test
    public void throwExceptionWhenHwfSuccessfulNotificationEmailIsRequested() {
        mockServer.expect(MockRestRequestMatchers.requestTo(END_POINT))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        CaseData caseData = new CaseData();
        caseData.setSolicitorEmail("test@test.com");
        caseData.setSolicitorName("Padmaja");
        caseData.setSolicitorReference("56789");
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        ccdRequest.setCaseId("12345");

        try {
            notificationService.sendHWFSuccessfulConfirmationEmail(ccdRequest, AUTH_TOKEN);
        } catch (Exception ex) {
            assertThat(ex.getMessage(), Is.is("500 Internal Server Error"));
            log.info(ex.toString());
        }

    }
}