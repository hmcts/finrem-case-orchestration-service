package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;

@Component
@RequiredArgsConstructor
@Slf4j
public class FormCandGDocumentsLetterHandler {

    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public void sendFormCAndGForBulkPrint(CaseDetails caseDetails, String authorisationToken) {
        String caseId = caseDetails.getId() == null ? "noId" : caseDetails.getId().toString();
        List<BulkPrintDocument> caseDocuments = getHearingCaseDocuments(caseDetails.getData(), caseId);

        if (!notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, caseDocuments);
        }
        if (!notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, caseDocuments);
        }
    }


    private List<BulkPrintDocument> getHearingCaseDocuments(Map<String, Object> caseData, String caseId) {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();

        // Render Case Data with @JSONProperty names
        try {
            caseData = objectMapper.readValue(objectMapper.writeValueAsString(caseData), HashMap.class);
        } catch (JsonProcessingException e) {
            return caseDocuments;
        }

        log.info("Fetching Contested Paper Case bulk print document for caseId {}", caseId);

        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, FORM_C).ifPresent(caseDocuments::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, FORM_G).ifPresent(caseDocuments::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, MINI_FORM_A).ifPresent(caseDocuments::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, OUT_OF_FAMILY_COURT_RESOLUTION).ifPresent(caseDocuments::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, HEARING_ADDITIONAL_DOC).ifPresent(caseDocuments::add);

        List<CaseDocument> formACaseDocuments = documentHelper.getFormADocumentsData(caseData);
        caseDocuments.addAll(formACaseDocuments.stream().map(documentHelper::getCaseDocumentAsBulkPrintDocument).collect(Collectors.toList()));

        return caseDocuments;
    }
}
