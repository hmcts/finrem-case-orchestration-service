package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT_PACK;

@Component
@Slf4j
public class ApprovedOrderNoticeOfHearingCorresponder extends HearingCorresponder {

    private final ObjectMapper objectMapper;
    private final DocumentHelper documentHelper;

    @Autowired
    public ApprovedOrderNoticeOfHearingCorresponder(BulkPrintService bulkPrintService,
                                                    NotificationService notificationService,
                                                    FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    ObjectMapper objectMapper, DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService, finremCaseDetailsMapper);
        this.objectMapper = objectMapper;
        this.documentHelper = documentHelper;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails) {
        List<CaseDocument> hearingNoticePack = getHearingNoticeDocumentPackFromCaseData(caseDetails);
        return documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingNoticePack);
    }

    private List<CaseDocument> getHearingNoticeDocumentPackFromCaseData(CaseDetails caseDetails) {
        List<Element<CaseDocument>> hearingNoticePack = objectMapper.convertValue(
            caseDetails.getData().get(HEARING_NOTICE_DOCUMENT_PACK),
            new TypeReference<>() {
            });

        return hearingNoticePack.stream()
            .map(Element::getValue)
            .toList();
    }


}
