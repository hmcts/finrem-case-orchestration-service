package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import static org.mockito.BDDMockito.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResendPaperHearingNotificationsTaskTest {

    private static final String AUTH_TOKEN = "testAuthToken";
    private static final String REFERENCE = "1234567890123456";

    @InjectMocks
    private ResendPaperHearingNotificationsTask resendTask;
    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;
    @Mock
    private CcdService ccdService;
    @Mock
    private SystemUserService systemUserService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));
    @Mock
    private NotificationService notificationService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private HearingCorrespondenceHelper hearingCorrespondenceHelper;

    @BeforeEach
    void setUp() {
        resendTask = new ResendPaperHearingNotificationsTask(
            caseReferenceCsvLoader, ccdService, systemUserService,
            finremCaseDetailsMapper, notificationService,
            applicationEventPublisher, hearingCorrespondenceHelper);

        // reflection hack to set the @Value fields if needed
        ReflectionTestUtils.setField(resendTask, "taskEnabled", true);
        ReflectionTestUtils.setField(resendTask, "csvFile", "test.csv");
        ReflectionTestUtils.setField(resendTask, "secret", "DUMMY_SECRET");
        ReflectionTestUtils.setField(resendTask, "caseTypeId", CaseType.CONTESTED.getCcdType());
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenNoResend() {
        ReflectionTestUtils.setField(resendTask, "taskEnabled", false);
        resendTask.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(notificationService);
        verifyNoInteractions(hearingCorrespondenceHelper);
    }

    @Test
    void givenApplicantActiveHearingPostalRequired_whenExecuteTask_thenApplicantNotificationTriggered() {
        //TODO
    }

    @Test
    void givenRespondentActiveHearingPostalRequired_whenExecuteTask_thenApplicantNotificationTriggered() {
       //TODO
    }

    @Test
    void givenIntervenerActiveHearingPostalRequired_whenExecuteTask_thenApplicantNotificationTriggered() {
      // TODO
    }

    private void mockApplicantSolicitorDigital(boolean applicantSolicitorDigital) {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(applicantSolicitorDigital);
    }

    private void mockRespondentSolicitorDigital(boolean respondentSolicitorDigital) {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(respondentSolicitorDigital);
    }

}
