package uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class SendOrderRespondentDocumentHandler extends SendOrderPartyDocumentHandler {
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final NotificationService notificationService;
    private final CaseDataService caseDataService;

    public SendOrderRespondentDocumentHandler(ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                              NotificationService notificationService,
                                              CaseDataService caseDataService) {

        super(CaseRole.RESP_SOLICITOR.getCcdCode());
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.notificationService = notificationService;
        this.caseDataService = caseDataService;
    }

    @Override
    protected List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getRespOrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected List<UnapprovedOrderCollection> getUnapprovedOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getRespRefusedOrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected CaseDocument getPartyCoverSheet(FinremCaseDetails caseDetails, String authToken) {
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.PaperNotificationRecipient.RESPONDENT;
        return consentOrderApprovedDocumentService.getPopulatedConsentCoverSheet(caseDetails, authToken, recipient);
    }

    @Override
    protected void addCoverSheetToPartyField(FinremCaseDetails caseDetails, CaseDocument bulkPrintSheet) {
        FinremCaseData caseData = caseDetails.getData();
        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
            if (caseDataService.isRespondentAddressConfidential(caseData)) {
                setConfidentialBulkPrintFieldForRespondent(caseDetails, bulkPrintSheet, caseData);
            } else {
                caseData.setBulkPrintCoverSheetApp(bulkPrintSheet);
            }
        }
    }

    @Override
    protected void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl) {
        caseData.setRespOrderCollection(orderColl);
    }

    @Override
    protected void addUnapprovedOrdersToPartyCollection(FinremCaseData caseData, List<UnapprovedOrderCollection> orderColl) {
        caseData.setRespRefusedOrderCollection(orderColl);
    }

    private static void setConfidentialBulkPrintFieldForRespondent(FinremCaseDetails finremCaseDetails,
                                                                   CaseDocument bulkPrintSheet,
                                                                   FinremCaseData caseData) {
        log.info("Case {}, has been marked as confidential. Adding respondent cover sheet to confidential field",
            finremCaseDetails.getId());
        caseData.setBulkPrintCoverSheetRes(null);
        caseData.setBulkPrintCoverSheetResConfidential(bulkPrintSheet);
    }
}
