package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@ActiveProfiles("test-mock-document-client")
public class BulkPrintServiceTest extends BaseServiceTest {

    @Autowired private DocumentClient documentClient;
    @Autowired private BulkPrintService bulkPrintService;
    @Autowired private ObjectMapper mapper;

    private UUID letterId;
    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor;

    @MockBean
    private GenerateCoverSheetService coverSheetService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @Before
    public void setUp() {
        letterId = UUID.randomUUID();
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
        when(genericDocumentService.bulkPrint(any())).thenReturn(letterId);
    }

    @Test
    public void shouldSendAssignedToJudgeNotificationLetterForBulkPrint() {
        UUID bulkPrintLetterId = bulkPrintService.sendNotificationLetterForBulkPrint(
            new CaseDocument(), caseDetails());

        assertThat(bulkPrintLetterId, is(letterId));
    }

    @Test
    public void shouldConvertCollectionDocument() {
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintService.approvedOrderCollection(caseDetails().getData());

        assertThat(bulkPrintDocuments, hasSize(4));
    }

    @Test
    public void whenPrintingGeneralLetter_thenLatestGeneralLetterIsSentToPrinting() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter.json", mapper);
        UUID bulkPrintLetterId = bulkPrintService.printLatestGeneralLetter(caseDetails);

        assertThat(bulkPrintLetterId, is(letterId));
    }

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddress() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";

        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(new CaseDocument());
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(new CaseDocument());

        Map<String, Object> caseData = bulkPrintService.sendToBulkPrint(caseDetails, AUTH_TOKEN);

        assertThat(caseData.containsKey("bulkPrintCoverSheetRes"), is(true));
        assertThat(caseData.get("bulkPrintLetterIdRes"), is(letterId));
        assertThat(caseData.get("bulkPrintLetterIdApp"), is(letterId));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(coverSheetService).generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService, times(2)).bulkPrint(any());
    }

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddressAsSolicitorEmailIsNo() {
        final String consentedBulkPrintSimpleJson = "/fixtures/contested/bulk_print_simple.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintSimpleJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(new CaseDocument());
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(new CaseDocument());

        Map<String, Object> caseData = bulkPrintService.sendToBulkPrint(caseDetails, AUTH_TOKEN);

        assertThat(caseData.containsKey("bulkPrintCoverSheetRes"), is(true));
        assertThat(caseData.get("bulkPrintLetterIdRes"), is(letterId));
        assertThat(caseData.get("bulkPrintLetterIdApp"), is(letterId));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void shouldSendForBulkPrintPackWithOnlyRespondentAddress() {
        final String consentedBulkPrintConsentOrderApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = Collections.emptyList();

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(new CaseDocument());
        when(consentOrderApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN)).thenReturn(bulkPrintDocuments);
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        Map<String, Object> caseData = bulkPrintService.sendToBulkPrint(caseDetails, AUTH_TOKEN);

        assertThat(caseData.containsKey("bulkPrintCoverSheetRes"), is(true));
        assertThat(caseData.get("bulkPrintLetterIdRes"), is(letterId));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnCoverSheetGeneration() {
        final String contestedHwfJson = "/fixtures/contested/hwf.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(contestedHwfJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenThrow(feignError());

        bulkPrintService.sendToBulkPrint(caseDetails, AUTH_TOKEN);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnSendForBulkPrint() {
        final String contestedHwfJson = "/fixtures/contested/hwf.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(contestedHwfJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(new CaseDocument());

        bulkPrintService.sendToBulkPrint(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenOrderNotApprovedFirstAndThenOrderIsApproved_WhenBulkPrinting_ThenConsentOrderApprovedDocumentsArePrinted() {
        final String consentedBulkPrintConsentOrderApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderApprovedJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(new CaseDocument());
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(new CaseDocument());

        Map<String, Object> caseData = bulkPrintService.sendToBulkPrint(caseDetails, AUTH_TOKEN);

        assertThat(caseData.containsKey("bulkPrintCoverSheetRes"), is(true));
        assertThat(caseData.get("bulkPrintLetterIdRes"), is(letterId));
        assertThat(caseData.get("bulkPrintLetterIdApp"), is(letterId));
    }

    private CaseDetails caseDetails() {
        return TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print.json", mapper);
    }
}
