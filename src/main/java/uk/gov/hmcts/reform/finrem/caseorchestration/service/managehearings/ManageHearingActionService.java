package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingActionService {

    private final ManageHearingsDocumentService manageHearingsDocumentService;
    private final ExpressCaseService expressCaseService;

    private static final String HEARING_NOTICE_DOCUMENT = "hearingNotice";
    private static final String FORM_C = "formC";
    private static final String FORM_G = "formG";
    private static final String OUT_OF_COURT_RESOLUTION = "outOfCourtResolution";

    /**
     * Adds a new hearing to the case details and generates all associated documents, including the hearing notice.
     *
     * This method updates the hearings collection in the case data and generates relevant documents
     * based on the hearing type and case configuration. The generated documents are added to the
     * hearing documents collection in the case data.
     *
     * @param finremCaseDetails the case details containing hearing and case data
     * @param authToken         the authorization token for accessing secure resources
     */
    public void performAddHearing(FinremCaseDetails finremCaseDetails, String authToken) {
        FinremCaseData caseData = finremCaseDetails.getData();
        ManageHearingsWrapper hearingWrapper = caseData.getManageHearingsWrapper();
        Hearing hearing = hearingWrapper.getWorkingHearing();
        HearingType hearingType = hearingWrapper.getWorkingHearing().getHearingType();

        UUID hearingId = UUID.randomUUID();
        addHearingToCollection(hearingWrapper, hearingId);

        Map<String, CaseDocument> documentMap = new HashMap<>();
        documentMap.put(HEARING_NOTICE_DOCUMENT, manageHearingsDocumentService.generateHearingNotice(hearing, finremCaseDetails, authToken));

        if (HearingType.FDA.equals(hearingType) || (HearingType.FDR.equals(hearingType) && expressCaseService.isExpressCase(caseData))) {
            documentMap.put(FORM_C, manageHearingsDocumentService.generateFormC(hearing, finremCaseDetails, authToken));

            if (!caseData.isFastTrackApplication()) {
                documentMap.put(FORM_G, manageHearingsDocumentService.generateFormG(hearing, finremCaseDetails, authToken));
            }

            documentMap.putAll(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, authToken));
            documentMap.put(OUT_OF_COURT_RESOLUTION, manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, authToken));
        }

        addDocumentsToCollection(documentMap, hearingWrapper);
        hearingWrapper.setWorkingHearing(null);
    }

    /**
     * Adds the current working hearing to the hearings collection in the wrapper.
     *
     * @param hearingsWrapper wrapper containing hearing-related data
     * @param hearingId       the ID for the hearing to be added
     */
    private void addHearingToCollection(ManageHearingsWrapper hearingsWrapper, UUID hearingId) {

        List<ManageHearingsCollectionItem> manageHearingsCollectionItemList = Optional.ofNullable(
                        hearingsWrapper.getHearings())
                .orElseGet(ArrayList::new);

        manageHearingsCollectionItemList.add(
                ManageHearingsCollectionItem
                        .builder()
                        .id(hearingId)
                        .value(hearingsWrapper.getWorkingHearing())
                        .build()
        );
        hearingsWrapper.setWorkingHearingId(hearingId);
        hearingsWrapper.setHearings(manageHearingsCollectionItemList);
    }

    /**
     * Adds generated documents to the hearing documents collection in the wrapper.
     * Takes a map of document types and their corresponding `CaseDocument` objects,
     * and adds them to the hearing documents collection in the `ManageHearingsWrapper`.
     * Each document is associated with the current working hearing ID.
     *
     * @param documentMap    a map containing document types as keys and their corresponding `CaseDocument` objects
     * @param hearingsWrapper the wrapper containing hearing-related data
     */
    private void addDocumentsToCollection(Map<String, CaseDocument> documentMap, ManageHearingsWrapper hearingsWrapper) {
        List<ManageHearingDocumentsCollectionItem> manageHearingDocuments = Optional.ofNullable(
                hearingsWrapper.getHearingDocumentsCollection())
            .orElseGet(ArrayList::new);

        documentMap.forEach((key, document) -> manageHearingDocuments.add(
            ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument.builder()
                    .hearingId(hearingsWrapper.getWorkingHearingId())
                    .hearingDocument(document)
                    .build())
                .build()
        ));

        hearingsWrapper.setHearingDocumentsCollection(manageHearingDocuments);
    }
}
