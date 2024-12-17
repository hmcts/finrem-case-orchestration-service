package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.FinremFormCandGCorresponder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COMPLIANCE_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addNonFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildHearingCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.isFastTrackApplication;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingDocumentService {

    protected static final String HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE = "This listing notice must be sent to the applicant and respondent"
        + " as default. If this listing needs to be sent to only one of these parties please use the general order event.";
    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final FinremFormCandGCorresponder finremFormCandGCorresponder;
    private final PfdNcdrDocumentService pfdNcdrDocumentService;

    public Map<String, CaseDocument> generateHearingDocuments(String authorisationToken, CaseDetails caseDetails) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        caseDetailsCopy = addHearingCourtFields(caseDetailsCopy);

        return Optional.of(Pair.of(caseDetailsCopy, authorisationToken))
            .filter(pair -> pair.getLeft().getData().get(FAST_TRACK_DECISION) != null)
            .map(this::courtCoverSheetDocuments)
            .orElseThrow(() -> new IllegalArgumentException("missing fastTrackDecision"));
    }

    private Map<String, CaseDocument> courtCoverSheetDocuments(Pair<CaseDetails, String> pair) {
        Map<String, CaseDocument> objectMap = Optional.of(pair)
            .filter(this::isFastTrackApplication)
            .map(this::generateFastTrackFormC)
            .orElseGet(() -> generateFormCAndG(pair));

        objectMap.put(OUT_OF_FAMILY_COURT_RESOLUTION, generatOutOfFamilyCourtResolutionDocument(pair));
        objectMap.putAll(generatePfdNcdrDocuments(pair));

        return objectMap;
    }

    private Map<String, CaseDocument> generateFormCAndG(Pair<CaseDetails, String> pair) {
        CaseDocument formCNonFastTrack =
            genericDocumentService.generateDocument(pair.getRight(), addNonFastTrackFields.apply(pair.getLeft()),
                documentConfiguration.getFormCNonFastTrackTemplate(pair.getLeft()), documentConfiguration.getFormCFileName());

        CaseDocument formG = genericDocumentService.generateDocument(pair.getRight(), pair.getLeft(),
            documentConfiguration.getFormGTemplate(pair.getLeft()), documentConfiguration.getFormGFileName());

        return createDocumentMap(formCNonFastTrack, formG);
    }

    private Map<String, CaseDocument> createDocumentMap(CaseDocument formC, CaseDocument formG) {
        Map<String, CaseDocument> documentMap = new HashMap<>();
        documentMap.put(FORM_C, formC);
        documentMap.put(FORM_G, formG);
        return documentMap;
    }

    private Map<String, CaseDocument> generateFastTrackFormC(Pair<CaseDetails, String> pair) {
        Map<String, CaseDocument> documentMap = new HashMap<>();
        documentMap.put(FORM_C,
            genericDocumentService.generateDocument(pair.getRight(), addFastTrackFields.apply(pair.getLeft()),
                documentConfiguration.getFormCFastTrackTemplate(pair.getLeft()), documentConfiguration.getFormCFileName()));
        return documentMap;
    }

    private CaseDocument generatOutOfFamilyCourtResolutionDocument(Pair<CaseDetails, String> pair) {
        return genericDocumentService.generateDocument(pair.getRight(), addFastTrackFields.apply(pair.getLeft()),
            documentConfiguration.getOutOfFamilyCourtResolutionTemplate(),
            documentConfiguration.getOutOfFamilyCourtResolutionName());
    }

    private Map<String, CaseDocument> generatePfdNcdrDocuments(Pair<CaseDetails, String> pair) {
        String caseId = pair.getLeft().getId().toString();
        String authToken = pair.getRight();

        Map<String, CaseDocument> documentMap = new HashMap<>();
        CaseDocument pfdNcdrComplianceLetter = pfdNcdrDocumentService.uploadPfdNcdrComplianceLetter(caseId, authToken);
        documentMap.put(PFD_NCDR_COMPLIANCE_LETTER, pfdNcdrComplianceLetter);

        // A cover letter is only required for non-digital respondents
        if (pfdNcdrDocumentService.isPdfNcdrCoverSheetRequired(pair.getLeft())) {
            CaseDocument pfdNcdrCoverLetter = pfdNcdrDocumentService.uploadPfdNcdrCoverLetter(caseId, authToken);
            documentMap.put(PFD_NCDR_COVER_LETTER, pfdNcdrCoverLetter);
        }

        return documentMap;
    }

    private boolean isFastTrackApplication(Pair<CaseDetails, String> pair) {
        return isFastTrackApplication.apply(pair.getLeft().getData());
    }

    CaseDetails addHearingCourtFields(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        data.put("courtDetails", buildHearingCourtDetails(data));
        return caseDetails;
    }

    @SuppressWarnings("java:S1874")
    void addCourtFields(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        data.put("courtDetails", buildFrcCourtDetails(data));
    }

    public void sendInitialHearingCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        finremFormCandGCorresponder.sendCorrespondence(caseDetails, authorisationToken);
    }

    /**
     * Checks for presence of Form C on case data.
     *
     * <p>It checks for form C only, because this form will be populated for
     * both non-fast track and fast track cases. Fast track cases will have
     * additionally form G populated.</p>
     */
    public boolean alreadyHadFirstHearing(FinremCaseDetails caseDetails) {
        return caseDetails.getData().getListForHearingWrapper().getFormC() != null;
    }
}
