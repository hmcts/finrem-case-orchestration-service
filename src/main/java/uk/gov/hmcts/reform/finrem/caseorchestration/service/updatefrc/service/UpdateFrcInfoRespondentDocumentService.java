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

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@Service
@Slf4j
public class UpdateFrcInfoRespondentDocumentService extends BaseUpdateFrcInfoDocumentService {

    @Autowired
    public UpdateFrcInfoRespondentDocumentService(GenericDocumentService genericDocumentService,
                                                  DocumentConfiguration documentConfiguration,
                                                  UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator) {
        super(genericDocumentService, documentConfiguration, updateFrcInfoLetterDetailsGenerator);
    }

    @Override
    public Optional<Document> getUpdateFrcInfoLetter(FinremCaseDetails caseDetails, String authToken) {
        if (shouldPrintForRespondentSolicitor(caseDetails)) {
            return Optional.of(generateSolicitorUpdateFrcInfoLetter(caseDetails, authToken, RESPONDENT));
        } else if (shouldPrintForRespondent(caseDetails)) {
            return Optional.of(generateLitigantUpdateFrcInfoLetter(caseDetails, authToken, RESPONDENT));
        }
        log.info("No frc info letter notification required for RESPONDENT or RESPONDENT SOLICITOR for caseID {}", caseDetails.getId());
        return Optional.empty();
    }

    private boolean shouldPrintForRespondentSolicitor(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isRespondentRepresentedByASolicitor()
            && !caseDetails.getCaseData().isRespondentSolicitorAgreeToReceiveEmails();
    }

    private boolean shouldPrintForRespondent(FinremCaseDetails caseDetails) {
        return !caseDetails.getCaseData().isRespondentRepresentedByASolicitor();
    }
}
