package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class PartyDocumentHandler extends CaseDocumentHandler<ContestedUploadedDocumentData> {

    private final String collectionName;
    private final String party;
    private final ObjectMapper mapper;

    protected static final String APPLICANT = "applicant";
    protected static final String RESPONDENT = "respondent";

    public PartyDocumentHandler(String collectionName, String party, ObjectMapper mapper) {
        super(mapper);
        this.collectionName = collectionName;
        this.party = party;
        this.mapper = mapper;
    }

    public void handle(List<ContestedUploadedDocumentData> uploadedDocuments,
                       Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> documentsFiltered = uploadedDocuments.stream()
            .filter(d -> {
                ContestedUploadedDocument uploadedCaseDocument = d.getUploadedCaseDocument();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentParty() != null
                    && uploadedCaseDocument.getCaseDocumentParty().equals(party);
            })
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isDocumentTypeValid(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> documentCollection = getDocumentCollection(caseData, collectionName);
        documentCollection.addAll(documentsFiltered);
        log.info("Adding items: {}, to {} Collection", documentsFiltered, collectionName);
        uploadedDocuments.removeAll(documentsFiltered);

        if (!documentCollection.isEmpty()) {
            caseData.put(collectionName, documentCollection);
        }
    }

    protected abstract boolean isDocumentTypeValid(String caseDocumentType);
}
