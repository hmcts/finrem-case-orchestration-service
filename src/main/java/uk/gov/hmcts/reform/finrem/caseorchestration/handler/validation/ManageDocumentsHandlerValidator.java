package uk.gov.hmcts.reform.finrem.caseorchestration.handler.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.List;

@Component
public class ManageDocumentsHandlerValidator {

    public static final String CHOOSE_A_DIFFERENT_PARTY = " not present on the case, do you want to continue?";
    public static final String INTERVENER_1 = "Intervener 1 ";
    public static final String INTERVENER_2 = "Intervener 2 ";
    public static final String INTERVENER_3 = "Intervener 3 ";
    public static final String INTERVENER_4 = "Intervener 4 ";

    public void validateSelectedIntervenerParties(FinremCaseData caseData, List<UploadCaseDocumentCollection> manageCaseDocumentCollection,
                                                  List<String> warnings) {

        if (StringUtils.isBlank(caseData.getIntervenerOneWrapper().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_ONE, manageCaseDocumentCollection)) {
            warnings.add(INTERVENER_1 + CHOOSE_A_DIFFERENT_PARTY);
        }
        if (StringUtils.isBlank(caseData.getIntervenerTwoWrapper().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_TWO, manageCaseDocumentCollection)) {
            warnings.add(INTERVENER_2 + CHOOSE_A_DIFFERENT_PARTY);
        }
        if (StringUtils.isBlank(caseData.getIntervenerThreeWrapper().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_THREE, manageCaseDocumentCollection)) {
            warnings.add(INTERVENER_3 + CHOOSE_A_DIFFERENT_PARTY);
        }
        if (StringUtils.isBlank(caseData.getIntervenerFourWrapper().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_FOUR, manageCaseDocumentCollection)) {
            warnings.add(INTERVENER_4 + CHOOSE_A_DIFFERENT_PARTY);
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
