package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;

@Service
public class UploadedDocumentService extends DocumentUploadService<UploadCaseDocumentCollection> {

    private static List<CaseDocumentType> administrativeCaseDocumentTypes = List.of(
        CaseDocumentType.ATTENDANCE_SHEETS,
        CaseDocumentType.JUDICIAL_NOTES,
        CaseDocumentType.JUDGMENT,
        CaseDocumentType.WITNESS_SUMMONS,
        CaseDocumentType.TRANSCRIPT
    );

    public UploadedDocumentService(ObjectMapper objectMapper) {
        super(objectMapper, UploadCaseDocumentCollection.class);
    }


    private void setDefaultsForDocumentTypes(UploadCaseDocumentCollection document) {
        UploadCaseDocument uploadCaseDocument = document.getUploadCaseDocument();
        if (administrativeCaseDocumentTypes.contains(uploadCaseDocument.getCaseDocumentType())) {
            uploadCaseDocument.setCaseDocumentParty(CaseDocumentParty.CASE);
            uploadCaseDocument.setCaseDocumentConfidentiality(YesOrNo.NO);
            uploadCaseDocument.setCaseDocumentFdr(YesOrNo.NO);
        } else if (CaseDocumentType.WITHOUT_PREJUDICE_OFFERS.equals(uploadCaseDocument.getCaseDocumentType())) {
            uploadCaseDocument.setCaseDocumentConfidentiality(YesOrNo.NO);
            uploadCaseDocument.setCaseDocumentFdr(YesOrNo.YES);
            uploadCaseDocument.setCaseDocumentParty(null);
        }

    }

    public void addDefaultsToToNewAdministrativeDocuments(List<UploadCaseDocumentCollection> managedCollections) {

        managedCollections.stream().forEach(document -> setDefaultsForDocumentTypes(document));
    }
}
