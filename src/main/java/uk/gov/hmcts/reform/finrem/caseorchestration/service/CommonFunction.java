package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.Field.LINE_1;
import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.Field.POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_AGREE_TO_RECEIVE_EMAILS;

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
                && StringUtils.isNotBlank((String) address.get(LINE_1))
                && StringUtils.isNotBlank((String) address.get(POSTCODE));
    }

    public static String buildFullName(Map<String, Object> caseData, String applicantFirstMiddleName, String applicantLastName) {
        return (
                nullToEmpty((caseData.get(applicantFirstMiddleName))).trim()
                + " "
                + nullToEmpty((caseData.get(applicantLastName))).trim()
        ).trim();
    }

    public static boolean isApplicantRepresentedByASolicitor(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APPLICANT_REPRESENTED)));
    }

    public static boolean isApplicantSolicitorAgreeToReceiveEmails(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(SOLICITOR_AGREE_TO_RECEIVE_EMAILS)));
    }

    public static boolean isRespondentRepresentedByASolicitor(Map<String, Object> caseData) {
        return isNotEmpty(RESP_SOLICITOR_NAME, caseData);
    }

    public static boolean isPaperApplication(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(Objects.toString(caseData.get(PAPER_APPLICATION)));
    }

    public static boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }

    public static boolean isAmendedConsentOrderType(RespondToOrderData respondToOrderData) {
        return AMENDED_CONSENT_ORDER.equalsIgnoreCase(respondToOrderData.getRespondToOrder().getDocumentType());
    }
}
