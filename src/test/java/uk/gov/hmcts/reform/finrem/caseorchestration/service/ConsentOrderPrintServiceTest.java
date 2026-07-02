package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementAuditService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.bulkPrintDocumentList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SEND_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@ActiveProfiles("test-mock-feign-clients")
public class ConsentOrderPrintServiceTest extends BaseServiceTest {

    private static final UUID LETTER_ID = UUID.randomUUID();

    private final ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
    private final CaseDocument caseDocument = caseDocument();

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private ConsentOrderPrintService consentOrderPrintService;
    @MockitoBean
    private EvidenceManagementAuditService evidenceManagementAuditService;

    @MockitoBean
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    @MockitoBean
    private GenerateCoverSheetService coverSheetService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private GenericDocumentService genericDocumentService;
    @MockitoBean
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @MockitoBean
    private DocumentOrderingService documentOrderingService;

    @Before
    public void init() {
        when(genericDocumentService.bulkPrint(any())).thenReturn(LETTER_ID);
    }

    @Test
    public void should_send_approve_order_when_no_general_order_available() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        CaseDataService caseDataService = mock(CaseDataService.class);
        when(caseDataService.isOrderApprovedCollectionPresent(caseDetails.getData())).thenReturn(true);
        when(caseDataService.isContestedOrderNotApprovedCollectionPresent(caseDetails.getData())).thenReturn(false);

        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);

        FinremCaseDetails caseDetailsBefore = defaultContestedFinremCaseDetails();
        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, caseDetailsBefore,
            CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getData();
        assertNotNull(caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(2)).bulkPrint(any(BulkPrintRequest.class));
    }

    @Test
    public void when_general_order_added_it_should_send_general_order_not_approve_order() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();

        caseDetails.getData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        CaseDataService caseDataService = mock(CaseDataService.class);
        when(caseDataService.isOrderApprovedCollectionPresent(caseDetails.getData())).thenReturn(true);
        when(caseDataService.isContestedOrderNotApprovedCollectionPresent(caseDetails.getData())).thenReturn(false);

        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);
        FinremCaseDetails caseDetailsBefore = defaultContestedFinremCaseDetails();
        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, caseDetailsBefore,
            CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getData();
        assertNotNull(caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(2)).bulkPrint(any(BulkPrintRequest.class));
    }

    @Test
    public void when_general_order_and_approved_order_then_send_latest_order_send_approve_order() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        FinremCaseDetails caseDetailsBefore = defaultContestedFinremCaseDetails();

        caseDetailsBefore.getData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        caseDetails.getData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        CaseDataService caseDataService = mock(CaseDataService.class);
        when(caseDataService.isOrderApprovedCollectionPresent(caseDetails.getData())).thenReturn(true);
        when(caseDataService.isContestedOrderNotApprovedCollectionPresent(caseDetails.getData())).thenReturn(false);

        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, caseDetailsBefore,
            CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getData();
        assertNotNull(caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(2)).bulkPrint(bulkPrintRequestArgumentCaptor.capture());
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().stream().map(BulkPrintDocument::getBinaryFileUrl)
            .collect(Collectors.toList()), hasItem("http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64/binary"));
    }

    @Test
    public void when_general_order_present_and_no_approved_orders_then_send_general_order() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        FinremCaseDetails caseDetailsBefore = defaultContestedFinremCaseDetails();

        CaseDocument generalOrder = caseDocument(DOC_URL, "GeneralOrder.pdf", BINARY_URL);

        caseDetails.getData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(generalOrder);
        caseDetailsBefore.getData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(null);

        CaseDataService caseDataService = mock(CaseDataService.class);
        when(caseDataService.isOrderApprovedCollectionPresent(caseDetails.getData())).thenReturn(false);

        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, caseDetailsBefore,
            CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getData();
        assertNotNull(caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(2)).bulkPrint(bulkPrintRequestArgumentCaptor.capture());
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().stream().map(BulkPrintDocument::getBinaryFileUrl)
            .toList(), hasItem("http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64/binary"));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().stream().map(BulkPrintDocument::getFileName)
            .toList(), hasItem("GeneralOrder.pdf"));
    }

    @Test
    public void when_general_order_and_approved_order_then_send_latest_order_send_general_order() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        FinremCaseDetails caseDetailsBefore = defaultContestedFinremCaseDetails();
        BulkPrintDocument bulkPrintDocument = BulkPrintDocument
            .builder()
            .binaryFileUrl(BINARY_URL)
            .fileName("NotApprovedCoverLetter.pdf")
            .build();

        CaseDocument generalOrder = caseDocument(DOC_URL, "GeneralOrder.pdf", BINARY_URL);

        caseDetailsBefore.getData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(generalOrder);
        caseDetails.getData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(generalOrder);

        when(documentOrderingService.isDocumentModifiedLater(any(), any(), anyString())).thenReturn(true);

        when(consentOrderApprovedDocumentService.approvedOrderDocuments(caseDetails, AUTH_TOKEN)).thenReturn(List.of(caseDocument()));
        CaseDataService caseDataService = mock(CaseDataService.class);
        when(caseDataService.isOrderApprovedCollectionPresent(caseDetails.getData())).thenReturn(true);
        when(caseDataService.isContestedOrderNotApprovedCollectionPresent(caseDetails.getData())).thenReturn(false);

        when(coverSheetService.generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(coverSheetService.generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(caseDocument);
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(bulkPrintDocumentList());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.notApprovedCoverLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(bulkPrintDocument);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, caseDetailsBefore,
            CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, AUTH_TOKEN);

        FinremCaseData caseData = caseDetails.getData();
        assertNotNull(caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes());
        assertEquals(caseData.getBulkPrintLetterIdRes(), LETTER_ID.toString());
        assertEquals(caseData.getBulkPrintLetterIdApp(), LETTER_ID.toString());

        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(2)).bulkPrint(bulkPrintRequestArgumentCaptor.capture());
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().stream().map(BulkPrintDocument::getBinaryFileUrl)
            .collect(Collectors.toList()), hasItem("http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64/binary"));
        assertThat(bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments().stream().map(BulkPrintDocument::getFileName)
            .collect(Collectors.toList()), hasItem("GeneralOrder.pdf"));
    }

    @Test
    public void givenConsentedCaseWithGeneralOrderAndNoApprovedOrder_whenSendConsentOrderToBulkPrint_thenPrintsThreeDocs() {
        // Arrange
        FinremCaseDetails caseDetails = consentedCaseDetailsWithGeneralOrder();

        BulkPrintDocument generalOrder = BulkPrintDocument.builder()
            .fileName("generalOrder.pdf")
            .binaryFileUrl(BINARY_URL)
            .build();
        BulkPrintDocument consentOrderNotApprovedCoverLetter = BulkPrintDocument.builder()
            .fileName("consentOrderNotApprovedCoverLetter.pdf")
            .build();

        // Applicant documents for bulk print
        CaseDocument applicantCoverSheet = caseDocument(DOC_URL, "applicantCoverSheet.pdf", BINARY_URL);
        when(coverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(applicantCoverSheet);
        List<BulkPrintDocument> applicantLetterPackDocs = List.of(
            generalOrder, consentOrderNotApprovedCoverLetter
        );
        when(consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, AUTH_TOKEN))
            .thenReturn(applicantLetterPackDocs);

        // Respondent documents for bulk print
        CaseDocument respondentCoverSheet = caseDocument(DOC_URL, "respondentCoverSheet.pdf", BINARY_URL);
        when(coverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN)).thenReturn(respondentCoverSheet);
        when(consentOrderNotApprovedDocumentService.notApprovedCoverLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(consentOrderNotApprovedCoverLetter);

        FinremCaseDetails caseDetailsBefore = consentedCaseDetailsWithGeneralOrder();

        // Act
        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, caseDetailsBefore, SEND_ORDER, AUTH_TOKEN);

        // Assert
        verify(genericDocumentService, times(2)).bulkPrint(bulkPrintRequestArgumentCaptor.capture());
        List<BulkPrintRequest> bulkPrintRequests = bulkPrintRequestArgumentCaptor.getAllValues();
        assertThat(bulkPrintRequests, hasSize(2));
        assertThat(bulkPrintRequests.getFirst().getRecipientParty(), is(APPLICANT));
        assertThat(bulkPrintRequests.getFirst().getAuthorisationToken(), is(AUTH_TOKEN));
        assertThat(bulkPrintRequests.get(1).getRecipientParty(), is(RESPONDENT));
        assertThat(bulkPrintRequests.get(1).getAuthorisationToken(), is(AUTH_TOKEN));

        // Applicant documents
        BulkPrintRequest applicantBulkPrintRequest = bulkPrintRequests.get(0);
        List<String> applicantBulkPrintDocumentFilenames = applicantBulkPrintRequest.getBulkPrintDocuments().stream()
            .map(BulkPrintDocument::getFileName)
            .toList();
        assertThat(applicantBulkPrintDocumentFilenames, containsInAnyOrder("generalOrder.pdf",
            "consentOrderNotApprovedCoverLetter.pdf", "applicantCoverSheet.pdf"));

        // Respondent documents
        BulkPrintRequest respondentBulkPrintRequest = bulkPrintRequests.get(1);
        List<String> respondentBulkPrintDocumentFilenames = respondentBulkPrintRequest.getBulkPrintDocuments().stream()
            .map(BulkPrintDocument::getFileName)
            .toList();
        assertThat(respondentBulkPrintDocumentFilenames, containsInAnyOrder("generalOrder.pdf",
            "consentOrderNotApprovedCoverLetter.pdf", "respondentCoverSheet.pdf"));
    }

    private FinremCaseDetails defaultContestedFinremCaseDetails() {
        List<ConsentOrderCollection> approvedOrderCollection = new ArrayList<>();
        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .consentOrder(caseDocument())
            .orderLetter(caseDocument())
            .build();
        approvedOrderCollection.add(ConsentOrderCollection.builder()
            .approvedOrder(approvedOrder)
            .build());

        FinremCaseData caseData = FinremCaseData.builder()
            .approvedOrderCollection(approvedOrderCollection)
            .build();

        return FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .id(987654321L)
            .state(State.APPLICATION_SUBMITTED)
            .data(caseData)
            .build();
    }

    private FinremCaseDetails consentedCaseDetailsWithGeneralOrder() {
        GeneralOrderWrapper generalOrderWrapper = GeneralOrderWrapper.builder()
            .generalOrderLatestDocument(caseDocument(DOC_URL, "generalOrder.pdf", BINARY_URL))
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .generalOrderWrapper(generalOrderWrapper)
            .build();

        return FinremCaseDetails.builder()
            .caseType(CaseType.CONSENTED)
            .id(987654321L)
            .state(State.APPLICATION_SUBMITTED)
            .data(caseData)
            .build();
    }
}
