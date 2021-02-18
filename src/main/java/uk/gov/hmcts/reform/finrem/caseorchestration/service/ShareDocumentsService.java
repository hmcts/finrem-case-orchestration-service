package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collection;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShareDocumentsService {

    static final Map<String, String> APPLICANT_DOCUMENT_COLLECTIONS_SHARING_MAP = ImmutableMap.<String, String>builder()
        .put("appHearingBundlesCollection", "appHearingBundlesCollectionShared")
        .put("appFormEExhibitsCollection", "appFormEExhibitsCollectionShared")
        .put("appChronologiesCollection", "appChronologiesCollectionShared")
        .put("appQACollection", "appQACollectionShared")
        .put("appStatementsExhibitsCollection", "appStatementsExhibitsCollectionShared")
        .put("appCaseSummariesCollection", "appCaseSummariesCollectionShared")
        .put("appFormsHCollection", "appFormsHCollectionShared")
        .put("appExpertEvidenceCollection", "appExpertEvidenceCollectionShared")
        .put("appCorrespondenceDocsCollection", "appCorrespondenceDocsCollectionShared")
        .put("appOtherCollection", "appOtherCollectionShared")
        .build();

    static final Map<String, String> RESPONDENT_DOCUMENT_COLLECTIONS_SHARING_MAP = ImmutableMap.<String, String>builder()
        .put("respHearingBundlesCollectionShared", "respHearingBundlesCollectionShared")
        .put("respFormEExhibitsCollectionShared", "respFormEExhibitsCollectionShared")
        .put("respChronologiesCollectionShared", "respChronologiesCollectionShared")
        .put("respQACollectionShared", "respQACollectionShared")
        .put("respStatementsExhibitsCollectionShared", "respStatementsExhibitsCollectionShared")
        .put("respCaseSummariesCollectionShared", "respCaseSummariesCollectionShared")
        .put("respFormsHCollectionShared", "respFormsHCollectionShared")
        .put("respExpertEvidenceCollectionShared", "respExpertEvidenceCollectionShared")
        .put("respCorrespondenceDocsCollShared", "respCorrespondenceDocsCollShared")
        .put("respOtherCollectionShared", "respOtherCollectionShared")
        .build();

    private final CaseDataService caseDataService;

    public void shareDocumentsWithRespondent(CaseDetails caseDetails) {
        copyDocumentCollections(caseDetails, APPLICANT_DOCUMENT_COLLECTIONS_SHARING_MAP);
    }

    public void shareDocumentsWithApplicant(CaseDetails caseDetails) {
        copyDocumentCollections(caseDetails, RESPONDENT_DOCUMENT_COLLECTIONS_SHARING_MAP);
    }

    public void clearSharedDocumentsVisibleToRespondent(CaseDetails caseDetails) {
        clearSharedDocumentCollections(caseDetails, APPLICANT_DOCUMENT_COLLECTIONS_SHARING_MAP.values());
    }

    public void clearSharedDocumentsVisibleToApplicant(CaseDetails caseDetails) {
        clearSharedDocumentCollections(caseDetails, RESPONDENT_DOCUMENT_COLLECTIONS_SHARING_MAP.values());
    }

    private void copyDocumentCollections(CaseDetails caseDetails, Map<String, String> documentCollectionsSharingMap) {
        documentCollectionsSharingMap.entrySet().stream().forEach(sourceToDestinationMapEntry -> caseDataService.copyCollection(caseDetails.getData(),
            sourceToDestinationMapEntry.getKey(), sourceToDestinationMapEntry.getValue()));
    }

    private void clearSharedDocumentCollections(CaseDetails caseDetails, Collection<String> documentCollectionsToClear) {
        documentCollectionsToClear.forEach(ccdFieldName -> caseDetails.getData().put(ccdFieldName, null));
    }
}
