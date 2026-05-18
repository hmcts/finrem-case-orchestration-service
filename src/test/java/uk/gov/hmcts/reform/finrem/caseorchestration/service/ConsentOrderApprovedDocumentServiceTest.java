package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_TITLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_JUDGE_TITLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;

@ExtendWith(MockitoExtension.class)
class ConsentOrderApprovedDocumentServiceTest {

    @InjectMocks
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @Mock
    private DocumentHelper documentHelper;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private ConsentedApplicationHelper consentedApplicationHelper;

    @Mock
    private PensionAnnexDateStampService pensionAnnexDateStampService;

    private FinremCaseDetails finremCaseDetails;

    private CaseDetails detailsCopy;

    @Mock
    private StampType mockedStampType;

    @Mock
    private CaseType mockedCaseType;

    @Nested
    class GenerateApprovedConsentOrderLetterTests {

        @BeforeEach
        void setUp() {
            finremCaseDetails = spy(FinremCaseDetails.builder().build());
            detailsCopy = CaseDetails.builder().data(new HashMap<>()).build();
            CaseDetails mappedCaseDetails = mock(CaseDetails.class);
            when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(mappedCaseDetails);
            when(documentHelper.deepCopy(mappedCaseDetails, CaseDetails.class)).thenReturn(detailsCopy);
        }

        @Test
        void shouldGenerateApprovedConsentOrderLetterForConsented() {
            when(finremCaseDetails.isConsentedApplication()).thenReturn(true);
            when(finremCaseDetails.isContestedApplication()).thenReturn(false);
            when(consentedApplicationHelper.isVariationOrder(finremCaseDetails.getData())).thenReturn(false);
            when(documentConfiguration.getApprovedConsentOrderFileName()).thenReturn(FILE_NAME);
            when(documentConfiguration.getApprovedConsentOrderTemplate(detailsCopy)).thenReturn("templateName");

            CaseDocument expectedGeneratedDocument = caseDocument("expected");
            when(genericDocumentService.generateDocument(AUTH_TOKEN, detailsCopy, "templateName",
                FILE_NAME)).thenReturn(expectedGeneratedDocument);

            // Act
            CaseDocument actualDocument = consentOrderApprovedDocumentService
                .generateApprovedConsentOrderLetter(finremCaseDetails, AUTH_TOKEN);

            assertAll(
                () -> assertEquals(expectedGeneratedDocument, actualDocument),
                () -> verify(finremCaseDetailsMapper).mapToCaseDetails(finremCaseDetails),
                () -> verify(finremCaseDetails).isConsentedApplication(),
                () -> verify(documentConfiguration).getApprovedConsentOrderFileName(),
                () -> verify(documentConfiguration).getApprovedConsentOrderTemplate(detailsCopy),
                () -> verify(genericDocumentService).generateDocument(AUTH_TOKEN, detailsCopy, "templateName",
                    FILE_NAME)
            );
        }

        @Test
        void shouldGenerateApprovedVariationOrderLetterForConsented() {
            when(finremCaseDetails.isConsentedApplication()).thenReturn(true);
            when(finremCaseDetails.isContestedApplication()).thenReturn(false);
            when(consentedApplicationHelper.isVariationOrder(finremCaseDetails.getData())).thenReturn(true);
            when(documentConfiguration.getApprovedVariationOrderFileName()).thenReturn(FILE_NAME);
            when(documentConfiguration.getApprovedConsentOrderTemplate(detailsCopy)).thenReturn("templateName");

            CaseDocument expectedGeneratedDocument = caseDocument("expected");
            when(genericDocumentService.generateDocument(AUTH_TOKEN, detailsCopy, "templateName",
                FILE_NAME)).thenReturn(expectedGeneratedDocument);

            // Act
            CaseDocument actualDocument = consentOrderApprovedDocumentService
                .generateApprovedConsentOrderLetter(finremCaseDetails, AUTH_TOKEN);

            assertAll(
                () -> assertEquals(expectedGeneratedDocument, actualDocument),
                () -> verify(finremCaseDetailsMapper).mapToCaseDetails(finremCaseDetails),
                () -> verify(finremCaseDetails).isConsentedApplication(),
                () -> verify(documentConfiguration).getApprovedVariationOrderFileName(),
                () -> verify(documentConfiguration).getApprovedConsentOrderTemplate(detailsCopy),
                () -> verify(genericDocumentService).generateDocument(AUTH_TOKEN, detailsCopy, "templateName",
                    FILE_NAME)
            );
        }

