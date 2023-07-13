package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class ContestedIntermHearingCorresponderTest {

    ContestedIntermHearingCorresponder intermHearingCorresponder;
    @Mock
    BulkPrintService bulkPrintService;
    @Mock
    NotificationService notificationService;

    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    GeneralApplicationDirectionsService generalApplicationDirectionsService;

    @Mock
    private BulkPrintDocument bulkPrintDoc;

    private CaseDetails caseDetails;
    private static final String AUTH_TOKEN = "Bearer token";


    @Before
    public void setUp() throws Exception {
        intermHearingCorresponder = new ContestedIntermHearingCorresponder(bulkPrintService, notificationService, finremCaseDetailsMapper,
            generalApplicationDirectionsService);
        caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
    }

    @Test
    public void shouldEmailApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        intermHearingCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);
        verify(notificationService).sendInterimNotificationEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        intermHearingCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);
        verify(notificationService).sendInterimNotificationEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailIntervenerSolicitor() {
        String intervenerEmailKey = "intervener1SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerOneWrapper(IntervenerOneWrapper.builder()
                    .intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                    .build())
                .build())
            .build());
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerOneWrapper.builder()
            .intervenerSolEmail(TEST_SOLICITOR_EMAIL).build(), caseDetails)).thenReturn(true);
        intermHearingCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);
        verify(notificationService).sendInterimNotificationEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldSendLettersToApplicantAndRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        intermHearingCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), anyString(), anyList());
    }

    @Test
    public void shouldSendLettersToApplicantAndEmailToRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        intermHearingCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(notificationService).sendInterimNotificationEmailToRespondentSolicitor(caseDetails);
    }


    @Test
    public void shouldEmailToApplicantAndSendLetterToRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        intermHearingCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(notificationService).sendInterimNotificationEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldSendLettersToIntervenersWhenPresent() {
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerOneWrapper(IntervenerOneWrapper.builder()
                    .intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                    .intervenerName("Intervener Name")
                    .build())
                .build())
            .build());

        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);

        intermHearingCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(bulkPrintService).printIntervenerDocuments(any(IntervenerOneWrapper.class), any(CaseDetails.class),
            anyString(), anyList());
    }

}
