package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.google.common.collect.ImmutableList.of;
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

        List<OrderRefusalData> orderRefusalCollection = Optional.ofNullable(caseData.get("orderRefusalCollectionNew"))
                .map(OrderRefusalTranslator::convertToRefusalOrderList)
                .orElse(Collections.emptyList());

        return Pair.of(caseDetails, orderRefusalCollection);
    }

    private static Pair<CaseDetails, List<OrderRefusalData>> applyPickOrderRefusalCollection(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<OrderRefusalData> orderRefusalCollection = Optional.ofNullable(caseData.get("orderRefusalCollection"))
                .map(OrderRefusalTranslator::convertToRefusalOrderList)
                .orElse(Collections.emptyList());

        return ImmutablePair.of(caseDetails, refusalOrderList(orderRefusalCollection));
    }

    private static List<OrderRefusalData> convertToRefusalOrderList(Object object) {
        return MAPPER.convertValue(object, new TypeReference<List<OrderRefusalData>>() {
        });
    }

    private static List<OrderRefusalData> refusalOrderList(List<OrderRefusalData> list) {
        return list.isEmpty() ? list : constructOrderRefusalList(list);
    }

    private static ImmutableList<OrderRefusalData> constructOrderRefusalList(
            List<OrderRefusalData> orderRefusalCollection) {
        return of(orderRefusalCollection.get(orderRefusalCollection.size() - 1));
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
        if (Objects.nonNull(data.get("orderRefusalCollectionNew"))) {
            Pair<CaseDetails, List<OrderRefusalData>> pair1 = pickLatestOrderRefusal.apply(caseDetails);
            Pair<CaseDetails, List<OrderRefusalData>> pair2 = pickOrderRefusalCollection.apply(caseDetails);
            append(pair1, pair2);
            data.put("orderRefusalCollection", pair1.getRight());
            data.put("orderRefusalCollectionNew", null);
        }
        return data;
    }

    private static void append(Pair<CaseDetails, List<OrderRefusalData>> pair1,
                               Pair<CaseDetails, List<OrderRefusalData>> pair2) {
        List<OrderRefusalData> right = pair2.getRight();
        AtomicInteger id = new AtomicInteger(pair1.getRight().size());
        List<OrderRefusalData> transformedPair2 = right.stream()
                .map(orderRefusalData -> transform(orderRefusalData, id.incrementAndGet()))
                .collect(toList());
        pair1.getRight().addAll(right);
    }

    private static OrderRefusalData transform(OrderRefusalData orderRefusalData, int id) {
        orderRefusalData.setId(String.valueOf(id));
        return orderRefusalData;
    }
}
