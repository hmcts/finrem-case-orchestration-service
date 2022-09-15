package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.CREATED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.REFERRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_OTHER;
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

    public List<GeneralApplicationCollectionData> getReadyForRejectOrReadyForReferList(Map<String, Object> caseData) {
        return getGeneralApplicationList(caseData).stream()
            .filter(obj -> Objects.equals(obj.getGeneralApplicationItems().getGeneralApplicationStatus(), CREATED.getId()))
            .toList();
    }

    public List<GeneralApplicationCollectionData> getReferredList(Map<String, Object> caseData) {
        return getGeneralApplicationList(caseData).stream()
            .filter(obj -> Objects.equals(obj.getGeneralApplicationItems().getGeneralApplicationStatus(), REFERRED.getId()))
            .toList();
    }

    public List<GeneralApplicationCollectionData> getOutcomeList(Map<String, Object> caseData) {
        return getGeneralApplicationList(caseData).stream()
            .filter(this::isEquals)
            .toList();
    }

    private boolean isEquals(GeneralApplicationCollectionData obj) {
        String generalApplicationStatus = obj.getGeneralApplicationItems().getGeneralApplicationStatus();
        return (Objects.equals(generalApplicationStatus, APPROVED.getId())
            || Objects.equals(generalApplicationStatus, NOT_APPROVED.getId())
            || Objects.equals(generalApplicationStatus, OTHER.getId()));
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

    public GeneralApplicationItems getApplicationItems(Map<String,Object> caseData) {
        GeneralApplicationItems.GeneralApplicationItemsBuilder builder =
            GeneralApplicationItems.builder();
        builder.generalApplicationReceivedFrom(Objects.toString(caseData.get(GENERAL_APPLICATION_RECEIVED_FROM), null));
        builder.generalApplicationCreatedBy(Objects.toString(caseData.get(GENERAL_APPLICATION_CREATED_BY), null));
        builder.generalApplicationHearingRequired(Objects.toString(caseData.get(GENERAL_APPLICATION_HEARING_REQUIRED), null));
        builder.generalApplicationTimeEstimate(Objects.toString(caseData.get(GENERAL_APPLICATION_TIME_ESTIMATE), null));
        builder.generalApplicationSpecialMeasures(Objects.toString(caseData.get(GENERAL_APPLICATION_SPECIAL_MEASURES), null));
        builder.generalApplicationDocument(convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT)));
        CaseDocument draftDocument = convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER));
        if (draftDocument != null) {
            builder.generalApplicationDraftOrder(draftDocument);
        }
        builder.generalApplicationCreatedDate(objectToDateTime(caseData.get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE)));
        builder.generalApplicationOutcomeOther(Objects.toString(caseData.get(GENERAL_APPLICATION_OUTCOME_OTHER), null));
        CaseDocument directionDocument = convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));
        if (directionDocument != null) {
            builder.generalApplicationDirectionsDocument(directionDocument);
        }
        String outcome = Objects.toString(caseData.get(GENERAL_APPLICATION_OUTCOME_DECISION), null);
        if (outcome != null) {
            setStatus(builder, outcome);
        } else {
            builder.generalApplicationStatus(CREATED.getId());
        }
        return builder.build();
    }

    private void setStatus(GeneralApplicationItems.GeneralApplicationItemsBuilder builder, String outcome) {
        switch (outcome) {
            case "Approved" -> builder.generalApplicationStatus(GeneralApplicationStatus.DIRECTION_APPROVED.getId());
            case "Not Approved" -> builder.generalApplicationStatus(GeneralApplicationStatus.DIRECTION_NOT_APPROVED.getId());
            case "Other" -> builder.generalApplicationStatus(GeneralApplicationStatus.DIRECTION_OTHER.getId());
            default -> builder.generalApplicationStatus(CREATED.getId());
        }
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

    public DynamicList objectToDynamicList(Object object) {
        if (object != null) {
            return objectMapper.registerModule(new JavaTimeModule()).convertValue(object, DynamicList.class);
        }
        return null;
    }

    public void deleteNonCollectionGeneralApplication(Map<String, Object> caseData) {
        if (caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            caseData.remove(GENERAL_APPLICATION_RECEIVED_FROM);
            caseData.remove(GENERAL_APPLICATION_CREATED_BY);
            caseData.remove(GENERAL_APPLICATION_HEARING_REQUIRED);
            caseData.remove(GENERAL_APPLICATION_TIME_ESTIMATE);
            caseData.remove(GENERAL_APPLICATION_SPECIAL_MEASURES);
            caseData.remove(GENERAL_APPLICATION_DOCUMENT);
            caseData.remove(GENERAL_APPLICATION_DRAFT_ORDER);
            caseData.remove(GENERAL_APPLICATION_TRACKING);

            List<GeneralApplicationData> generalApplicationList
                = Optional.ofNullable(caseData.get(GENERAL_APPLICATION_DOCUMENT_COLLECTION))
                .map(this::convertToGeneralApplicationDataList)
                .orElse(new ArrayList<>());

            if (generalApplicationList.size() == 1) {
                caseData.remove(GENERAL_APPLICATION_DOCUMENT_COLLECTION);
                caseData.remove(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE);
                caseData.remove(GENERAL_APPLICATION_DOCUMENT_LATEST);
            }
        }
    }
}
