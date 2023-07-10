package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContestedSendOrderGeneralOrderLetterCorresponderTest {

    private static final String AUTHORISATION_TOKEN = "Bearer token";
    ContestedSendOrderGeneralOrderLetterCorresponder contestedSendOrderGeneralOrderLetterCorresponder;
    @Mock
    private BulkPrintService bulkprintService;
    @Mock
    NotificationService notificationService;
    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    GeneralOrderService generalOrderService;
    private CaseDetails caseDetails;

    @Before
    public void setupTest() {
        contestedSendOrderGeneralOrderLetterCorresponder =
            new ContestedSendOrderGeneralOrderLetterCorresponder(bulkprintService, notificationService, finremCaseDetailsMapper, generalOrderService);
        caseDetails = CaseDetails.builder().id(1234L).data(new HashMap<>()).build();

    }

    @Test
    public void shouldSendLetterToApplicantAndRespondentSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        BulkPrintDocument bulkPrintDocument = BulkPrintDocument.builder().build();
        when(generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData(), AUTHORISATION_TOKEN,
            caseDetails.getId().toString())).thenReturn(
            bulkPrintDocument);
        contestedSendOrderGeneralOrderLetterCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(generalOrderService, times(2)).getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData(), AUTHORISATION_TOKEN,
            caseDetails.getId().toString());

        verify(bulkprintService).printApplicantDocuments(caseDetails, AUTHORISATION_TOKEN, List.of(bulkPrintDocument));
        verify(bulkprintService).printApplicantDocuments(caseDetails, AUTHORISATION_TOKEN, List.of(bulkPrintDocument));
    }

    @Test
    public void shouldSendLettersToInterveners() {

        ObjectMapper mapper = new ObjectMapper();
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder()
            .intervenerName("Intervener 1")
            .intervenerEmail("Intervener email")
            .build();
        caseDetails.getData().put("intervener1", mapper.convertValue(intervenerOneWrapper, Map.class));
        caseDetails.setCaseTypeId(CaseType.CONTESTED.getCcdType());

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);

        BulkPrintDocument bulkPrintDocument = BulkPrintDocument.builder().build();
        when(generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData(), AUTHORISATION_TOKEN,
            caseDetails.getId().toString())).thenReturn(
            bulkPrintDocument);

        FinremCaseDetails finremCaseDetails =
            FinremCaseDetails.builder().data(FinremCaseData.builder().intervenerOneWrapper(intervenerOneWrapper).build()).build();
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(finremCaseDetails);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerOneWrapper, finremCaseDetails)).thenReturn(false);

        contestedSendOrderGeneralOrderLetterCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(bulkprintService).printIntervenerDocuments(any(IntervenerOneWrapper.class), any(CaseDetails.class), anyString(), anyList());
    }
}
