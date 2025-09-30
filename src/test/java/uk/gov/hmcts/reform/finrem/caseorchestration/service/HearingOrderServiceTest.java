package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;

@ExtendWith(MockitoExtension.class)
class HearingOrderServiceTest {

    private static final String DRAFT_DIRECTION_ORDER_BIN_URL = "anyDraftDirectionOrderBinaryUrl";

    private static final String FILENAME_ENDING_WITH_DOCX = "filename_ending_with.docx";

    private HearingOrderService underTest;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private OrderDateService orderDateService;

    @Mock
    private DocumentHelper documentHelper;

    @Mock
    private UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;

    @BeforeEach
    void setUp() {
        underTest = new HearingOrderService(genericDocumentService, documentHelper, orderDateService,
            uploadedDraftOrderCategoriser);
    }

    @Nested
    class StampAndStoreJudgeApprovedOrders {
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 10, 1, 0, 0);
        CaseDocument uao1Docx = caseDocument("UAO1.docx");
        CaseDocument uao1Pdf = caseDocument("UAO1.pdf");
        CaseDocument stampedUao1Pdf = caseDocument("StampedUAO1.pdf");

        // Mocks
        List<DirectionOrderCollection> originalFinalOrderCollection = mock(ArrayList.class);
        StampType mockedStampType = mock(StampType.class);

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenSingleApprovedOrder_whenJudgeUploads_thenStoredInExpectedProperties(boolean doesExistInFinalOrderCollection) {
            // Arrange
            FinremCaseData finremCaseData = setupFinremCaseData(DraftDirectionWrapper.builder()
                .judgeApprovedOrderCollection(List.of(
                    createJudgeApprovedOrder(uao1Docx)
                ))
                .build());

            when(documentHelper.getStampType(finremCaseData)).thenReturn(mockedStampType);
            when(genericDocumentService.convertDocumentIfNotPdfAlready(uao1Docx, AUTH_TOKEN, CASE_ID)).thenReturn(uao1Pdf);
            when(genericDocumentService.stampDocument(uao1Pdf, AUTH_TOKEN, mockedStampType, CASE_ID)).thenReturn(stampedUao1Pdf);
            List<DirectionOrderCollection> createdDateSyncedFinalOrderCollection = new ArrayList<>(List.of(
                createStampedDirectionOrderCollection(
                    caseDocument("existingFinalOrderOne.pdf"),
                    LocalDateTime.of(2025, 6, 4, 11, 59))
            ));
            when(orderDateService.syncCreatedDateAndMarkDocumentStamped(originalFinalOrderCollection, AUTH_TOKEN))
                .thenReturn(createdDateSyncedFinalOrderCollection);
            when(documentHelper.checkIfOrderAlreadyInFinalOrderCollection(createdDateSyncedFinalOrderCollection, stampedUao1Pdf))
                .thenReturn(doesExistInFinalOrderCollection);

            try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
                // Act
                underTest.stampAndStoreJudgeApprovedOrders(finremCaseData, AUTH_TOKEN);

                InOrder inOrder = Mockito.inOrder(genericDocumentService, orderDateService, documentHelper);
                inOrder.verify(genericDocumentService).convertDocumentIfNotPdfAlready(uao1Docx, AUTH_TOKEN, CASE_ID);
                inOrder.verify(genericDocumentService).stampDocument(uao1Pdf, AUTH_TOKEN, mockedStampType, CASE_ID);
                inOrder.verify(orderDateService).syncCreatedDateAndMarkDocumentStamped(originalFinalOrderCollection, AUTH_TOKEN);
                inOrder.verify(documentHelper).checkIfOrderAlreadyInFinalOrderCollection(createdDateSyncedFinalOrderCollection, stampedUao1Pdf);

                assertLatestDraftHearingOrder(finremCaseData, stampedUao1Pdf);
                if (doesExistInFinalOrderCollection) {
                    assertFinalOrderCollection(finremCaseData,
                        createdDateSyncedFinalOrderCollection.getFirst());
                } else {
                    assertFinalOrderCollection(finremCaseData,
                        createdDateSyncedFinalOrderCollection.getFirst(),
                        createStampedDirectionOrderCollection(stampedUao1Pdf, fixedDateTime));
                }
                assertUploadHearingOrder(finremCaseData, createUploadHearingEntry(stampedUao1Pdf));
            }
        }

        private void assertLatestDraftHearingOrder(FinremCaseData finremCaseData, CaseDocument expectedOrder) {
            assertThat(finremCaseData.getLatestDraftHearingOrder()).isEqualTo(expectedOrder);
        }

        private void assertUploadHearingOrder(FinremCaseData finremCaseData, DirectionOrderCollection... expectedOrder) {
            assertThat(finremCaseData.getUploadHearingOrder()).containsExactly(expectedOrder);
        }

        private void assertFinalOrderCollection(FinremCaseData finremCaseData, DirectionOrderCollection... expectedOrder) {
            assertThat(finremCaseData.getFinalOrderCollection()).containsExactly(expectedOrder);
        }

        private FinremCaseData setupFinremCaseData(DraftDirectionWrapper draftDirectionWrapper) {
            return FinremCaseData.builder()
                .ccdCaseId(CASE_ID)
                .finalOrderCollection(originalFinalOrderCollection)
                .draftDirectionWrapper(draftDirectionWrapper)
                .build();
        }

        private DirectionOrderCollection createUploadHearingEntry(CaseDocument uploadDraftDocument) {
            return createUploadHearingEntry(uploadDraftDocument, null);
        }

        private DirectionOrderCollection createUploadHearingEntry(CaseDocument uploadDraftDocument,
                                                                  List<DocumentCollectionItem> additionalDocs) {
            return DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .uploadDraftDocument(uploadDraftDocument)
                    .additionalDocuments(additionalDocs)
                    .build())
                .build();
        }

        private DirectionOrderCollection createStampedDirectionOrderCollection(CaseDocument uploadDraftDocument,
                                                                               LocalDateTime orderDateTime) {
            return createStampedDirectionOrderCollection(uploadDraftDocument, orderDateTime, null);
        }

        private DirectionOrderCollection createStampedDirectionOrderCollection(CaseDocument uploadDraftDocument,
                                                                               LocalDateTime orderDateTime,
                                                                               List<DocumentCollectionItem> additionalDocs) {
            return DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .isOrderStamped(YesOrNo.YES)
                    .orderDateTime(orderDateTime)
                    .uploadDraftDocument(uploadDraftDocument)
                    .additionalDocuments(additionalDocs)
                    .build())
                .build();
        }

        private DraftDirectionOrderCollection createJudgeApprovedOrder(CaseDocument uploadDraftDocument) {
            return DraftDirectionOrderCollection.builder()
                .value(DraftDirectionOrder.builder()
                    .uploadDraftDocument(uploadDraftDocument)
                    .build())
                .build();
        }
    }

    @Test
    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrdersV2() {
        FinremCallbackRequest callbackRequest = getContestedNewCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();
        DraftDirectionOrder other
            = DraftDirectionOrder.builder().uploadDraftDocument(caseDocument()).purposeOfDocument("Other").build();
        finremCaseData.getDraftDirectionWrapper().setLatestDraftDirectionOrder(other);

        underTest.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);

        List<DraftDirectionOrderCollection> judgesAmendedOrderCollection
            = finremCaseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection();
        DraftDirectionOrder directionOrder = judgesAmendedOrderCollection.getFirst().getValue();
        assertEquals(caseDocument(), directionOrder.getUploadDraftDocument());
        assertEquals("Other", directionOrder.getPurposeOfDocument());
    }

    @Test
    void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrdersV2WhenNoDraft() {
        FinremCallbackRequest callbackRequest = getContestedNewCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();

        underTest.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);

        List<DraftDirectionOrderCollection> judgesAmendedOrderCollection
            = finremCaseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection();
        assertNull(judgesAmendedOrderCollection);
    }

    @Test
    void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders() {
        DraftDirectionOrder latestDraftDirectionOrder = makeDraftDirectionOrder();

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(
            FinremCaseData.builder().draftDirectionWrapper(DraftDirectionWrapper.builder()
                .latestDraftDirectionOrder(latestDraftDirectionOrder)
                .build()).build()
        ).build();

        underTest.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(finremCaseDetails);

        assertThat(finremCaseDetails.getData().getDraftDirectionWrapper())
            .extracting(DraftDirectionWrapper::getJudgesAmendedOrderCollection)
            .extracting(List::getFirst)
            .extracting(DraftDirectionOrderCollection::getValue).isEqualTo(latestDraftDirectionOrder);
    }

    private DraftDirectionOrder makeDraftDirectionOrder() {
        return DraftDirectionOrder.builder().uploadDraftDocument(CaseDocument.builder()
            .documentBinaryUrl(DRAFT_DIRECTION_ORDER_BIN_URL)
            .documentFilename(FILENAME_ENDING_WITH_DOCX)
            .build()).build();
    }

    protected FinremCallbackRequest getContestedNewCallbackRequest() {
        FinremCaseData caseData = getFinremCaseData();
        caseData.getContactDetailsWrapper().setRespondentFmName("David");
        caseData.getContactDetailsWrapper().setRespondentLname("Goodman");
        caseData.setCcdCaseType(CaseType.CONTESTED);
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONTESTED)
                .id(Long.valueOf(CASE_ID))
                .data(caseData)
                .build())
            .build();
    }

    private FinremCaseData getFinremCaseData() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setApplicantFmName("Victoria");
        caseData.getContactDetailsWrapper().setApplicantLname("Goodman");
        caseData.getContactDetailsWrapper().setApplicantSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setApplicantSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        return caseData;
    }
}
