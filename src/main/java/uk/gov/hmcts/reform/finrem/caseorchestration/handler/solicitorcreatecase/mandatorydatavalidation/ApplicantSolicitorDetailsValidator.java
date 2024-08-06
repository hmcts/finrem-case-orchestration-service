package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.NullChecker;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
class ApplicantSolicitorDetailsValidator implements MandatoryDataValidator {
    @Override
    public List<String> validate(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();

        List<String> ret = new ArrayList<>();

        if (YesOrNo.NO.equals(contactDetailsWrapper.getApplicantRepresented())) {
            log.info("{} - Skip validating solicitor details since the applicant is not represented", caseData.getCcdCaseId());
            return ret;
        }
        if (contactDetailsWrapper.getSolicitorAddress() == null
            || !NullChecker.anyNonNull(contactDetailsWrapper.getSolicitorAddress())) {
            ret.add("Applicant solicitor's address is required.");
        }
        if (!StringUtils.hasText(contactDetailsWrapper.getSolicitorEmail())) {
            ret.add("Applicant solicitor's email is required.");
        }
        if (!StringUtils.hasText(contactDetailsWrapper.getSolicitorPhone())) {
            ret.add("Applicant solicitor's phone is required.");
        }
        if (!StringUtils.hasText(contactDetailsWrapper.getSolicitorFirm())) {
            ret.add("Applicant solicitor's name of your firm is required.");
        }
        if (!StringUtils.hasText(contactDetailsWrapper.getSolicitorName())) {
            ret.add("Applicant solicitor's name is required.");
        }
        return ret;
    }
}
