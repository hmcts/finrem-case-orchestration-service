package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT_PACK;

@Component
@RequiredArgsConstructor
@Slf4j
public class HearingNoticeEmailAndLetterHandler {

    private final ObjectMapper objectMapper;
    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;
    private final CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;
    private final NotificationService notificationService;
    private final CaseDataService caseDataService;


    public void printHearingNoticePackAndSendToApplicantAndRespondent(CaseDetails caseDetails,
                                                                      String authorisationToken) {
        List<CaseDocument> hearingNoticePack = getHearingNoticeDocumentPackFromCaseData(caseDetails);
        List<BulkPrintDocument> documentsToPrint = documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingNoticePack);

        notifyApplicant(caseDetails, authorisationToken, documentsToPrint);
        notifyRespondent(caseDetails, authorisationToken, documentsToPrint);
    }

    private void notifyApplicant(CaseDetails caseDetails, String authorisationToken, List<BulkPrintDocument> documentsToPrint) {
        if (checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())
            && caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {

            notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
            return;
        }
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documentsToPrint);
    }

    private void notifyRespondent(CaseDetails caseDetails, String authorisationToken, List<BulkPrintDocument> documentsToPrint) {
        if (checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())
            && caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseDetails)) {
            notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
        } else {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documentsToPrint);
        }
    }


    private List<CaseDocument> getHearingNoticeDocumentPackFromCaseData(CaseDetails caseDetails) {
        List<Element<CaseDocument>> hearingNoticePack = objectMapper.convertValue(
            caseDetails.getData().get(HEARING_NOTICE_DOCUMENT_PACK),
            new TypeReference<>() {
            });

        return hearingNoticePack.stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
    }

}
