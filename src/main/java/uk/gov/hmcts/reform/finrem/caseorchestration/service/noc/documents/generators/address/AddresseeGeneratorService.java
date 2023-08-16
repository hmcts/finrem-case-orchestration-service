package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.SOLICITOR;

@Component
@RequiredArgsConstructor
@Slf4j
public class AddresseeGeneratorService {

    private final ApplicantAddresseeGenerator applicantAddresseeGenerator;
    private final RespondentAddresseeGenerator respondentAddresseeGenerator;
    private final SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    public Addressee generateAddressee(CaseDetails caseDetails, ChangedRepresentative changedRepresentative,
                                       DocumentHelper.PaperNotificationRecipient recipient, String party) {
        log.info("In the buildAddressee method for case {} and recipient type {}", caseDetails.getId(), recipient);
        return this.addresseeGeneratorMap().get(recipient).generate(caseDetails, changedRepresentative, party);
    }

    private Map<DocumentHelper.PaperNotificationRecipient, AddresseeGenerator> addresseeGeneratorMap() {
        return Map.of(DocumentHelper.PaperNotificationRecipient.APPLICANT, applicantAddresseeGenerator,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT, respondentAddresseeGenerator,
                SOLICITOR, solicitorAddresseeGenerator);
    }
}
