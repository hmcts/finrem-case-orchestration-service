package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimUploadAdditionalDocument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BIRMINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BRISTOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CFC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CLEAVELAND_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DEVON_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DORSET_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_HUMBER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_KENT_SURREY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LANCASHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LIVERPOOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LONDON_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MANCHESTER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NEWPORT_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWALES_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NOTTINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NWYORKSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_PROMPT_FOR_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SWANSEA_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_THAMESVALLEY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_WALES_FRC_COURT_LIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterimHearingContestedAboutToStartHandler implements CallbackHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.INTERIM_HEARING.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        log.info("In Interim hearing callback");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        loadInterimHearing(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
    }

    private void loadInterimHearing(Map<String, Object> caseData) {
        if (caseData.get(INTERIM_HEARING_TYPE) != null) {
            InterimHearingData.InterimHearingDataBuilder builder = InterimHearingData.builder();
            builder.id(UUID.randomUUID().toString());
            builder.value(loadInterimHearingData(caseData));
            InterimHearingData interimHearingData = builder.build();
            List<InterimHearingData> interimHearingList = Optional.ofNullable(caseData.get(INTERIM_HEARING_COLLECTION))
                .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());
            interimHearingList.add(0,interimHearingData);
            caseData.put(INTERIM_HEARING_COLLECTION,interimHearingList);
        }
    }

    private InterimHearingItems loadInterimHearingData(Map<String, Object> caseData) {
        return InterimHearingItems.builder()
            .interimHearingType((String) caseData.get(INTERIM_HEARING_TYPE))
            .interimHearingDate((String) caseData.get(INTERIM_HEARING_DATE))
            .interimHearingTime((String) caseData.get(INTERIM_HEARING_TIME))
            .interimHearingTimeEstimate((String) caseData.get(INTERIM_HEARING_TIME_ESTIMATE))
            .interimAdditionalInformationAboutHearing((String) caseData.get(INTERIM_HEARING_ADDITIONAL_INFO))
            .interimPromptForAnyDocument((String) caseData.get(INTERIM_HEARING_PROMPT_FOR_DOCUMENT))
            .interimRegionList((String) caseData.get(INTERIM_HEARING_REGION_LIST))
            .interimCfcCourtList((String) caseData.get(INTERIM_HEARING_CFC_COURT_LIST))
            .interimWalesFRCList((String) caseData.get(INTERIM_HEARING_WALES_FRC_COURT_LIST))
            .interimLondonFRCList((String) caseData.get(INTERIM_HEARING_LONDON_FRC_COURT_LIST))
            .interimDevonCourtList((String) caseData.get(INTERIM_HEARING_DEVON_COURT_LIST))
            .interimDorsetCourtList((String) caseData.get(INTERIM_HEARING_DORSET_COURT_LIST))
            .interimHumberCourtList((String) caseData.get(INTERIM_HEARING_HUMBER_COURT_LIST))
            .interimMidlandsFRCList((String) caseData.get(INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST))
            .interimBristolCourtList((String) caseData.get(INTERIM_HEARING_BRISTOL_COURT_LIST))
            .interimNewportCourtList((String) caseData.get(INTERIM_HEARING_NEWPORT_COURT_LIST))
            .interimNorthEastFRCList((String) caseData.get(INTERIM_HEARING_NORTHEAST_COURT_LIST))
            .interimNorthWestFRCList((String) caseData.get(INTERIM_HEARING_NORTHWEST_COURT_LIST))
            .interimSouthEastFRCList((String) caseData.get(INTERIM_HEARING_SOUTHEAST_COURT_LIST))
            .interimSouthWestFRCList((String) caseData.get(INTERIM_HEARING_SOUTHWEST_COURT_LIST))
            .interimSwanseaCourtList((String) caseData.get(INTERIM_HEARING_SWANSEA_COURT_LIST))
            .interimLiverpoolCourtList((String) caseData.get(INTERIM_HEARING_LIVERPOOL_COURT_LIST))
            .interimBirminghamCourtList((String) caseData.get(INTERIM_HEARING_BIRMINGHAM_COURT_LIST))
            .interimCleavelandCourtList((String) caseData.get(INTERIM_HEARING_CLEAVELAND_COURT_LIST))
            .interimKentSurreyCourtList((String) caseData.get(INTERIM_HEARING_KENT_SURREY_COURT_LIST))
            .interimLancashireCourtList((String) caseData.get(INTERIM_HEARING_LANCASHIRE_COURT_LIST))
            .interimManchesterCourtList((String) caseData.get(INTERIM_HEARING_MANCHESTER_COURT_LIST))
            .interimNorthWalesCourtList((String) caseData.get(INTERIM_HEARING_NORTHWALES_COURT_LIST))
            .interimNottinghamCourtList((String) caseData.get(INTERIM_HEARING_NOTTINGHAM_COURT_LIST))
            .interimNwyorkshireCourtList((String) caseData.get(INTERIM_HEARING_NWYORKSHIRE_COURT_LIST))
            .interimBedfordshireCourtList((String) caseData.get(INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST))
            .interimThamesvalleyCourtList((String) caseData.get(INTERIM_HEARING_THAMESVALLEY_COURT_LIST))
            .interimUploadAdditionalDocument(loadInterimUploadedDocument(caseData))
            .build();
    }

    private InterimUploadAdditionalDocument loadInterimUploadedDocument(Map<String, Object> caseData) {
        CaseDocument interimUploadAdditionalDocument  = convertToCaseDocument(caseData.get(INTERIM_HEARING_UPLOADED_DOCUMENT));
        return InterimUploadAdditionalDocument.builder()
            .documentUrl(interimUploadAdditionalDocument.getDocumentUrl())
            .documentFilename(interimUploadAdditionalDocument.getDocumentFilename())
            .documentBinaryUrl(interimUploadAdditionalDocument.getDocumentBinaryUrl())
            .build();
    }

    private List<InterimHearingData> convertToInterimHearingDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }
    private CaseDocument convertToCaseDocument(Object object) {
        return objectMapper.convertValue(object, CaseDocument.class);
    }
}
