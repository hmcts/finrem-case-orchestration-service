package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.CREATED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.REFERRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
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
    private final GenericDocumentService service;

    public List<GeneralApplicationCollectionData> getGeneralApplicationList(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(GENERAL_APPLICATION_COLLECTION))
            .map(this::covertToGeneralApplicationData).orElse(new ArrayList<>());

    }

    public List<GeneralApplicationCollectionData> getReadyForRejectOrReadyForReferList(Map<String, Object> caseData) {
        return getGeneralApplicationList(caseData).stream()
            .filter(obj -> Objects.equals(obj.getGeneralApplicationItems().getGeneralApplicationStatus(), CREATED.getId()))
            .collect(Collectors.toList());
    }

    public List<GeneralApplicationCollectionData> getReferredList(Map<String, Object> caseData) {
        return getGeneralApplicationList(caseData).stream()
            .filter(obj -> Objects.equals(obj.getGeneralApplicationItems().getGeneralApplicationStatus(), REFERRED.getId()))
            .collect(Collectors.toList());
    }

    public List<GeneralApplicationCollectionData> getOutcomeList(Map<String, Object> caseData) {
        return getGeneralApplicationList(caseData).stream()
            .filter(this::isEquals)
            .collect(Collectors.toList());
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

    public GeneralApplicationCollectionData migrateExistingGeneralApplication(Map<String, Object> caseData,
                                                                              String userAuthorisation, String caseId) {
        if (caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            String collectionId = UUID.randomUUID().toString();
            caseData.put(GENERAL_APPLICATION_TRACKING, collectionId);
            return GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(getApplicationItems(caseData, userAuthorisation, caseId))
                .build();
        }
        return null;
    }

    public GeneralApplicationCollectionData retrieveInitialGeneralApplicationData(Map<String, Object> caseData,
                                                                                  String collectionId,
                                                                                  String userAuthorisation, String caseId) {
        if (caseData.get(GENERAL_APPLICATION_CREATED_BY) != null) {
            return GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(getApplicationItems(caseData, userAuthorisation, caseId))
                .build();
        }
        return null;
    }

    public GeneralApplicationItems getApplicationItems(Map<String,Object> caseData, String userAuthorisation, String caseId) {
        GeneralApplicationItems.GeneralApplicationItemsBuilder builder =
            GeneralApplicationItems.builder();
        builder.generalApplicationReceivedFrom(Objects.toString(caseData.get(GENERAL_APPLICATION_RECEIVED_FROM), null));
        builder.generalApplicationCreatedBy(Objects.toString(caseData.get(GENERAL_APPLICATION_CREATED_BY), null));
        builder.generalApplicationHearingRequired(Objects.toString(caseData.get(GENERAL_APPLICATION_HEARING_REQUIRED), null));
        builder.generalApplicationTimeEstimate(Objects.toString(caseData.get(GENERAL_APPLICATION_TIME_ESTIMATE), null));
        builder.generalApplicationSpecialMeasures(Objects.toString(caseData.get(GENERAL_APPLICATION_SPECIAL_MEASURES), null));

        CaseDocument caseDocument = convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT));
        if (caseDocument != null) {
            log.info("General Application Document before converting to Pdf {}", caseDocument);
            CaseDocument pdfCaseDocument = getPdfDocument(caseDocument, userAuthorisation, caseId);
            builder.generalApplicationDocument(pdfCaseDocument);
            log.info("General Application Document after converting to Pdf {}", pdfCaseDocument);
        }

        CaseDocument draftDocument = convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER));
        if (draftDocument != null) {
            log.info("General Application Draft Document before converting to Pdf {}", draftDocument);
            CaseDocument draftCaseDocument = getPdfDocument(draftDocument, userAuthorisation, caseId);
            builder.generalApplicationDraftOrder(draftCaseDocument);
            log.info("General Application Draft Document after converting to Pdf {}", draftCaseDocument);
        }
        builder.generalApplicationCreatedDate(objectToDateTime(caseData.get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE)));
        builder.generalApplicationOutcomeOther(Objects.toString(caseData.get(GENERAL_APPLICATION_OUTCOME_OTHER), null));
        CaseDocument directionDocument = convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));
        if (directionDocument != null) {
            log.info("General Application Direction Document before converting to Pdf {}", directionDocument);
            CaseDocument directionCaseDocument = getPdfDocument(directionDocument, userAuthorisation, caseId);
            builder.generalApplicationDirectionsDocument(directionCaseDocument);
            log.info("General Application Direction Document after converting to Pdf {}", directionCaseDocument);
        }
        String outcome = Objects.toString(caseData.get(GENERAL_APPLICATION_OUTCOME_DECISION), null);
        String directionGiven = Objects.toString(caseData.get(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED),null);
        if (outcome != null) {
            setStatus(builder, outcome, directionGiven);
        } else {
            builder.generalApplicationStatus(CREATED.getId());
        }
        return builder.build();
    }

    private void setStatus(GeneralApplicationItems.GeneralApplicationItemsBuilder builder, String outcome, String directionGiven) {
        switch (outcome) {
            case "Approved" -> builder.generalApplicationStatus(directionGiven == null ? APPROVED.getId() : DIRECTION_APPROVED.getId());
            case "Not Approved" -> builder.generalApplicationStatus(directionGiven == null ? NOT_APPROVED.getId() : DIRECTION_NOT_APPROVED.getId());
            case "Other" -> builder.generalApplicationStatus(directionGiven == null ? OTHER.getId() : DIRECTION_OTHER.getId());
            default -> builder.generalApplicationStatus(OTHER.getId());
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
            caseData.remove(GENERAL_APPLICATION_OUTCOME_DECISION);
            caseData.remove(GENERAL_APPLICATION_OUTCOME_OTHER);
        }
    }

    public CaseDocument getPdfDocument(CaseDocument document, String userAuthorisation, String caseId) {
        return service.convertDocumentIfNotPdfAlready(document, userAuthorisation, caseId);
    }
}
