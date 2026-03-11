package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;

public class NocUtils {

    private NocUtils() {

    }

    /**
     * Creates a map that clears the {@code CHANGE_ORGANISATION_REQUEST} field.
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
}
