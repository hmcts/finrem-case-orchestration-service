package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalapplications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsMultiLetterOnlyAllPartiesCorresponder;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class GeneralApplicationsDetailsCorresponder extends CaseDetailsMultiLetterOnlyAllPartiesCorresponder {

    private final GeneralApplicationHelper generalApplicationHelper;

    @Autowired
    public GeneralApplicationsDetailsCorresponder(BulkPrintService bulkPrintService,
                                                  NotificationService notificationService,
                                                  FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  GeneralApplicationHelper generalApplicationHelper) {
        super(bulkPrintService, notificationService, finremCaseDetailsMapper);
        this.generalApplicationHelper = generalApplicationHelper;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails, String authorisationToken) {
        log.info("GeneralApplicationsCorresponder.getDocumentsToPrint for case {}", caseDetails.getId());

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        generalApplicationHelper.getGeneralApplicationList(caseDetails.getData()).stream()
            .sorted(generalApplicationHelper::getCompareTo)
            .limit(1)
            .findFirst()
            .ifPresent(
                (generalApplicationCollectionData) -> {
                    log.info("Found general application for case {}", caseDetails.getId());
                    GeneralApplicationItems generalApplicationItems = generalApplicationCollectionData.getGeneralApplicationItems();
                    final BulkPrintDocument genAppDirectionsDoc = BulkPrintDocument.builder()
                        .binaryFileUrl(generalApplicationItems.getGeneralApplicationDirectionsDocument().getDocumentBinaryUrl())
                        .fileName(generalApplicationItems.getGeneralApplicationDirectionsDocument().getDocumentFilename())
                        .build();
                    bulkPrintDocuments.add(genAppDirectionsDoc);
                    log.info("Got generalApplicationDocumentDirections {}, for caseId {}", genAppDirectionsDoc,
                        caseDetails.getId());

                    if (generalApplicationItems.getGeneralApplicationDocument() != null) {
                        final BulkPrintDocument genAppDoc = BulkPrintDocument.builder()
                            .binaryFileUrl(generalApplicationItems.getGeneralApplicationDocument().getDocumentBinaryUrl())
                            .fileName(generalApplicationItems.getGeneralApplicationDocument().getDocumentFilename())
                            .build();
                        log.info("Got generalApplicationDocument {} for caseId {}",
                            generalApplicationItems.getGeneralApplicationDocument(), caseDetails.getId());
                        bulkPrintDocuments.add(genAppDoc);
                    }
                    if (generalApplicationItems.getGeneralApplicationDraftOrder() != null) {
                        final BulkPrintDocument draftDoc = BulkPrintDocument.builder()
                            .binaryFileUrl(generalApplicationItems.getGeneralApplicationDraftOrder().getDocumentBinaryUrl())
                            .fileName(generalApplicationItems.getGeneralApplicationDraftOrder().getDocumentFilename())
                            .build();
                        bulkPrintDocuments.add(draftDoc);
                    }
                });
        return bulkPrintDocuments;
    }

    @Override
    protected boolean shouldSendApplicantSolicitorEmail(CaseDetails caseDetails) {
        return false;
    }

    @Override
    protected boolean shouldSendRespondentSolicitorEmail(CaseDetails caseDetails) {
        return false;
    }

    @Override
    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        return false;
    }
}
