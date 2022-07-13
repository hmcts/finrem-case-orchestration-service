package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationinterim.GeneralApplicationLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationorder.GeneralApplicationOrderDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.GeneralApplicationWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
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

    public void startGeneralApplicationDirections(FinremCaseDetails caseDetails) {
        caseDetails.getCaseData().getRegionWrapper()
            .setGeneralApplicationRegionWrapper(null);
        GeneralApplicationWrapper generalApplicationData = caseDetails.getCaseData().getGeneralApplicationWrapper();


        generalApplicationData.setGeneralApplicationHearingRequired(null);
        generalApplicationData.setGeneralApplicationDirectionsHearingDate(null);
        generalApplicationData.setGeneralApplicationDirectionsHearingTime(null);
        generalApplicationData.setGeneralApplicationDirectionsHearingTimeEstimate(null);
        generalApplicationData.setGeneralApplicationDirectionsAdditionalInformation(null);
        generalApplicationData.setGeneralApplicationDirectionsCourtOrderDate(null);
        generalApplicationData.setGeneralApplicationDirectionsJudgeType(null);
        generalApplicationData.setGeneralApplicationDirectionsJudgeName(null);
        generalApplicationData.setGeneralApplicationDirectionsRecitals(null);
        generalApplicationData.setGeneralApplicationDirectionsTextFromJudge(null);
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
