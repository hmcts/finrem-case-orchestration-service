package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.InterimHearingItemMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemIds;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TRACKING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterimHearingContestedAboutToStartHandler implements CallbackHandler {

    private final InterimHearingHelper interimHearingHelper;
    private final InterimHearingItemMapper interimHearingItemMapper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.INTERIM_HEARING.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        log.info("In Interim hearing about to start callback");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        loadInterimHearing(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
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
            interimHearingList.add(0,interimHearingData);
            caseData.put(INTERIM_HEARING_COLLECTION,interimHearingList);

            interimHearingItemMapper.loadBulkPrintDocuments(caseData);
        } else {
            List<InterimHearingCollectionItemData> list = interimHearingList.stream()
                .map(obj -> getTrackingObject(obj.getId())).collect(Collectors.toList());
            log.info("INTERIM_HEARING_TRACKING ELSE {}", list);
            caseData.put(INTERIM_HEARING_TRACKING, list);
        }
    }


    private List<InterimHearingCollectionItemData> setTrackingForBulkPrintAndNotification(Map<String, Object> caseData,
                                                                                          String collectionId) {
        List<InterimHearingCollectionItemData> list  = Optional.ofNullable(caseData.get(INTERIM_HEARING_TRACKING))
            .map(interimHearingHelper::convertToTrackingDataList).orElse(new ArrayList<>());
        list.add(getTrackingObject(collectionId));
        return list;
    }

    private InterimHearingCollectionItemData getTrackingObject(String collectionId) {
        return InterimHearingCollectionItemData.builder().id(UUID.randomUUID().toString())
                .value(InterimHearingCollectionItemIds.builder().ihItemIds(collectionId).build()).build();
    }
}
