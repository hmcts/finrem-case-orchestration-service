package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint.BulkPrintCoverLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.ApprovedConsentOrderDocumentCategoriser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
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

    @Spy
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

    @Mock
    private ApprovedConsentOrderDocumentCategoriser approvedConsentOrderCategoriser;

    @Mock
    private BulkPrintCoverLetterDetailsMapper bulkPrintLetterDetailsMapper;

    @Mock
    private DocumentOrderingService documentOrderingService;

    @Mock
    private CaseDataService caseDataService;

    private FinremCaseDetails finremCaseDetails;

    private CaseDetails detailsCopy;

    @Mock
    private StampType stampType;

    @Mock
    private CaseType caseType;

    private final CaseDocument pdfDocument = caseDocument("pdfDocument");

    private final CaseDocument stampedCaseDocument = caseDocument("stampedCaseDocument");

    private final CaseDocument stampedAndAnnexedDoc = caseDocument("stampedAndAnnexedDoc");

    @BeforeEach
    void setUp() {
        finremCaseDetails = spy(FinremCaseDetails.builder().build());
    }

    @Nested
    class GenerateApprovedConsentOrderLetterTests {

        @BeforeEach
        void setUp() {
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

    @Nested
    class StampPensionDocumentTests {

        @Test
        void shouldPensionDocumentAndIgnoreNullDocument() throws Exception {
            PensionTypeCollection toBeStamped = pensionDocumentData();
            PensionTypeCollection stamped = pensionDocumentData();
            when(documentHelper.deepCopy(toBeStamped, PensionTypeCollection.class)).thenReturn(stamped);

            PensionTypeCollection pensionCollectionDataWithNullDocument = pensionDocumentData();
            pensionCollectionDataWithNullDocument.getTypedCaseDocument().setPensionDocument(null);

            LocalDate approvalDate = mock(LocalDate.class);
            when(genericDocumentService.stampDocument(toBeStamped.getTypedCaseDocument().getPensionDocument(), AUTH_TOKEN,
                stampType, caseType)).thenReturn(stampedCaseDocument);

            doReturn(stampedCaseDocument)
                .when(pensionAnnexDateStampService)
                .appendApprovedDateToDocument(
                    stampedCaseDocument,
                    AUTH_TOKEN,
                    approvalDate,
                    caseType
                );

            List<PensionTypeCollection> stampPensionDocuments = consentOrderApprovedDocumentService
                .stampPensionDocuments(List.of(toBeStamped, pensionCollectionDataWithNullDocument), AUTH_TOKEN,
                    stampType, approvalDate, caseType);

            assertAll(
                () -> assertThat(stampPensionDocuments).hasSize(1),
                () -> verify(genericDocumentService).stampDocument(toBeStamped.getTypedCaseDocument().getPensionDocument(),
                    AUTH_TOKEN, stampType, caseType),
                () -> verify(pensionAnnexDateStampService).appendApprovedDateToDocument(stampedCaseDocument,
                    AUTH_TOKEN, approvalDate, caseType),
                () -> verify(documentHelper).deepCopy(toBeStamped, PensionTypeCollection.class),
                () -> verifyNoMoreInteractions(genericDocumentService, pensionAnnexDateStampService, documentHelper)
            );
        }
    }

    @Nested
    class AddApprovedConsentOrderCoverLetterTests {

        @Mock
        private DocumentHelper.PaperNotificationRecipient recipient;

        private CaseDetails caseDetailsForBulkPrint;

        private final CaseDocument coverLetter = caseDocument("coverLetter");

        @Mock
        private BulkPrintDocument bulkPrintDocument;

        @Test
        void shouldDoNothingForNonPaperApplication() {
            finremCaseDetails = FinremCaseDetails.builder()
                .data(FinremCaseData.builder().build())
                .build();
            when(caseDataService.isPaperApplication(finremCaseDetails.getData())).thenReturn(false);

            var result = consentOrderApprovedDocumentService.addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN, recipient);

            assertAll(
                () -> assertThat(result).isEmpty(),
                () -> verify(caseDataService).isPaperApplication(finremCaseDetails.getData()),
                () -> verifyNoInteractions(documentHelper, consentedApplicationHelper, documentConfiguration, genericDocumentService),
                () -> verifyNoMoreInteractions(caseDataService)
            );
        }

        @Test
        void shouldGenerateVariationOrderCoversheet() {
            FinremCaseData finremCaseData = FinremCaseData.builder().build();
            finremCaseDetails = FinremCaseDetails.builder()
                .data(finremCaseData)
                .build();

            Map<String, Object> map = new HashMap<>();
            stubIsPaperApplicationAndPrepareLetterTemplateData(map);
            stubApprovedConsentOrderNotificationTemplate();
            stubCoversheetToBulkPrintDocument();

            when(consentedApplicationHelper.isVariationOrder(finremCaseData)).thenReturn(true);
            when(documentConfiguration.getApprovedVariationOrderNotificationFileName())
                .thenReturn("approvedVariationOrderNotificationFileName");
            when(genericDocumentService.generateDocument(AUTH_TOKEN, caseDetailsForBulkPrint,
                "approvedConsentOrderNotificationTemplate",
                "approvedVariationOrderNotificationFileName")).thenReturn(coverLetter);

            var result = consentOrderApprovedDocumentService.addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN, recipient);

            assertAll(
                () -> assertThat(result).containsOnly(bulkPrintDocument),
                () -> assertThat(map).containsEntry("orderType", "variation"),
                () -> verify(documentHelper).prepareLetterTemplateData(finremCaseDetails, recipient),
                () -> verify(consentedApplicationHelper).isVariationOrder(finremCaseData),
                () -> verify(genericDocumentService).generateDocument(
                    AUTH_TOKEN, caseDetailsForBulkPrint,
                    "approvedConsentOrderNotificationTemplate",
                    "approvedVariationOrderNotificationFileName"
                )
            );
        }

        @Test
        void shouldGenerateConsentOrderCoversheet() {
            FinremCaseData finremCaseData = FinremCaseData.builder().build();
            finremCaseDetails = FinremCaseDetails.builder()
                .data(finremCaseData)
                .build();

            Map<String, Object> map = new HashMap<>();
            stubIsPaperApplicationAndPrepareLetterTemplateData(map);
            stubApprovedConsentOrderNotificationTemplate();
            stubCoversheetToBulkPrintDocument();

            when(consentedApplicationHelper.isVariationOrder(finremCaseData)).thenReturn(false);
            when(documentConfiguration.getApprovedConsentOrderNotificationFileName())
                .thenReturn("approvedConsentOrderNotificationFileName");
            when(genericDocumentService.generateDocument(AUTH_TOKEN, caseDetailsForBulkPrint,
                "approvedConsentOrderNotificationTemplate",
                "approvedConsentOrderNotificationFileName")).thenReturn(coverLetter);

            var result = consentOrderApprovedDocumentService.addApprovedConsentOrderCoverLetter(finremCaseDetails, AUTH_TOKEN, recipient);

            assertAll(
                () -> assertThat(result).containsOnly(bulkPrintDocument),
                () -> assertThat(map).containsEntry("orderType", "consent"),
                () -> verify(documentHelper).prepareLetterTemplateData(finremCaseDetails, recipient),
                () -> verify(consentedApplicationHelper).isVariationOrder(finremCaseData),
                () -> verify(genericDocumentService).generateDocument(
                    AUTH_TOKEN, caseDetailsForBulkPrint,
                    "approvedConsentOrderNotificationTemplate",
                    "approvedConsentOrderNotificationFileName"
                )
            );
        }

        private void stubIsPaperApplicationAndPrepareLetterTemplateData(Map<String, Object> map) {
            when(caseDataService.isPaperApplication(finremCaseDetails.getData())).thenReturn(true);

            caseDetailsForBulkPrint = mock(CaseDetails.class);
            when(caseDetailsForBulkPrint.getData()).thenReturn(map);
            when(documentHelper.prepareLetterTemplateData(finremCaseDetails, recipient)).thenReturn(caseDetailsForBulkPrint);
        }

        private void stubCoversheetToBulkPrintDocument() {
            when(documentHelper.mapToBulkPrintDocument(coverLetter)).thenReturn(bulkPrintDocument);
        }

        private void stubApprovedConsentOrderNotificationTemplate() {
            when(documentConfiguration.getApprovedConsentOrderNotificationTemplate())
                .thenReturn("approvedConsentOrderNotificationTemplate");
        }
    }

    @Nested
    class StampAndPopulateContestedConsentApprovedOrderCollectionTests {

        @Test
        void shouldStoreStampedAnnexedDocToConsentOrderAndApproveOrder() {
            CaseDocument latestConsentInContestedConsentOrder = caseDocument("latestConsentInContestedConsentOrder");

            finremCaseDetails = FinremCaseDetails.builder()
                .caseType(caseType)
                .data(FinremCaseData.builder()
                    .consentOrder(latestConsentInContestedConsentOrder)
                    .build())
                .build();

            // mocking
            stubStampType(finremCaseDetails.getData());
            when(genericDocumentService.convertDocumentIfNotPdfAlready(latestConsentInContestedConsentOrder, AUTH_TOKEN, caseType))
                .thenReturn(pdfDocument);
            when(genericDocumentService.stampDocument(pdfDocument, AUTH_TOKEN, stampType, caseType))
                .thenReturn(stampedCaseDocument);
            when(genericDocumentService.annexStampDocument(stampedCaseDocument, AUTH_TOKEN, stampType, caseType))
                .thenReturn(stampedAndAnnexedDoc);

            consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(finremCaseDetails, AUTH_TOKEN);

            assertAll(
                () -> assertThat(finremCaseDetails.getData())
                    .extracting(FinremCaseData::getConsentOrder)
                    .isEqualTo(stampedAndAnnexedDoc),
                () -> assertThat(finremCaseDetails.getData().getConsentOrderWrapper()
                    .getContestedConsentedApprovedOrders())
                    .allSatisfy(order -> {
                        assertThat(order.getApprovedOrder()).isNotNull();
                        assertThat(order.getApprovedOrder().getConsentOrder())
                            .isEqualTo(stampedAndAnnexedDoc);
                    })
            );
        }

        @Test
        void shouldStorePensionDocumentToConsentPensionCollectionAndApproveOrder() throws Exception {
            // given
            LocalDate consentDateOfOrder = mock(LocalDate.class);

            CaseDocument pensionDocument1 = caseDocument("pensionDocument1");
            CaseDocument pensionDocument2 = caseDocument("pensionDocument2");

            PensionTypeCollection ptc1 = buildPensionTypeCollection(pensionDocument1);
            PensionTypeCollection ptc2 = buildPensionTypeCollection(pensionDocument2);

            finremCaseDetails = buildCaseDetails(consentDateOfOrder, false, ptc1, ptc2);

            stubStampType(finremCaseDetails.getData());

            // when (set up flows)
            when(genericDocumentService.stampDocument(nullable(CaseDocument.class),
                eq(AUTH_TOKEN), eq(stampType), eq(caseType)))
                .thenReturn(caseDocument("unexpectedDocument"));
            TestFlowResult result1 = setupPensionFlow(ptc1, pensionDocument1, consentDateOfOrder);
            TestFlowResult result2 = setupPensionFlow(ptc2, pensionDocument2, consentDateOfOrder);

            // execute
            consentOrderApprovedDocumentService
                .stampAndPopulateContestedConsentApprovedOrderCollection(finremCaseDetails, AUTH_TOKEN);

            // then
            assertAll(
                () -> assertThat(finremCaseDetails.getData().getConsentPensionCollection())
                    .isEqualTo(List.of(result1.ptcCopy(), result2.ptcCopy())),

                () -> assertThat(finremCaseDetails.getData()
                    .getConsentOrderWrapper()
                    .getContestedConsentedApprovedOrders())
                    .allSatisfy(order -> assertThat(order.getApprovedOrder().getPensionDocuments())
                        .isEqualTo(List.of(result1.ptcCopy(), result2.ptcCopy()))),

                () -> verify(pensionAnnexDateStampService)
                    .appendApprovedDateToDocument(result1.stamped(), AUTH_TOKEN,
                        consentDateOfOrder, caseType),

                () -> verify(pensionAnnexDateStampService)
                    .appendApprovedDateToDocument(result2.stamped(), AUTH_TOKEN,
                        consentDateOfOrder, caseType)
            );
        }

        @Test
        void shouldAppendPensionDocumentToContestedConsentedApprovedOrders() throws Exception {
            // given
            LocalDate consentDateOfOrder = mock(LocalDate.class);

            CaseDocument pensionDocument1 = caseDocument("pensionDocument1");

            PensionTypeCollection ptc1 = buildPensionTypeCollection(pensionDocument1);

            finremCaseDetails = buildCaseDetails(consentDateOfOrder, true, ptc1);

            stubStampType(finremCaseDetails.getData());

            // when (set up flows)
            when(genericDocumentService.stampDocument(nullable(CaseDocument.class),
                eq(AUTH_TOKEN), eq(stampType), eq(caseType)))
                .thenReturn(caseDocument("unexpectedDocument"));
            TestFlowResult result1 = setupPensionFlow(ptc1, pensionDocument1, consentDateOfOrder);

            // execute
            consentOrderApprovedDocumentService
                .stampAndPopulateContestedConsentApprovedOrderCollection(finremCaseDetails, AUTH_TOKEN);

            // then
            assertAll(
                () -> assertThat(finremCaseDetails.getData().getConsentPensionCollection())
                    .isEqualTo(List.of(result1.ptcCopy())),

                () -> assertThat(finremCaseDetails.getData()
                    .getConsentOrderWrapper()
                    .getContestedConsentedApprovedOrders())
                    .hasSize(2),

                () -> verify(pensionAnnexDateStampService)
                    .appendApprovedDateToDocument(result1.stamped(), AUTH_TOKEN,
                        consentDateOfOrder, caseType)
            );
        }

        private TestFlowResult setupPensionFlow(
            PensionTypeCollection ptc,
            CaseDocument pensionDocument,
            LocalDate consentDateOfOrder
        ) throws Exception {

            CaseDocument stamped = mock(CaseDocument.class);

            when(genericDocumentService.stampDocument(
                pensionDocument,
                AUTH_TOKEN,
                stampType,
                caseType
            )).thenReturn(stamped);

            CaseDocument stampedWithDate = caseDocument("stamped-" + pensionDocument.getDocumentFilename());

            when(pensionAnnexDateStampService.appendApprovedDateToDocument(
                stamped,
                AUTH_TOKEN,
                consentDateOfOrder,
                caseType
            )).thenReturn(stampedWithDate);

            PensionTypeCollection ptcCopy =
                PensionTypeCollection.builder()
                    .typedCaseDocument(PensionType.builder().build())
                    .build();

            when(documentHelper.deepCopy(ptc, PensionTypeCollection.class))
                .thenReturn(ptcCopy);

            return new TestFlowResult(ptcCopy, stamped, stampedWithDate);
        }

        private record TestFlowResult(
            PensionTypeCollection ptcCopy,
            CaseDocument stamped,
            CaseDocument stampedWithDate
        ) {}

        private PensionTypeCollection buildPensionTypeCollection(CaseDocument doc) {
            return PensionTypeCollection.builder()
                .typedCaseDocument(PensionType.builder()
                    .pensionDocument(doc)
                    .build())
                .build();
        }

        private FinremCaseDetails buildCaseDetails(
            LocalDate consentDateOfOrder, boolean havingExistingApprovedOrders,
            PensionTypeCollection... ptcs
        ) {
            return FinremCaseDetails.builder()
                .caseType(caseType)
                .data(FinremCaseData.builder()
                    .consentOrderWrapper(ConsentOrderWrapper.builder()
                        .consentDateOfOrder(consentDateOfOrder)
                        .contestedConsentedApprovedOrders(
                            havingExistingApprovedOrders ? new ArrayList<>(List.of(mock(ConsentOrderCollection.class))) : null
                        )
                        .build())
                    .consentPensionCollection(Arrays.stream(ptcs).toList())
                    .build())
                .build();
        }
    }

    @Nested
    class GenerateAndPopulateConsentOrderLetterTests {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldGenerateOrderLetterAndCategorised(boolean approvedOrdersEmpty) {
            finremCaseDetails = FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .consentOrderWrapper(ConsentOrderWrapper.builder()
                        .contestedConsentedApprovedOrders(new ArrayList<>(
                            approvedOrdersEmpty ? List.of() : List.of(ConsentOrderCollection.builder()
                                .approvedOrder(ApprovedOrder.builder().orderLetter(caseDocument("existingOrderLetter")).build())
                                .build())
                        ))
                        .build())
                    .build())
                .build();
            FinremCaseData finremCaseData = finremCaseDetails.getData();

            CaseDocument orderLetter = caseDocument("orderLetter");
            doReturn(orderLetter).when(consentOrderApprovedDocumentService)
                .generateApprovedConsentOrderLetter(finremCaseDetails, AUTH_TOKEN);

            consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(finremCaseDetails, AUTH_TOKEN);

            assertAll(
                () -> verify(approvedConsentOrderCategoriser).categorise(finremCaseData),
                () -> verify(consentOrderApprovedDocumentService).generateApprovedConsentOrderLetter(
                    finremCaseDetails, AUTH_TOKEN),
                () -> {
                    if (!approvedOrdersEmpty) {
                        assertThat(finremCaseData.getConsentOrderWrapper()
                            .getContestedConsentedApprovedOrders())
                            .extracting(ConsentOrderCollection::getApprovedOrder)
                            .extracting(ApprovedOrder::getOrderLetter)
                            .containsOnly(orderLetter);
                    } else {
                        assertThat(finremCaseData.getConsentOrderWrapper()
                            .getContestedConsentedApprovedOrders()).isEmpty();
                    }
                }
            );
        }
    }

    @Nested
    class AddGeneratedApprovedConsentOrderDocumentsToCaseTests {

        @Test
        void givenConsentDateOfOrderNull_shouldGenerateOrderLetterAndAnnexStampDocument() {
            LocalDate orderDirectionDate = mock(LocalDate.class);
            List<PensionTypeCollection> pensionCollection = mock(List.class);
            CaseDocument latestConsentOrder = caseDocument("latestConsentOrder");

            finremCaseDetails = FinremCaseDetails.builder()
                .id(CASE_ID_IN_LONG)
                .caseType(caseType)
                .data(FinremCaseData.builder()
                    .latestConsentOrder(latestConsentOrder)
                    .pensionCollection(pensionCollection)
                    .orderDirectionDate(orderDirectionDate)
                    .build())
                .build();

            stubStampType(finremCaseDetails.getData());

            CaseDocument orderLetter = caseDocument("orderLetter");
            doReturn(orderLetter).when(consentOrderApprovedDocumentService)
                .generateApprovedConsentOrderLetter(finremCaseDetails, AUTH_TOKEN);
            List<PensionTypeCollection> stampedPensionDocs = mock(List.class);
            doReturn(stampedPensionDocs).when(consentOrderApprovedDocumentService)
                .stampPensionDocuments(pensionCollection, AUTH_TOKEN, stampType, orderDirectionDate, caseType);
            when(genericDocumentService.annexStampDocument(latestConsentOrder, AUTH_TOKEN, stampType, caseType))
                .thenReturn(stampedAndAnnexedDoc);

            consentOrderApprovedDocumentService.addGeneratedApprovedConsentOrderDocumentsToCase(AUTH_TOKEN, finremCaseDetails);

            assertAll(
                () -> verify(consentOrderApprovedDocumentService).generateApprovedConsentOrderLetter(
                    finremCaseDetails, AUTH_TOKEN),
                () -> verify(consentOrderApprovedDocumentService).stampPensionDocuments(
                    pensionCollection, AUTH_TOKEN, stampType, orderDirectionDate, caseType),
                () -> verify(genericDocumentService).annexStampDocument(latestConsentOrder, AUTH_TOKEN, stampType, caseType),
                () -> assertThat(finremCaseDetails.getData().getApprovedOrderCollection())
                    .extracting(ConsentOrderCollection::getApprovedOrder)
                    .extracting(ApprovedOrder::getPensionDocuments, ApprovedOrder::getOrderLetter, ApprovedOrder::getConsentOrder)
                    .containsExactly(Tuple.tuple(stampedPensionDocs, orderLetter, stampedAndAnnexedDoc))
            );
        }

        @Test
        void givenConsentDateOfOrderNotNull_shouldGenerateOrderLetterAndAnnexStampDocument() {
            LocalDate orderDirectionDate = mock(LocalDate.class);
            LocalDate consentDateOfOrder = mock(LocalDate.class);
            List<PensionTypeCollection> pensionCollection = mock(List.class);
            CaseDocument latestConsentOrder = caseDocument("latestConsentOrder");

            finremCaseDetails = FinremCaseDetails.builder()
                .id(CASE_ID_IN_LONG)
                .caseType(caseType)
                .data(FinremCaseData.builder()
                    .latestConsentOrder(latestConsentOrder)
                    .pensionCollection(pensionCollection)
                    .orderDirectionDate(orderDirectionDate)
                    .consentOrderWrapper(ConsentOrderWrapper.builder()
                        .consentDateOfOrder(consentDateOfOrder)
                        .build())
                    .build())
                .build();

            stubStampType(finremCaseDetails.getData());

            CaseDocument orderLetter = caseDocument("orderLetter");
            doReturn(orderLetter).when(consentOrderApprovedDocumentService)
                .generateApprovedConsentOrderLetter(finremCaseDetails, AUTH_TOKEN);
            List<PensionTypeCollection> stampedPensionDocs = mock(List.class);
            doReturn(stampedPensionDocs).when(consentOrderApprovedDocumentService)
                .stampPensionDocuments(pensionCollection, AUTH_TOKEN, stampType, consentDateOfOrder, caseType);
            when(genericDocumentService.annexStampDocument(latestConsentOrder, AUTH_TOKEN, stampType, caseType))
                .thenReturn(stampedAndAnnexedDoc);

            consentOrderApprovedDocumentService.addGeneratedApprovedConsentOrderDocumentsToCase(AUTH_TOKEN, finremCaseDetails);

            assertAll(
                () -> verify(consentOrderApprovedDocumentService).generateApprovedConsentOrderLetter(
                    finremCaseDetails, AUTH_TOKEN),
                () -> verify(consentOrderApprovedDocumentService).stampPensionDocuments(
                    pensionCollection, AUTH_TOKEN, stampType, consentDateOfOrder, caseType),
                () -> verify(genericDocumentService).annexStampDocument(latestConsentOrder, AUTH_TOKEN, stampType, caseType),
                () -> assertThat(finremCaseDetails.getData().getApprovedOrderCollection())
                    .extracting(ConsentOrderCollection::getApprovedOrder)
                    .extracting(ApprovedOrder::getPensionDocuments, ApprovedOrder::getOrderLetter, ApprovedOrder::getConsentOrder)
                    .containsExactly(Tuple.tuple(stampedPensionDocs, orderLetter, stampedAndAnnexedDoc))
            );
        }
    }

    @Nested
    class GetApprovedOrderModifiedAfterNotApprovedOrderTests {

        @Test
        void shouldInvokeDocumentOrderServiceToDetermine_whenLastConsentOrdersFound() {
            CaseDocument latestApprovedConsentOrder = caseDocument("latestApprovedConsentOrder");
            CaseDocument latestRefusedConsentOrder = caseDocument("latestRefusedConsentOrder");

            ConsentOrderWrapper consentOrderWrapper = ConsentOrderWrapper.builder()
                .consentedNotApprovedOrders(List.of(
                    toConsentOrderCollection(caseDocument()),
                    toConsentOrderCollection(latestRefusedConsentOrder))
                )
                .contestedConsentedApprovedOrders(List.of(
                    toConsentOrderCollection(latestApprovedConsentOrder))
                )
                .build();

            when(documentOrderingService.isDocumentModifiedLater(latestApprovedConsentOrder,
                latestRefusedConsentOrder, AUTH_TOKEN)).thenReturn(false);

            boolean actual = consentOrderApprovedDocumentService
                .getApprovedOrderModifiedAfterNotApprovedOrder(consentOrderWrapper, AUTH_TOKEN);

            assertAll(
                () -> assertThat(actual).isFalse(),
                () -> verify(documentOrderingService).isDocumentModifiedLater(latestApprovedConsentOrder,
                    latestRefusedConsentOrder, AUTH_TOKEN)
            );
        }

        @ParameterizedTest
        @MethodSource
        void shouldReturnFalse_whenApprovedOrdersCannotBeCompared(List<ConsentOrderCollection> refusedOrders,
                                                                  List<ConsentOrderCollection> approvedOrders) {
            ConsentOrderWrapper consentOrderWrapper = ConsentOrderWrapper.builder()
                .consentedNotApprovedOrders(refusedOrders)
                .contestedConsentedApprovedOrders(approvedOrders)
                .build();

            assertFalse(consentOrderApprovedDocumentService
                .getApprovedOrderModifiedAfterNotApprovedOrder(consentOrderWrapper, AUTH_TOKEN));
        }

        private static Stream<Arguments> shouldReturnFalse_whenApprovedOrdersCannotBeCompared() {
            return Stream.of(
                Arguments.of(mockConsentOrderCollectionList(), null),
                Arguments.of(null, null),
                Arguments.of(Collections.emptyList(), null),
                Arguments.of(null, Collections.emptyList()),
                Arguments.of(Collections.emptyList(), Collections.emptyList())
            );
        }

        private static List<ConsentOrderCollection> mockConsentOrderCollectionList() {
            return List.of(toConsentOrderCollection(caseDocument()));
        }

        private static ConsentOrderCollection toConsentOrderCollection(CaseDocument consentOrder) {
            return ConsentOrderCollection.builder()
                .approvedOrder(ApprovedOrder.builder().consentOrder(consentOrder).build()).build();
        }
    }

    @Nested
    class GetPopulatedConsentCoverSheetTests {

        @Test
        void shouldGenerateBulkPrintCoverSheet() {
            finremCaseDetails = FinremCaseDetails.builder()
                .caseType(CaseType.CONSENTED)
                .data(FinremCaseData.builder()
                    .build())
                .build();
            DocumentHelper.PaperNotificationRecipient recipient = mock(DocumentHelper.PaperNotificationRecipient.class);

            Map<String, Object> letterDetailsAsMap = mock(Map.class);
            when(bulkPrintLetterDetailsMapper
                .getLetterDetailsAsMap(finremCaseDetails, recipient,
                    finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList())
            ).thenReturn(letterDetailsAsMap);

            when(documentConfiguration.getBulkPrintTemplate(finremCaseDetails, recipient))
                .thenReturn("template");

            when(documentConfiguration.getBulkPrintFileName()).thenReturn("filename");

            CaseDocument bulkPrintCoverSheet = caseDocument("bulkPrintCoverSheet");
            when(genericDocumentService.generateDocumentFromPlaceholdersMap(AUTH_TOKEN, letterDetailsAsMap,
                "template", "filename",
                CaseType.CONSENTED)).thenReturn(bulkPrintCoverSheet);

            var result = consentOrderApprovedDocumentService.getPopulatedConsentCoverSheet(finremCaseDetails, AUTH_TOKEN, recipient);

            assertAll(
                () -> assertThat(result).isEqualTo(bulkPrintCoverSheet),
                () -> verify(bulkPrintLetterDetailsMapper)
                    .getLetterDetailsAsMap(eq(finremCaseDetails), eq(recipient), any(DefaultCourtListWrapper.class)),
                () -> verify(genericDocumentService).generateDocumentFromPlaceholdersMap(AUTH_TOKEN, letterDetailsAsMap,
                    "template", "filename", CaseType.CONSENTED),
                () -> verifyNoMoreInteractions(genericDocumentService)
            );
        }
    }

    @Test
    void givenFinremCaseDetails_whenAddApprovedConsentCoverLetter_thenCaseDocsAdded() {
        CaseDocument coverLetter = CaseDocument.builder()
            .documentFilename("approvedConsentOrderNotificationFileName")
            .documentUrl("approvedConsentOrderNotificationUrl")
            .documentBinaryUrl("approvedConsentOrderNotificationBinaryUrl")
            .build();
        CaseDetails mockedCaseDetails = mock(CaseDetails.class);
        when(documentHelper.prepareLetterTemplateData(
            finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(mockedCaseDetails);
        when(documentConfiguration.getApprovedConsentOrderNotificationFileName()).thenReturn("approvedConsentOrderNotificationFileName");
        when(genericDocumentService.generateDocument(eq(AUTH_TOKEN), any(CaseDetails.class), anyString(), anyString())).thenReturn(coverLetter);
        when(documentConfiguration.getApprovedConsentOrderNotificationTemplate()).thenReturn("approvedConsentOrderNotificationTemplate");
        List<CaseDocument> documents = new ArrayList<>();
        consentOrderApprovedDocumentService.addApprovedConsentCoverLetter(
            finremCaseDetails, documents, AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);
        assertThat(documents).containsOnly(coverLetter);
    }

    private void stubStampType(FinremCaseData finremCaseData) {
        when(documentHelper.getStampType(finremCaseData)).thenReturn(stampType);
    }
}
