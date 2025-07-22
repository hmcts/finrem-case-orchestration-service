package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_DIRECTION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

public class HearingOrderServiceTest extends BaseServiceTest {

    private static final String DRAFT_DIRECTION_ORDER_BIN_URL = "anyDraftDirectionOrderBinaryUrl";
    private static final String FILENAME_ENDING_WITH_DOCX = "filename_ending_with.docx";

    @Autowired
    HearingOrderService hearingOrderService;

    @MockitoBean
    private GenericDocumentService genericDocumentService;
    @MockitoBean
    private OrderDateService orderDateService;

    @Test
    public void convertPdfDocument() {
        when(genericDocumentService.stampDocument(any(), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), any()))
            .thenReturn(caseDocument());
        when(orderDateService.syncCreatedDateAndMarkDocumentStamped(any(), any())).thenReturn(new ArrayList<>());
        DocumentHelper documentHelper = mock(DocumentHelper.class);
        when(documentHelper.checkIfOrderAlreadyInFinalOrderCollection(anyList(), any())).thenReturn(false);

        Map<String, Object> caseData = prepareCaseData(makeDraftDirectionOrderCollectionWithOneElement());
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(any(), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), any());

        CaseDocument latestDraftHearingOrder = (CaseDocument) caseData.get(LATEST_DRAFT_HEARING_ORDER);
        assertThat(latestDraftHearingOrder, is(notNullValue()));
        assertThat(latestDraftHearingOrder.getDocumentFilename(), is(FILE_NAME));
        assertThat(latestDraftHearingOrder.getDocumentBinaryUrl(), is(BINARY_URL));

        List<CollectionElement<DirectionOrder>> hearingOrderCollection = (List<CollectionElement<DirectionOrder>>) caseDetails.getData()
            .get(HEARING_ORDER_COLLECTION);
        assertThat(hearingOrderCollection, hasSize(1));
        assertThat((List<DirectionOrderCollection>) caseDetails.getData().get(FINAL_ORDER_COLLECTION), hasSize(1));
        assertThat(hearingOrderCollection.get(0).getValue().getUploadDraftDocument().getDocumentBinaryUrl(), is(BINARY_URL));
    }

    @Test(expected = InvalidCaseDataException.class)
    public void throwsExceptionIfNoDocumentFound() {
        CaseDetails emptyCaseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(emptyCaseDetails, AUTH_TOKEN);
    }

    @Test
    public void whenLatestDraftDirOrderIsSameAsLastDraftOrder_itDoesntOverrideIt() {
        List<CollectionElement<DraftDirectionOrder>> draftDirectionOrders = makeDraftDirectionOrderCollectionWithOneElement();
        Map<String, Object> caseData = prepareCaseData(draftDirectionOrders);
        caseData.put(LATEST_DRAFT_DIRECTION_ORDER, DraftDirectionOrder.builder()
            .uploadDraftDocument(draftDirectionOrders.get(0).getValue().getUploadDraftDocument())
            .purposeOfDocument(draftDirectionOrders.get(0).getValue().getPurposeOfDocument())
            .build());

        assertThat(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(
            CaseDetails.builder().id(123L)
                .data(caseData).build(), AUTH_TOKEN), is(true));
    }

    @Test
    public void whenLatestDraftDirOrderIsDifferentThanLastDraftOrder_itDoesOverrideIt() {
        Map<String, Object> caseData = prepareCaseData(makeDraftDirectionOrderCollectionWithOneElement());
        caseData.put(LATEST_DRAFT_DIRECTION_ORDER, DraftDirectionOrder.builder()
            .uploadDraftDocument(caseDocument())
            .purposeOfDocument("some other purpose")
            .build());

        assertThat(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(
            CaseDetails.builder().id(123L)
                .data(caseData).build(), AUTH_TOKEN), is(true));
    }

    @Test
    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrdersV2() {
        FinremCallbackRequest callbackRequest = getContestedNewCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();
        DraftDirectionOrder other
            = DraftDirectionOrder.builder().uploadDraftDocument(caseDocument()).purposeOfDocument("Other").build();
        finremCaseData.getDraftDirectionWrapper().setLatestDraftDirectionOrder(other);


        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);

        List<DraftDirectionOrderCollection> judgesAmendedOrderCollection
            = finremCaseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection();
        DraftDirectionOrder directionOrder = judgesAmendedOrderCollection.get(0).getValue();
        Assert.assertEquals(caseDocument(), directionOrder.getUploadDraftDocument());
        Assert.assertEquals("Other", directionOrder.getPurposeOfDocument());
    }

    @Test
    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrdersV2WhenNoDraft() {
        FinremCallbackRequest callbackRequest = getContestedNewCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();

        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);

        List<DraftDirectionOrderCollection> judgesAmendedOrderCollection
            = finremCaseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection();
        Assert.assertNull(judgesAmendedOrderCollection);
    }

    @Test
    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders() {
        Map<String, Object> caseData = new HashMap<>();
        DraftDirectionOrder latestDraftDirectionOrder = makeDraftDirectionOrder();
        caseData.put(LATEST_DRAFT_DIRECTION_ORDER, latestDraftDirectionOrder);

        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(CaseDetails.builder().data(caseData).build());

        assertThat(((List<CollectionElement>) caseData.get(JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION)).get(0).getValue(),
            is(latestDraftDirectionOrder));
    }

    private List<CollectionElement<DraftDirectionOrder>> makeDraftDirectionOrderCollectionWithOneElement() {
        List<CollectionElement<DraftDirectionOrder>> draftDirectionOrderCollection = new ArrayList<>();

        draftDirectionOrderCollection.add(CollectionElement.<DraftDirectionOrder>builder()
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

    private Map<String, Object> prepareCaseData(List<CollectionElement<DraftDirectionOrder>> draftDirectionOrders) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DRAFT_DIRECTION_ORDER_COLLECTION, draftDirectionOrders);
        caseData.put(HEARING_ORDER_COLLECTION, null);
        return convertCaseDataToStringRepresentation(caseData);
    }
}
