package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.approvedordernotice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailRespondentCorresponder;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT_PACK;

@Component
@Slf4j
public class ApprovedOrderNoticeOfHearingRespondentCorresponder extends MultiLetterOrEmailRespondentCorresponder {


    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    @Autowired
    public ApprovedOrderNoticeOfHearingRespondentCorresponder(NotificationService notificationService,
                                                              BulkPrintService bulkPrintService,
                                                              DocumentHelper documentHelper, ObjectMapper objectMapper) {
        super(notificationService, bulkPrintService);
        this.documentHelper = documentHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void emailSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
    }

    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails) {
        List<CaseDocument> hearingNoticePack = getHearingNoticeDocumentPackFromCaseData(caseDetails);
        List<BulkPrintDocument> documentsToPrint = documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingNoticePack);
        return documentsToPrint;
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
