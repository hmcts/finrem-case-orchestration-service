package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.FinremFormCandGCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.FormCandGCorresponder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addNonFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;
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
    private final FormCandGCorresponder formCandGCorresponder;

    private final FinremFormCandGCorresponder finremFormCandGCorresponder;

    private final SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;

    private final AdditionalHearingDocumentService additionalHearingDocumentService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public Map<String, CaseDocument> generateHearingDocuments(String authorisationToken, CaseDetails caseDetails) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        caseDetailsCopy = addCourtFields(caseDetailsCopy);

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

    private boolean isFastTrackApplication(Pair<CaseDetails, String> pair) {
        return isFastTrackApplication.apply(pair.getLeft().getData());
    }

    @SuppressWarnings("java:S1874")
    CaseDetails addCourtFields(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        data.put("courtDetails", buildFrcCourtDetails(data));
        return caseDetails;
    }

    /**
     * No Return.
     * <p>Please use @{@link #sendInitialHearingCorrespondence(FinremCaseDetails, String)}</p>
     *
     * @param caseDetails        instance of CaseDetails
     * @param authorisationToken instance of String
     * @deprecated Use {@link CaseDetails caseDetails, String authorisationToken}
     */
    @Deprecated(since = "15-june-2023")
    @SuppressWarnings("java:S1133")
    public void sendInitialHearingCorrespondence(CaseDetails caseDetails, String authorisationToken) {
        formCandGCorresponder.sendCorrespondence(caseDetails, authorisationToken);
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
        return caseDetails.getData().getFormC() != null;
    }

    public List<String> sendListForHearingCorrespondence(CaseDetails caseDetails, CaseDetails caseDetailsBefore, String authorisationToken) {

        List<String> errors = new ArrayList<>();
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        errors.addAll(selectablePartiesCorrespondenceService.validateApplicantAndRespondentCorrespondenceAreSelected(finremCaseDetails.getData(),
            HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE));
        if (!errors.isEmpty()) {
            return errors;
        }
        FinremCaseDetails finremCaseDetailsBefore = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetailsBefore);
        if (finremCaseDetailsBefore != null && finremCaseDetailsBefore.getData().getFormC() != null) {
            log.info("Sending Additional Hearing Document to bulk print for Contested Case ID: {}", finremCaseDetails.getId());
            additionalHearingDocumentService.sendAdditionalHearingDocuments(authorisationToken, finremCaseDetails);
            log.info("Sent Additional Hearing Document to bulk print for Contested Case ID: {}", finremCaseDetails.getId());
        } else {
            log.info("Sending Forms A, C, G to bulk print for Contested Case ID: {}", finremCaseDetails.getId());
            this.sendInitialHearingCorrespondence(finremCaseDetails, authorisationToken);
            log.info("sent Forms A, C, G to bulk print for Contested Case ID: {}", finremCaseDetails.getId());
        }
        return errors;
    }


}
