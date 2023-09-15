package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
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
        String caseId = caseDetails.getId() == null ? "noId" : caseDetails.getId().toString();
        return getHearingCaseDocuments(caseDetails.getData(), caseId);
    }

    private List<CaseDocument> getHearingCaseDocuments(FinremCaseData caseData, String caseId) {
        List<CaseDocument> caseDocuments = new ArrayList<>();

        log.info("Fetching Contested Paper Case bulk print document for caseId {}", caseId);
        Optional.ofNullable(caseData.getFormC()).ifPresent(formC -> caseDocuments.add(formC));
        Optional.ofNullable(caseData.getFormG()).ifPresent(formG -> caseDocuments.add(formG));
        Optional.ofNullable(caseData.getMiniFormA())
            .ifPresent(miniFormA -> caseDocuments.add(miniFormA));
        Optional.ofNullable(caseData.getOutOfFamilyCourtResolution()).ifPresent(
            outOfFamilyCourtResolution -> caseDocuments.add(outOfFamilyCourtResolution));
        Optional.ofNullable(caseData.getAdditionalListOfHearingDocuments())
            .ifPresent(hearingAdditionalDoc -> caseDocuments.add(hearingAdditionalDoc));


        List<CaseDocument> formACaseDocuments = documentHelper.getFormADocumentsData(caseData);
        caseDocuments.addAll(formACaseDocuments);

        return caseDocuments;
    }


}
