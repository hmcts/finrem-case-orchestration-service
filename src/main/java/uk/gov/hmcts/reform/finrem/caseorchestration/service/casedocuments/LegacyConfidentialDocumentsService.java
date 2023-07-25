package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyConfidentialDocumentsService {

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public List<UploadCaseDocumentCollection> mapLegacyConfidentialDocumentToConfidentialDocumentCollection(
        List<ConfidentialUploadedDocumentData> legacyConfidentialDocumentsUploaded) {

        return legacyConfidentialDocumentsUploaded != null
            ? legacyConfidentialDocumentsUploaded.stream().map(this::getUploadCaseDocumentCollection)
            .toList()
            : new ArrayList<>();
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private UploadCaseDocumentCollection getUploadCaseDocumentCollection(
        ConfidentialUploadedDocumentData legacyConfidentialDocumentsCollection) {

        UploadConfidentialDocument legacyConfidentialDocument = legacyConfidentialDocumentsCollection.getValue();
        return UploadCaseDocumentCollection.builder()
            .id(legacyConfidentialDocumentsCollection.getId())
            .uploadCaseDocument(
                UploadCaseDocument.builder()
                    .caseDocuments(legacyConfidentialDocument.getDocumentLink())
                    .caseDocumentType(legacyConfidentialDocument.getDocumentType())
                    .hearingDetails(legacyConfidentialDocument.getDocumentComment())
                    .caseDocumentConfidential(YesOrNo.YES)
                    .caseDocumentUploadDateTime(legacyConfidentialDocument.getConfidentialDocumentUploadDateTime())
                    .build())
            .build();
    }

}
