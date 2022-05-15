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

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@Service
@Slf4j
public class UpdateFrcInfoRespondentDocumentService extends BaseUpdateFrcInfoDocumentService {

    @Autowired
    public UpdateFrcInfoRespondentDocumentService(GenericDocumentService genericDocumentService,
                                                  DocumentConfiguration documentConfiguration,
                                                  CaseDataService caseDataService,
                                                  UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator) {
        super(genericDocumentService, documentConfiguration, caseDataService, updateFrcInfoLetterDetailsGenerator);
    }

    @Override
    public Optional<CaseDocument> getUpdateFrcInfoLetter(CaseDetails caseDetails, String authToken) {
        if (shouldPrintForRespondentSolicitor(caseDetails)) {
            return Optional.ofNullable(generateSolicitorUpdateFrcInfoLetter(caseDetails, authToken, RESPONDENT));
        } else if (shouldPrintForRespondent(caseDetails)) {
            return Optional.ofNullable(generateLitigantUpdateFrcInfoLetter(caseDetails, authToken, RESPONDENT));
        }
        return Optional.empty();
    }

    private boolean shouldPrintForRespondentSolicitor(CaseDetails caseDetails) {
        return caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())
            && !caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseDetails);
    }

    private boolean shouldPrintForRespondent(CaseDetails caseDetails) {
        return !caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData());
    }
}
