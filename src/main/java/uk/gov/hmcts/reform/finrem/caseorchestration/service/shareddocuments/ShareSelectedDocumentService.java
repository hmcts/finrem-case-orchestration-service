package uk.gov.hmcts.reform.finrem.caseorchestration.service.shareddocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShareSelectedDocumentService {

    private  final List<DocumentSharer> documentCollectionSharers;


    public void copySharedDocumentsToSharedCollection(FinremCaseData caseData,
                                                       String role,
                                                       List<DynamicMultiSelectListElement> documentList) {
        documentList.forEach(doc -> {
            String[] collectionIdAndFilename = doc.getCode().split("#");
            String collId = collectionIdAndFilename[0];
            String collName = collectionIdAndFilename[1];
            documentCollectionSharers.stream()
                .forEach(sharer -> sharer.shareDocumentsToSharedPartyCollection(caseData, collId, collName, role));

        });
    }
}
