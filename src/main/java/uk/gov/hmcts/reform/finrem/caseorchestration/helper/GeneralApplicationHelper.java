package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TRACKING;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationHelper {

    private final ObjectMapper objectMapper;

    public List<GeneralApplicationCollectionData> getGeneralApplicationList(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(GENERAL_APPLICATION_COLLECTION))
            .map(this::covertToGeneralApplicationData).orElse(new ArrayList<>());

    }

    public List<GeneralApplicationCollectionData> covertToGeneralApplicationData(Object object) {
        return objectMapper.registerModule(new JavaTimeModule()).convertValue(object, new TypeReference<>() {
        });
    }

    public List<GeneralApplicationData> convertToGeneralApplicationDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public CaseDocument convertToCaseDocument(Object object) {
        if (object != null) {
            return objectMapper.registerModule(new JavaTimeModule()).convertValue(object, CaseDocument.class);
        }
        return null;
    }

    public String objectToString(Object object) {
        if (object != null) {
            return objectMapper.registerModule(new JavaTimeModule()).convertValue(object, String.class);
        }
        return null;
    }

    public LocalDate objectToDateTime(Object object) {
        if (object != null) {
            return objectMapper.registerModule(new JavaTimeModule()).convertValue(object, LocalDate.class);
        }
        return null;
    }

    public GeneralApplicationCollectionData migrateExistingGeneralApplication(Map<String, Object> caseData) {
        if (caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            String collectionId = UUID.randomUUID().toString();
            caseData.put(GENERAL_APPLICATION_TRACKING, collectionId);
            return GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(getApplicationItems(caseData))
                .build();
        }
        return null;
    }

    public GeneralApplicationCollectionData retrieveInitialGeneralApplicationData(Map<String, Object> caseData,
                                                                                  String collectionId) {
        if (caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            return GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(getApplicationItems(caseData))
                .build();
        }
        return null;
    }

    private GeneralApplicationItems getApplicationItems(Map<String,Object> caseData) {

        GeneralApplicationItems.GeneralApplicationItemsBuilder builder =
            GeneralApplicationItems.builder();
        builder.generalApplicationReceivedFrom(objectToString(caseData.get(GENERAL_APPLICATION_RECEIVED_FROM)));
        builder.generalApplicationCreatedBy(objectToString(caseData.get(GENERAL_APPLICATION_CREATED_BY)));
        builder.generalApplicationHearingRequired(objectToString(caseData.get(GENERAL_APPLICATION_HEARING_REQUIRED)));
        builder.generalApplicationTimeEstimate(objectToString(caseData.get(GENERAL_APPLICATION_TIME_ESTIMATE)));
        builder.generalApplicationSpecialMeasures(objectToString(caseData.get(GENERAL_APPLICATION_SPECIAL_MEASURES)));
        builder.generalApplicationDocument(convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT)));
        CaseDocument draftDocument = convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER));
        if (draftDocument != null) {
            builder.generalApplicationDraftOrder(draftDocument);
        }
        builder.generalApplicationCreatedDate(objectToDateTime(caseData.get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE)));
        return builder.build();
    }

    public int getCompareTo(GeneralApplicationCollectionData e1, GeneralApplicationCollectionData e2) {
        if (e2 == null || e2.getGeneralApplicationItems() == null
            || e2.getGeneralApplicationItems().getGeneralApplicationCreatedDate() == null
            || e1 == null || e1.getGeneralApplicationItems() == null
            || e1.getGeneralApplicationItems().getGeneralApplicationCreatedDate() == null) {
            return 0;
        }
        return e2.getGeneralApplicationItems().getGeneralApplicationCreatedDate()
            .compareTo(e1.getGeneralApplicationItems().getGeneralApplicationCreatedDate());
    }
}
