package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;

public class DraftHearingOrderServiceTest extends BaseServiceTest {

    public static final String DRAFT_DIRECTION_ORDER_BIN_URL = "anyDraftDirectionOrderBinaryUrl";

    @Autowired DraftHearingOrderService draftHearingOrderService;

    @Test
    public void givenNoDraftDirectionOrders_whenAppendingToHearingOrders_nothingIsAppended() {
        Map<String, Object> caseData = prepareCaseData(emptyList(), emptyList());
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        draftHearingOrderService.appendLatestDraftHearingOrderToHearingOrderCollection(caseDetails);

        List<CollectionElement<DirectionOrder>> hearingOrderCollection = (List<CollectionElement<DirectionOrder>>) caseDetails.getData()
            .get(HEARING_ORDER_COLLECTION);
        assertThat(hearingOrderCollection, is(empty()));
    }

    @Test
    public void givenDraftDirectionOrders_whenAppendingToHearingOrders_latestOrderIsAppended() {
        List<CollectionElement<DraftDirectionOrder>> draftDirectionOrderCollection = new ArrayList<>();
        draftDirectionOrderCollection.add(CollectionElement.<DraftDirectionOrder>builder()
            .value(DraftDirectionOrder.builder().uploadDraftDocument(CaseDocument.builder()
                .documentBinaryUrl(DRAFT_DIRECTION_ORDER_BIN_URL).build()).build()).build());

        Map<String, Object> caseData = prepareCaseData(draftDirectionOrderCollection, null);
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        draftHearingOrderService.appendLatestDraftHearingOrderToHearingOrderCollection(caseDetails);

        List<CollectionElement<DirectionOrder>> hearingOrderCollection = (List<CollectionElement<DirectionOrder>>) caseDetails.getData()
            .get(HEARING_ORDER_COLLECTION);
        assertThat(hearingOrderCollection, hasSize(1));
        assertThat(hearingOrderCollection.get(0).getValue().getUploadDraftDocument().getDocumentBinaryUrl(), is(DRAFT_DIRECTION_ORDER_BIN_URL));
    }

    private Map<String, Object> prepareCaseData(List<CollectionElement<DraftDirectionOrder>> draftDirectionOrders,
                                                List<CollectionElement<DirectionOrder>> directionOrders) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DRAFT_DIRECTION_ORDER_COLLECTION, draftDirectionOrders);
        caseData.put(HEARING_ORDER_COLLECTION, directionOrders);
        return convertCaseDataToStringRepresentation(caseData);
    }
}
