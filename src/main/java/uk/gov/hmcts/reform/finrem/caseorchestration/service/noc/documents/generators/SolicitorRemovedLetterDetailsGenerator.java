package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address.AddresseeGeneratorService;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;

@Component
public class SolicitorRemovedLetterDetailsGenerator extends AbstractLetterDetailsGenerator {

    @Autowired
    public SolicitorRemovedLetterDetailsGenerator(
        AddresseeGeneratorService addresseeGeneratorService,
        CourtDetailsMapper courtDetailsMapper) {
        super(addresseeGeneratorService, courtDetailsMapper);
    }

    @Override
    ChangedRepresentative getChangeOfRepresentative(RepresentationUpdate representationUpdate) {
        return representationUpdate.getRemoved();
    }

    @Override
    String getNoticeOfChangeText() {
        return "You've completed notice of acting on this, your access to this case has now been revoked.";
    }

    @Override
    String getSolicitorReference(FinremCaseDetails caseDetails,
                                 FinremCaseDetails caseDetailsBefore,
                                 RepresentationUpdate representationUpdate) {
        return getSolicitorReference(caseDetailsBefore, representationUpdate);
    }

    @Override
    String getSolicitorFirmName(RepresentationUpdate representationUpdate,
                                FinremCaseDetails caseDetails,
                                FinremCaseDetails caseDetailsBefore) {
        return getSolicitorFirmName(caseDetailsBefore, representationUpdate);

    }

    @Override
    FinremCaseDetails getCaseDetailsToUse(FinremCaseDetails caseDetails,
                                          FinremCaseDetails caseDetailsBefore,
                                          DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.SOLICITOR
            ? caseDetailsBefore
            : caseDetails;

    }
}
