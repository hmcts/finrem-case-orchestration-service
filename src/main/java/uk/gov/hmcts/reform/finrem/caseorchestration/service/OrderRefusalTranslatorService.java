package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_REFUSAL_COLLECTION;

@Service
@RequiredArgsConstructor
public final class OrderRefusalTranslatorService {

    private static final String ORDER_REFUSAL_COLLECTION_NEW = "orderRefusalCollectionNew";
    private static final Map<String, String> REFUSAL_KEYS =
        ImmutableMap.of("Transferred to Applicant’s home Court", "Transferred to Applicant home Court - A",
            "Transferred to Applicant's home Court", "Transferred to Applicant home Court - B"
        );
    private static final Function<Pair<CaseDetails, List<OrderRefusalData>>, CaseDetails> translate =
        OrderRefusalTranslatorService::applyTranslate;
    private final ObjectMapper objectMapper;
    private final Function<CaseDetails, Pair<CaseDetails, List<OrderRefusalData>>> pickLatestOrderRefusal =
        this::applyPickLatest;
    public UnaryOperator<Pair<CaseDetails, String>> translateOrderRefusalCollection =
        this::applyOrderRefusalCollectionTranslation;
    private final Function<CaseDetails, Pair<CaseDetails, List<OrderRefusalData>>> pickOrderRefusalCollection =
        this::applyPickOrderRefusalCollection;

    private static CaseDetails applyTranslate(Pair<CaseDetails, List<OrderRefusalData>> pair) {
        CaseDetails caseDetails = pair.getLeft();
        Map<String, Object> caseData = caseDetails.getData();
        List<OrderRefusalData> orderRefusalCollection = pair.getRight();
        caseData.put(ORDER_REFUSAL_COLLECTION_NEW, orderRefusalCollection);

        orderRefusalCollection.forEach(orderRefusalData -> {
            List<String> orderRefusal = orderRefusalData.getOrderRefusal().getOrderRefusal();
            orderRefusalData.getOrderRefusal().setOrderRefusal(
                orderRefusal.stream()
                    .map(s -> REFUSAL_KEYS.getOrDefault(s, s))
                    .collect(toList()));
        });

        return caseDetails;
    }

    private static List<OrderRefusalData> append(Pair<CaseDetails, List<OrderRefusalData>> orderRefusalCollection,
                                                 Pair<CaseDetails, List<OrderRefusalData>> orderRefusalNew) {
        if (!orderRefusalCollection.getRight().isEmpty()) {
            List<OrderRefusalData> orderRefusalDataList = new ArrayList<>();
            orderRefusalDataList.addAll(orderRefusalCollection.getValue());
            orderRefusalDataList.addAll(orderRefusalNew.getValue());
            return orderRefusalDataList;
        }
        return orderRefusalNew.getRight();
    }

    private Pair<CaseDetails, String> applyOrderRefusalCollectionTranslation(Pair<CaseDetails, String> pair) {
        return ImmutablePair.of(translateOrderRefusalCollection(pair.getLeft()), pair.getRight());
    }

    private Pair<CaseDetails, List<OrderRefusalData>> applyPickLatest(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<OrderRefusalData> orderRefusalCollectionNew = ofNullable(caseData.get(ORDER_REFUSAL_COLLECTION_NEW))
            .map(this::convertToRefusalOrderList)
            .orElse(Collections.emptyList());

        return Pair.of(caseDetails, orderRefusalCollectionNew);
    }

    private Pair<CaseDetails, List<OrderRefusalData>> applyPickOrderRefusalCollection(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<OrderRefusalData> orderRefusalCollection = ofNullable(caseData.get(ORDER_REFUSAL_COLLECTION))
            .map(this::convertToRefusalOrderList)
            .orElse(Collections.emptyList());

        return ImmutablePair.of(caseDetails, orderRefusalCollection);
    }

    private List<OrderRefusalData> convertToRefusalOrderList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public CaseDetails translateOrderRefusalCollection(CaseDetails caseDetails) {
        return pickLatestOrderRefusal.andThen(translate).apply(caseDetails);
    }

    public Map<String, Object> copyToOrderRefusalCollection(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        if (nonNull(data.get(ORDER_REFUSAL_COLLECTION_NEW))) {
            Pair<CaseDetails, List<OrderRefusalData>> orderRefusalNew = pickLatestOrderRefusal.apply(caseDetails);
            Pair<CaseDetails, List<OrderRefusalData>> orderRefusalCollection = pickOrderRefusalCollection
                .apply(caseDetails);
            data.put(ORDER_REFUSAL_COLLECTION, append(orderRefusalCollection, orderRefusalNew));
            data.put(ORDER_REFUSAL_COLLECTION_NEW, null);
        }
        return data;
    }
}
