package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address.AddresseeGeneratorService;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;

@Component
public class SolicitorAddedLetterDetailsGenerator extends AbstractLetterDetailsGenerator {

    @Autowired
    public SolicitorAddedLetterDetailsGenerator(
        AddresseeGeneratorService addresseeGeneratorService,
        CourtDetailsMapper courtDetailsMapper) {
        super(addresseeGeneratorService, courtDetailsMapper);
    }

    @Override
    ChangedRepresentative getChangeOfRepresentative(RepresentationUpdate representationUpdate) {
        return representationUpdate.getAdded();
    }

    @Override
    String getNoticeOfChangeText() {
        return "Your notice of change has been completed successfully. You can now view your client's case.";
    }

    @Override
    String getSolicitorReference(FinremCaseDetails caseDetails,
                                 FinremCaseDetails caseDetailsBefore,
                                 RepresentationUpdate representationUpdate) {
        return getSolicitorReference(caseDetails, representationUpdate);
    }

    @Override
    String getSolicitorFirmName(RepresentationUpdate representationUpdate, FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return getSolicitorFirmName(caseDetails, representationUpdate);
    }

    @Override
    FinremCaseDetails getCaseDetailsToUse(FinremCaseDetails caseDetails,
                                    FinremCaseDetails caseDetailsBefore,
                                    DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.SOLICITOR
            ? caseDetails
            : caseDetailsBefore;
    }
}
