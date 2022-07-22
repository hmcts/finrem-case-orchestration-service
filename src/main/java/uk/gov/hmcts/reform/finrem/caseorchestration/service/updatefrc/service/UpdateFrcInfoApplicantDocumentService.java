package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@Service
@Slf4j
public class UpdateFrcInfoApplicantDocumentService extends  BaseUpdateFrcInfoDocumentService {

    @Autowired
    public UpdateFrcInfoApplicantDocumentService(GenericDocumentService genericDocumentService,
                                                 DocumentConfiguration documentConfiguration,
                                                 UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator) {
        super(genericDocumentService, documentConfiguration, updateFrcInfoLetterDetailsGenerator);
    }

    @Override
    public Optional<Document> getUpdateFrcInfoLetter(FinremCaseDetails caseDetails, String authToken) {
        if (shouldPrintForApplicantSolicitor(caseDetails)) {
            return Optional.of(generateSolicitorUpdateFrcInfoLetter(caseDetails, authToken, APPLICANT));
        } else if (shouldPrintForApplicant(caseDetails)) {
            return Optional.of(generateLitigantUpdateFrcInfoLetter(caseDetails, authToken, APPLICANT));
        }
        log.info("No frc info letter notification required for APPLICANT or APPLICANT SOLICITOR for caseID {}", caseDetails.getId());
        return Optional.empty();
    }

    private boolean shouldPrintForApplicantSolicitor(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isApplicantRepresentedByASolicitor()
            && !caseDetails.getCaseData().isApplicantSolicitorAgreeToReceiveEmails();
    }

    private boolean shouldPrintForApplicant(FinremCaseDetails caseDetails) {
        return !caseDetails.getCaseData().isApplicantRepresentedByASolicitor();
    }
}
