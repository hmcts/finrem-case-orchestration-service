package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationAboutToStartHandler implements CallbackHandler {

    private final GeneralApplicationHelper helper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to start general application for Case ID: {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingGeneralApplication = helper.getGeneralApplicationList(caseData);
        GeneralApplicationCollectionData data = migrateExistingGeneralApplication(caseData);

        if (data != null) {
            log.info("data ={}=", data);
            existingGeneralApplication.add(data);
            caseData.put(GENERAL_APPLICATION_COLLECTION,existingGeneralApplication);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }

    private GeneralApplicationCollectionData migrateExistingGeneralApplication(Map<String, Object> caseData) {
        if (caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            return GeneralApplicationCollectionData.builder()
                .id(UUID.randomUUID().toString())
                .generalApplicationItems(getApplicationItems(caseData))
                .build();
        }
        return null;
    }

    private GeneralApplicationItems getApplicationItems(Map<String,Object> caseData) {

        GeneralApplicationItems.GeneralApplicationItemsBuilder builder =
            GeneralApplicationItems.builder();
        builder.generalApplicationReceivedFrom(helper.objectToString(caseData.get(GENERAL_APPLICATION_RECEIVED_FROM)));
        builder.generalApplicationCreatedBy(helper.objectToString(caseData.get(GENERAL_APPLICATION_CREATED_BY)));
        builder.generalApplicationHearingRequired(helper.objectToString(caseData.get(GENERAL_APPLICATION_HEARING_REQUIRED)));
        builder.generalApplicationTimeEstimate(helper.objectToString(caseData.get(GENERAL_APPLICATION_TIME_ESTIMATE)));
        builder.generalApplicationSpecialMeasures(helper.objectToString(caseData.get(GENERAL_APPLICATION_SPECIAL_MEASURES)));
        builder.generalApplicationDocument(helper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT)));
        CaseDocument draftDocument = helper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER));
        log.info("draftDocument ={}=", draftDocument);
        if (draftDocument != null) {
            builder.generalApplicationDraftOrder(draftDocument);
        }
        return builder.build();
    }
}
