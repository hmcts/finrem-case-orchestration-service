package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@SuppressWarnings({"java:S6204", "java:S110"})
public class FinremFormCandGCorresponder extends FinremHearingCorresponder {

    @Autowired
    public FinremFormCandGCorresponder(BulkPrintService bulkPrintService,
                                       NotificationService notificationService,
                                       DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService, documentHelper);
    }

    @Override
    public List<CaseDocument> getCaseDocuments(FinremCaseDetails caseDetails) {
        return getHearingCaseDocuments(caseDetails.getData());
    }

    @Override
    protected void printRespondentDocuments(String authorisationToken, FinremCaseDetails caseDetails) {
        List<CaseDocument> caseDocuments = getCaseDocuments(caseDetails);
        Optional.ofNullable(caseDetails.getData().getListForHearingWrapper().getPfdNcdrCoverLetter())
            .ifPresent(caseDocuments::add);

        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken,
            documentHelper.getCaseDocumentsAsBulkPrintDocuments(caseDocuments, caseDetails.getCaseType(), authorisationToken));
    }

    private List<CaseDocument> getHearingCaseDocuments(FinremCaseData caseData) {
        List<CaseDocument> caseDocuments = new ArrayList<>();

        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        Optional.ofNullable(listForHearingWrapper.getFormC()).ifPresent(caseDocuments::add);
        Optional.ofNullable(listForHearingWrapper.getFormG()).ifPresent(caseDocuments::add);
        Optional.ofNullable(caseData.getMiniFormA())
            .ifPresent(caseDocuments::add);
        Optional.ofNullable(caseData.getOutOfFamilyCourtResolution()).ifPresent(
            caseDocuments::add);
        Optional.ofNullable(listForHearingWrapper.getAdditionalListOfHearingDocuments())
            .ifPresent(caseDocuments::add);
        Optional.ofNullable(listForHearingWrapper.getPfdNcdrComplianceLetter())
            .ifPresent(caseDocuments::add);

        List<CaseDocument> formACaseDocuments = documentHelper.getFormADocumentsData(caseData);
        caseDocuments.addAll(formACaseDocuments);

        return caseDocuments;
    }
}
