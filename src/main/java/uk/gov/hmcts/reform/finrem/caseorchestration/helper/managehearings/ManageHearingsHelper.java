package uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils.isPdf;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils.isWordDocument;

@Slf4j
@Component
public class ManageHearingsHelper {

    public boolean areAllAdditionalHearingDocsWordOrPdf(ManageHearingsWrapper manageHearingsWrapper) {
        return manageHearingsWrapper.getWorkingHearing().getAdditionalHearingDocs().stream()
            .allMatch(doc -> isPdf(doc.getValue()) || isWordDocument(doc.getValue()));
    }
}
