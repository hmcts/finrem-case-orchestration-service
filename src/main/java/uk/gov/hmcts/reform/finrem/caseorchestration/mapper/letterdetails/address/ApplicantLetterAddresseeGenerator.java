package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
@AllArgsConstructor
@Slf4j
public class ApplicantLetterAddresseeGenerator implements LetterAddresseeGenerator {

    private final CaseDataService caseDataService;

    public AddresseeDetails generate(CaseDetails caseDetails) {
        AddresseeDetails addresseeDetails;
        Map<String, Object> caseData = caseDetails.getData();
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        if (caseDataService.isApplicantRepresentedByASolicitor(caseData)) {
            log.info("Applicant is represented by a solicitor on case {}", caseDetails.getId());
            addresseeDetails = AddresseeDetails.builder().reference(nullToEmpty((caseData.get(SOLICITOR_REFERENCE))))
                .addresseeName(nullToEmpty((caseData.get(isConsentedApplication ? CONSENTED_SOLICITOR_NAME : CONTESTED_SOLICITOR_NAME))))
                .addressToSendTo((Map) caseData.get(isConsentedApplication ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS)).build();
        } else {
            log.info("Applicant is not represented by a solicitor on case {}", caseDetails.getId());
            addresseeDetails =
                AddresseeDetails.builder().addresseeName(caseDataService.buildFullName(caseData, APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME))
                    .addressToSendTo((Map) caseData.get(APPLICANT_ADDRESS)).build();
        }
        return addresseeDetails;
    }

    public AddresseeDetails generate(FinremCaseDetails caseDetails) {
        AddresseeDetails addresseeDetails;
        FinremCaseData caseData = caseDetails.getData();
        if (caseData.isApplicantRepresentedByASolicitor()) {
            log.info("Applicant is represented by a solicitor on case {}", caseDetails.getId());
            addresseeDetails = AddresseeDetails.builder().reference(nullToEmpty((caseData.getContactDetailsWrapper().getSolicitorReference())))
                .addresseeName(nullToEmpty((caseData.getAppSolicitorName())))
                .finremAddressToSendTo(caseData.getAppSolicitorAddress()).build();
        } else {
            log.info("Applicant is not represented by a solicitor on case {}", caseDetails.getId());
            addresseeDetails = AddresseeDetails.builder().addresseeName(caseData.getFullApplicantName())
                .finremAddressToSendTo(caseData.getContactDetailsWrapper().getApplicantAddress()).build();
        }
        return addresseeDetails;
    }
}
