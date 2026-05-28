package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_NOC_REJECTED;

public class NocUtils {

    private NocUtils() {

    }

    /**
     * Creates a map that clears the {@code CHANGE_ORGANISATION_REQUEST} field.
     * 
     * <p>
     * This is typically used when updating case data to explicitly remove
     * any existing change organisation request by setting the field value to {@code null}.
     *
     * @return a map containing the {@code CHANGE_ORGANISATION_REQUEST} key with a {@code null} value
     */
    public static Map<String, Object> clearChangeOrganisationRequestField() {
        Map<String, Object> map = new HashMap<>();
        map.put(CHANGE_ORGANISATION_REQUEST, null);
        return map;
    }

    /**
     * Determines whether a Notice of Change (NoC) request is accepted based on raw case data.
     *
     * <p>A NoC request is considered accepted if the {@code IS_NOC_REJECTED} flag
     * in the provided case data map is not equal to {@code YES_VALUE}.</p>
     *
     * @param caseData a map containing case data, including the {@code IS_NOC_REJECTED} flag
     * @return {@code true} if the NoC request is accepted (i.e. not explicitly rejected),
     *         {@code false} if it is rejected
     */
    public static boolean isNocRequestAccepted(Map<String, Object> caseData) {
        return !YES_VALUE.equals(caseData.get(IS_NOC_REJECTED));
    }

    /**
     * Determines whether a Notice of Change (NoC) request is accepted based on structured case data.
     *
     * <p>A NoC request is considered accepted if the {@code isNocRejected} field
     * is either {@code No} or {@code null}.</p>
     *
     * @param caseData the {@link FinremCaseData} containing the NoC rejection flag
     * @return {@code true} if the NoC request is accepted (i.e. not rejected or not set),
     *         {@code false} if it is explicitly rejected
     */
    public static boolean isNocRequestAccepted(FinremCaseData caseData) {
        return YesOrNo.isNoOrNull(caseData.getIsNocRejected());
    }
}
