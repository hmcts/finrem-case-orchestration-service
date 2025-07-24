package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.postalhearingdocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.postalhearingdocuments.HearingTypeResolver.getByWorkingHearingAndDocumentType;

@Component
class FdrHearingDocumentSupplier implements PostalHearingDocumentSupplier {

    /**
     * Gets an Express Form C and Form G CaseDocument.
     * Filters out non-null case documents from the list, so exceptions are not thrown when documents are missing.
     * @param finremCaseDetails the case details containing the hearing documents
     * @return a list of {@link CaseDocument} that are posted for FDR Express cases
     */
    @Override
    public List<CaseDocument> get(FinremCaseDetails finremCaseDetails) {
        HearingType workingHearingType = finremCaseDetails.getData().getManageHearingsWrapper().getWorkingHearing().getHearingType();

        if (!HearingType.FDR.equals(workingHearingType)) {
            return Collections.emptyList();
        }
        CaseDocument formC = getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_C_EXPRESS);
        CaseDocument formG = getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_G);

        return Stream.of(formC, formG)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
