package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DraftOrderUtils {

    private DraftOrderUtils() {
    }

    public static List<CaseDocument> consolidateUploadingDocuments(CaseDocument draftOrder, CaseDocument pensionSharingAnnex,
                                                                   List<DocumentCollection> attachments) {
        List<CaseDocument> documentList = new ArrayList<>(Stream.of(draftOrder, pensionSharingAnnex).filter(Objects::nonNull).toList());
        if (attachments != null) {
            documentList.addAll(attachments.stream()
                .map(DocumentCollection::getValue)
                .filter(Objects::nonNull)
                .toList());
        }
        return documentList;
    }
}
