package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitorAddresseeGenerator implements AddresseeGenerator {

    private final DocumentHelper documentHelper;

    public Addressee generate(FinremCaseDetails caseDetails,
                              ChangedRepresentative changedRepresentative,
                              String party) {
        log.info("In the generate addressee method for Solicitor");
        return AddresseeGeneratorHelper.generateAddressee(caseDetails, getRecipient(party));
    }

    private DocumentHelper.PaperNotificationRecipient getRecipient(String party) {
        return party.equalsIgnoreCase(APPLICANT)
            ? DocumentHelper.PaperNotificationRecipient.APPLICANT
            : DocumentHelper.PaperNotificationRecipient.RESPONDENT;
    }
}
