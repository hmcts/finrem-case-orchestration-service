package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftHearingOrderService {

    private final ObjectMapper objectMapper;

    public void appendLatestDraftHearingOrderToHearingOrderCollection(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<CollectionElement<DraftDirectionOrder>> draftDirectionOrders = Optional.ofNullable(caseData.get(DRAFT_DIRECTION_ORDER_COLLECTION))
            .map(this::convertToListOfDraftDirectionOrder)
            .orElse(emptyList());

        List<CollectionElement<DirectionOrder>> directionOrders = Optional.ofNullable(caseData.get(HEARING_ORDER_COLLECTION))
            .map(this::convertToListOfDirectionOrder)
            .orElse(new ArrayList<>());

        if (!draftDirectionOrders.isEmpty()) {
            DraftDirectionOrder latestDraftDirectionOrder = draftDirectionOrders.get(draftDirectionOrders.size() - 1).getValue();
            DirectionOrder newDirectionOrder = DirectionOrder.builder()
                .uploadDraftDocument(latestDraftDirectionOrder.getUploadDraftDocument())
                .build();
            directionOrders.add(CollectionElement.<DirectionOrder>builder().value(newDirectionOrder).build());
            caseData.put(HEARING_ORDER_COLLECTION, directionOrders);
        }
    }

    private List<CollectionElement<DraftDirectionOrder>> convertToListOfDraftDirectionOrder(Object value) {
        return objectMapper.convertValue(value, new TypeReference<>() {});
    }

    private List<CollectionElement<DirectionOrder>> convertToListOfDirectionOrder(Object value) {
        return objectMapper.convertValue(value, new TypeReference<>() {});
    }
}
