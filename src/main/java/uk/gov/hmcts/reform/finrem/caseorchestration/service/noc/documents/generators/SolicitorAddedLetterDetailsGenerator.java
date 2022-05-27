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
public class SolicitorAddedLetterDetailsGenerator extends AbstractLetterDetailsGenerator {

    @Autowired
    public SolicitorAddedLetterDetailsGenerator(
        AddresseeGeneratorService addresseeGeneratorService,
        DocumentHelper documentHelper, CaseDataService caseDataService) {
        super(addresseeGeneratorService, documentHelper, caseDataService);
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
    String getSolicitorReference(CaseDetails caseDetails, CaseDetails caseDetailsBefore, RepresentationUpdate representationUpdate) {
        return getSolicitorReference(caseDetails, representationUpdate);
    }

    @Override
    String getSolicitorFirmName(RepresentationUpdate representationUpdate, CaseDetails caseDetails,
                                CaseDetails caseDetailsBefore) {
        return getSolicitorFirmName(caseDetails, representationUpdate);

    }

    @Override
    CaseDetails getCaseDetailsToUse(CaseDetails caseDetails, CaseDetails caseDetailsBefore, DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.SOLICITOR
            ? caseDetails
            : caseDetailsBefore;
    }
}
