package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class PartyDocumentHandler extends CaseDocumentHandler<ContestedUploadedDocumentData> {

    protected static final String APPLICANT = "applicant";
    protected static final String RESPONDENT = "respondent";
    private final String collectionName;
    private final String party;

    public PartyDocumentHandler(String collectionName, String party, ObjectMapper mapper) {
        super(mapper);
        this.collectionName = collectionName;
        this.party = party;
    }

    public void handle(List<ContestedUploadedDocumentData> uploadedDocuments,
                       Map<String, Object> caseData) {
        CaseDocumentHandler.setDocumentUploadDate(uploadedDocuments);
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
            caseData.put(collectionName, sortDocumentsByDateDesc(documentCollection));
        }
    }

    @Override
    protected List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData, String collection) {
        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }
        return objectMapper.convertValue(caseData.get(collection), new TypeReference<>() {
        });
    }

    protected abstract boolean isDocumentTypeValid(String caseDocumentType);

}
