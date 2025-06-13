package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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
     *
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

        Map<String, CaseDocument> documentMap = new HashMap<>();
        documentMap.put(HEARING_NOTICE_DOCUMENT, manageHearingsDocumentService.generateHearingNotice(finremCaseDetails, authToken));

        boolean shouldGenerateFormC = HearingType.FDA.equals(hearingType)
            || (HearingType.FDR.equals(hearingType) && expressCaseService.isExpressCase(caseData));

        boolean isNotFastTrack = YesOrNo.isNoOrNull(caseData.getFastTrackDecision());

        if (shouldGenerateFormC) {
            documentMap.put(FORM_C, manageHearingsDocumentService.generateFormC(finremCaseDetails, authToken));

            if (isNotFastTrack) {
                documentMap.put(FORM_G, manageHearingsDocumentService.generateFormG(finremCaseDetails, authToken));
            }

            documentMap.putAll(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, authToken));
            documentMap.put(OUT_OF_COURT_RESOLUTION, manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, authToken));
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

    /**
     * Regenerates the hearing tab data for the case.
     * This method processes the hearings collection and maps each hearing to its corresponding
     * tab data representation ordered by hearing date ASC. The resulting tab data is then updated in the case data.
     *
     * @param caseData the case data containing the hearings and hearing documents
     */
    private void updateTabData(FinremCaseData caseData) {

        ManageHearingsWrapper hearingsWrapper = caseData.getManageHearingsWrapper();
        List<ManageHearingsCollectionItem> hearings = hearingsWrapper.getHearings();

        List<HearingTabCollectionItem> hearingTabItems = hearings.stream()
            .sorted(Comparator.comparing(hearingCollectionItem ->
                hearingCollectionItem.getValue().getHearingDate()))
            .map(hearingCollectionItem -> HearingTabCollectionItem.builder()
                .value(hearingTabDataMapper.mapHearingToTabData(
                    hearingCollectionItem,
                    caseData.getManageHearingsWrapper().getHearingDocumentsCollection()))
                .build())
            .toList();

        List<HearingTabCollectionItem> applicantTabItems = filterHearingTabItems(hearingTabItems, APPLICANT);
        List<HearingTabCollectionItem> respondentTabItems = filterHearingTabItems(hearingTabItems, RESPONDENT);
        List<HearingTabCollectionItem> int1TabItems = filterHearingTabItems(hearingTabItems, INTERVENER1);
        List<HearingTabCollectionItem> int2TabItems = filterHearingTabItems(hearingTabItems, INTERVENER2);
        List<HearingTabCollectionItem> int3TabItems = filterHearingTabItems(hearingTabItems, INTERVENER3);
        List<HearingTabCollectionItem> int4TabItems = filterHearingTabItems(hearingTabItems, INTERVENER4);

        hearingsWrapper.setHearingTabItems(hearingTabItems);
        hearingsWrapper.setRespondentHearingTabItems(respondentTabItems);
        hearingsWrapper.setApplicantHearingTabItems(applicantTabItems);
        hearingsWrapper.setInt1HearingTabItems(int1TabItems);
        hearingsWrapper.setInt2HearingTabItems(int2TabItems);
        hearingsWrapper.setInt3HearingTabItems(int3TabItems);
        hearingsWrapper.setInt4HearingTabItems(int4TabItems);
    }

    private List<HearingTabCollectionItem> filterHearingTabItems(List<HearingTabCollectionItem> hearingTabItems, String party) {
        return hearingTabItems.stream()
            .filter(hearingTabItem -> hearingTabItem.getValue().getTabConfidentialParties().contains(party))
            .toList();
    }
}
