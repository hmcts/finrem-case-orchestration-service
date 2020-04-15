package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers;

import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.applyMappings;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.CommonConditions.isApplicantRepresented;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.CommonConditions.isRespondentRepresented;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;

/**
 * Mapping fields related to addresses (applicant, respondent and applicant solicitor, respondent solicitor).
 *
 * <p>We need it because we want to map to fields CCDConfigConstant.APPLICANT_ADDRESS
 * and CCDConfigConstant.RESPONDENT_ADDRESS different set of values. it may be either address details
 * of citizen (if not represented) or their solicitor (if represented).
 */
public class AddressesMapper {

    /**
     * There should be only 2 fields with address:
     * 1. `applicantAddress` where all data about applicant or their solicitor is stored
     * 2. `respondentAddress` where all data about respondent or their solicitor is stored
     *
     * <p>We don't need to stored data about applicant's/respondent's address when they are represented.
     * */
    public static void setupAddressesForApplicantAndRespondent(Map<String, Object> transformedCaseData) {
        addressesForApplicant(transformedCaseData);
        addressesForRespondent(transformedCaseData);
    }

    /**
     * It doesn't return anything, but updates `transformedCaseData`. It adds new keys with maps where all data related
     * top addresses are stored.
     * */
    public static void applyAddressesMappings(List<OcrDataField> ocrDataFields, Map<String, Object> transformedCaseData) {
        applyMappings("applicant", APPLICANT_ADDRESS, ocrDataFields, transformedCaseData);
        applyMappings("applicantSolicitor", "applicantSolicitorAddress", ocrDataFields, transformedCaseData);
        applyMappings("respondent", "respondentAddress", ocrDataFields, transformedCaseData);
        applyMappings("respondentSolicitor", "rSolicitorAddress", ocrDataFields, transformedCaseData);
    }

    private static void addressesForApplicant(Map<String, Object> transformedCaseData) {
        String applicantKey = isApplicantRepresented(transformedCaseData) ? "applicantSolicitorAddress" : "applicantAddress";

        transformedCaseData.put(APPLICANT_ADDRESS, transformedCaseData.get(applicantKey));
        transformedCaseData.remove("applicantSolicitorAddress");
    }

    private static void addressesForRespondent(Map<String, Object> transformedCaseData) {
        String applicantKey = isRespondentRepresented(transformedCaseData) ? "rSolicitorAddress" : "respondentAddress";

        transformedCaseData.put(RESPONDENT_ADDRESS, transformedCaseData.get(applicantKey));
        transformedCaseData.remove("rSolicitorAddress");
    }
}
