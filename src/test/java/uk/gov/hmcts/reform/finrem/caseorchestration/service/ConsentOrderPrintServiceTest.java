package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.EvidenceManagementClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.bulkPrintDocumentList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

@ActiveProfiles("test-mock-feign-clients")
public class ConsentOrderPrintServiceTest extends BaseServiceTest {

    private static final UUID LETTER_ID = UUID.randomUUID();

    private final ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
    private final Document caseDocument = newDocument();

    @Autowired private ObjectMapper mapper;
    @Autowired private ConsentOrderPrintService consentOrderPrintService;
    @Autowired private EvidenceManagementClient evidenceManagementClientMock;

    @MockBean private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    @MockBean private GenerateCoverSheetService coverSheetService;
    @MockBean private GenericDocumentService genericDocumentService;
    @MockBean private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @Before
    public void init() {
        when(genericDocumentService.bulkPrint(any())).thenReturn(LETTER_ID);
    }

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddress() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN))
            .thenReturn(bulkPrintDocumentList());

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getCaseData();
        assertThat(caseData.getBulkPrintCoverSheetRes(), is(notNullValue()));
        assertThat(caseData.getBulkPrintLetterIdRes(), is(LETTER_ID));
        assertThat(caseData.getBulkPrintLetterIdApp(), is(LETTER_ID));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(consentOrderNotApprovedDocumentService).prepareApplicantLetterPack(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService, times(2)).bulkPrint(any());
    }

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddressAsSolicitorEmailIsNo() {
        final String consentedBulkPrintSimpleJson = "/fixtures/contested/bulk_print_simple.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintSimpleJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getCaseData();
        assertThat(caseData.getBulkPrintCoverSheetRes(), is(notNullValue()));
        assertThat(caseData.getBulkPrintLetterIdRes(), is(LETTER_ID));
        assertThat(caseData.getBulkPrintLetterIdApp(), is(LETTER_ID));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void shouldSendForBulkPrintPackWithOnlyRespondentAddress() {
        final String consentedBulkPrintConsentOrderApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = emptyList();

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(consentOrderApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN)).thenReturn(bulkPrintDocuments);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getCaseData();
        assertThat(caseData.getBulkPrintCoverSheetRes(), is(notNullValue()));
        assertThat(caseData.getBulkPrintLetterIdRes(), is(LETTER_ID));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenOrderNotApprovedFirstAndThenOrderIsApproved_WhenBulkPrinting_ThenConsentOrderApprovedDocumentsArePrinted() {
        final String consentedBulkPrintConsentOrderApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderApprovedJson, mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getCaseData();
        assertThat(caseData.getBulkPrintCoverSheetRes(), is(notNullValue()));
        assertThat(caseData.getBulkPrintLetterIdRes(), is(LETTER_ID));
        assertThat(caseData.getBulkPrintLetterIdApp(), is(LETTER_ID));
    }

    @Test
    public void shouldPrintRespondentOrdersIfNotApprovedOrderMissing() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);

        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), anyString()))
            .thenReturn(emptyList());
        when(coverSheetService.generateRespondentCoverSheet(any(), eq(AUTH_TOKEN))).thenReturn(caseDocument);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService).bulkPrint(bulkPrintRequestArgumentCaptor.capture());
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().stream().map(BulkPrintDocument::getBinaryFileUrl)
            .collect(Collectors.toList()), hasItem("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void shouldPrintLettersForApplicant() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintDocumentList();

        when(coverSheetService.generateApplicantCoverSheet(any(), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateRespondentCoverSheet(any(), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN))
            .thenReturn(bulkPrintDocuments);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);
        assertThat(caseDetails.getCaseData().getBulkPrintLetterIdApp(), is(LETTER_ID));
    }

    @Test
    public void shouldNotPrintLettersForApplicantIfNoDocuments() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);

        when(coverSheetService.generateApplicantCoverSheet(any(), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateRespondentCoverSheet(any(), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN))
            .thenReturn(emptyList());

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);
        assertThat(caseDetails.getCaseData().getBulkPrintLetterIdApp(), is(nullValue()));
    }

    @Test
    public void shouldSendForBulkPrintPackForConsentInContestedApprovedOrder() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/consent-in-contested-application-approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);

        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN))
            .thenReturn(bulkPrintDocumentList());
        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getCaseData();
        assertThat(caseData.getBulkPrintCoverSheetRes(), is(notNullValue()));
        assertThat(caseData.getBulkPrintLetterIdRes(), is(LETTER_ID));
        assertThat(caseData.getBulkPrintLetterIdApp(), is(LETTER_ID));

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService, times(2)).bulkPrint(any());
    }

    @Test
    public void shouldNotSendApplicantPackForPrintingIfConsentedForEmail() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/consent-in-contested-application-approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.YES);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN))
            .thenReturn(bulkPrintDocumentList());
        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getCaseData();
        assertThat(caseData.getBulkPrintCoverSheetRes(), is(notNullValue()));
        assertThat(caseData.getBulkPrintLetterIdRes(), is(LETTER_ID));
        assertThat(caseData.getBulkPrintLetterIdApp(), nullValue());

        verify(coverSheetService).generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        verify(genericDocumentService, times(1)).bulkPrint(any());
    }

    @Test
    public void givenGeneralOrderIssuedAfterNotApprovedConsentOrder_whenSendOrderToBulkPrint_generalOrderIsPrinted() {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(
            "/fixtures/contested/bulk_print_consent_order_not_approved.json", mapper);

        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN))
            .thenReturn(bulkPrintDocumentList());
        when(consentOrderNotApprovedDocumentService.notApprovedConsentOrder(any())).thenReturn(singletonList(caseDocument));
        Date now = new Date();
        Date beforeNow = new Date(now.getTime() - 1);
        when(evidenceManagementClientMock.auditFileUrls(eq(AUTH_TOKEN), any())).thenReturn(asList(
            FileUploadResponse.builder().modifiedOn(now).build(),
            FileUploadResponse.builder().modifiedOn(beforeNow).build()));

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(2)).bulkPrint(bulkPrintRequestArgumentCaptor.capture());
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().stream().map(BulkPrintDocument::getBinaryFileUrl)
            .collect(Collectors.toList()), hasItem("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void shouldStoreConfidentialCoversheetsWhenAddressesAreConfidential() {
        final String consentedBulkPrintConsentOrderNotApprovedJson
            = "/fixtures/contested/bulk_print_consent_order_not_approved.json";
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(consentedBulkPrintConsentOrderNotApprovedJson, mapper);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantAddressConfidential(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentAddressConfidential(YesOrNo.YES);

        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN))
            .thenReturn(bulkPrintDocumentList());

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getCaseData();

        assertThat(caseData.getBulkPrintCoverSheetApp(), is(nullValue()));
        assertThat(caseData.getBulkPrintCoverSheetAppConfidential(), is(notNullValue()));

        assertThat(caseData.getBulkPrintCoverSheetRes(), is(nullValue()));
        assertThat(caseData.getBulkPrintCoverSheetResConfidential(), is(notNullValue()));
    }
}
