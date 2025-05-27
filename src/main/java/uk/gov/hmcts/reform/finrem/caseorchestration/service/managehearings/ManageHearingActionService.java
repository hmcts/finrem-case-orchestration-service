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

        UUID hearingId = UUID.randomUUID();
        addHearingToCollection(hearingWrapper, hearingId);
        addHearingNotice(hearingWrapper, finremCaseDetails, hearingId, authToken);

        if (HearingType.FDA.equals(hearingWrapper.getWorkingHearing().getHearingType())) {
            // Generate hearing type specific docs
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

        List<ManageHearingDocumentsCollectionItem> manageHearingDocuments = Optional.ofNullable(
                        hearingsWrapper.getHearingDocumentsCollection())
                .orElseGet(ArrayList::new);

        manageHearingDocuments.add(
                ManageHearingDocumentsCollectionItem.builder()
                        .value(ManageHearingDocument
                                .builder()
                                .hearingId(hearingId)
                                .hearingDocument(hearingNotice)
                                .build())
                        .build()
        );

        hearingsWrapper.setHearingDocumentsCollection(manageHearingDocuments);
    }

    private void addFormCAndG(ManageHearingsWrapper hearingsWrapper,
                          FinremCaseDetails finremCaseDetails,
                          UUID hearingId,
                          String authToken) {

        FinremCaseData caseData = finremCaseDetails.getData();

        if(caseData.isFastTrackApplication()) {
            // Generate fastTrack Form C
        } else {
            // Generate Form C for express case
            // Generate From G for express case
        }
    }
}
