package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullApplicantName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullRespondentName;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdditionalHearingDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final BulkPrintService bulkPrintService;

    public void createAndSendAdditionalHearingDocuments(String authorisationToken, CaseDetails caseDetails) {
        CaseDocument document = generateAdditionalHearingDocument(caseDetails, authorisationToken);
        addAdditionalHearingDocumentToCaseData(caseDetails, document);
        bulkPrintAdditionalHearingDocuments(caseDetails, authorisationToken);
    }

    public CaseDocument generateAdditionalHearingDocument(CaseDetails caseDetails, String authorisationToken) {
        log.info("Generating Additional Hearing Document for Case ID: {}", caseDetails.getId());
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        prepareCaseDetailsForDocumentGeneration(caseDetailsCopy);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getAdditionalHearingTemplate(),
            documentConfiguration.getAdditionalHearingFileName());
    }

    private void prepareCaseDetailsForDocumentGeneration(CaseDetails caseDetails) {
        try {
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

            caseData.put("CCDCaseNumber", caseDetails.getId());
            caseData.put("DivorceCaseNumber", caseDetails.getData().get(DIVORCE_CASE_NUMBER));
            caseData.put("ApplicantName", buildFullApplicantName(caseDetails));
            caseData.put("RespondentName", buildFullRespondentName(caseDetails));
            caseData.put("HearingType", caseDetails.getData().get(HEARING_TYPE));
            caseData.put("HearingVenue", selectedFRCDetails.getCourtName());
            caseData.put("HearingDate", caseDetails.getData().get(HEARING_DATE));
            caseData.put("HearingTime", caseDetails.getData().get(HEARING_TIME));
            caseData.put("HearingLength", caseDetails.getData().get(TIME_ESTIMATE));
            caseData.put("AnyOtherDirections", caseDetails.getData().get(HEARING_ADDITIONAL_INFO));

            caseData.put("CourtName", selectedFRCDetails.getCourtName());
            caseData.put("CourtAddress", selectedFRCDetails.getCourtAddress());
            caseData.put("CourtPhone", selectedFRCDetails.getPhoneNumber());
            caseData.put("CourtEmail", selectedFRCDetails.getEmail());

        } catch (IOException | NullPointerException e) {
            log.info("Failed to load court details json");
        }
    }

    void addAdditionalHearingDocumentToCaseData(CaseDetails caseDetails, CaseDocument document) {
        AdditionalHearingDocumentData generatedDocumentData = AdditionalHearingDocumentData.builder()
            .additionalHearingDocument(AdditionalHearingDocument.builder()
                .document(document)
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
                additionalHearingDocument.getAdditionalHearingDocument().getDocument()));

        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, document);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, document);
    }
}

