package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESP_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationHelper {

    private final ObjectMapper objectMapper;
    private final GenericDocumentService service;

    public List<GeneralApplicationCollectionData> getGeneralApplicationList(FinremCaseData caseData, String collectionName) {
        GeneralApplicationWrapper wrapper = caseData.getGeneralApplicationWrapper();
        switch (collectionName) {
            case INTERVENER1_GENERAL_APPLICATION_COLLECTION -> {
                return Optional.ofNullable(wrapper.getIntervener1GeneralApplications())
                    .map(this::covertToGeneralApplicationData).orElse(new ArrayList<>());
            }
            case INTERVENER2_GENERAL_APPLICATION_COLLECTION -> {
                return Optional.ofNullable(wrapper.getIntervener2GeneralApplications())
                    .map(this::covertToGeneralApplicationData).orElse(new ArrayList<>());
            }
            case INTERVENER3_GENERAL_APPLICATION_COLLECTION -> {
                return Optional.ofNullable(wrapper.getIntervener3GeneralApplications())
                    .map(this::covertToGeneralApplicationData).orElse(new ArrayList<>());
            }
            case INTERVENER4_GENERAL_APPLICATION_COLLECTION -> {
                return Optional.ofNullable(wrapper.getIntervener4GeneralApplications())
                    .map(this::covertToGeneralApplicationData).orElse(new ArrayList<>());
            }
            case APP_RESP_GENERAL_APPLICATION_COLLECTION -> {
                return Optional.ofNullable(wrapper.getAppRespGeneralApplications())
                    .map(this::covertToGeneralApplicationData).orElse(new ArrayList<>());
            }
            default -> {
                return Optional.ofNullable(wrapper.getGeneralApplications())
                    .map(this::covertToGeneralApplicationData).orElse(new ArrayList<>());
            }
        }
    }

    public List<GeneralApplicationCollectionData> getReadyForRejectOrReadyForReferList(FinremCaseData caseData) {
        return getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION).stream()
            .filter(obj -> Objects.equals(obj.getGeneralApplicationItems().getGeneralApplicationStatus(), CREATED.getId()))
            .collect(Collectors.toList());
    }

    public List<GeneralApplicationCollectionData> getReferredList(FinremCaseData caseData) {
        return getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION).stream()
            .filter(obj -> Objects.equals(obj.getGeneralApplicationItems().getGeneralApplicationStatus(), REFERRED.getId()))
            .collect(Collectors.toList());
    }

    public List<GeneralApplicationCollectionData> getOutcomeList(FinremCaseData caseData) {
        return getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION).stream()
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

    public List<GeneralApplicationsCollection> convertToGeneralApplicationsCollection(Object object) {
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

    public GeneralApplicationCollectionData migrateExistingGeneralApplication(FinremCaseData caseData,
                                                                              String userAuthorisation, String caseId) {
        if (caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            String collectionId = UUID.randomUUID().toString();
            caseData.getGeneralApplicationWrapper().setGeneralApplicationTracking(collectionId);
            return GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(getApplicationItems(caseData, userAuthorisation, caseId))
                .build();
        }
        return null;
    }

    public GeneralApplicationCollectionData retrieveInitialGeneralApplicationData(FinremCaseData caseData,
                                                                                  String collectionId,
                                                                                  String userAuthorisation, String caseId) {
        if (caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            return GeneralApplicationCollectionData.builder()
                .id(collectionId)
                .generalApplicationItems(getApplicationItems(caseData, userAuthorisation, caseId))
                .build();
        }
        return null;
    }

    public GeneralApplicationItems getApplicationItems(FinremCaseData caseData, String userAuthorisation, String caseId) {
        GeneralApplicationItems.GeneralApplicationItemsBuilder builder =
            GeneralApplicationItems.builder();
        if (caseData.getGeneralApplicationWrapper().getGeneralApplicationReceivedFrom() != null
            && !caseData.getGeneralApplicationWrapper().getGeneralApplicationReceivedFrom().isEmpty()) {
            List<DynamicRadioListElement> dynamicListElements = new ArrayList<>();
            buildDynamicIntervenerList(dynamicListElements, caseData);
            String existingValue = caseData.getGeneralApplicationWrapper().getGeneralApplicationReceivedFrom();
            DynamicRadioListElement listElement = DynamicRadioListElement.builder()
                .code(existingValue).label(existingValue).build();
            DynamicRadioList existingRadioList = DynamicRadioList.builder().value(listElement)
                .listItems(dynamicListElements).build();
            builder.generalApplicationSender(existingRadioList);
        } else {
            builder.generalApplicationSender(null);
        }
        builder.generalApplicationCreatedBy(Objects.toString(caseData.getGeneralApplicationWrapper()
            .getGeneralApplicationCreatedBy(), null));
        builder.generalApplicationHearingRequired(Objects.toString(caseData.getGeneralApplicationWrapper()
            .getGeneralApplicationHearingRequired(), null));
        builder.generalApplicationTimeEstimate(Objects.toString(caseData.getGeneralApplicationWrapper()
            .getGeneralApplicationTimeEstimate(), null));
        builder.generalApplicationSpecialMeasures(Objects.toString(caseData.getGeneralApplicationWrapper()
            .getGeneralApplicationSpecialMeasures(), null));

        CaseDocument caseDocument = convertToCaseDocument(caseData.getGeneralApplicationWrapper().getGeneralApplicationDocument());
        if (caseDocument != null) {
            log.info("General Application Document before converting to Pdf {}", caseDocument);
            CaseDocument pdfCaseDocument = getPdfDocument(caseDocument, userAuthorisation, caseId);
            builder.generalApplicationDocument(pdfCaseDocument);
            log.info("General Application Document after converting to Pdf {}", pdfCaseDocument);
        }

        CaseDocument draftDocument = convertToCaseDocument(caseData.getGeneralApplicationWrapper().getGeneralApplicationDraftOrder());
        if (draftDocument != null) {
            log.info("General Application Draft Document before converting to Pdf {}", draftDocument);
            CaseDocument draftCaseDocument = getPdfDocument(draftDocument, userAuthorisation, caseId);
            builder.generalApplicationDraftOrder(draftCaseDocument);
            log.info("General Application Draft Document after converting to Pdf {}", draftCaseDocument);
        }
        builder.generalApplicationCreatedDate(objectToDateTime(caseData.getGeneralApplicationWrapper().getGeneralApplicationLatestDocumentDate()));
        builder.generalApplicationOutcomeOther(Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcomeOther(), null));
        CaseDocument directionDocument = convertToCaseDocument(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument());
        if (directionDocument != null) {
            log.info("General Application Direction Document before converting to Pdf {}", directionDocument);
            CaseDocument directionCaseDocument = getPdfDocument(directionDocument, userAuthorisation, caseId);
            builder.generalApplicationDirectionsDocument(directionCaseDocument);
            log.info("General Application Direction Document after converting to Pdf {}", directionCaseDocument);
        }
        String outcome = Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcome(), null);
        String directionGiven = Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsHearingRequired(),null);
        if (outcome != null) {
            setStatus(builder, outcome, directionGiven);
        } else {
            builder.generalApplicationStatus(CREATED.getId());
        }
        return builder.build();
    }

    private void setStatus(GeneralApplicationItems.GeneralApplicationItemsBuilder builder, String outcome, String directionGiven) {
        switch (outcome) {
            case "APPROVED" -> builder.generalApplicationStatus(directionGiven == null ? APPROVED.getId() : DIRECTION_APPROVED.getId());
            case "NOT_APPROVED" -> builder.generalApplicationStatus(directionGiven == null ? NOT_APPROVED.getId() : DIRECTION_NOT_APPROVED.getId());
            case "OTHER" -> builder.generalApplicationStatus(directionGiven == null ? OTHER.getId() : DIRECTION_OTHER.getId());
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

    public void deleteNonCollectionGeneralApplication(FinremCaseData caseData) {
        if (caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            caseData.getGeneralApplicationWrapper().setGeneralApplicationCreatedBy(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationReceivedFrom(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationHearingRequired(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationTimeEstimate(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationSpecialMeasures(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationDocument(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationDraftOrder(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationTracking(null);

            List<GeneralApplicationData> generalApplicationList
                = Optional.ofNullable(caseData.getGeneralApplicationWrapper().getGeneralApplicationDocumentCollection())
                .map(this::convertToGeneralApplicationDataList)
                .orElse(new ArrayList<>());

            if (generalApplicationList.size() == 1) {
                caseData.getGeneralApplicationWrapper().setGeneralApplicationDocumentCollection(null);
                caseData.getGeneralApplicationWrapper().setGeneralApplicationLatestDocumentDate(null);
                caseData.getGeneralApplicationWrapper().setGeneralApplicationLatestDocument(null);
            }
            caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcome(null);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationOutcomeOther(null);
        }
    }

    public CaseDocument getPdfDocument(CaseDocument document, String userAuthorisation, String caseId) {
        return service.convertDocumentIfNotPdfAlready(document, userAuthorisation, caseId);
    }

    public DynamicRadioListElement getDynamicListElements(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    public DynamicRadioList getDynamicRadioList(List<DynamicRadioListElement> dynamicRadioListElement) {
        return DynamicRadioList.builder()
            .value(dynamicRadioListElement.get(0))
            .listItems(dynamicRadioListElement)
            .build();
    }

    public boolean getIntervenerPopulated(IntervenerWrapper wrapper) {
        return (ObjectUtils.isNotEmpty(wrapper.getIntervenerOrganisation())
            && ObjectUtils.isNotEmpty(wrapper.getIntervenerOrganisation().getOrganisation())
            && ObjectUtils.isNotEmpty(wrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(wrapper.getIntervenerName());
    }

    public void populateGeneralApplicationSender(FinremCaseData caseData) {
        List<DynamicRadioListElement> dynamicListElements = new ArrayList<>();
        buildDynamicIntervenerList(dynamicListElements, caseData);
        List<GeneralApplicationsCollection> generalApplications =
            caseData.getGeneralApplicationWrapper().getGeneralApplications();
        if (generalApplications != null && !generalApplications.isEmpty()) {
            caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(ga -> {
                GeneralApplicationItems generalApplicationItems = ga.getValue();
                if (generalApplicationItems.getGeneralApplicationReceivedFrom() != null
                    && !generalApplicationItems.getGeneralApplicationReceivedFrom().isEmpty()) {
                    String existingCode = StringUtils.capitalize(
                        generalApplicationItems.getGeneralApplicationReceivedFrom());
                    String existingLabel = StringUtils.capitalize(
                        generalApplicationItems.getGeneralApplicationReceivedFrom());
                    DynamicRadioListElement newListElement = DynamicRadioListElement.builder()
                        .code(existingCode).label(existingLabel).build();
                    DynamicRadioList existingRadioList = DynamicRadioList.builder().value(newListElement)
                        .listItems(dynamicListElements).build();
                    generalApplicationItems.setGeneralApplicationSender(existingRadioList);
                    generalApplicationItems.setGeneralApplicationReceivedFrom(null);
                }
            });
        }
    }

    public void buildDynamicIntervenerList(List<DynamicRadioListElement> dynamicListElements,
                                            FinremCaseData caseData) {
        dynamicListElements.addAll(List.of(getDynamicListElements(APPLICANT, APPLICANT),
            getDynamicListElements(RESPONDENT, RESPONDENT),
            getDynamicListElements(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        ));
        IntervenerOneWrapper oneWrapper = caseData.getIntervenerOneWrapper();
        if (getIntervenerPopulated(oneWrapper)) {
            dynamicListElements.add(getDynamicListElements(INTERVENER1, INTERVENER1));
        }
        IntervenerTwoWrapper twoWrapper = caseData.getIntervenerTwoWrapper();
        if (getIntervenerPopulated(twoWrapper)) {
            dynamicListElements.add(getDynamicListElements(INTERVENER2, INTERVENER2));
        }
        IntervenerThreeWrapper threeWrapper = caseData.getIntervenerThreeWrapper();
        if (getIntervenerPopulated(threeWrapper)) {
            dynamicListElements.add(getDynamicListElements(INTERVENER3, INTERVENER3));
        }
        IntervenerFourWrapper fourWrapper = caseData.getIntervenerFourWrapper();
        if (getIntervenerPopulated(fourWrapper)) {
            dynamicListElements.add(getDynamicListElements(INTERVENER4, INTERVENER4));
        }
    }
}
