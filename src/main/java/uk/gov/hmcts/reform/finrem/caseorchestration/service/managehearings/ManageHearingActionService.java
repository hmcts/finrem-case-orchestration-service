package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
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
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COMPLIANCE_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingActionService {

    private final ManageHearingsDocumentService manageHearingsDocumentService;
    private final ExpressCaseService expressCaseService;
    private final HearingTabDataMapper hearingTabDataMapper;

    private record DocumentRecord(CaseDocument caseDocument, CaseDocumentType caseDocumentType) {}

    /**
     * Adds a new hearing to the case and generates associated documents.
     * Updates the hearings collection and generates documents based on the hearing type
     * and case configuration. Adds the generated documents to the hearing documents collection.
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

        Map<String, DocumentRecord> documentMap = new HashMap<>();

        generateHearingNotice(finremCaseDetails, authToken, documentMap);

        boolean shouldGenerateFormC = HearingType.FDA.equals(hearingType)
            || (HearingType.FDR.equals(hearingType) && expressCaseService.isExpressCase(caseData));

        boolean isNotFastTrack = YesOrNo.isNoOrNull(caseData.getFastTrackDecision());

        if (shouldGenerateFormC) {
            generateFormC(finremCaseDetails, authToken, documentMap);

            if (isNotFastTrack) {
                generateFormG(finremCaseDetails, authToken, documentMap);
            }

            Map<String, CaseDocument> pfdDocs = manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, authToken);
            documentMap.put(PFD_NCDR_COVER_LETTER, new DocumentRecord(pfdDocs.get(PFD_NCDR_COVER_LETTER), CaseDocumentType.PFD_NCDR_COVER_LETTER));
            documentMap.put(PFD_NCDR_COMPLIANCE_LETTER, new DocumentRecord(pfdDocs.get(PFD_NCDR_COMPLIANCE_LETTER), CaseDocumentType.PFD_NCDR_COMPLIANCE_LETTER));

            generateOutOfCourtResolution(finremCaseDetails, authToken, documentMap);
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

        List<ManageHearingsCollectionItem> manageHearingsCollectionItemList = ofNullable(
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
     * Takes a map containing documents and descriptive information encapsulated in `DocumentRecord`.
     * Adds each document to the hearing documents collection in the `ManageHearingsWrapper`.
     * Each document is associated with the current working hearing ID.
     *
     * @param documentMap   a map containing a string describing the document against a `DocumentRecord`
     *                      containing the corresponding `CaseDocument` object and `CaseDocumentType`.
     * @param hearingsWrapper the wrapper containing hearing-related data
     */
    private void addDocumentsToCollection(Map<String, DocumentRecord> documentMap,
                                          ManageHearingsWrapper hearingsWrapper) {
        List<ManageHearingDocumentsCollectionItem> manageHearingDocuments = ofNullable(
                hearingsWrapper.getHearingDocumentsCollection())
            .orElseGet(ArrayList::new);

        documentMap.forEach((key, documentRecord) -> {

            CaseDocument caseDocument = documentRecord.caseDocument();
            CaseDocumentType caseDocumentType = documentRecord.caseDocumentType();

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
     * Updates the hearing tab data for the case by processing the hearings collection.
     * Maps each hearing to its corresponding tab data representation, sorts the data
     * by hearing date in ascending order, and categorizes it by party. The updated
     * tab data is then stored in the case data. The resulting tab data is then merged
     * with any existing migrated tab items and updated in the case data.
     *
     * @param caseData the case data containing the hearings and hearing documents
     */
    public void updateTabData(FinremCaseData caseData) {
        ManageHearingsWrapper hearingsWrapper = caseData.getManageHearingsWrapper();
        List<ManageHearingsCollectionItem> hearings = nonMigratedHearings(Optional.ofNullable(hearingsWrapper.getHearings())
            .orElseGet(ArrayList::new));

        List<HearingTabCollectionItem> hearingTabItems = mapAndSortHearings(hearings, caseData);

        Map<String, List<HearingTabCollectionItem>> partyTabItems = Map.of(
            APPLICANT, filterHearingTabItems(hearingTabItems, APPLICANT),
            RESPONDENT, filterHearingTabItems(hearingTabItems, RESPONDENT),
            INTERVENER1, filterHearingTabItems(hearingTabItems, INTERVENER1),
            INTERVENER2, filterHearingTabItems(hearingTabItems, INTERVENER2),
            INTERVENER3, filterHearingTabItems(hearingTabItems, INTERVENER3),
            INTERVENER4, filterHearingTabItems(hearingTabItems, INTERVENER4)
        );

        caseData.getManageHearingsWrapper().setHearingTabItems(mergeMigratedHearingTabItems(caseData, hearingTabItems));

        hearingsWrapper.setApplicantHearingTabItems(mergeMigratedHearingTabItems(caseData, partyTabItems.get(APPLICANT)));
        hearingsWrapper.setRespondentHearingTabItems(mergeMigratedHearingTabItems(caseData, partyTabItems.get(RESPONDENT)));
        hearingsWrapper.setInt1HearingTabItems(mergeMigratedHearingTabItems(caseData, partyTabItems.get(INTERVENER1)));
        hearingsWrapper.setInt2HearingTabItems(mergeMigratedHearingTabItems(caseData, partyTabItems.get(INTERVENER2)));
        hearingsWrapper.setInt3HearingTabItems(mergeMigratedHearingTabItems(caseData, partyTabItems.get(INTERVENER3)));
        hearingsWrapper.setInt4HearingTabItems(mergeMigratedHearingTabItems(caseData, partyTabItems.get(INTERVENER4)));

        manageHearingsDocumentService.categoriseSystemDuplicateDocs(hearings,
            hearingsWrapper.getHearingDocumentsCollection());
    }

    private List<HearingTabCollectionItem> mergeMigratedHearingTabItems(FinremCaseData caseData,
                                                                        List<HearingTabCollectionItem> hearingToBeAdded) {
        List<HearingTabCollectionItem> migratedHearingTabItems = getMigratedHearingTabItems(caseData);
        migratedHearingTabItems.addAll(hearingToBeAdded);
        return migratedHearingTabItems;
    }

    private List<HearingTabCollectionItem> mapAndSortHearings(List<ManageHearingsCollectionItem> hearings, FinremCaseData caseData) {
        return hearings.stream()
            .sorted(Comparator.comparing(hearing -> hearing.getValue().getHearingDate()))
            .map(hearing -> HearingTabCollectionItem.builder()
                .value(hearingTabDataMapper.mapHearingToTabData(
                    hearing,
                    caseData.getManageHearingsWrapper().getHearingDocumentsCollection()))
                .build())
            .toList();
    }

    private List<HearingTabCollectionItem> filterHearingTabItems(List<HearingTabCollectionItem> hearingTabItems, String party) {
        return hearingTabItems.stream()
            .filter(hearingTabItem -> hearingTabItem.getValue().getTabConfidentialParties().contains(party))
            .toList();
    }

    private void generateHearingNotice(FinremCaseDetails finremCaseDetails, String authToken, Map<String, DocumentRecord> documentMap) {
        documentMap.put(
            HEARING_NOTICE_DOCUMENT,
            new DocumentRecord(
                manageHearingsDocumentService.generateHearingNotice(finremCaseDetails, authToken),
                CaseDocumentType.HEARING_NOTICE
            )
        );
    }

    private void generateFormC(FinremCaseDetails finremCaseDetails, String authToken, Map<String, DocumentRecord> documentMap) {
        documentMap.put(
            FORM_C,
            new DocumentRecord(
                manageHearingsDocumentService.generateFormC(finremCaseDetails, authToken),
                CaseDocumentType.FORM_C
            )
        );
    }

    private void generateFormG(FinremCaseDetails finremCaseDetails, String authToken, Map<String, DocumentRecord> documentMap) {
        documentMap.put(
            FORM_G,
            new DocumentRecord(
                manageHearingsDocumentService.generateFormG(finremCaseDetails, authToken),
                CaseDocumentType.FORM_G
            )
        );
    }

    private void generateOutOfCourtResolution(FinremCaseDetails finremCaseDetails, String authToken, Map<String, DocumentRecord> documentMap) {
        documentMap.put(
            OUT_OF_COURT_RESOLUTION,
            new DocumentRecord(
                manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, authToken),
                CaseDocumentType.OUT_OF_COURT_RESOLUTION
            )
        );
    }

    /**
     * Returns a filtered list of {@link ManageHearingsCollectionItem} where the associated hearing
     * has not been migrated. A hearing is considered "non-migrated" if its {@code wasMigrated} field
     * is either {@code null} or explicitly set to {@link YesOrNo#NO}.
     *
     * <p>
     * This method safely handles {@code null} values at every level — the input list, the collection items,
     * the hearing objects, and the migration status field.
     *
     * @param hearings the list of {@link ManageHearingsCollectionItem} to filter; may be {@code null}
     * @return a list of hearings that are either not marked as migrated or have no migration status set
     */
    private List<ManageHearingsCollectionItem> nonMigratedHearings(List<ManageHearingsCollectionItem> hearings) {
        return emptyIfNull(hearings).stream()
            .filter(item -> {
                Hearing hearing = item != null ? item.getValue() : null;
                YesOrNo wasMigrated = hearing != null ? hearing.getWasMigrated() : null;
                return wasMigrated == null || wasMigrated == YesOrNo.NO;
            })
            .toList();
    }

    /**
     * Retrieves the list of {@link HearingTabCollectionItem} from the given {@link FinremCaseData}
     * where the hearing has been migrated.
     *
     * <p>
     * A hearing is considered "migrated" if its {@code tabHearingMigratedDate} field is not {@code null}.
     * This method ensures safe handling of {@code null} values at all levels and returns the result as a mutable {@link ArrayList}.
     *
     * @param caseData the {@link FinremCaseData} containing the hearing tab items
     * @return a mutable list of hearing tab items that have a non-null {@code tabHearingMigratedDate};
     *         returns an empty list if none are found or if input is {@code null}
     */
    private List<HearingTabCollectionItem> getMigratedHearingTabItems(FinremCaseData caseData) {
        return emptyIfNull(caseData.getManageHearingsWrapper().getHearingTabItems())
            .stream()
            .filter(item -> item.getValue() != null && item.getValue().getTabHearingMigratedDate() != null)
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
