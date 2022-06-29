package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ALL_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TRACKING;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterimHearingHelper {

    private final ObjectMapper objectMapper;

    public List<InterimHearingData> isThereAnExistingInterimHearing(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(new ArrayList<>());

    }

    public List<InterimHearingCollectionItemData> getInterimHearingTrackingList(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(INTERIM_HEARING_TRACKING))
            .map(this::convertToTrackingDataList).orElse(new ArrayList<>());
    }

    public List<InterimHearingBulkPrintDocumentsData> getInterimHearingBulkPrintDocumentList(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(INTERIM_HEARING_ALL_DOCUMENT))
            .map(this::convertToBulkPrintDocumentDataList).orElse(new ArrayList<>());
    }


    public List<InterimHearingData> convertToInterimHearingDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public List<InterimHearingBulkPrintDocumentsData> convertToBulkPrintDocumentDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public List<InterimHearingCollectionItemData> convertToTrackingDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public CaseDocument convertToCaseDocument(Object object) {
        return objectMapper.convertValue(object, CaseDocument.class);
    }
}
