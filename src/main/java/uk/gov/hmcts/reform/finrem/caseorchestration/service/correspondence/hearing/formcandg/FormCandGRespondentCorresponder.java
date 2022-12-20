package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.formcandg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailRespondentCorresponder;

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
@Slf4j
public class FormCandGRespondentCorresponder extends MultiLetterOrEmailRespondentCorresponder {

    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    @Autowired
    public FormCandGRespondentCorresponder(NotificationService notificationService,
                                           BulkPrintService bulkPrintService,
                                           DocumentHelper documentHelper, ObjectMapper objectMapper) {
        super(notificationService, bulkPrintService);
        this.documentHelper = documentHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void emailSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails) {
        String caseId = caseDetails.getId() == null ? "noId" : caseDetails.getId().toString();
        return getHearingCaseDocuments(caseDetails.getData(), caseId);
    }

    private List<BulkPrintDocument> getHearingCaseDocuments(Map<String, Object> caseData, String caseId) {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();
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