package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO_LABEL;

@Component
public class ManageDocumentsHandlerValidator {

    public static final String CHOOSE_A_DIFFERENT_PARTY = " not present on the case, do you want to continue?";

    public void validateSelectedIntervenerParties(FinremCaseData caseData, List<UploadCaseDocumentCollection> manageCaseDocumentCollection,
                                                  List<String> warnings) {

        if (StringUtils.isBlank(caseData.getIntervenerOne().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_ONE, manageCaseDocumentCollection)) {
            warnings.add(INTERVENER_ONE_LABEL + CHOOSE_A_DIFFERENT_PARTY);
        }
        if (StringUtils.isBlank(caseData.getIntervenerTwo().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_TWO, manageCaseDocumentCollection)) {
            warnings.add(INTERVENER_TWO_LABEL + CHOOSE_A_DIFFERENT_PARTY);
        }
        if (StringUtils.isBlank(caseData.getIntervenerThree().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_THREE, manageCaseDocumentCollection)) {
            warnings.add(INTERVENER_THREE_LABEL + CHOOSE_A_DIFFERENT_PARTY);
        }
        if (StringUtils.isBlank(caseData.getIntervenerFour().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_FOUR, manageCaseDocumentCollection)) {
            warnings.add(INTERVENER_FOUR_LABEL + CHOOSE_A_DIFFERENT_PARTY);
        }
    }

    private boolean isIntervenerPartySelected(CaseDocumentParty caseDocumentParty,
                                              List<UploadCaseDocumentCollection> manageCaseDocumentCollection) {
        return manageCaseDocumentCollection.stream().anyMatch(documentCollection -> {
            if (documentCollection.getUploadCaseDocument().getCaseDocumentParty() != null) {
                return caseDocumentParty.equals(documentCollection.getUploadCaseDocument().getCaseDocumentParty());
            }
            return false;
        });
    }
}
