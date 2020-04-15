package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers;

import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.applyMappings;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.CommonConditions.isApplicantRepresented;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.CommonConditions.isRespondentRepresented;

/**
 * Mapping fields related to addresses (applicant, respondent and applicant solicitor, respondent solicitor).
 *
 * <p>We need it because we want to map to fields CCDConfigConstant.APPLICANT_ADDRESS
 * and CCDConfigConstant.RESPONDENT_ADDRESS different set of values. it may be either address details
 * of citizen (if not represented) or their solicitor (if represented).
 */
public class AddressesMapper {

    public static class CcdFields {
        public static final String APPLICANT = "applicantAddress";
        public static final String RESPONDENT = "respondentAddress";
    }

    public static class TempCcdFields {
        public static final String APPLICANT_SOLICITOR = "applicantSolicitorAddress";
        public static final String RESPONDENT_SOLICITOR = "rSolicitorAddress";
    }

    /**
     * There should be only 2 fields with address:
     * 1. `applicantAddress` where all data about applicant or their solicitor is stored
     * 2. `respondentAddress` where all data about respondent or their solicitor is stored
     *
     * <p>We don't need to stored data about applicant's/respondent's address when they are represented.
     */
    public static void setupAddressesForApplicantAndRespondent(Map<String, Object> transformedCaseData) {
        addressesForApplicant(transformedCaseData);
        addressesForRespondent(transformedCaseData);
    }

    /**
     * It doesn't return anything, but updates `transformedCaseData`. It adds new keys with maps where all data related
     * top addresses are stored.
     */
    public static void applyAddressesMappings(List<OcrDataField> ocrDataFields, Map<String, Object> transformedCaseData) {
        applyMappings("applicant", CcdFields.APPLICANT, ocrDataFields, transformedCaseData);
        applyMappings("applicantSolicitor", TempCcdFields.APPLICANT_SOLICITOR, ocrDataFields, transformedCaseData);
        applyMappings("respondent", CcdFields.RESPONDENT, ocrDataFields, transformedCaseData);
        applyMappings("respondentSolicitor", TempCcdFields.RESPONDENT_SOLICITOR, ocrDataFields, transformedCaseData);
    }

    private static void addressesForApplicant(Map<String, Object> transformedCaseData) {
        String applicantKey = isApplicantRepresented(transformedCaseData) ? TempCcdFields.APPLICANT_SOLICITOR : CcdFields.APPLICANT;

        transformedCaseData.put(CcdFields.APPLICANT, transformedCaseData.get(applicantKey));
        transformedCaseData.remove(TempCcdFields.APPLICANT_SOLICITOR);
    }

    private static void addressesForRespondent(Map<String, Object> transformedCaseData) {
        String applicantKey = isRespondentRepresented(transformedCaseData) ? TempCcdFields.RESPONDENT_SOLICITOR : CcdFields.RESPONDENT;

        transformedCaseData.put(CcdFields.RESPONDENT, transformedCaseData.get(applicantKey));
        transformedCaseData.remove(TempCcdFields.RESPONDENT_SOLICITOR);
    }
}
