package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;

import java.util.List;

@Slf4j
@Service
public class ManageScannedDocsContestedAboutToStartHandler extends FinremCallbackHandler {

    @Autowired
    public ManageScannedDocsContestedAboutToStartHandler(
        FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_SCANNED_DOCS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        log.info("Received request to manage scanned documents for Case ID : {}",
            callbackRequest.getCaseDetails().getId());
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();

        if (ObjectUtils.isEmpty(finremCaseData.getScannedDocuments())) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(finremCaseData)
                .errors(List.of("There are no scanned documents available"))
                .build();
        }

        List<DynamicMultiSelectListElement> list = finremCaseData.getScannedDocuments().stream()
            .map(sdc -> DynamicMultiSelectListElement.builder()
                .code(sdc.getId())
                .label(scannedDocumentListLabelFormat(sdc.getValue()))
                .build())
            .toList();
        finremCaseData.setScannedDocsToUpdate(DynamicMultiSelectList.builder().listItems(list).build());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

    private String scannedDocumentListLabelFormat(ScannedDocument scannedDocument) {
        return String.format("**%s** [%s]<br/>%s",
            scannedDocument.getControlNumber(),
            scannedDocument.getUrl().getDocumentFilename(),
            StringUtils.capitalize(scannedDocument.getType().getValue()));
    }
}
