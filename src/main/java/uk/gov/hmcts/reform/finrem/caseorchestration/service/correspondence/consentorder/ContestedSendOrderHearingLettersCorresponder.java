package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsMultiLetterOnlyAllPartiesCorresponder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

@Component
public class ContestedSendOrderHearingLettersCorresponder extends CaseDetailsMultiLetterOnlyAllPartiesCorresponder {

    private final DocumentHelper documentHelper;

    @Autowired
    public ContestedSendOrderHearingLettersCorresponder(BulkPrintService bulkPrintService,
                                                        NotificationService notificationService,
                                                        FinremCaseDetailsMapper finremCaseDetailsMapper, DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService, finremCaseDetailsMapper);
        this.documentHelper = documentHelper;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails, String authorisationToken) {
        return createHearingDocumentPack(caseDetails.getData(), authorisationToken, caseDetails.getId().toString());
    }

    private List<BulkPrintDocument> createHearingDocumentPack(Map<String, Object> caseData, String authorisationToken, String caseId) {
        List<BulkPrintDocument> hearingDocumentPack = new ArrayList<>();

        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, CONTESTED_ORDER_APPROVED_COVER_LETTER).ifPresent(hearingDocumentPack::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, LATEST_DRAFT_HEARING_ORDER).ifPresent(hearingDocumentPack::add);

        if (documentHelper.hasAnotherHearing(caseData)) {
            Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
            latestAdditionalHearingDocument.ifPresent(
                caseDocument -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument)));
        }

        List<BulkPrintDocument> otherHearingDocuments = documentHelper.getHearingDocumentsAsBulkPrintDocuments(
            caseData, authorisationToken, caseId);

        if (otherHearingDocuments != null) {
            hearingDocumentPack.addAll(otherHearingDocuments);
        }

        return hearingDocumentPack;
    }
}
