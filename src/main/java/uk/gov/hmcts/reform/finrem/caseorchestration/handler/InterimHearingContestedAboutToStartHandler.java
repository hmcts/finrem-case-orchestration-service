package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.InterimHearingItemMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TRACKING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PARTIES_ON_CASE;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterimHearingContestedAboutToStartHandler
    implements CallbackHandler<Map<String, Object>> {

    private final InterimHearingHelper interimHearingHelper;
    private final InterimHearingItemMapper interimHearingItemMapper;
    
    private final PartyService partyService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.INTERIM_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(
        CallbackRequest callbackRequest,
        String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("In Interim hearing about to start callback for Case ID: {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();

        loadInterimHearing(caseData);
        DynamicMultiSelectList allActivePartyList = partyService.getAllActivePartyList(caseDetails);
        caseData.put(PARTIES_ON_CASE, allActivePartyList);
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }

    private void loadInterimHearing(Map<String, Object> caseData) {

        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);

        if (caseData.get(INTERIM_HEARING_TYPE) != null) {
            var collectionId = UUID.randomUUID().toString();
            List<InterimHearingCollectionItemData> trackingList = setTrackingForBulkPrintAndNotification(caseData, collectionId);
            caseData.put(INTERIM_HEARING_TRACKING, trackingList);

            log.info("INTERIM_HEARING_TRACKING IF {}", trackingList);
            InterimHearingData.InterimHearingDataBuilder builder = InterimHearingData.builder();
            builder.id(collectionId);
            builder.value(interimHearingItemMapper.loadInterimHearingData(caseData));
            InterimHearingData interimHearingData = builder.build();
            interimHearingList.add(0, interimHearingData);
            caseData.put(INTERIM_HEARING_COLLECTION, interimHearingList);

            interimHearingItemMapper.loadBulkPrintDocuments(caseData);
        } else {
            List<InterimHearingCollectionItemData> list = interimHearingList.stream()
                .map(obj -> interimHearingHelper.getTrackingObject(obj.getId())).toList();
            log.info("INTERIM_HEARING_TRACKING ELSE {}", list);
            caseData.put(INTERIM_HEARING_TRACKING, list);
        }
    }


    private List<InterimHearingCollectionItemData> setTrackingForBulkPrintAndNotification(Map<String, Object> caseData,
                                                                                          String collectionId) {
        List<InterimHearingCollectionItemData> list = interimHearingHelper.getInterimHearingTrackingList(caseData);
        list.add(interimHearingHelper.getTrackingObject(collectionId));
        return list;
    }
}
