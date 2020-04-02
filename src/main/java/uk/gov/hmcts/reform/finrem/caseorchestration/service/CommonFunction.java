package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonFunction {

    public static String nullToEmpty(Object o) {
        return o == null ? StringUtils.EMPTY : o.toString();
    }

    static final Function<List<Map>, Map>
        getLastMapValue = (listMap) ->
        listMap.stream().reduce((first, second) -> second).get();

    static final Function<List<Map>, Map>
        getFirstMapValue = (listMap) ->
        listMap.stream().findFirst().get();

    public static boolean addressLineOneAndPostCodeAreBothNotEmpty(Map address) {
        return  ObjectUtils.isNotEmpty(address)
                && StringUtils.isNotBlank((String) address.get("AddressLine1"))
                && StringUtils.isNotBlank((String) address.get("PostCode"));
    }

    public static boolean isApplicantRepresented(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APPLICANT_REPRESENTED)));
    }

    public static String buildFullName(Map<String, Object> caseData, String applicantFirstMiddleName, String applicantLastName) {
        return (
                nullToEmpty((caseData.get(applicantFirstMiddleName))).trim()
                + " "
                + nullToEmpty((caseData.get(applicantLastName))).trim()
        ).trim();
    }
}
