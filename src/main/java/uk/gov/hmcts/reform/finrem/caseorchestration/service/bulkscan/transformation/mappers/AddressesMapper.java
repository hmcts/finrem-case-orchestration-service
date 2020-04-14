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
    public static void applyAddressesMappings(List<OcrDataField> ocrDataFields, Map<String, Object> transformedCaseData) {
        String applicantAddress = isApplicantRepresented(transformedCaseData) ? "applicantSolicitor" : "applicant";
        applyMappings(applicantAddress, APPLICANT_ADDRESS, ocrDataFields, transformedCaseData);

        String respondentAddress = isRespondentRepresented(transformedCaseData) ? "respondentSolicitor" : "respondent";
        applyMappings(respondentAddress, RESPONDENT_ADDRESS, ocrDataFields, transformedCaseData);
    }
}
