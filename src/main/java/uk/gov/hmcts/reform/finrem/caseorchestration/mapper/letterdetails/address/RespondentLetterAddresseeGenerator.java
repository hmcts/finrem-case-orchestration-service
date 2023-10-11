package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
@AllArgsConstructor
@Slf4j
public class RespondentLetterAddresseeGenerator implements LetterAddresseeGenerator {

    private final CaseDataService caseDataService;

    public AddresseeDetails generate(CaseDetails caseDetails) {
        AddresseeDetails addresseeDetails;
        Map<String, Object> caseData = caseDetails.getData();
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        if (caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())) {
            log.info("Respondent is represented by a solicitor");
            addresseeDetails = AddresseeDetails.builder().reference(nullToEmpty((caseData.get(RESP_SOLICITOR_REFERENCE))))
                .addresseeName(nullToEmpty((caseData.get(RESP_SOLICITOR_NAME))))
                .addressToSendTo((Map) caseData.get(RESP_SOLICITOR_ADDRESS)).build();
        } else {
            log.info("Respondent is not represented by a solicitor sett addressee to respondent name");
            addresseeDetails = AddresseeDetails.builder().addresseeName(caseDataService.buildFullName(caseData,
                    isConsentedApplication ? CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME : CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME,
                    isConsentedApplication ? CONSENTED_RESPONDENT_LAST_NAME : CONTESTED_RESPONDENT_LAST_NAME))
                .addressToSendTo((Map) caseData.get(RESPONDENT_ADDRESS)).build();
        }
        return addresseeDetails;
    }

    @Override
    public AddresseeDetails generate(FinremCaseDetails caseDetails) {
        AddresseeDetails addresseeDetails;
        if (caseDetails.getData().isRespondentRepresentedByASolicitor()) {
            log.info("Respondent is represented by a solicitor for case{}", caseDetails.getId());
            addresseeDetails = AddresseeDetails.builder()
                .reference(nullToEmpty((caseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorReference())))
                .addresseeName(nullToEmpty(caseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorName()))
                .finremAddressToSendTo(caseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorAddress())
                .build();
        } else {
            log.info("Respondent is not represented by a solicitor for case{}", caseDetails.getId());
            addresseeDetails = AddresseeDetails.builder()
                .addresseeName(caseDetails.getData().getRespondentFullName())
                .finremAddressToSendTo(caseDetails.getData().getContactDetailsWrapper().getRespondentAddress())
                .build();
        }
        return addresseeDetails;
    }
}
