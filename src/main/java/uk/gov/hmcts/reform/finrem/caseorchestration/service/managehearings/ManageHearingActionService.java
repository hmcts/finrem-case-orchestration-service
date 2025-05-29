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

    /**
     * Adds a new hearing to the case details and generates the associated hearing notice document.
     *
     * @param finremCaseDetails case details containing hearing data
     * @param authToken         authorization token for accessing secure resources
     */
    public void performAddHearing(FinremCaseDetails finremCaseDetails, String authToken) {
        FinremCaseData caseData = finremCaseDetails.getData();
        ManageHearingsWrapper hearingWrapper = caseData.getManageHearingsWrapper();
        HearingType hearingType = hearingWrapper.getWorkingHearing().getHearingType();

        UUID hearingId = UUID.randomUUID();
        addHearingToCollection(hearingWrapper, hearingId);
        addHearingNotice(hearingWrapper, finremCaseDetails, hearingId, authToken);

        if (HearingType.FDA.equals(hearingType) ||
            (HearingType.FDR.equals(hearingType)) &&
                expressCaseService.isExpressCase(caseData)) {
            addFormCAndG(hearingWrapper, finremCaseDetails, hearingId, authToken);
            addPfdNcdrDocuments(hearingWrapper, finremCaseDetails, authToken);
            addOutOfCourtResolutionDocument(hearingWrapper, finremCaseDetails, authToken);
            // TODO: Attach 'out of court' and PFD supporting documents
        }

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
     * Generates and adds a hearing notice document to the hearing documents collection.
     *
     * @param hearingsWrapper wrapper containing hearing-related data
     * @param finremCaseDetails case details containing the hearing data
     * @param hearingId the ID for the associated hearing
     * @param authToken authorization token for accessing secure resources
     */
    private void addHearingNotice(ManageHearingsWrapper hearingsWrapper,
                                  FinremCaseDetails finremCaseDetails,
                                  UUID hearingId,
                                  String authToken) {

        CaseDocument hearingNotice = manageHearingsDocumentService
                .generateHearingNotice(hearingsWrapper.getWorkingHearing(), finremCaseDetails, authToken);

        addDocumentToCollection(hearingNotice, hearingsWrapper);
    }

    private void addFormCAndG(ManageHearingsWrapper hearingsWrapper,
                          FinremCaseDetails finremCaseDetails,
                          UUID hearingId,
                          String authToken) {

        FinremCaseData caseData = finremCaseDetails.getData();

        // TODO: Need to handle to handle not creating duplicate docs
        CaseDocument formC =
           manageHearingsDocumentService.generateFormC(hearingsWrapper.getWorkingHearing(), finremCaseDetails, authToken);
        addDocumentToCollection(formC, hearingsWrapper);

        if(!caseData.isFastTrackApplication()) {
            CaseDocument formG =
                manageHearingsDocumentService.generateFormG(hearingsWrapper.getWorkingHearing(), finremCaseDetails, authToken);
            addDocumentToCollection(formG, hearingsWrapper);
        }
    }

    private void addPfdNcdrDocuments(ManageHearingsWrapper hearingsWrapper,
                                         FinremCaseDetails finremCaseDetails,
                                         String authToken) {
        Map<String, CaseDocument> pfdNcdrComplianceLetter = manageHearingsDocumentService
            .generatePfdNcdrDocuments(finremCaseDetails, authToken);
        pfdNcdrComplianceLetter.forEach((key, document) -> addDocumentToCollection(document, hearingsWrapper));
    }

    private void addOutOfCourtResolutionDocument(ManageHearingsWrapper hearingsWrapper,
                                                  FinremCaseDetails finremCaseDetails,
                                                  String authToken) {
        CaseDocument outOfCourtResolutionDocument =
            manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, authToken);
        addDocumentToCollection(outOfCourtResolutionDocument, hearingsWrapper);
    }

    private void addDocumentToCollection(CaseDocument document, ManageHearingsWrapper hearingsWrapper) {
        List<ManageHearingDocumentsCollectionItem> manageHearingDocuments = Optional.ofNullable(
                hearingsWrapper.getHearingDocumentsCollection())
            .orElseGet(ArrayList::new);

        manageHearingDocuments.add(
            ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument
                    .builder()
                    .hearingId(hearingsWrapper.getWorkingHearingId())
                    .hearingDocument(document)
                    .build())
                .build()
        );

        hearingsWrapper.setHearingDocumentsCollection(manageHearingDocuments);
    }
}
