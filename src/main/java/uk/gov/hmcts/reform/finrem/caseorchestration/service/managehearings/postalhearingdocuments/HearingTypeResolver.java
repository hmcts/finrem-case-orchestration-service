package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.postalhearingdocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

class HearingTypeResolver {

    /**
     * Retrieves the case's current working hearing.
     * Then uses that to the most recent hearing document with the passed CaseDocumentType argument.
     * If no notice is found, returns an empty list.
     * @param finremCaseDetails the case details containing the hearing documents.
     * @param documentType a {@link CaseDocumentType} identifying the type of hearing document.
     * @return a {@link CaseDocument}
     */
    public static CaseDocument getByWorkingHearingAndDocumentType(FinremCaseDetails finremCaseDetails,
                                                            CaseDocumentType documentType) {
        ManageHearingsWrapper wrapper = finremCaseDetails.getData().getManageHearingsWrapper();
        UUID hearingId = wrapper.getWorkingHearingId();

        return wrapper.getHearingDocumentsCollection().stream()
            .map(ManageHearingDocumentsCollectionItem::getValue)
            .filter(Objects::nonNull)
            .filter(doc -> Objects.equals(hearingId, doc.getHearingId()))
            .filter(doc -> Objects.equals(documentType, doc.getHearingCaseDocumentType()))
            .map(ManageHearingDocument::getHearingDocument)
            .filter(Objects::nonNull)
            .max(Comparator.comparing(CaseDocument::getUploadTimestamp,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .orElse(null);
    }
}
