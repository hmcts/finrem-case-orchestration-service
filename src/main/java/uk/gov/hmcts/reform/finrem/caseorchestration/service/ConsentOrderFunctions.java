package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toList;

final class ConsentOrderFunctions {
    private static Map<String, String> REFUSAL_KEYS =
            ImmutableMap.of("Transferred to Applicant’s home Court", "Transferred to Applicant home Court - A",
                    "Transferred to Applicant's home Court", "Transferred to Applicant home Court - B"
            );

    static UnaryOperator<Pair<CaseDetails, String>> translateOrderRefusalCollection =
            ConsentOrderFunctions::applyOrderRefusalCollectionTranslation;

    static Pair<CaseDetails, String> applyOrderRefusalCollectionTranslation(
            Pair<CaseDetails, String> pair) {
        return ImmutablePair.of(translateOrderRefusalCollection(pair.getLeft()), pair.getRight());
    }

    static CaseDetails translateOrderRefusalCollection(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<OrderRefusalData> orderRefusalCollection = caseData.getOrderRefusalCollection();

        orderRefusalCollection.forEach(orderRefusalData -> {
            List<String> orderRefusal = orderRefusalData.getOrderRefusal().getOrderRefusal();
            orderRefusalData.getOrderRefusal().setOrderRefusal(
                    orderRefusal.stream()
                            .map(s -> REFUSAL_KEYS.containsKey(s)  ? REFUSAL_KEYS.get(s) : s)
                            .collect(toList()));
        });

        return caseDetails;
    }
}
