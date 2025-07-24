package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.postalhearingdocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.postalhearingdocuments.HearingTypeResolver.getByWorkingHearingAndDocumentType;

@Component
class AllHearingsDocumentSupplier implements PostalHearingDocumentSupplier {

    /**
     * Hearings have a core set of documents that need to be posted.
     * These are the documents that are always posted.
     * <ul>
     *     <li>Hearing Notice</li>
     *     <li>Form A</li>
     *     <li>Out of Court Resolution</li>
     *     <li>PDF NCDR Compliance Letter</li>
     *     <li>PDF NCDR Cover Letter</li>
     * </ul>
     * Removes non-null objects from the list, so exceptions are not thrown when documents are missing.
     * The Form A doesn't exist in the hearing documents collection, so it is added separately.
     *
     * @param finremCaseDetails the case details containing the hearing documents
     * @return a list of {@link CaseDocument} that are posted for all cases
     */
    @Override
    public List<CaseDocument> get(FinremCaseDetails finremCaseDetails) {
        return Stream.of(
                getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.HEARING_NOTICE),
                getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.OUT_OF_COURT_RESOLUTION),
                getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.PFD_NCDR_COMPLIANCE_LETTER),
                getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.PFD_NCDR_COVER_LETTER),
                finremCaseDetails.getData().getMiniFormA()
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
