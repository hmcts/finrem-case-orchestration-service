package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationinterim.GeneralApplicationLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationorder.GeneralApplicationOrderDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BEDFORDSHIRE_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BRISTOL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CFC_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DEVON_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DORSET_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LANCASHIRE_COURT;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_THAMESVALLEY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_OTHER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationDirectionsService {

    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final GeneralApplicationLetterDetailsMapper generalApplicationLetterDetailsMapper;
    private final GeneralApplicationOrderDetailsMapper generalApplicationOrderDetailsMapper;

    public void startGeneralApplicationDirections(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        Stream.of(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION,
            GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC,
            GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_WALES_FRC,
            GENERAL_APPLICATION_DIRECTIONS_BEDFORDSHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT,
            GENERAL_APPLICATION_DIRECTIONS_BRISTOL_COURT,
            GENERAL_APPLICATION_DIRECTIONS_CFC_COURT,
            GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT,
            GENERAL_APPLICATION_DIRECTIONS_DEVON_COURT,
            GENERAL_APPLICATION_DIRECTIONS_DORSET_COURT,
            GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT,
            GENERAL_APPLICATION_DIRECTIONS_LANCASHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT,
            GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT,
            GENERAL_APPLICATION_DIRECTIONS_THAMESVALLEY_COURT,
            GENERAL_APPLICATION_DIRECTIONS_WALES_OTHER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION,
            GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE,
            GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE,
            GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME,
            GENERAL_APPLICATION_DIRECTIONS_RECITALS,
            GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE
        ).forEach(caseData::remove);
    }

    public void submitInterimHearing(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> documents = prepareInterimHearingDocumentsToPrint(caseDetails, authorisationToken);
        printInterimDocumentPackAndSendToApplicantAndRespondent(caseDetails, authorisationToken, documents);
    }

    private void printInterimDocumentPackAndSendToApplicantAndRespondent(FinremCaseDetails caseDetails, String authorisationToken,
                                                                         List<BulkPrintDocument> documents) {
        FinremCaseData caseData = caseDetails.getCaseData();
        if (caseData.isPaperCase() || !caseData.isApplicantSolicitorAgreeToReceiveEmails()) {
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
        }
        if (caseData.isPaperCase() || !isRespondentSolicitorAgreeToReceiveEmails(caseData)) {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
        }
    }

    private List<BulkPrintDocument> prepareInterimHearingDocumentsToPrint(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<BulkPrintDocument> documents = new ArrayList<>();
        Document interimDocument = prepareInterimHearingRequiredNoticeDocument(caseDetails, authorisationToken);
        documents.add(documentHelper.getDocumentAsBulkPrintDocument(interimDocument).orElse(null));

        if (!isNull(caseData.getInterimWrapper().getInterimUploadAdditionalDocument())) {
            log.warn("Additional uploaded interim document found for printing for case");
            Document caseDocument = caseData.getInterimWrapper().getInterimUploadAdditionalDocument();
            Document additionalUploadedDocuments = genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument, authorisationToken);
            documents.add(documentHelper.getDocumentAsBulkPrintDocument(additionalUploadedDocuments).orElse(null));
            caseData.getInterimWrapper().setInterimUploadAdditionalDocument(additionalUploadedDocuments);
        }

        caseData.getInterimWrapper().setInterimHearingDirectionsDocument(interimDocument);
        return documents;
    }

    private Document prepareInterimHearingRequiredNoticeDocument(FinremCaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> interimLetterDetailsMap = generalApplicationLetterDetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails, caseDetails.getCaseData().getRegionWrapper().getInterimCourtList());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, interimLetterDetailsMap,
            documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate(),
            documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName());
    }

    public void submitGeneralApplicationDirections(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> documents = prepareDocumentsToPrint(caseDetails, authorisationToken);
        printDocumentPackAndSendToApplicantAndRespondent(caseDetails, authorisationToken, documents);
        resetStateToGeneralApplicationPrestate(caseDetails);
    }

    private List<BulkPrintDocument> prepareDocumentsToPrint(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<BulkPrintDocument> documents = new ArrayList<>();
        Document directionsDocument =
            caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsHearingRequired().isYes()
                ? prepareHearingRequiredNoticeDocument(caseDetails, authorisationToken)
                : prepareGeneralApplicationDirectionsOrderDocument(caseDetails, authorisationToken);

        documents.add(documentHelper.getDocumentAsBulkPrintDocument(directionsDocument).orElse(null));
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsDocument(directionsDocument);

        Stream.of(caseData.getGeneralApplicationWrapper().getGeneralApplicationLatestDocument(),
            caseData.getGeneralApplicationWrapper().getGeneralApplicationDraftOrder()).forEach(
                document -> documents.add(documentHelper.getDocumentAsBulkPrintDocument(document).orElse(null)));

        return documents;
    }

    private void printDocumentPackAndSendToApplicantAndRespondent(FinremCaseDetails caseDetails, String authorisationToken,
                                                                  List<BulkPrintDocument> documents) {
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
    }

    private void resetStateToGeneralApplicationPrestate(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        String generalApplicationPreState = caseData.getGeneralApplicationWrapper().getGeneralApplicationPreState();
        if (generalApplicationPreState != null) {
            caseData.setState(generalApplicationPreState);
        }
    }

    private Document prepareGeneralApplicationDirectionsOrderDocument(FinremCaseDetails caseDetails,
                                                                          String authorisationToken) {
        Map<String, Object> orderDetailsMap = generalApplicationOrderDetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails, caseDetails.getCaseData().getRegionWrapper()
                .getDefaultCourtList());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            orderDetailsMap,
            documentConfiguration.getGeneralApplicationOrderTemplate(),
            documentConfiguration.getGeneralApplicationOrderFileName());
    }

    private Document prepareHearingRequiredNoticeDocument(FinremCaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> letterDetailsMap = generalApplicationLetterDetailsMapper.getDocumentTemplateDetailsAsMap(
            caseDetails, caseDetails.getCaseData().getRegionWrapper().getGeneralApplicationCourtList());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, letterDetailsMap,
            documentConfiguration.getGeneralApplicationHearingNoticeTemplate(),
            documentConfiguration.getGeneralApplicationHearingNoticeFileName());
    }

    private boolean isRespondentSolicitorAgreeToReceiveEmails(FinremCaseData caseData) {
        return !caseData.isPaperCase()
            && caseData.isRespondentRepresentedByASolicitor()
            && StringUtils.isNotBlank(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())
            && caseData.getRespSolNotificationsEmailConsent() != null
            && caseData.getRespSolNotificationsEmailConsent().isYes();
    }

    public boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }
}