        @Test
        void shouldGenerateApprovedConsentOrderLetterForContested() {
            when(finremCaseDetails.isConsentedApplication()).thenReturn(false);
            when(finremCaseDetails.isContestedApplication()).thenReturn(true);
            when(documentConfiguration.getApprovedConsentOrderFileName()).thenReturn(FILE_NAME);
            when(documentConfiguration.getApprovedConsentOrderTemplate(detailsCopy)).thenReturn("templateName");

            detailsCopy.getData().putAll(Map.of(
                CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "contestedFirstMiddleName",
                CONTESTED_RESPONDENT_LAST_NAME, "contestedLastName",
                CONTESTED_ORDER_DIRECTION_JUDGE_TITLE, "contestedJudgeTitle",
                CONTESTED_ORDER_DIRECTION_JUDGE_NAME, "contestedJudgeName",
                CONTESTED_ORDER_DIRECTION_DATE, "contestedDirectionDate"
            ));

            CaseDocument expectedGeneratedDocument = caseDocument("expected");
            when(genericDocumentService.generateDocument(eq(AUTH_TOKEN), argThat(cd ->
                cd.getData().get(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME).equals("contestedFirstMiddleName")
                    && cd.getData().get(CONSENTED_RESPONDENT_LAST_NAME).equals("contestedLastName")
                    && cd.getData().get(CONSENTED_ORDER_DIRECTION_JUDGE_TITLE).equals("contestedJudgeTitle")
                    && cd.getData().get(CONSENTED_ORDER_DIRECTION_JUDGE_NAME).equals("contestedJudgeName")
                    && cd.getData().get(CONSENTED_ORDER_DIRECTION_DATE).equals("contestedDirectionDate")
            ), eq("templateName"), eq(FILE_NAME))).thenReturn(expectedGeneratedDocument);

            // Act
            CaseDocument actualDocument = consentOrderApprovedDocumentService
                .generateApprovedConsentOrderLetter(finremCaseDetails, AUTH_TOKEN);

            assertAll(
                () -> assertEquals(expectedGeneratedDocument, actualDocument),
                () -> verify(finremCaseDetailsMapper).mapToCaseDetails(finremCaseDetails),
                () -> verify(finremCaseDetails).isConsentedApplication(),
                () -> verify(documentConfiguration).getApprovedConsentOrderFileName(),
                () -> verify(documentConfiguration).getApprovedConsentOrderTemplate(detailsCopy),
                () -> verify(genericDocumentService).generateDocument(AUTH_TOKEN, detailsCopy, "templateName",
                    FILE_NAME)
            );
        }
    }

