package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.bulkPrintDocumentList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.caseDocumentToBulkPrintDocument;

@ActiveProfiles("test-mock-document-client")
public class BulkPrintServiceTest extends BaseServiceTest {

    @Autowired private DocumentClient documentClient;
    @Autowired private BulkPrintService bulkPrintService;
    @Autowired private ObjectMapper mapper;

    @MockBean private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    @MockBean private GeneralOrderService generalOrderService;
    @MockBean private FeatureToggleService featureToggleService;
    @MockBean private GenerateCoverSheetService coverSheetService;
    @MockBean private GenericDocumentService genericDocumentService;
    @MockBean private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @Value("${document.bulkPrintTemplate}") private String documentBulkPrintTemplate;
    @Value("${document.bulkPrintFileName}") private String documentBulkPrintFileName;

    @Value("${document.approvedConsentOrderNotificationTemplate}") private String documentApprovedConsentOrderNotificationTemplate;
    @Value("${document.approvedConsentOrderNotificationFileName}") private String documentApprovedConsentOrderNotificationFileName;

    private UUID letterId;
    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor;
    private CaseDocument caseDocument = new CaseDocument();
    private BulkPrintDocument bulkPrintDocument = caseDocumentToBulkPrintDocument(caseDocument);

    @Before
    public void setUp() {
        letterId = UUID.randomUUID();
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
        when(genericDocumentService.bulkPrint(any())).thenReturn(letterId);
    }

    @Test
    public void shouldSendAssignedToJudgeNotificationLetterForBulkPrint() {
        UUID bulkPrintLetterId = bulkPrintService.sendNotificationLetterForBulkPrint(
            caseDocument, caseDetails());

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

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN, bulkPrintDocument))
            .thenReturn(bulkPrintDocumentList());

        Map<String, Object> caseData = bulkPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        assertThat(caseData.containsKey("bulkPrintCoverSheetRes"), is(true));
        assertThat(caseData.get("bulkPrintLetterIdRes"), is(letterId));
        assertThat(caseData.get("bulkPrintLetterIdApp"), is(letterId));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(consentOrderNotApprovedDocumentService).prepareApplicantLetterPack(caseDetails, AUTH_TOKEN, bulkPrintDocument);
        verify(genericDocumentService, times(2)).bulkPrint(any());
    }

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddressAsSolicitorEmailIsNo() {
        final String consentedBulkPrintSimpleJson = "/fixtures/contested/bulk_print_simple.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintSimpleJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);

        Map<String, Object> caseData = bulkPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

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

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(consentOrderApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN)).thenReturn(bulkPrintDocuments);
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        Map<String, Object> caseData = bulkPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        assertThat(caseData.containsKey("bulkPrintCoverSheetRes"), is(true));
        assertThat(caseData.get("bulkPrintLetterIdRes"), is(letterId));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenOrderNotApprovedFirstAndThenOrderIsApproved_WhenBulkPrinting_ThenConsentOrderApprovedDocumentsArePrinted() {
        final String consentedBulkPrintConsentOrderApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderApprovedJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);

        Map<String, Object> caseData = bulkPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        assertThat(caseData.containsKey("bulkPrintCoverSheetRes"), is(true));
        assertThat(caseData.get("bulkPrintLetterIdRes"), is(letterId));
        assertThat(caseData.get("bulkPrintLetterIdApp"), is(letterId));
    }

    @Test
    public void shouldAddGeneralOrdersIfToggleOn() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = Collections.emptyList();

        when(featureToggleService.isPrintGeneralOrderEnabled()).thenReturn(true);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(CaseDetails.class), anyString(), any(BulkPrintDocument.class)))
            .thenReturn(bulkPrintDocuments);
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.sendOrderForBulkPrintRespondent(caseDocument, caseDetails);
        assertThat(uuid, is(letterId));
        verify(generalOrderService).getLatestGeneralOrderForPrintingConsented(caseDetails.getData());
    }

    @Test
    public void shouldNotAddGeneralOrdersIfToggleOff() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = Collections.emptyList();

        when(featureToggleService.isPrintGeneralOrderEnabled()).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN, bulkPrintDocument))
            .thenReturn(bulkPrintDocuments);
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.sendOrderForBulkPrintRespondent(caseDocument, caseDetails);

        assertThat(uuid, is(letterId));

        verify(generalOrderService, times(0)).getLatestGeneralOrderForPrintingConsented(caseDetails.getData());
    }

    @Test
    public void shouldPrintRespondentOrdersIfNotApprovedOrderMissing() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);

        when(featureToggleService.isPrintGeneralOrderEnabled()).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.notApprovedConsentOrder(caseDetails.getData())).thenReturn(Collections.emptyList());
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.sendOrderForBulkPrintRespondent(caseDocument, caseDetails);

        assertThat(uuid, is(letterId));

        verify(generalOrderService, times(0)).getLatestGeneralOrderForPrintingConsented(caseDetails.getData());
    }

    @Test
    public void shouldPrintLettersForApplicant() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN, bulkPrintDocument))
            .thenReturn(bulkPrintDocuments);
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantConsentOrderNotApprovedDocuments(caseDetails, AUTH_TOKEN);
        assertThat(uuid, is(letterId));
    }

    @Test
    public void shouldNotPrintLettersForApplicantIfNoDocuments() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = Collections.emptyList();

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN, bulkPrintDocument))
            .thenReturn(bulkPrintDocuments);
        when(documentClient.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);

        UUID uuid = bulkPrintService.printApplicantConsentOrderNotApprovedDocuments(caseDetails, AUTH_TOKEN);
        assertTrue(uuid == null);
    }

    private CaseDetails caseDetails() {
        return TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print.json", mapper);
    }
}
