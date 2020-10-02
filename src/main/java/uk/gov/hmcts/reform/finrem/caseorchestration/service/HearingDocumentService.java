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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.addNonFastTrackFields;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.isFastTrackApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullApplicantName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullRespondentName;

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
        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> data = caseDetails.getData();
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourt(data)));
            data.put("courtDetails", buildFrcCourtDetails(courtDetails));
            return caseDetails;
        } catch (IOException | NullPointerException e) {
            return caseDetails;
        }
    }

    public void sendFormCAndGForBulkPrint(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> caseDocuments = getHearingCaseDocuments(caseDetails.getData());
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, caseDocuments);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, caseDocuments);
    }

    private List<BulkPrintDocument> getHearingCaseDocuments(Map<String, Object> caseData) {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();

        // Render Case Data with @JSONProperty names
        try {
            caseData = objectMapper.readValue(objectMapper.writeValueAsString(caseData), HashMap.class);
        } catch (JsonProcessingException e) {
            return caseDocuments;
        }

        log.info("Fetching Contested Paper Case bulk print document from Case Data: {}", caseData);

        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, FORM_C).ifPresent(caseDocuments::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, FORM_G).ifPresent(caseDocuments::add);

        List<CaseDocument> formACaseDocuments = documentHelper.getFormADocumentsData(caseData);
        caseDocuments.addAll(formACaseDocuments.stream().map(documentHelper::getCaseDocumentAsBulkPrintDocument).collect(Collectors.toList()));

        log.info("Sending Contested Paper Case bulk print documents: {}", caseDocuments);

        return caseDocuments;
    }

    public void createAndSendAdditionalHearingDocuments(String authorisationToken, CaseDetails caseDetails) throws IOException {
        CaseDocument document = generateAdditionalHearingDocument(caseDetails, authorisationToken);
        addAdditionalHearingDocumentToCaseData(caseDetails, document);
        bulkPrintAdditionalHearingDocuments(caseDetails, authorisationToken);
    }

    private CaseDocument generateAdditionalHearingDocument(CaseDetails caseDetails, String authorisationToken) throws IOException {
        log.info("Generating Additional Hearing Document for Case ID: {}", caseDetails.getId());
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        prepareCaseDetailsForDocumentGeneration(caseDetailsCopy);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getAdditionalHearingTemplate(), documentConfiguration.getAdditionalHearingFileName());
    }

    private void prepareCaseDetailsForDocumentGeneration(CaseDetails caseDetails) throws IOException {
        Map<String, Object> caseData = caseDetails.getData();
        Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
        Map<String, Object> courtDetails = (Map<String, Object>)
            courtDetailsMap.get(caseData.get(CaseHearingFunctions.getSelectedCourt(caseData)));

        FrcCourtDetails selectedFRCDetails = FrcCourtDetails.builder()
            .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
            .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
            .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
            .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
            .build();

        caseData.put("ccdCaseNumber", caseDetails.getId());
        caseData.put("DivorceCaseNumber", caseDetails.getData().get(DIVORCE_CASE_NUMBER));
        caseData.put("ApplicantName", buildFullApplicantName(caseDetails));
        caseData.put("RespondentName", buildFullRespondentName(caseDetails));
        caseData.put("HearingType", caseDetails.getData().get(HEARING_TYPE));
        caseData.put("HearingVenue", selectedFRCDetails.getCourtName());
        caseData.put("HearingDate", HEARING_DATE);
        caseData.put("HearingTime", HEARING_TIME);
        caseData.put("HearingLength", TIME_ESTIMATE);
        caseData.put("AnyOtherDirections", HEARING_ADDITIONAL_INFO);

        caseData.put("CourtName", selectedFRCDetails.getCourtName());
        caseData.put("CourtAddress", selectedFRCDetails.getCourtAddress());
        caseData.put("CourtPhone", selectedFRCDetails.getPhoneNumber());
        caseData.put("CourtEmail", selectedFRCDetails.getEmail());
    }

    private void addAdditionalHearingDocumentToCaseData(CaseDetails caseDetails, CaseDocument document) {
        AdditionalHearingDocumentData generatedDocumentData = AdditionalHearingDocumentData.builder()
            .additionalHearingDocument(AdditionalHearingDocument.builder()
                .additionalHearingDocument(document)
                .build())
            .build();

        Map<String, Object> caseData = caseDetails.getData();
        List<AdditionalHearingDocumentData> additionalHearingDocumentDataList =
            Optional.ofNullable(caseData.get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION))
                .map(documentHelper::convertToAdditionalHearingDocumentData)
                .orElse(new ArrayList<>(1));

        additionalHearingDocumentDataList.add(generatedDocumentData);

        caseData.put(ADDITIONAL_HEARING_DOCUMENT_COLLECTION, additionalHearingDocumentDataList);
    }

    private void bulkPrintAdditionalHearingDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<AdditionalHearingDocumentData> additionalHearingDocumentData =
            documentHelper.convertToAdditionalHearingDocumentData(
                caseDetails.getData().get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION));

        AdditionalHearingDocumentData additionalHearingDocument = additionalHearingDocumentData.get(additionalHearingDocumentData.size() - 1);

        List<BulkPrintDocument> document = Collections.singletonList(
            bulkPrintService.getBulkPrintDocumentFromCaseDocument(
                additionalHearingDocument.getAdditionalHearingDocument().getAdditionalHearingDocument()));

        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, document);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, document);
    }
}
