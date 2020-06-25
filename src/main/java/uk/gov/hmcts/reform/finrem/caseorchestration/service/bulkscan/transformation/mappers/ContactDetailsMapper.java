package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.applyMappings;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresentedByASolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isRespondentRepresentedByASolicitor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactDetailsMapper {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CcdFields {
        public static final String APPLICANT = CCDConfigConstant.APPLICANT_ADDRESS;
        public static final String APPLICANT_SOLICITOR = CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
        public static final String APPLICANT_PHONE = CCDConfigConstant.APPLICANT_PHONE;
        public static final String APPLICANT_EMAIL = CCDConfigConstant.APPLICANT_EMAIL;
        public static final String APPLICANT_SOLICITOR_EMAIL = CCDConfigConstant.SOLICITOR_EMAIL;
        public static final String APPLICANT_SOLICITOR_PHONE = CCDConfigConstant.SOLICITOR_PHONE;

        public static final String RESPONDENT = CCDConfigConstant.RESPONDENT_ADDRESS;
        public static final String RESPONDENT_SOLICITOR = CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
    }

    /**
     * We only store contact details to citizens or theirs solicitors (if they are represented). Never both.
     */
    public static void setupContactDetailsForApplicantAndRespondent(Map<String, Object> transformedCaseData) {
        setupContactDetailsForApplicant(transformedCaseData);
        setupAddressForRespondent(transformedCaseData);
    }

    /**
     * It doesn't return anything, but updates `transformedCaseData`. It adds new keys with maps where all data related
     * top addresses are stored.
     */
    public static void applyAddressesMappings(List<OcrDataField> ocrDataFields, Map<String, Object> transformedCaseData) {
        applyMappings("applicant", CcdFields.APPLICANT, ocrDataFields, transformedCaseData);
        applyMappings("respondent", CcdFields.RESPONDENT, ocrDataFields, transformedCaseData);
    }

    private static void setupContactDetailsForApplicant(Map<String, Object> transformedCaseData) {
        if (isApplicantRepresentedByASolicitor(transformedCaseData)) {
            transformedCaseData.put(CcdFields.APPLICANT_SOLICITOR, transformedCaseData.get(CcdFields.APPLICANT));
            transformedCaseData.remove(CcdFields.APPLICANT);

            transformedCaseData.put(CcdFields.APPLICANT_SOLICITOR_PHONE, transformedCaseData.get(CcdFields.APPLICANT_PHONE));
            transformedCaseData.put(CcdFields.APPLICANT_SOLICITOR_EMAIL, transformedCaseData.get(CcdFields.APPLICANT_EMAIL));

            transformedCaseData.remove(CcdFields.APPLICANT_PHONE);
            transformedCaseData.remove(CcdFields.APPLICANT_EMAIL);
        }
    }

    private static void setupAddressForRespondent(Map<String, Object> transformedCaseData) {
        if (isRespondentRepresentedByASolicitor(transformedCaseData)) {
            transformedCaseData.put(CcdFields.RESPONDENT_SOLICITOR, transformedCaseData.get(CcdFields.RESPONDENT));
            transformedCaseData.remove(CcdFields.RESPONDENT);
        }
    }
}
