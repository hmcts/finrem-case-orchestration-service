package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyConfidentialDocumentsService {

    public List<UploadCaseDocumentCollection> getConfidentialCaseDocumentCollection(
        List<ConfidentialUploadedDocumentData> legacyConfidentialDocumentsUploaded) {

        return legacyConfidentialDocumentsUploaded != null
            ? legacyConfidentialDocumentsUploaded.stream().map(this::getUploadCaseDocumentCollection)
            .collect(Collectors.toList())
            : new ArrayList<>();
    }

    private UploadCaseDocumentCollection getUploadCaseDocumentCollection(
        ConfidentialUploadedDocumentData legacyConfidentialDocumentsCollection) {

        UploadConfidentialDocument legacyConfidentialDocument = legacyConfidentialDocumentsCollection.getValue();
        return UploadCaseDocumentCollection.builder()
            .id(legacyConfidentialDocumentsCollection.getId())
            .uploadCaseDocument(
                UploadCaseDocument.builder()
                    .caseDocuments(legacyConfidentialDocument.getDocumentLink())
                    .caseDocumentType(legacyConfidentialDocument.getDocumentType())
                    .caseDocumentParty(CaseDocumentParty.forValue(legacyConfidentialDocument.getCaseDocumentParty()))
                    .caseDocumentOther(legacyConfidentialDocument.getDocumentComment())
                    .caseDocumentConfidential(YesOrNo.YES)
                    .caseDocumentUploadDateTime(legacyConfidentialDocument.getConfidentialDocumentUploadDateTime())
                    .build())
            .build();
    }

}
