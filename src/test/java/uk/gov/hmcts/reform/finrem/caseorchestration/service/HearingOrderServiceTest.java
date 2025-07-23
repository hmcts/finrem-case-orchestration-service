package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType.FAMILY_COURT_STAMP;

@ExtendWith(MockitoExtension.class)
class HearingOrderServiceTest {

    private static final String DRAFT_DIRECTION_ORDER_BIN_URL = "anyDraftDirectionOrderBinaryUrl";

    private static final String FILENAME_ENDING_WITH_DOCX = "filename_ending_with.docx";

    HearingOrderService underTest;

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
        underTest = new HearingOrderService(genericDocumentService, documentHelper, new ObjectMapper(), orderDateService,
            uploadedDraftOrderCategoriser);
    }

    @Test
    void shouldConvertLastJudgeApprovedOrderToPdfAndStampAndStoreLatestDraftHearingOrder() {
        // Arrange
        when(genericDocumentService.stampDocument(any(), eq(AUTH_TOKEN), eq(FAMILY_COURT_STAMP), any()))
            .thenReturn(caseDocument());
        when(orderDateService.syncCreatedDateAndMarkDocumentStamped(any(), any())).thenReturn(new ArrayList<>());
        when(documentHelper.checkIfOrderAlreadyInFinalOrderCollection(anyList(), any())).thenReturn(false);
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);

        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder().ccdCaseId("123");
        builder.draftDirectionWrapper(DraftDirectionWrapper.builder()
            .judgeApprovedOrderCollection(makeDraftDirectionOrderCollectionWithOneElement())
            .build());
        builder.uploadHearingOrder(null);
        FinremCaseData finremCaseData = builder.build();

        // Act
        underTest.convertLastJudgeApprovedOrderToPdfAndStampAndStoreLatestDraftHearingOrder(finremCaseData, AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(any(), eq(AUTH_TOKEN), eq(FAMILY_COURT_STAMP), any());

        CaseDocument latestDraftHearingOrder = finremCaseData.getLatestDraftHearingOrder();
        assertThat(latestDraftHearingOrder)
            .isNotNull()
            .extracting(CaseDocument::getDocumentFilename, CaseDocument::getDocumentBinaryUrl)
            .containsOnly(FILE_NAME, BINARY_URL);

        List<DirectionOrderCollection> hearingOrderCollection = finremCaseData.getUploadHearingOrder();
        assertThat(finremCaseData.getFinalOrderCollection()).hasSize(1);
        assertThat(hearingOrderCollection).hasSize(1)
            .extracting(DirectionOrderCollection::getValue)
            .extracting(DirectionOrder::getUploadDraftDocument)
            .extracting(CaseDocument::getDocumentBinaryUrl)
            .containsOnly(BINARY_URL);
    }

    @Test
    void shouldThrowsExceptionIfNoJudgeApprovedOrderUploaded() {
        FinremCaseData finremCaseData = FinremCaseData.builder().ccdCaseId("123").build();

        assertThrows(InvalidCaseDataException.class, () ->
            underTest.convertLastJudgeApprovedOrderToPdfAndStampAndStoreLatestDraftHearingOrder(finremCaseData, AUTH_TOKEN)
        );
    }

    @Test
    void whenLatestDraftDirOrderIsSameAsLastDraftOrder_itDoesntOverrideIt() {
        List<DraftDirectionOrderCollection> draftDirectionOrders = makeDraftDirectionOrderCollectionWithOneElement();
        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder().ccdCaseId("123");
        builder.draftDirectionWrapper(DraftDirectionWrapper.builder()
            .latestDraftDirectionOrder(DraftDirectionOrder.builder()
                .uploadDraftDocument(draftDirectionOrders.getFirst().getValue().getUploadDraftDocument())
                .purposeOfDocument(draftDirectionOrders.getFirst().getValue().getPurposeOfDocument())
                .build())
            .judgeApprovedOrderCollection(draftDirectionOrders)
            .build());
        builder.uploadHearingOrder(null);

        assertThat(underTest.latestDraftDirectionOrderOverridesSolicitorCollection(builder.build(), AUTH_TOKEN)).isTrue();
    }

    @Test
    void whenLatestDraftDirOrderIsDifferentThanLastDraftOrder_itDoesOverrideIt() {
        List<DraftDirectionOrderCollection> draftDirectionOrders = makeDraftDirectionOrderCollectionWithOneElement();
        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder().ccdCaseId("123");
        builder.draftDirectionWrapper(DraftDirectionWrapper.builder()
            .latestDraftDirectionOrder(DraftDirectionOrder.builder()
                .uploadDraftDocument(caseDocument())
                .purposeOfDocument("other purpose")
                .build())
            .judgeApprovedOrderCollection(draftDirectionOrders)
            .build());
        builder.uploadHearingOrder(null);

        assertThat(underTest.latestDraftDirectionOrderOverridesSolicitorCollection(builder.build(), AUTH_TOKEN)).isTrue();
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
//
//    @Test
//    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrdersV2WhenNoDraft() {
//        FinremCallbackRequest callbackRequest = getContestedNewCallbackRequest();
//        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
//        FinremCaseData finremCaseData = caseDetails.getData();
//
//        underTest.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
//
//        List<DraftDirectionOrderCollection> judgesAmendedOrderCollection
//            = finremCaseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection();
//        Assert.assertNull(judgesAmendedOrderCollection);
//    }
//
//    @Test
//    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders() {
//        Map<String, Object> caseData = new HashMap<>();
//        DraftDirectionOrder latestDraftDirectionOrder = makeDraftDirectionOrder();
//        caseData.put(LATEST_DRAFT_DIRECTION_ORDER, latestDraftDirectionOrder);
//
//        underTest.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(CaseDetails.builder().data(caseData).build());
//
//        assertThat(((List<CollectionElement>) caseData.get(JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION)).get(0).getValue(),
//            is(latestDraftDirectionOrder));
//    }

    private List<DraftDirectionOrderCollection> makeDraftDirectionOrderCollectionWithOneElement() {
        List<DraftDirectionOrderCollection> draftDirectionOrderCollection = new ArrayList<>();

        draftDirectionOrderCollection.add(DraftDirectionOrderCollection.builder()
            .value(makeDraftDirectionOrder())
            .build());
        return draftDirectionOrderCollection;
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
                .id(12345L)
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
