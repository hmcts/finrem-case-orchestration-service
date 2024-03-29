package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailAllPartiesCorresponder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public abstract class HearingCorrespondenceBaseTest {

    @Mock
    NotificationService notificationService;
    @Mock
    BulkPrintService bulkPrintService;
    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    DocumentHelper documentHelper;
    CaseDetails caseDetails;
    MultiLetterOrEmailAllPartiesCorresponder applicantAndRespondentMultiLetterCorresponder;


    @Test
    public void shouldEmailApplicantAndRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(notificationService).sendPrepareForHearingEmailRespondent(caseDetails);
        verify(notificationService).sendPrepareForHearingEmailApplicant(caseDetails);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void shouldSendLettersToApplicantAndRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), anyString(), anyList());
    }

    @Test
    public void shouldSendLettersToApplicantAndEmailToRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(notificationService).sendPrepareForHearingEmailRespondent(caseDetails);
    }


    @Test
    public void shouldEmailToApplicantAndSendLetterToRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(notificationService).sendPrepareForHearingEmailApplicant(caseDetails);
    }


    @Test
    public void shouldEmailInterveners() {

        ObjectMapper mapper = new ObjectMapper();
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerName("Intervener 1")
            .intervenerEmail("Intervener email")
            .intervenerCorrespondenceEnabled(Boolean.TRUE)
            .build();
        caseDetails.getData().put("intervener1", mapper.convertValue(intervenerOne, Map.class));
        caseDetails.setCaseTypeId(CaseType.CONTESTED.getCcdType());

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isContestedApplication(any(CaseDetails.class))).thenReturn(true);

        FinremCaseDetails finremCaseDetails =
            FinremCaseDetails.builder().data(FinremCaseData.builder().intervenerOne(intervenerOne).build()).build();
        Mockito.lenient().when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class))).thenReturn(finremCaseDetails);
        Mockito.lenient().when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerOne.class),
            any(FinremCaseDetails.class))).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerOne))
            .thenReturn(SolicitorCaseDataKeysWrapper.builder().build());

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(notificationService).sendPrepareForHearingEmailRespondent(caseDetails);
        verify(notificationService).sendPrepareForHearingEmailApplicant(caseDetails);
        verify(notificationService).sendPrepareForHearingEmailIntervener(any(CaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class));

    }


    @Test
    public void shouldSendLettersToInterveners() {

        ObjectMapper mapper = new ObjectMapper();
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerName("Intervener 1")
            .intervenerEmail("Intervener email")
            .intervenerCorrespondenceEnabled(Boolean.TRUE)
            .build();
        caseDetails.getData().put("intervener1", mapper.convertValue(intervenerOne, Map.class));
        caseDetails.setCaseTypeId(CaseType.CONTESTED.getCcdType());

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);

        FinremCaseDetails finremCaseDetails =
            FinremCaseDetails.builder().data(FinremCaseData.builder().intervenerOne(intervenerOne).build()).build();
        Mockito.lenient().when(finremCaseDetailsMapper
            .mapToFinremCaseDetails(caseDetails)).thenReturn(finremCaseDetails);
        Mockito.lenient().when(notificationService
            .isIntervenerSolicitorDigitalAndEmailPopulated(intervenerOne, finremCaseDetails)).thenReturn(false);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(notificationService).sendPrepareForHearingEmailRespondent(caseDetails);
        verify(notificationService).sendPrepareForHearingEmailApplicant(caseDetails);
        verify(bulkPrintService).printIntervenerDocuments(any(IntervenerOne.class), any(CaseDetails.class), anyString(), anyList());
    }


    protected BulkPrintDocument getBulkPrintDocument() {
        return BulkPrintDocument.builder().build();
    }
}
