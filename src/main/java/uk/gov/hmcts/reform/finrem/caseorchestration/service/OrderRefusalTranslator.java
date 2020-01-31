package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

public final class OrderRefusalTranslator {
    private static Map<String, String> REFUSAL_KEYS =
            ImmutableMap.of("Transferred to Applicant’s home Court", "Transferred to Applicant home Court - A",
                    "Transferred to Applicant's home Court", "Transferred to Applicant home Court - B"
            );
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static Function<CaseDetails, Pair<CaseDetails, List<OrderRefusalData>>> pickLatestOrderRefusal =
            OrderRefusalTranslator::applyPickLatest;

    private static Function<CaseDetails, Pair<CaseDetails, List<OrderRefusalData>>> pickOrderRefusalCollection =
            OrderRefusalTranslator::applyPickOrderRefusalCollection;

    private static Function<Pair<CaseDetails, List<OrderRefusalData>>, CaseDetails> translate =
            OrderRefusalTranslator::applyTranslate;

    static UnaryOperator<Pair<CaseDetails, String>> translateOrderRefusalCollection =
            OrderRefusalTranslator::applyOrderRefusalCollectionTranslation;

    private static Pair<CaseDetails, String> applyOrderRefusalCollectionTranslation(Pair<CaseDetails, String> pair) {
        return ImmutablePair.of(translateOrderRefusalCollection(pair.getLeft()), pair.getRight());
    }

    private static Pair<CaseDetails, List<OrderRefusalData>> applyPickLatest(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<OrderRefusalData> orderRefusalCollectionNew = ofNullable(caseData.get("orderRefusalCollectionNew"))
                .map(OrderRefusalTranslator::convertToRefusalOrderList)
                .orElse(Collections.emptyList());

        return Pair.of(caseDetails, orderRefusalCollectionNew);
    }

    private static Pair<CaseDetails, List<OrderRefusalData>> applyPickOrderRefusalCollection(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<OrderRefusalData> orderRefusalCollection = ofNullable(caseData.get("orderRefusalCollection"))
                .map(OrderRefusalTranslator::convertToRefusalOrderList)
                .orElse(Collections.emptyList());

        return ImmutablePair.of(caseDetails, orderRefusalCollection);
    }

    private static List<OrderRefusalData> convertToRefusalOrderList(Object object) {
        return MAPPER.convertValue(object, new TypeReference<List<OrderRefusalData>>() {
        });
    }

    private static CaseDetails applyTranslate(Pair<CaseDetails, List<OrderRefusalData>> pair) {
        CaseDetails caseDetails = pair.getLeft();
        Map<String, Object> caseData = caseDetails.getData();
        List<OrderRefusalData> orderRefusalCollection = pair.getRight();
        caseData.put("orderRefusalCollectionNew", orderRefusalCollection);

        orderRefusalCollection.forEach(orderRefusalData -> {
            List<String> orderRefusal = orderRefusalData.getOrderRefusal().getOrderRefusal();
            orderRefusalData.getOrderRefusal().setOrderRefusal(
                    orderRefusal.stream()
                            .map(s -> REFUSAL_KEYS.getOrDefault(s, s))
                            .collect(toList()));
        });

        return caseDetails;
    }

    public static CaseDetails translateOrderRefusalCollection(CaseDetails caseDetails) {
        return pickLatestOrderRefusal.andThen(translate).apply(caseDetails);
    }

    public static Map<String, Object> copyToOrderRefusalCollection(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        if (nonNull(data.get("orderRefusalCollectionNew"))) {
            Pair<CaseDetails, List<OrderRefusalData>> orderRefusalNew = pickLatestOrderRefusal.apply(caseDetails);
            Pair<CaseDetails, List<OrderRefusalData>> orderRefusalCollection = pickOrderRefusalCollection
                    .apply(caseDetails);
            data.put("orderRefusalCollection", append(orderRefusalCollection, orderRefusalNew));
            data.put("orderRefusalCollectionNew", null);
        }
        return data;
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
}