    @Test
    void givenNullDocumentInPensionDocuments_whenStampingDocuments_thenTheNullValueIsIgnored() throws Exception {
        PensionTypeCollection toBeStamped = pensionDocumentData();
        PensionTypeCollection stamped = pensionDocumentData();
        when(documentHelper.deepCopy(toBeStamped, PensionTypeCollection.class)).thenReturn(stamped);

        PensionTypeCollection pensionCollectionDataWithNullDocument = pensionDocumentData();
        pensionCollectionDataWithNullDocument.getTypedCaseDocument().setPensionDocument(null);

        CaseDocument stampedCaseDocument = caseDocument("stampedCaseDocument");
        LocalDate approvalDate = mock(LocalDate.class);
        when(genericDocumentService.stampDocument(toBeStamped.getTypedCaseDocument().getPensionDocument(), AUTH_TOKEN,
            mockedStampType, mockedCaseType)).thenReturn(stampedCaseDocument);

        doReturn(stampedCaseDocument)
            .when(pensionAnnexDateStampService)
            .appendApprovedDateToDocument(
                stampedCaseDocument,
                AUTH_TOKEN,
                approvalDate,
                mockedCaseType
            );

        List<PensionTypeCollection> stampPensionDocuments = consentOrderApprovedDocumentService
            .stampPensionDocuments(List.of(toBeStamped, pensionCollectionDataWithNullDocument), AUTH_TOKEN,
                mockedStampType, approvalDate, mockedCaseType);

        assertAll(
            () -> assertThat(stampPensionDocuments).hasSize(1),
            () -> verify(genericDocumentService).stampDocument(toBeStamped.getTypedCaseDocument().getPensionDocument(),
                AUTH_TOKEN, mockedStampType, mockedCaseType),
            () -> verify(pensionAnnexDateStampService).appendApprovedDateToDocument(stampedCaseDocument,
                AUTH_TOKEN, approvalDate, mockedCaseType),
            () -> verify(documentHelper).deepCopy(toBeStamped, PensionTypeCollection.class)
        );
    }

//    @Test
//    void stampsAndPopulatesCaseDataForContestedConsentOrder() throws Exception {
//        when(pdfStampingServiceMock.stampDocument(document(), AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, CONTESTED))
//            .thenReturn(document());
//        finremCaseDetails = defaultConsentedFinremCaseDetails();
//        FinremCaseData caseData = finremCaseDetails.getData();
//        caseData.setConsentOrder(caseDocument());
//
//        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(finremCaseDetails, AUTH_TOKEN);
//        assertThat(getDocumentList(caseData), hasSize(1));
//
//        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(finremCaseDetails, AUTH_TOKEN);
//        assertThat(getDocumentList(caseData), hasSize(2));
//    }
//
//    @Test
//    public void givenFinremCaseDetails_whenAddGenApprovedDocs_thenCaseDocsAdded() {
//        when(pdfStampingServiceMock.stampDocument(
//            any(Document.class), eq(AUTH_TOKEN), eq(false), eq(StampType.FAMILY_COURT_STAMP), eq(CONTESTED)))
//            .thenReturn(document());
//        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
//        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
//        FinremCaseDetails finremCaseDetails = finremCaseDetails();
//        consentOrderApprovedDocumentService
//           .addGeneratedApprovedConsentOrderDocumentsToCase(AUTH_TOKEN, finremCaseDetails);
//
//        assertThat(finremCaseDetails.getData().getApprovedOrderCollection(), hasSize(3));
//    }
//
//    @Test
//    public void givenFinremCaseDetails_whenAddApprovedConsentCoverLetter_thenCaseDocsAdded() {
//        CaseDocument coverLetter = CaseDocument.builder()
//            .documentFilename("approvedConsentOrderNotificationFileName")
//            .documentUrl("approvedConsentOrderNotificationUrl")
//            .documentBinaryUrl("approvedConsentOrderNotificationBinaryUrl")
//            .build();
//        when(documentHelper.prepareLetterTemplateData(
//            finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(caseDetails);
//        when(documentConfiguration.getApprovedConsentOrderNotificationFileName()).thenReturn("approvedConsentOrderNotificationFileName");
//        when(genericDocumentService.generateDocument(eq(AUTH_TOKEN), any(CaseDetails.class), anyString(), anyString())).thenReturn(coverLetter);
//        when(documentConfiguration.getApprovedConsentOrderNotificationTemplate()).thenReturn("approvedConsentOrderNotificationTemplate");
//        List<CaseDocument> documents = new ArrayList<>();
//        consentOrderApprovedDocumentService.addApprovedConsentCoverLetter(
//            finremCaseDetails, documents, AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);
//        assertThat(documents, hasSize(1));
//        assertThat(documents, hasItem(coverLetter));
//    }
//
//    @Test
//    public void givenApprovedOrderModifiedLatest_whenThereIsANotApprovedOrder_thenReturnTrue() {
//        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_").build();
//        CaseDocument caseDocument2 = CaseDocument.builder().documentBinaryUrl("test_url_2").build();
//        ConsentInContestedApprovedOrder approvedOrder = ConsentInContestedApprovedOrder.builder()
//            .consentOrder(caseDocument).orderLetter(caseDocument).build();
//        UnapproveOrder approvedOrder2 = UnapproveOrder.builder().caseDocument(caseDocument2).orderReceivedAt(LocalDateTime.now()).build();
//        ConsentInContestedApprovedOrderCollection collection1 = ConsentInContestedApprovedOrderCollection.builder()
//            .approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
//        UnapprovedOrderCollection collection2 = UnapprovedOrderCollection.builder()
//            .value(approvedOrder2).id(UUID.randomUUID().toString()).build();
//        ConsentOrderWrapper wrapper = ConsentOrderWrapper.builder()
//            .appConsentApprovedOrders(List.of(collection1)).appRefusedOrderCollection(List.of(collection2)).build();
//        consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, AUTH_TOKEN);
//        when(evidenceManagementAuditService.audit(any(), eq(AUTH_TOKEN))).thenReturn(asList(
//            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().toString()).build(),
//            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().minusDays(2).toString()).build()));
//        assertThat(documentOrderingService.isDocumentModifiedLater(caseDocument, caseDocument2, AUTH_TOKEN), is(true));
//    }
//
//    @Test
//    public void givenNotApprovedOrderModifiedLatest_whenThereIsAApprovedOrder_thenReturnFalse() {
//        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_").build();
//        CaseDocument caseDocument2 = CaseDocument.builder().documentBinaryUrl("test_url_2").build();
//        ConsentInContestedApprovedOrder approvedOrder = ConsentInContestedApprovedOrder.builder()
//            .consentOrder(caseDocument).orderLetter(caseDocument).build();
//        UnapproveOrder approvedOrder2 = UnapproveOrder.builder().caseDocument(caseDocument2).orderReceivedAt(LocalDateTime.now()).build();
//        ConsentInContestedApprovedOrderCollection collection1 = ConsentInContestedApprovedOrderCollection.builder()
//            .approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
//        UnapprovedOrderCollection collection2 = UnapprovedOrderCollection.builder().value(approvedOrder2).id(UUID.randomUUID().toString()).build();
//        ConsentOrderWrapper wrapper = ConsentOrderWrapper.builder().appConsentApprovedOrders(List.of(collection1))
//            .appRefusedOrderCollection(List.of(collection2)).build();
//        consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, AUTH_TOKEN);
//        when(evidenceManagementAuditService.audit(any(), eq(AUTH_TOKEN))).thenReturn(asList(
//            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().minusDays(2).toString()).build(),
//            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().toString()).build()
//        ));
//        assertThat(documentOrderingService.isDocumentModifiedLater(caseDocument, caseDocument2, AUTH_TOKEN), is(false));
//    }
//
//    @Test
//    public void givenNoNotApprovedOrder_whenThereIsAApprovedOrder_thenReturnTrue() {
//        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_").build();
//        ApprovedOrder approvedOrder = ApprovedOrder.builder().consentOrder(caseDocument).orderLetter(caseDocument).build();
//        ConsentOrderCollection collection1 = ConsentOrderCollection.builder().approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
//        ConsentOrderWrapper wrapper = ConsentOrderWrapper.builder().contestedConsentedApprovedOrders(List.of(collection1)).build();
//        assertThat(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, AUTH_TOKEN), equalTo(true));
//    }
//
//    @Test
//    public void givenNoApprovedOrder_whenThereIsANotApprovedOrder_thenReturnFalse() {
//        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl("test_url_2").build();
//        ApprovedOrder approvedOrder = ApprovedOrder.builder().consentOrder(caseDocument).build();
//        ConsentOrderCollection collection = ConsentOrderCollection.builder().approvedOrder(approvedOrder).id(UUID.randomUUID().toString()).build();
//        ConsentOrderWrapper wrapper = ConsentOrderWrapper.builder().consentedNotApprovedOrders(List.of(collection)).build();
//        assertThat(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, AUTH_TOKEN), equalTo(false));
//    }
//
//    private List<ConsentOrderCollection> getDocumentList(FinremCaseData data) {
//        return Optional.ofNullable(data.getConsentOrderWrapper().getContestedConsentedApprovedOrders()).orElse(new ArrayList<>());
//    }
//
//    private FinremCaseDetails finremCaseDetails() {
//        return TestSetUpUtils.finremCaseDetailsFromResource(
//            "/fixtures/approvedOrder/consentedApprovedOrder.json", mapper);
//    }
}
