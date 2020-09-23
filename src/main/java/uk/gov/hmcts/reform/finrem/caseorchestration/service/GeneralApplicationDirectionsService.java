package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CFC_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_RECITALS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_FRC;

@Service
@RequiredArgsConstructor
public class GeneralApplicationDirectionsService {

    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;

    public void startGeneralApplicationDirections(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_WALES_FRC);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_CFC_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_RECITALS);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE);
    }

    public void submitGeneralApplicationDirections(CaseDetails caseDetails, String authorisationToken) {
        if (caseDetails.getData().get(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED).equals(YES_VALUE)) {
            printHearingRequiredNoticePack(caseDetails, authorisationToken);
        } else {
            printGeneralApplicationDirectionsOrder(caseDetails, authorisationToken);
        }
    }

    private void printGeneralApplicationDirectionsOrder(CaseDetails caseDetails, String authorisationToken) {
        BulkPrintDocument generalApplicationOrder = documentHelper.getCaseDocumentAsBulkPrintDocument(
            genericDocumentService.generateDocument(authorisationToken, caseDetails,
                documentConfiguration.getGeneralApplicationHearingNoticeTemplate(),
                documentConfiguration.getGeneralApplicationHearingNoticeFileName()));

        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, asList(generalApplicationOrder));
    }

    private void printHearingRequiredNoticePack(CaseDetails caseDetails, String authorisationToken) {
        BulkPrintDocument hearingRequiredNotice = documentHelper.getCaseDocumentAsBulkPrintDocument(
            genericDocumentService.generateDocument(authorisationToken, caseDetails,
                documentConfiguration.getGeneralApplicationHearingNoticeTemplate(),
                documentConfiguration.getGeneralApplicationHearingNoticeFileName()));

        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, asList(hearingRequiredNotice));
    }
}
