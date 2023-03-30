package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FinremFormCandGCorresponder extends FinremHearingCorresponder {

    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    @Autowired
    public FinremFormCandGCorresponder(BulkPrintService bulkPrintService,
                                       NotificationService notificationService,
                                       DocumentHelper documentHelper, ObjectMapper objectMapper) {
        super(bulkPrintService, notificationService);
        this.documentHelper = documentHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(FinremCaseDetails caseDetails) {
        String caseId = caseDetails.getId() == null ? "noId" : caseDetails.getId().toString();
        return getHearingCaseDocuments(caseDetails.getData(), caseId);
    }

    private List<BulkPrintDocument> getHearingCaseDocuments(FinremCaseData caseData, String caseId) {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();

        log.info("Fetching Contested Paper Case bulk print document for caseId {}", caseId);
        Optional.ofNullable(caseData.getFormC()).ifPresent(formC -> caseDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(formC)));
        Optional.ofNullable(caseData.getFormG()).ifPresent(formG -> caseDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(formG)));
        Optional.ofNullable(caseData.getMiniFormA())
            .ifPresent(miniFormA -> caseDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(miniFormA)));
        Optional.ofNullable(caseData.getOutOfFamilyCourtResolution()).ifPresent(
            outOfFamilyCourtResolution -> caseDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(outOfFamilyCourtResolution)));
        Optional.ofNullable(caseData.getAdditionalListOfHearingDocuments())
            .ifPresent(hearingAdditionalDoc -> caseDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(hearingAdditionalDoc)));


        List<CaseDocument> formACaseDocuments = documentHelper.getFormADocumentsData(caseData);
        caseDocuments.addAll(formACaseDocuments.stream().map(documentHelper::getCaseDocumentAsBulkPrintDocument).collect(Collectors.toList()));

        return caseDocuments;
    }


}
