package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAddedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Component
public class IntervenerAddedLetterDetailsGenerator {


    public IntervenerAddedLetterDetails generate(FinremCaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient,
                                                 IntervenerDetails intervenerDetails) {
        return IntervenerAddedLetterDetails.builder()
            .courtDetails(buildFrcCourtDetails(caseDetails.getData()))
            .build();
    }
}
