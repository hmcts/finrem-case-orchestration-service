package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

@RunWith(MockitoJUnitRunner.class)
public class ContestedSendOrderHearingLettersCorresponderTest {

    private static final String AUTHORISATION_TOKEN = "Bearer token";
    ContestedSendOrderHearingLettersCorresponder contestedSendOrderHearingLettersCorresponder;
    @Mock
    private BulkPrintService bulkprintService;
    @Mock
    NotificationService notificationService;
    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private DocumentHelper documentHelper;
    private CaseDetails caseDetails;

    @Before
    public void setupTest() {
        contestedSendOrderHearingLettersCorresponder =
            new ContestedSendOrderHearingLettersCorresponder(bulkprintService, notificationService, finremCaseDetailsMapper, documentHelper);
        caseDetails = CaseDetails.builder().id(1234L).data(new HashMap<>()).build();

    }

    @Test
    public void shouldSendLetterToApplicantAndRespondentSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        setUpGetBulkPrintDocumentMocks();

        contestedSendOrderHearingLettersCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(bulkprintService).printApplicantDocuments(any(CaseDetails.class), any(), any());

        verify(bulkprintService).printRespondentDocuments(any(CaseDetails.class), any(), any());
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

        setUpGetBulkPrintDocumentMocks();

        FinremCaseDetails finremCaseDetails =
            FinremCaseDetails.builder().data(FinremCaseData.builder().intervenerOneWrapper(intervenerOneWrapper).build()).build();
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(finremCaseDetails);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerOneWrapper, finremCaseDetails)).thenReturn(false);

        contestedSendOrderHearingLettersCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);

        verify(bulkprintService).printIntervenerDocuments(any(IntervenerOneWrapper.class), any(CaseDetails.class), anyString(), anyList());
    }


    private void setUpGetBulkPrintDocumentMocks() {
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);

        when(documentHelper.getDocumentLinkAsBulkPrintDocument(any(), eq(LATEST_DRAFT_HEARING_ORDER))).thenReturn(
            Optional.of(BulkPrintDocument.builder().binaryFileUrl("HearingOrderBinaryURL").build()));
        when(documentHelper.getHearingDocumentsAsBulkPrintDocuments(any(), any(), anyString())).thenReturn(
            singletonList(BulkPrintDocument.builder().binaryFileUrl("OtherHearingOrderDocumentsURL").build()));

        CaseDocument additionalHearingDocument = CaseDocument.builder().documentBinaryUrl("AdditionalHearingDocumentURL").build();
        when(documentHelper.getLatestAdditionalHearingDocument(any())).thenReturn(Optional.of(additionalHearingDocument));
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(eq(additionalHearingDocument))).thenReturn(
            BulkPrintDocument.builder().binaryFileUrl(additionalHearingDocument.getDocumentBinaryUrl()).build());
    }
}
