package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.address;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.SOLICITOR;

@Component
@RequiredArgsConstructor
@Slf4j
public class AddresseeBuilderService {

    private final ApplicantAddresseeGenerator applicantAddresseeGenerator;
    private final RespondentAddresseeGenerator respondentAddresseeGenerator;
    private final SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    public Addressee buildAddressee(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("In the buildAddressee method for case {} and recipient type {}", caseDetails.getId(), recipient);
        return this.addresseeGeneratorMap().get(recipient).generate(caseDetails);
    }

    private Map<DocumentHelper.PaperNotificationRecipient, AddresseeGenerator> addresseeGeneratorMap() {
        ImmutableMap<DocumentHelper.PaperNotificationRecipient, AddresseeGenerator> map =
            ImmutableMap.of(DocumentHelper.PaperNotificationRecipient.APPLICANT, applicantAddresseeGenerator,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT, respondentAddresseeGenerator,
                SOLICITOR, solicitorAddresseeGenerator);
        return map;
    }
}
