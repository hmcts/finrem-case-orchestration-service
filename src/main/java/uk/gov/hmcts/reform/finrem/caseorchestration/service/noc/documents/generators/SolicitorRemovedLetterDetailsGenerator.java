package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address.AddresseeGeneratorService;

@Component
public class SolicitorRemovedLetterDetailsGenerator extends AbstractLetterDetailsGenerator {

    @Autowired
    public SolicitorRemovedLetterDetailsGenerator(
        AddresseeGeneratorService addresseeGeneratorService,
        DocumentHelper documentHelper, CaseDataService caseDataService) {
        super(addresseeGeneratorService, documentHelper, caseDataService);
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
    String getSolicitorReference(CaseDetails caseDetails, CaseDetails caseDetailsBefore, RepresentationUpdate representationUpdate) {
        return getSolicitorReference(caseDetailsBefore, representationUpdate);
    }

    @Override
    String getSolicitorFirmName(RepresentationUpdate representationUpdate, CaseDetails caseDetails,
                                CaseDetails caseDetailsBefore) {
        return getSolicitorFirmName(caseDetailsBefore, representationUpdate);

    }

    @Override
    CaseDetails getCaseDetailsToUse(CaseDetails caseDetails, CaseDetails caseDetailsBefore, DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.SOLICITOR
            ? caseDetailsBefore
            : caseDetails;

    }
}
