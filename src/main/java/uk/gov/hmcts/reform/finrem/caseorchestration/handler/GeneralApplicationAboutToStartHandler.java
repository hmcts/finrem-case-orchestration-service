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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationAboutToStartHandler implements CallbackHandler {

    private final IdamService idamService;
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

        List<GeneralApplicationCollectionData> existingGeneralApplications = helper.getExistingGeneralApplications(caseData);

        GeneralApplicationCollectionData data = GeneralApplicationCollectionData.builder()
                .generalApplicationItems(GeneralApplicationItems.builder()
                    .generalApplicationCreatedBy(idamService.getIdamFullName(userAuthorisation)).build()).build();

        if (existingGeneralApplications.isEmpty()) {
            List<GeneralApplicationCollectionData> dataList = new ArrayList<>();
            dataList.add(data);
            caseData.put(GENERAL_APPLICATION_COLLECTION,dataList);
        } else {
            existingGeneralApplications.add(data);
            caseData.put(GENERAL_APPLICATION_COLLECTION,existingGeneralApplications);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
    }
}
