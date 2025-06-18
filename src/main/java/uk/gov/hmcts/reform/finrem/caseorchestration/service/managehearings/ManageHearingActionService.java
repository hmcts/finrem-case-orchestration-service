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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

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
     * Updates the hearing tab data for the case by processing the hearings collection.
     * Maps each hearing to its corresponding tab data representation, sorts the data
     * by hearing date in ascending order, and categorizes it by party. The updated
     * tab data is then stored in the case data.
     *
     * @param caseData the case data containing the hearings and hearing documents
     */
    public void updateTabData(FinremCaseData caseData) {
        ManageHearingsWrapper hearingsWrapper = caseData.getManageHearingsWrapper();
        List<ManageHearingsCollectionItem> hearings = Optional.ofNullable(hearingsWrapper.getHearings())
            .orElseGet(ArrayList::new);

        List<HearingTabCollectionItem> hearingTabItems = mapAndSortHearings(hearings, caseData);

        Map<String, List<HearingTabCollectionItem>> partyTabItems = Map.of(
            APPLICANT, filterHearingTabItems(hearingTabItems, APPLICANT),
            RESPONDENT, filterHearingTabItems(hearingTabItems, RESPONDENT),
            INTERVENER1, filterHearingTabItems(hearingTabItems, INTERVENER1),
            INTERVENER2, filterHearingTabItems(hearingTabItems, INTERVENER2),
            INTERVENER3, filterHearingTabItems(hearingTabItems, INTERVENER3),
            INTERVENER4, filterHearingTabItems(hearingTabItems, INTERVENER4)
        );

        hearingsWrapper.setHearingTabItems(hearingTabItems);
        hearingsWrapper.setApplicantHearingTabItems(partyTabItems.get(APPLICANT));
        hearingsWrapper.setRespondentHearingTabItems(partyTabItems.get(RESPONDENT));
        hearingsWrapper.setInt1HearingTabItems(partyTabItems.get(INTERVENER1));
        hearingsWrapper.setInt2HearingTabItems(partyTabItems.get(INTERVENER2));
        hearingsWrapper.setInt3HearingTabItems(partyTabItems.get(INTERVENER3));
        hearingsWrapper.setInt4HearingTabItems(partyTabItems.get(INTERVENER4));
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
