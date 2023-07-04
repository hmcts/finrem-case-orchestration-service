package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.REFERRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@Slf4j
@Service
public class GeneralApplicationReferToJudgeAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService service;

    public GeneralApplicationReferToJudgeAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationHelper helper,
                                                        GeneralApplicationService service) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_REFER_TO_JUDGE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = caseDetails.getId().toString();
        log.info("Received on start request to {} for Case ID: {}",
            EventType.GENERAL_APPLICATION_REFER_TO_JUDGE,
            caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationReferList());

        if (existingList.isEmpty() && caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            migrateExistingApplication(caseData, userAuthorisation, caseId);

        } else {
            if (dynamicList == null) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                    .errors(List.of("There is no general application available to refer.")).build();
            }
            setGeneralApplicationList(caseData, existingList, dynamicList);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void setGeneralApplicationList(FinremCaseData caseData,
                                           List<GeneralApplicationCollectionData> existingList,
                                           DynamicList dynamicList) {
        final String valueCode = dynamicList.getValueCode();
        String label = dynamicList.getValue().getLabel();
        String referredApplicationDetails = label.substring(label.indexOf("-") + 1);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferDetail(referredApplicationDetails);

        final List<GeneralApplicationCollectionData> applicationCollectionDataList
            = existingList.stream().map(ga -> setStatus(ga, valueCode)).sorted(helper::getCompareTo).collect(Collectors.toList());

        service.updateGeneralApplicationCollectionData(applicationCollectionDataList, caseData);
        caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(
            ga -> ga.getValue().setAppRespGeneralApplicationReceivedFrom(null));
    }

    private void migrateExistingApplication(FinremCaseData caseData, String userAuthorisation, String caseId) {
        List<GeneralApplicationCollectionData> existingGeneralApplication =
            helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        GeneralApplicationCollectionData data =
            helper.migrateExistingGeneralApplication(caseData, userAuthorisation, caseId);
        if (data != null) {
            data.getGeneralApplicationItems().setGeneralApplicationStatus(REFERRED.getId());
            existingGeneralApplication.add(data);
            service.updateGeneralApplicationCollectionData(existingGeneralApplication, caseData);
        }
        helper.deleteNonCollectionGeneralApplication(caseData);
    }

    private GeneralApplicationCollectionData setStatus(GeneralApplicationCollectionData data, String code) {
        if (code.equals(data.getId())) {
            data.getGeneralApplicationItems().setGeneralApplicationStatus(REFERRED.getId());
        }
        return data;
    }
}
