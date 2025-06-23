package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_COURT_RESOLUTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingActionService {

    private final ManageHearingsDocumentService manageHearingsDocumentService;
    private final ExpressCaseService expressCaseService;
    private final HearingTabDataMapper hearingTabDataMapper;

    /**
     * Adds a new hearing to the case and generates associated documents.
     * Updates the hearings collection and generates documents based on the hearing type
     * and case configuration. Adds the generated documents to the hearing documents collection.
     *
     * Todo: when merging tabs work from master, check that the generatePfdNcdrDocuments parts live in private methods
     *
     * @param finremCaseDetails case details containing hearing and case data
     * @param authToken         authorization token for secure resource access
     */
    public void performAddHearing(FinremCaseDetails finremCaseDetails, String authToken) {
        FinremCaseData caseData = finremCaseDetails.getData();
        ManageHearingsWrapper hearingWrapper = caseData.getManageHearingsWrapper();
        HearingType hearingType = hearingWrapper.getWorkingHearing().getHearingType();

        UUID hearingId = UUID.randomUUID();
        addHearingToCollection(hearingWrapper, hearingId);

        Map<String, Pair<CaseDocument, CaseDocumentType>> documentMap = new HashMap<>();

        generateHearingNotice(finremCaseDetails, authToken, documentMap);

        boolean shouldGenerateFormC = HearingType.FDA.equals(hearingType)
            || (HearingType.FDR.equals(hearingType) && expressCaseService.isExpressCase(caseData));

        boolean isNotFastTrack = YesOrNo.isNoOrNull(caseData.getFastTrackDecision());

        if (shouldGenerateFormC) {
            generateFormC(finremCaseDetails, authToken, documentMap);

            if (isNotFastTrack) {
                generateFormG(finremCaseDetails, authToken, documentMap);
            }

            documentMap.putAll(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, authToken));

            generateOutOfCourtResolution(finremCaseDetails, authToken, documentMap);
        }

        addDocumentsToCollection(documentMap, hearingWrapper);
        updateTabData(caseData);
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
     * Takes a map containing documents and descriptive information about them.
     * Adds each document to the hearing documents collection in the `ManageHearingsWrapper`.
     * Each document is associated with the current working hearing ID.
     *
     * @param documentMap   a map containing a string describing the document against a pair with
     *                      the corresponding CaseDocument object and CaseDocumentType.
     * @param hearingsWrapper the wrapper containing hearing-related data
     */
    private void addDocumentsToCollection(Map<String, Pair<CaseDocument, CaseDocumentType>> documentMap,
                                          ManageHearingsWrapper hearingsWrapper) {
        List<ManageHearingDocumentsCollectionItem> manageHearingDocuments = Optional.ofNullable(
                hearingsWrapper.getHearingDocumentsCollection())
            .orElseGet(ArrayList::new);

        documentMap.forEach((key, pair) -> {

            CaseDocument caseDocument = pair.getLeft();
            CaseDocumentType caseDocumentType = pair.getRight();

            manageHearingDocuments.add(
                    ManageHearingDocumentsCollectionItem.builder()
                            .value(ManageHearingDocument.builder()
                                    .hearingId(hearingsWrapper.getWorkingHearingId())
                                    .hearingDocument(caseDocument)
                                    .hearingCaseDocumentType(caseDocumentType)
                                    .build())
                            .build()
            );
        });

        hearingsWrapper.setHearingDocumentsCollection(manageHearingDocuments);
    }

    /**
     * Regenerates the hearing tab data for the case.
     * This method processes the hearings collection and maps each hearing to its corresponding
     * tab data representation ordered by hearing date ASC. The resulting tab data is then updated in the case data.
     *
     * @param caseData the case data containing the hearings and hearing documents
     */
    private void updateTabData(FinremCaseData caseData) {
        List<ManageHearingsCollectionItem> hearings =
            caseData.getManageHearingsWrapper().getHearings();

        List<HearingTabCollectionItem> hearingTabItems = hearings.stream()
            .sorted(Comparator.comparing(hearingCollectionItem ->
                hearingCollectionItem.getValue().getHearingDate()))
            .map(hearingCollectionItem -> HearingTabCollectionItem.builder()
                .value(hearingTabDataMapper.mapHearingToTabData(
                    hearingCollectionItem,
                    caseData.getManageHearingsWrapper().getHearingDocumentsCollection()))
                .build())
            .toList();

        caseData.getManageHearingsWrapper().setHearingTabItems(hearingTabItems);
    }

    /**
     * Generates a hearing notice.
     * Adds it to the passed document map.
     */
    private void generateHearingNotice(
            FinremCaseDetails finremCaseDetails,
            String authToken,
            Map<String, Pair<CaseDocument, CaseDocumentType>> documentMap) {
        documentMap.put(
                HEARING_NOTICE_DOCUMENT,
                Pair.of(
                        manageHearingsDocumentService.generateHearingNotice(finremCaseDetails, authToken),
                        CaseDocumentType.HEARING_NOTICE
                )
        );
    }

    /**
     * Generates a Form C.
     * Adds it to the passed document map.
     */
    private void generateFormC(
            FinremCaseDetails finremCaseDetails,
            String authToken,
            Map<String, Pair<CaseDocument, CaseDocumentType>> documentMap) {
        documentMap.put(
                FORM_C,
                Pair.of(
                        manageHearingsDocumentService.generateFormC(finremCaseDetails, authToken),
                        CaseDocumentType.FORM_C
                )
        );
    }

    /**
     * Generates a Form G.
     * Adds it to the passed document map.
     */
    private void generateFormG(
            FinremCaseDetails finremCaseDetails,
            String authToken,
            Map<String, Pair<CaseDocument, CaseDocumentType>> documentMap) {
        documentMap.put(
                FORM_G,
                Pair.of(
                        manageHearingsDocumentService.generateFormG(finremCaseDetails, authToken),
                        CaseDocumentType.FORM_G
                )
        );
    }

    /**
     * Generates Out of court resolution.
     * Adds it to the passed document map.
     */
    private void generateOutOfCourtResolution(
            FinremCaseDetails finremCaseDetails,
            String authToken,
            Map<String, Pair<CaseDocument, CaseDocumentType>> documentMap) {
        documentMap.put(
                OUT_OF_COURT_RESOLUTION,
                Pair.of(
                        manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, authToken),
                        CaseDocumentType.OUT_OF_COURT_RESOLUTION
                )
        );
    }
}
