package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator;

import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@Service
@Slf4j
public class UpdateFrcInfoApplicantDocumentService extends  BaseUpdateFrcInfoDocumentService {

    @Autowired
    public UpdateFrcInfoApplicantDocumentService(GenericDocumentService genericDocumentService,
                                                 DocumentConfiguration documentConfiguration,
                                                 CaseDataService caseDataService,
                                                 UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator) {
        super(genericDocumentService, documentConfiguration, caseDataService, updateFrcInfoLetterDetailsGenerator);
    }

    @Override
    public Optional<CaseDocument> getUpdateFrcInfoLetter(CaseDetails caseDetails, String authToken) {
        if (shouldPrintForApplicantSolicitor(caseDetails)) {
            return Optional.of(generateSolicitorUpdateFrcInfoLetter(caseDetails, authToken, APPLICANT));
        } else if (shouldPrintForApplicant(caseDetails)) {
            return Optional.of(generateLitigantUpdateFrcInfoLetter(caseDetails, authToken, APPLICANT));
        }
        return Optional.empty();
    }

    private boolean shouldPrintForApplicantSolicitor(CaseDetails caseDetails) {
        return caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())
            && !caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails);
    }

    private boolean shouldPrintForApplicant(CaseDetails caseDetails) {
        return !caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData());
    }
}
