package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formc.FormCLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formg.FormGLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;
    private final FormCLetterDetailsMapper formCLetterDetailsMapper;
    private final FormGLetterDetailsMapper formGLetterDetailsMapper;
    private final NotificationService notificationService;


    public Map<String, Object> generateHearingDocuments(String authorisationToken, FinremCaseDetails caseDetails) {
        return Optional.of(Pair.of(caseDetails, authorisationToken))
            .filter(pair -> pair.getLeft().getCaseData().getFastTrackDecision() != null)
            .map(this::courtCoverSheetDocuments)
            .orElseThrow(() -> new IllegalArgumentException("missing fastTrackDecision"));
    }

    private Map<String, Object> courtCoverSheetDocuments(Pair<FinremCaseDetails, String> pair) {
        Map<String, Object> documentMap =  Optional.of(pair)
            .filter(detailsPair -> pair.getLeft().getCaseData().isFastTrackApplication())
            .map(this::generateFastTrackFormC)
            .orElseGet(() -> generateFormCAndG(pair));
        documentMap.put(OUT_OF_FAMILY_COURT_RESOLUTION, generateOutOfFamilyCourtResolutionDocument(pair));
        return documentMap;
    }

    private Map<String, Object> generateFormCAndG(Pair<FinremCaseDetails, String> pair) {

        Map<String, Object> formCDetailsMap = formCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(pair.getLeft(),
            pair.getLeft().getCaseData().getRegionWrapper().getDefaultCourtList());

        CompletableFuture<Document> formCNonFastTrack = supplyAsync(() ->
            genericDocumentService.generateDocumentFromPlaceholdersMap(pair.getRight(), formCDetailsMap,
                documentConfiguration.getFormCNonFastTrackTemplate(), documentConfiguration.getFormCFileName()));

        Map<String, Object> formGDetailsMap = formGLetterDetailsMapper.getDocumentTemplateDetailsAsMap(pair.getLeft(),
            pair.getLeft().getCaseData().getRegionWrapper().getDefaultCourtList());

        CompletableFuture<Document> formG = supplyAsync(() -> genericDocumentService.generateDocumentFromPlaceholdersMap(
            pair.getRight(), formGDetailsMap,
            documentConfiguration.getFormGTemplate(), documentConfiguration.getFormGFileName()));

        return formCNonFastTrack
            .thenCombine(formG, this::createDocumentMap).join();
    }

    private Map<String, Object> createDocumentMap(Document formC, Document formG) {
        return Maps.newHashMap(Map.of(FORM_C, formC, FORM_G, formG));
    }

    private Map<String, Object> generateFastTrackFormC(Pair<FinremCaseDetails, String> pair) {
        Map<String, Object> formCDetailsMap = formCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(pair.getLeft(),
            pair.getLeft().getCaseData().getRegionWrapper().getDefaultCourtList());

        return Maps.newHashMap(Map.of(FORM_C,
            genericDocumentService.generateDocumentFromPlaceholdersMap(pair.getRight(), formCDetailsMap,
                documentConfiguration.getFormCFastTrackTemplate(), documentConfiguration.getFormCFileName())));
    }

    private Document generateOutOfFamilyCourtResolutionDocument(Pair<FinremCaseDetails, String> pair) {
        return genericDocumentService.generateDocumentFromPlaceholdersMap(pair.getRight(), new HashMap<>(),
            documentConfiguration.getOutOfFamilyCourtResolutionTemplate(),
            documentConfiguration.getOutOfFamilyCourtResolutionName());
    }

    public void sendFormCAndGForBulkPrint(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> caseDocuments = getHearingCaseDocuments(caseDetails.getCaseData());

        if (!notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, caseDocuments);
        }
        if (!notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, caseDocuments);
        }
    }

    /**
     * Checks for presence of Form C on case data.
     *
     * <p>It checks for form C only, because this form will be populated for
     * both non-fast track and fast track cases. Fast track cases will have
     * additionally form G populated.</p>
     */
    @Deprecated
    public boolean alreadyHadFirstHearing(CaseDetails caseDetails) {
        return caseDetails.getData().containsKey(FORM_C);
    }

    public boolean alreadyHadFirstHearing(FinremCaseDetails caseDetails) {
        return Optional.ofNullable(caseDetails.getCaseData().getFormC()).isPresent();
    }

    private List<BulkPrintDocument> getHearingCaseDocuments(FinremCaseData caseData) {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();

        log.info("Fetching Contested Paper Case bulk print document from Case Data: {}", caseData);

        documentHelper.getDocumentAsBulkPrintDocument(caseData.getFormC()).ifPresent(caseDocuments::add);
        documentHelper.getDocumentAsBulkPrintDocument(caseData.getFormG()).ifPresent(caseDocuments::add);
        documentHelper.getDocumentAsBulkPrintDocument(caseData.getMiniFormA()).ifPresent(caseDocuments::add);
        documentHelper.getDocumentAsBulkPrintDocument(caseData.getOutOfFamilyCourtResolution()).ifPresent(caseDocuments::add);

        List<Document> formADocuments = Optional.ofNullable(caseData.getCopyOfPaperFormA().stream()
            .map(collectionElement -> collectionElement.getValue().getUploadedDocument()).toList())
            .orElse(new ArrayList<>());

        caseDocuments.addAll(formADocuments.stream()
            .map(documentHelper::getDocumentAsBulkPrintDocument)
            .flatMap(Optional::stream)
            .toList());

        log.info("Sending Contested Paper Case bulk print documents for {} from Case Data: {}",
            caseData.getCcdCaseId(), caseData);

        return caseDocuments;
    }
}
