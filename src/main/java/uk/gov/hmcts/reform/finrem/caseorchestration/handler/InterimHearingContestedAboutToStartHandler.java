package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.InterimHearingItemMapper;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollectionItemIds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterimHearingContestedAboutToStartHandler implements CallbackHandler {

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
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();

        loadInterimHearing(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).build();
    }

    private void loadInterimHearing(FinremCaseData caseData) {

        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(caseData.getInterimWrapper().getInterimHearings())
            .orElse(new ArrayList<>());

        if (caseData.getInterimWrapper().getInterimHearingType() != null) {
            var collectionId = UUID.randomUUID().toString();
            List<InterimHearingCollectionItemData> trackingList = setTrackingForBulkPrintAndNotification(caseData, collectionId);
            caseData.getInterimWrapper().setInterimHearingCollectionItemIds(trackingList);

            log.info("INTERIM_HEARING_TRACKING IF {}", trackingList);
            InterimHearingCollection interimHearingData = InterimHearingCollection.builder()
                .id(UUID.fromString(collectionId))
                .value(interimHearingItemMapper.loadInterimHearingData(caseData))
                .build();
            interimHearingList.add(0,interimHearingData);
            caseData.getInterimWrapper().setInterimHearings(interimHearingList);

            interimHearingItemMapper.loadBulkPrintDocuments(caseData);
        } else {
            List<InterimHearingCollectionItemData> interimTrackingList = interimHearingList.stream()
                .map(obj -> getTrackingObject(String.valueOf(obj.getId()))).collect(Collectors.toList());
            log.info("INTERIM_HEARING_TRACKING ELSE {}", interimTrackingList);
            caseData.getInterimWrapper().setInterimHearingCollectionItemIds(interimTrackingList);
        }
    }

    private List<InterimHearingCollectionItemData> setTrackingForBulkPrintAndNotification(FinremCaseData caseData,
                                                                                          String collectionId) {
        List<InterimHearingCollectionItemData> list  = caseData.getInterimWrapper().getInterimHearingCollectionItemIds();
        list.add(getTrackingObject(collectionId));
        return list;
    }

    private InterimHearingCollectionItemData getTrackingObject(String collectionId) {
        return InterimHearingCollectionItemData.builder().id(UUID.randomUUID().toString())
                .value(InterimHearingCollectionItemIds.builder().ihItemIds(collectionId).build()).build();
    }
}
