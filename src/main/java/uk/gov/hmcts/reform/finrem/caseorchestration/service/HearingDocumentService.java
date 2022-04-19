package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addNonFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.isFastTrackApplication;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final BulkPrintService bulkPrintService;

    public Map<String, Object> generateHearingDocuments(String authorisationToken, CaseDetails caseDetails) {
        CaseDetails courtDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        courtDetailsCopy = addCourtFields(courtDetailsCopy);

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
        return ImmutableMap.of(FORM_C, formC, FORM_G, formG);
    }

    private Map<String, Object> generateFastTrackFormC(Pair<CaseDetails, String> pair) {
        return ImmutableMap.of(FORM_C,
            genericDocumentService.generateDocument(pair.getRight(), addFastTrackFields.apply(pair.getLeft()),
                documentConfiguration.getFormCFastTrackTemplate(), documentConfiguration.getFormCFileName()));
    }

    private boolean isFastTrackApplication(Pair<CaseDetails, String> pair) {
        return isFastTrackApplication.apply(pair.getLeft().getData());
    }

    CaseDetails addCourtFields(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        data.put("courtDetails", buildFrcCourtDetails(data));
        return caseDetails;
    }

    public void sendFormCAndGForBulkPrint(CaseDetails caseDetails, String authorisationToken) {
        String caseId = caseDetails.getId().toString().equals(null) ? "noId" : caseDetails.getId().toString();
        List<BulkPrintDocument> caseDocuments = getHearingCaseDocuments(caseDetails.getData(), caseId);
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, caseDocuments);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, caseDocuments);
    }

    /**
     * Checks for presence of Form C on case data.
     *
     * <p>It checks for form C only, because this form will be populated for
     * both non-fast track and fast track cases. Fast track cases will have
     * additionally form G populated.</p>
     */
    public boolean alreadyHadFirstHearing(CaseDetails caseDetails) {
        return caseDetails.getData().containsKey(FORM_C);
    }

    private List<BulkPrintDocument> getHearingCaseDocuments(Map<String, Object> caseData, String caseId) {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();

        // Render Case Data with @JSONProperty names
        try {
            caseData = objectMapper.readValue(objectMapper.writeValueAsString(caseData), HashMap.class);
        } catch (JsonProcessingException e) {
            return caseDocuments;
        }

        log.info("Fetching Contested Paper Case bulk print document for %s from Case Data: {}", caseId, caseData);

        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, FORM_C).ifPresent(caseDocuments::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, FORM_G).ifPresent(caseDocuments::add);

        List<CaseDocument> formACaseDocuments = documentHelper.getFormADocumentsData(caseData);
        log.info("Form A Case Documents for %s: {}", caseId, formACaseDocuments);
        caseDocuments.addAll(formACaseDocuments.stream().map(documentHelper::getCaseDocumentAsBulkPrintDocument).collect(Collectors.toList()));

        log.info("Sending Contested Paper Case bulk print documents: {}", caseDocuments);

        return caseDocuments;
    }
}
