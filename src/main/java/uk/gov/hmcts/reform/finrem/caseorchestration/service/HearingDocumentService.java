package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addNonFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.isFastTrackApplication;

@Service
@RequiredArgsConstructor
public class HearingDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    public Map<String, Object> generateHearingDocuments(String authorisationToken, CaseDetails caseDetails) {
        CaseDetails courtDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        if (featureToggleService.isContestedCourtDetailsMigrationEnabled()) {
            courtDetailsCopy = addCourtFields(courtDetailsCopy);
        }

        return Optional.of(Pair.of(courtDetailsCopy, authorisationToken))
            .filter(pair -> pair.getLeft().getData().get(FAST_TRACK_DECISION) != null)
            .map(this::courtCoverSheetDocuments)
            .orElseThrow(() -> new IllegalArgumentException("missing fastTrackDecision"));
    }

    private Map<String, Object> courtCoverSheetDocuments(Pair<CaseDetails, String> pair) {
        return Optional.of(pair)
            .filter(this::isFastTrackApplication)
            .map(this::generateFastTrackFormC)
            .orElseGet(() -> generateFormCAndG(pair));
    }

    private Map<String, Object> generateFormCAndG(Pair<CaseDetails, String> pair) {
        CompletableFuture<CaseDocument> formCNonFastTrack =
            supplyAsync(() -> genericDocumentService.generateDocument(pair.getRight(), addNonFastTrackFields.apply(pair.getLeft()),
                documentConfiguration.getFormCNonFastTrackTemplate(), documentConfiguration.getFormCFileName()));

        CompletableFuture<CaseDocument> formG = supplyAsync(() -> genericDocumentService.generateDocument(pair.getRight(), pair.getLeft(),
            documentConfiguration.getFormGTemplate(), documentConfiguration.getFormGFileName()));

        return formCNonFastTrack
            .thenCombine(formG, this::createDocumentMap).join();
    }

    private Map<String, Object> createDocumentMap(CaseDocument formC, CaseDocument formG) {
        return ImmutableMap.of("formC", formC, "formG", formG);
    }

    private Map<String, Object> generateFastTrackFormC(Pair<CaseDetails, String> pair) {
        return ImmutableMap.of("formC",
            genericDocumentService.generateDocument(pair.getRight(), addFastTrackFields.apply(pair.getLeft()),
                documentConfiguration.getFormCFastTrackTemplate(), documentConfiguration.getFormCFileName()));
    }

    private boolean isFastTrackApplication(Pair<CaseDetails, String> pair) {
        return isFastTrackApplication.apply(pair.getLeft().getData());
    }

    private CaseDetails addCourtFields(CaseDetails caseDetails) {
        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> data = caseDetails.getData();
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourt(data)));
            data.put("courtDetails", buildCourtDetails(courtDetails));
            return caseDetails;
        } catch (IOException | NullPointerException e) {
            return caseDetails;
        }
    }
}
