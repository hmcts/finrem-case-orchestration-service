package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@Slf4j
@Service
public class GeneralApplicationOutcomeAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService service;

    public GeneralApplicationOutcomeAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationHelper helper,
                                                         GeneralApplicationService service) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_OUTCOME.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        final String caseId = caseDetails.getId().toString();
        log.info("Received on start request to outcome decision general application for Case ID: {}", caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        if (existingList.isEmpty() && caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            log.info("outcome stage migrate existing general application for Case ID: {}", caseId);
            migrateExistingApplication(caseDetails, userAuthorisation);
        } else {
            DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeList());

            final String outcome = Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcome().getValue(), null);
            log.info("Outcome decision {} for general application for Case ID: {} Event type {}",
                outcome, caseId, EventType.GENERAL_APPLICATION_OUTCOME);

            final String valueCode = dynamicList.getValueCode();
            log.info("Selected dynamic list code : {} Case ID: {}", valueCode, caseId);
            final List<GeneralApplicationCollectionData> applicationCollectionDataList
                = existingList.stream().map(ga -> setStatusForElement(caseData, ga, valueCode, outcome)).sorted(helper::getCompareTo).toList();

            log.info("applicationCollectionDataList : {} caseId {}", applicationCollectionDataList.size(), caseId);
            service.updateGeneralApplicationCollectionData(applicationCollectionDataList, caseData);
            caseData.getGeneralApplicationWrapper().setGeneralApplications(
                helper.convertToGeneralApplicationsCollection(applicationCollectionDataList));
            if (caseData.getGeneralApplicationWrapper().getGeneralApplications() != null
                && !caseData.getGeneralApplicationWrapper().getGeneralApplications().isEmpty()) {
                caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(
                    ga -> ga.getValue().setAppRespGeneralApplicationReceivedFrom(null));
            }
            caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcome(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcomeOther(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcomeList(null);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void migrateExistingApplication(FinremCaseDetails caseDetails, String userAuthorisation)  {
        String caseId = caseDetails.getId().toString();
        FinremCaseData caseData = caseDetails.getData();
        GeneralApplicationCollectionData data =
            helper.migrateExistingGeneralApplication(caseData, userAuthorisation, caseId);
        List<GeneralApplicationCollectionData> existingGeneralApplication =
            helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        if (data != null) {
            String status = Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcome().getValue(), null);

            log.info("In migration outcome decision {} for general application for Case ID: {} Event type {}",
                status, caseId, EventType.GENERAL_APPLICATION_OUTCOME);
            updateStatus(caseData, data, status);
            existingGeneralApplication.add(data);
            service.updateGeneralApplicationCollectionData(existingGeneralApplication, caseData);
            if (caseData.getGeneralApplicationWrapper().getGeneralApplications() != null
                && !caseData.getGeneralApplicationWrapper().getGeneralApplications().isEmpty()) {
                caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(
                    ga -> ga.getValue().setAppRespGeneralApplicationReceivedFrom(null));
            }
        }
        helper.deleteNonCollectionGeneralApplication(caseData);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcomeList(null);
    }

    private GeneralApplicationCollectionData setStatusForElement(FinremCaseData caseData,
                                                                 GeneralApplicationCollectionData data,
                                                                 String code,
                                                                 String status) {
        if (code.equals(data.getId())) {
            return updateStatus(caseData, data, status);
        }
        return data;
    }

    private GeneralApplicationCollectionData updateStatus(FinremCaseData caseData,
                                                          GeneralApplicationCollectionData data,
                                                          String status) {
        GeneralApplicationItems items = data.getGeneralApplicationItems();
        items.setGeneralApplicationOutcomeOther(Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeOther(), null));
        switch (status) {
            case "Approved" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.APPROVED.getId());
            case "Not Approved" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.NOT_APPROVED.getId());
            case "Other" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.OTHER.getId());
            default -> throw new IllegalStateException("Unexpected value: " + status);
        }
        return data;
    }
}
