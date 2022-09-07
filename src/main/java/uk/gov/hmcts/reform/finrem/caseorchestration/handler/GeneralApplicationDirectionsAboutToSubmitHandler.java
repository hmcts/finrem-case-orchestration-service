package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationDirectionsAboutToSubmitHandler implements CallbackHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationDirectionsService service;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_DIRECTIONS.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final String caseId = caseDetails.getId().toString();
        log.info("Processing About to Submit callback for event {} with Case ID : {}",
            EventType.GENERAL_APPLICATION_DIRECTIONS, callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseData = caseDetails.getData();
        List<BulkPrintDocument> documents = new ArrayList<>();
        updateApplicationStatus(caseId, caseData, documents);

        List<String> errors = new ArrayList<>();
        try {
            service.submitCollectionGeneralApplicationDirections(caseDetails, documents, userAuthorisation);
        } catch (InvalidCaseDataException invalidCaseDataException) {
            errors.add(invalidCaseDataException.getMessage());
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).errors(errors).build();
    }

    private void updateApplicationStatus(String caseId, Map<String, Object> caseData, List<BulkPrintDocument> bulkPrintDocuments) {
        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(caseData);
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_DIRECTIONS_LIST));

        String[] choice =  dynamicList.getValueCode().split("#");

        final String outcome  = choice[1];
        log.info("outcome decision {} general application for Case ID: {}", outcome, caseId);

        final String valueCode = choice[0];
        log.info("selected dynamic list code : {} Case ID: {}", valueCode, caseId);
        final List<GeneralApplicationCollectionData> applicationCollectionDataList
            = existingList.stream().map(ga -> setStatus(caseData, ga, valueCode, outcome, bulkPrintDocuments))
            .sorted(helper::getCompareTo).toList();

        log.info("applicationCollectionDataList : {} caseId {}", applicationCollectionDataList.size(), caseId);
        caseData.put(GENERAL_APPLICATION_COLLECTION, applicationCollectionDataList);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_LIST);
        caseData.remove(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT);
    }

    private GeneralApplicationCollectionData setStatus(Map<String, Object> caseData,
                                                       GeneralApplicationCollectionData data,
                                                       String code,
                                                       String outcome,
                                                       List<BulkPrintDocument> bulkPrintDocuments) {
        if (code.equals(data.getId())) {
            GeneralApplicationItems items = data.getGeneralApplicationItems();
            CaseDocument directionDocument = helper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));
            if (directionDocument != null) {
                items.setGeneralApplicationDirectionsDocument(directionDocument);
            }
            switch (outcome) {
                case "Approved" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.DIRECTION_APPROVED.getId());
                case "Not Approved" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.DIRECTION_NOT_APPROVED.getId());
                case "Other" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.DIRECTION_OTHER.getId());
                default -> throw new IllegalStateException("Unexpected value: " + outcome);
            }

            final BulkPrintDocument genDoc = BulkPrintDocument.builder()
                .binaryFileUrl(items.getGeneralApplicationDocument().getDocumentBinaryUrl())
                .build();
            bulkPrintDocuments.add(genDoc);

            if (items.getGeneralApplicationDraftOrder() != null) {
                final BulkPrintDocument draftDoc = BulkPrintDocument.builder()
                    .binaryFileUrl(items.getGeneralApplicationDraftOrder().getDocumentBinaryUrl())
                    .build();
                bulkPrintDocuments.add(draftDoc);
            }
        }
        return data;
    }
}
