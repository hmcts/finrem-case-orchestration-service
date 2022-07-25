package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.approvedorderhearing.ApprovedOrderNoticeOfHearingDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.DocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovedOrderNoticeOfHearingService {

    private final DocumentHelper documentHelper;
    private final GenericDocumentService genericDocumentService;
    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    private final CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;
    private final NotificationService notificationService;
    private final ApprovedOrderNoticeOfHearingDetailsMapper approvedOrderNoticeOfHearingDetailsMapper;

    public void createAndStoreHearingNoticeDocumentPack(FinremCaseDetails caseDetails,
                                                        String authToken) {
        List<Document> hearingNoticesToAdd = new ArrayList<>();

        Document noticeOfHearingDocument = prepareHearingRequiredNoticeDocumentComplexType(caseDetails, authToken);
        hearingNoticesToAdd.add(noticeOfHearingDocument);
        List<Document> hearingNoticeDocuments = addHearingNoticeToHearingDocumentCollection(noticeOfHearingDocument, caseDetails);
        hearingNoticeDocuments.forEach(document ->
            additionalHearingDocumentService.addAdditionalHearingDocumentToCaseData(caseDetails, document));

        Optional.ofNullable(caseDetails.getCaseData().getLatestDraftHearingOrder())
            .ifPresent(hearingNoticesToAdd::add);
        List<DocumentCollection> hearingNoticePack = Optional.ofNullable(caseDetails.getCaseData().getHearingNoticeDocumentPack())
            .orElse(new ArrayList<>());

        hearingNoticePack.addAll(convertHearingNoticeDocumentPackToCcdCollection(hearingNoticesToAdd));
        caseDetails.getCaseData().setHearingNoticeDocumentPack(hearingNoticePack);
    }

    public void printHearingNoticePackAndSendToApplicantAndRespondent(FinremCaseDetails caseDetails,
                                                                      String authorisationToken) {
        List<Document> hearingNoticePack = getHearingNoticePack(caseDetails);
        List<BulkPrintDocument> documentsToPrint = documentHelper.getDocumentsAsBulkPrintDocuments(hearingNoticePack);

        notifyApplicant(caseDetails, authorisationToken, documentsToPrint);
        notifyRespondent(caseDetails, authorisationToken, documentsToPrint);
    }

    private List<Document> getHearingNoticePack(FinremCaseDetails caseDetails) {
        return Optional.ofNullable(caseDetails.getCaseData().getHearingNoticeDocumentPack()).orElse(new ArrayList<>())
            .stream()
            .map(DocumentCollection::getValue)
            .collect(Collectors.toList());
    }

    private void notifyApplicant(FinremCaseDetails caseDetails, String authorisationToken, List<BulkPrintDocument> documentsToPrint) {
        if (checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)
            && caseDetails.getCaseData().isApplicantSolicitorAgreeToReceiveEmails()) {
            notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
            return;
        }
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documentsToPrint);
    }

    private void notifyRespondent(FinremCaseDetails caseDetails, String authorisationToken, List<BulkPrintDocument> documentsToPrint) {
        if (checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)
            && caseDetails.getCaseData().isRespondentSolicitorAgreeToReceiveEmails()) {
            notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
        } else {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documentsToPrint);
        }
    }

    private Document prepareHearingRequiredNoticeDocumentComplexType(FinremCaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> mapOfLetterDetails = approvedOrderNoticeOfHearingDetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails, caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, mapOfLetterDetails,
            documentConfiguration.getAdditionalHearingTemplate(),
            documentConfiguration.getAdditionalHearingFileName());
    }

    private List<Document> addHearingNoticeToHearingDocumentCollection(Document noticeOfHearingDocument,
                                                                           FinremCaseDetails caseDetails) {
        List<Document> hearingNoticeDocuments =
            Optional.ofNullable(caseDetails.getCaseData().getHearingNoticesDocumentCollection()).orElse(new ArrayList<>())
                .stream()
                .map(DocumentCollection::getValue)
                .collect(Collectors.toList());

        hearingNoticeDocuments.add(noticeOfHearingDocument);
        return hearingNoticeDocuments;
    }

    private List<DocumentCollection> convertHearingNoticeDocumentPackToCcdCollection(List<Document> hearingNoticePack) {
        return hearingNoticePack.stream()
            .map(document -> DocumentCollection.builder().value(document).build())
            .collect(Collectors.toList());
    }
}
