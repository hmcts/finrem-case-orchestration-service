package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.DocumentPurpose;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class HearingOrderServiceTest extends BaseServiceTest {

    private static final String DRAFT_DIRECTION_ORDER_BIN_URL = "anyDraftDirectionOrderBinaryUrl";
    private static final String FILENAME_ENDING_WITH_DOCX = "filename_ending_with.docx";

    @Autowired HearingOrderService hearingOrderService;

    @MockBean private GenericDocumentService genericDocumentService;

    @Test
    public void convertPdfDocument() {
        when(genericDocumentService.convertDocumentIfNotPdfAlready(isA(Document.class), eq(AUTH_TOKEN))).thenReturn(newDocument());
        when(genericDocumentService.stampDocument(isA(Document.class), eq(AUTH_TOKEN))).thenReturn(newDocument());

        FinremCaseData caseData = prepareCaseData(makeDraftDirectionOrderCollectionWithOneElement());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseData(caseData).build();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService).convertDocumentIfNotPdfAlready(isA(Document.class), eq(AUTH_TOKEN));
        verify(genericDocumentService).stampDocument(isA(Document.class), eq(AUTH_TOKEN));

        Document latestDraftHearingOrder = caseData.getLatestDraftHearingOrder();
        assertThat(latestDraftHearingOrder, is(notNullValue()));
        assertThat(latestDraftHearingOrder.getFilename(), is(FILE_NAME));
        assertThat(latestDraftHearingOrder.getBinaryUrl(), is(BINARY_URL));

        List<DirectionOrderCollection> hearingOrderCollection = caseData.getUploadHearingOrder();

        assertThat(hearingOrderCollection, hasSize(1));
        assertThat(hearingOrderCollection.get(0).getValue().getUploadDraftDocument().getBinaryUrl(), is(BINARY_URL));
    }

    @Test(expected = InvalidCaseDataException.class)
    public void throwsExceptionIfNoDocumentFound() {
        FinremCaseDetails emptyCaseDetails = FinremCaseDetails.builder().caseData(new FinremCaseData()).build();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(emptyCaseDetails, AUTH_TOKEN);
    }

    @Test
    public void whenLatestDraftDirOrderIsSameAsLastDraftOrder_itDoesntOverrideIt() {
        List<DraftDirectionOrderCollection> draftDirectionOrders = makeDraftDirectionOrderCollectionWithOneElement();
        FinremCaseData caseData = prepareCaseData(draftDirectionOrders);
        caseData.getDraftDirectionWrapper().setLatestDraftDirectionOrder(DraftDirectionOrder.builder()
                .uploadDraftDocument(draftDirectionOrders.get(0).getValue().getUploadDraftDocument())
                .purposeOfDocument(draftDirectionOrders.get(0).getValue().getPurposeOfDocument())
            .build());

        assertThat(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(FinremCaseDetails.builder()
            .caseData(caseData).build()), is(false));
    }

    @Test
    public void whenLatestDraftDirOrderIsDifferentThanLastDraftOrder_itDoesOverrideIt() {
        FinremCaseData caseData = prepareCaseData(makeDraftDirectionOrderCollectionWithOneElement());
        caseData.getDraftDirectionWrapper().setLatestDraftDirectionOrder(DraftDirectionOrder.builder()
                .uploadDraftDocument(newDocument())
                .purposeOfDocument(DocumentPurpose.DRAFT_ORDER)
            .build());

        assertThat(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(FinremCaseDetails.builder()
                .caseData(caseData).build()), is(true));
    }

    @Test
    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders() {
        FinremCaseData caseData = new FinremCaseData();
        DraftDirectionOrder latestDraftDirectionOrder = makeDraftDirectionOrder();
        caseData.getDraftDirectionWrapper().setLatestDraftDirectionOrder(latestDraftDirectionOrder);

        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(FinremCaseDetails.builder()
            .caseData(caseData).build());

        assertThat(caseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection().get(0).getValue(),
            is(latestDraftDirectionOrder));
    }

    private List<DraftDirectionOrderCollection> makeDraftDirectionOrderCollectionWithOneElement() {
        return List.of(
            DraftDirectionOrderCollection.builder()
                .value(makeDraftDirectionOrder())
                .build());
    }

    private DraftDirectionOrder makeDraftDirectionOrder() {
        return DraftDirectionOrder.builder().uploadDraftDocument(draftDirectionDocument()).build();
    }

    private Document draftDirectionDocument() {
        return Document.builder()
            .binaryUrl(DRAFT_DIRECTION_ORDER_BIN_URL)
            .filename(FILENAME_ENDING_WITH_DOCX)
            .build();
    }

    private FinremCaseData prepareCaseData(List<DraftDirectionOrderCollection> draftDirectionOrders) {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getDraftDirectionWrapper().setDraftDirectionOrderCollection(draftDirectionOrders);
        caseData.setUploadHearingOrder(null);
        return caseData;
    }
}
