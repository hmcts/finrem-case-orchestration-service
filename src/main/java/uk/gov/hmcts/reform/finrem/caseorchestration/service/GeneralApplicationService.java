package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantAndRespondentEvidenceParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationSuportingDocumentItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationSupportingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESP_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_PRE_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationService {

    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final IdamService idamService;
    private final GenericDocumentService genericDocumentService;
    private final AssignCaseAccessService accessService;
    private final GeneralApplicationHelper helper;

    public FinremCaseData updateGeneralApplications(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        helper.populateGeneralApplicationSender(caseDetailsBefore.getData(), caseDetailsBefore.getData()
            .getGeneralApplicationWrapper().getGeneralApplications());

        List<GeneralApplicationCollectionData> generalApplicationListBefore =
            helper.getGeneralApplicationList(caseDetailsBefore.getData(), GENERAL_APPLICATION_COLLECTION);
        List<GeneralApplicationCollectionData> generalApplicationList =
            helper.getGeneralApplicationList(caseDetails.getData(), GENERAL_APPLICATION_COLLECTION);

        String initialCollectionId = Objects.toString(caseDetails.getData().getGeneralApplicationWrapper()
            .getGeneralApplicationTracking(), null);

        String loggedInUserCaseRole = accessService.getActiveUser(caseDetails.getId().toString(), userAuthorisation);
        log.info("Logged in user case role {}", loggedInUserCaseRole);

        List<GeneralApplicationCollectionData> interimGeneralApplicationList = generalApplicationList.stream()
            .filter(f -> generalApplicationListBefore.stream().map(GeneralApplicationCollectionData::getId)
                .noneMatch(i -> i.equals(f.getId()))).toList();

        List<GeneralApplicationCollectionData> interimGeneralApplicationListForRoleType = new ArrayList<>();
        List<GeneralApplicationCollectionData> applicationsForRoleType;

        final List<GeneralApplicationCollectionData> processableList = interimGeneralApplicationList.stream()
            .filter(f -> !(initialCollectionId != null && initialCollectionId.equals(f.getId()))).toList();

        FinremCaseData caseData = caseDetails.getData();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();
        caseData.getGeneralApplicationWrapper().setGeneralApplicationPreState(caseDetailsBefore.getState().getStateId());

        String caseId = caseDetails.getId().toString();
        log.info("Processing general application for case id {}", caseId);

        switch (loggedInUserCaseRole) {
            case INTERVENER1 -> {
                interimGeneralApplicationListForRoleType = getInterimGeneralApplicationList(
                    INTERVENER1_GENERAL_APPLICATION_COLLECTION, caseData, caseDataBefore);
                interimGeneralApplicationListForRoleType.forEach(ga -> ga.getGeneralApplicationItems()
                    .getGeneralApplicationSender().getValue().setCode(INTERVENER1));
            }
            case INTERVENER2 -> {
                interimGeneralApplicationListForRoleType = getInterimGeneralApplicationList(
                    INTERVENER2_GENERAL_APPLICATION_COLLECTION, caseData, caseDataBefore);
                interimGeneralApplicationListForRoleType.forEach(ga -> ga.getGeneralApplicationItems()
                    .getGeneralApplicationSender().getValue().setCode(INTERVENER2));
            }
            case INTERVENER3 -> {
                interimGeneralApplicationListForRoleType = getInterimGeneralApplicationList(
                    INTERVENER3_GENERAL_APPLICATION_COLLECTION, caseData, caseDataBefore);
                interimGeneralApplicationListForRoleType.forEach(ga -> ga.getGeneralApplicationItems()
                    .getGeneralApplicationSender().getValue().setCode(INTERVENER3));
            }
            case INTERVENER4 -> {
                interimGeneralApplicationListForRoleType = getInterimGeneralApplicationList(
                    INTERVENER4_GENERAL_APPLICATION_COLLECTION, caseData, caseDataBefore);
                interimGeneralApplicationListForRoleType.forEach(ga -> ga.getGeneralApplicationItems()
                    .getGeneralApplicationSender().getValue().setCode(INTERVENER4));
            }
            case APPLICANT, RESPONDENT -> {
                interimGeneralApplicationListForRoleType = getInterimGeneralApplicationList(
                    APP_RESP_GENERAL_APPLICATION_COLLECTION, caseData, caseDataBefore);
                interimGeneralApplicationListForRoleType.forEach(ga -> {
                    String receivedFrom = ga.getGeneralApplicationItems().getAppRespGeneralApplicationReceivedFrom();
                    DynamicRadioListElement dynamicListElement = DynamicRadioListElement.builder().build();
                    if (ApplicantAndRespondentEvidenceParty.APPLICANT.getValue().equals(receivedFrom)) {
                        dynamicListElement.setCode(APPLICANT);
                        dynamicListElement.setLabel(APPLICANT);
                    } else if (ApplicantAndRespondentEvidenceParty.RESPONDENT.getValue().equals(receivedFrom)) {
                        dynamicListElement.setCode(RESPONDENT);
                        dynamicListElement.setLabel(RESPONDENT);
                    }
                    ga.getGeneralApplicationItems().setGeneralApplicationSender(DynamicRadioList.builder()
                        .value(dynamicListElement).listItems(List.of(dynamicListElement)).build());
                });
            }
            default -> log.info("The current user is a caseworker on case {}", caseDetails.getId());
        }

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList =
            processableList.stream().map(items -> setUserAndDate(caseDetails, items, userAuthorisation))
                .collect(Collectors.toList());

        if (!generalApplicationListBefore.isEmpty()) {
            generalApplicationCollectionDataList.addAll(generalApplicationListBefore);
        }

        if (initialCollectionId != null) {
            GeneralApplicationCollectionData originalGeneralApplicationList
                = helper.retrieveInitialGeneralApplicationData(
                caseData,
                initialCollectionId,
                userAuthorisation,
                caseId);
            generalApplicationCollectionDataList.add(originalGeneralApplicationList);
        }

        if (!loggedInUserCaseRole.equalsIgnoreCase("Case")) {
            List<GeneralApplicationCollectionData> processableListForRoleType =
                interimGeneralApplicationListForRoleType.stream().filter(
                    f -> !(initialCollectionId != null && initialCollectionId.equals(f.getId()))).toList();
            List<GeneralApplicationCollectionData> generalApplicationCollectionDataListForRoleType =
                processableListForRoleType.stream().map(items -> setUserAndDate(caseDetails, items, userAuthorisation))
                    .toList();
            generalApplicationCollectionDataList.addAll(generalApplicationCollectionDataListForRoleType);
            if (!loggedInUserCaseRole.equalsIgnoreCase(APPLICANT)
                && !loggedInUserCaseRole.equalsIgnoreCase(RESPONDENT)) {
                applicationsForRoleType = generalApplicationCollectionDataList.stream()
                    .filter(ga -> ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode()
                        .equalsIgnoreCase(loggedInUserCaseRole))
                    .collect(Collectors.toList());
            } else {
                applicationsForRoleType = generalApplicationCollectionDataList.stream()
                    .filter(ga -> APPLICANT.equals(
                        ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())
                        || RESPONDENT.equals(
                        ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode()))
                    .collect(Collectors.toList());
            }

            List<GeneralApplicationCollectionData> applicationCollectionDataListForRoleType =
                applicationsForRoleType.stream()
                .sorted(helper::getCompareTo)
                .collect(Collectors.toList());

            List<GeneralApplicationsCollection> applicationCollection = helper.convertToGeneralApplicationsCollection(
                applicationCollectionDataListForRoleType);

            if (loggedInUserCaseRole.equalsIgnoreCase(INTERVENER1)) {
                caseData.getGeneralApplicationWrapper().setIntervener1GeneralApplications(applicationCollection);
            } else if (loggedInUserCaseRole.equalsIgnoreCase(INTERVENER2)) {
                caseData.getGeneralApplicationWrapper().setIntervener2GeneralApplications(applicationCollection);
            } else if (loggedInUserCaseRole.equalsIgnoreCase(INTERVENER3)) {
                caseData.getGeneralApplicationWrapper().setIntervener3GeneralApplications(applicationCollection);
            } else if (loggedInUserCaseRole.equalsIgnoreCase(INTERVENER4)) {
                caseData.getGeneralApplicationWrapper().setIntervener4GeneralApplications(applicationCollection);
            } else if (loggedInUserCaseRole.equalsIgnoreCase(APPLICANT)
                || loggedInUserCaseRole.equalsIgnoreCase(RESPONDENT)) {
                List<GeneralApplicationsCollection> appRespCollection = new ArrayList<>();
                appRespCollection.addAll(applicationCollection);
                appRespCollection.forEach(ga -> {
                    String receivedFrom = ga.getValue().getGeneralApplicationSender().getValue().getCode();
                    if (APPLICANT.equals(receivedFrom)) {
                        ga.getValue().setAppRespGeneralApplicationReceivedFrom(
                            ApplicantAndRespondentEvidenceParty.APPLICANT.getValue());
                    } else if (RESPONDENT.equals(receivedFrom)) {
                        ga.getValue().setAppRespGeneralApplicationReceivedFrom(
                            ApplicantAndRespondentEvidenceParty.RESPONDENT.getValue());
                    }
                });
                appRespCollection.forEach(ga -> ga.getValue().setGeneralApplicationSender(null));
                caseData.getGeneralApplicationWrapper().setAppRespGeneralApplications(applicationCollection);
            }
        }
        List<GeneralApplicationCollectionData> applicationCollectionDataList =
            generalApplicationCollectionDataList.stream()
            .sorted(helper::getCompareTo)
            .collect(Collectors.toList());

        if (loggedInUserCaseRole.equalsIgnoreCase("Case")) {
            updateGeneralApplicationCollectionData(applicationCollectionDataList, caseData);
            if (caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications() != null
                && !caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().isEmpty()) {
                caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().forEach(
                    ga -> ga.getValue().setGeneralApplicationSender(null));
            }
        }

        applicationCollectionDataList.forEach(
            ga -> ga.getGeneralApplicationItems().setAppRespGeneralApplicationReceivedFrom(null));

        caseData.getGeneralApplicationWrapper().setGeneralApplications(
            helper.convertToGeneralApplicationsCollection(applicationCollectionDataList));
        return caseData;
    }

    public List<GeneralApplicationCollectionData> getInterimGeneralApplicationList(
        String generalApplicationCollection, FinremCaseData caseData, FinremCaseData caseDataBefore) {
        List<GeneralApplicationCollectionData> generalApplicationListForRoleType =
            helper.getGeneralApplicationList(caseData, generalApplicationCollection);
        List<GeneralApplicationCollectionData> generalApplicationListBeforeForRoleType =
            helper.getGeneralApplicationList(caseDataBefore, generalApplicationCollection);
        return generalApplicationListForRoleType.stream()
            .filter(f -> generalApplicationListBeforeForRoleType.stream().map(GeneralApplicationCollectionData::getId)
                .noneMatch(i -> i.equals(f.getId()))).collect(Collectors.toList());
    }

    private GeneralApplicationCollectionData setUserAndDate(FinremCaseDetails caseDetails,
                                                            GeneralApplicationCollectionData items,
                                                            String userAuthorisation) {
        String caseId = caseDetails.getId().toString();
        log.info("Setting user and date for new application {}", caseId);
        GeneralApplicationItems generalApplicationItems = items.getGeneralApplicationItems();
        generalApplicationItems.setGeneralApplicationCreatedBy(idamService.getIdamFullName(userAuthorisation));
        generalApplicationItems.setGeneralApplicationCreatedDate(LocalDate.now());
        CaseDocument caseDocument =
            covertToPdf(generalApplicationItems.getGeneralApplicationDocument(), userAuthorisation, caseId);
        generalApplicationItems.setGeneralApplicationDocument(caseDocument);
        generalApplicationItems.setGeneralApplicationStatus(GeneralApplicationStatus.CREATED.getId());
        if (generalApplicationItems.getGeneralApplicationDraftOrder() != null) {
            CaseDocument draftDocument =
                covertToPdf(generalApplicationItems.getGeneralApplicationDraftOrder(), userAuthorisation, caseId);
            generalApplicationItems.setGeneralApplicationDraftOrder(draftDocument);
        }

        List<GeneralApplicationSupportingDocumentData> gaSupportDocuments =
            generalApplicationItems.getGaSupportDocuments();
        if (gaSupportDocuments != null && !gaSupportDocuments.isEmpty()) {
            List<GeneralApplicationSupportingDocumentData> generalApplicationSupportingDocumentDataList
                = gaSupportDocuments.stream()
                .map(sd -> processSupportingDocuments(sd.getValue(), userAuthorisation, caseId))
                .collect(Collectors.toList());
            generalApplicationItems.setGaSupportDocuments(generalApplicationSupportingDocumentDataList);
        }

        GeneralApplicationCollectionData.GeneralApplicationCollectionDataBuilder builder =
            GeneralApplicationCollectionData.builder();
        builder.id(UUID.randomUUID().toString());
        builder.generalApplicationItems(generalApplicationItems);

        return builder.build();
    }

    private GeneralApplicationSupportingDocumentData processSupportingDocuments(
        GeneralApplicationSuportingDocumentItems sdItems,
        String userAuthorisation,
        String caseId) {
        GeneralApplicationSupportingDocumentData.GeneralApplicationSupportingDocumentDataBuilder builder =
            GeneralApplicationSupportingDocumentData.builder();
        builder.id(UUID.randomUUID().toString());
        builder.value(GeneralApplicationSuportingDocumentItems.builder()
            .supportDocument(covertToPdf(sdItems.getSupportDocument(), userAuthorisation, caseId))
            .build());
        return builder.build();
    }

    private CaseDocument covertToPdf(CaseDocument caseDocument, String userAuthorisation, String caseId) {
        return genericDocumentService.convertDocumentIfNotPdfAlready(
            documentHelper.convertToCaseDocument(caseDocument), userAuthorisation, caseId);
    }


    public void updateCaseDataSubmit(Map<String, Object> caseData,
                                     CaseDetails caseDetailsBefore,
                                     String authorisationToken,
                                     String caseId) {
        caseData.put(GENERAL_APPLICATION_PRE_STATE, caseDetailsBefore.getState());
        caseData.put(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE, LocalDate.now());

        CaseDocument applicationDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
            documentHelper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT)), authorisationToken,
            caseId);
        caseData.put(GENERAL_APPLICATION_DOCUMENT_LATEST, applicationDocument);

        if (caseData.get(GENERAL_APPLICATION_DRAFT_ORDER) != null) {
            CaseDocument draftOrderPdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
                documentHelper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER)),
                authorisationToken, caseId);
            caseData.put(GENERAL_APPLICATION_DRAFT_ORDER, draftOrderPdfDocument);
        }
        updateGeneralApplicationDocumentCollection(caseData, applicationDocument);
    }

    private void updateGeneralApplicationDocumentCollection(Map<String, Object> caseData,
                                                            CaseDocument applicationDocument) {
        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationDocument(applicationDocument).build();

        List<GeneralApplicationData> generalApplicationList = Optional.ofNullable(
            caseData.get(GENERAL_APPLICATION_DOCUMENT_COLLECTION))
            .map(this::convertToGeneralApplicationDataList)
            .orElse(new ArrayList<>());

        generalApplicationList.add(
            GeneralApplicationData.builder()
                .id(UUID.randomUUID().toString())
                .generalApplication(generalApplication)
                .build()
        );

        caseData.put(GENERAL_APPLICATION_DOCUMENT_COLLECTION, generalApplicationList);
    }

    private List<GeneralApplicationData> convertToGeneralApplicationDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public void updateCaseDataStart(Map<String, Object> caseData, String authorisationToken) {
        Stream.of(GENERAL_APPLICATION_RECEIVED_FROM,
            GENERAL_APPLICATION_HEARING_REQUIRED,
            GENERAL_APPLICATION_TIME_ESTIMATE,
            GENERAL_APPLICATION_SPECIAL_MEASURES,
            GENERAL_APPLICATION_DOCUMENT,
            GENERAL_APPLICATION_DRAFT_ORDER,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT
        ).forEach(caseData::remove);
        caseData.put(GENERAL_APPLICATION_CREATED_BY, idamService.getIdamFullName(authorisationToken));
    }

    public void updateGeneralApplicationCollectionData(List<GeneralApplicationCollectionData> generalApplications,
                                                       FinremCaseData caseData) {
        log.info("entering updateGeneralApplicationCollection Data for case Id 1688466084529064");
        generalApplications.forEach(ga -> {
            if (ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom() != null) {
                log.info("general application received from is {} on case id 1688466084529064 with status {}",
                    ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom(),
                    ga.getGeneralApplicationItems().getGeneralApplicationStatus());
            }
            if (ga.getGeneralApplicationItems().getGeneralApplicationSender() != null) {
                log.info("general application sender from is {} on case id 1688466084529064 with status {}",
                    ga.getGeneralApplicationItems().getGeneralApplicationSender(),
                    ga.getGeneralApplicationItems().getGeneralApplicationStatus());
            }
        });
        List<GeneralApplicationCollectionData> appRespGeneralApplications =
            generalApplications.stream().filter(ga ->
                APPLICANT.equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())
                || RESPONDENT.equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode()
                )).collect(Collectors.toList());
        appRespGeneralApplications.forEach(ga -> {
            if (APPLICANT.equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())) {
                ga.getGeneralApplicationItems().setAppRespGeneralApplicationReceivedFrom(
                    ApplicantAndRespondentEvidenceParty.APPLICANT.getValue());
            } else if (RESPONDENT.equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())) {
                ga.getGeneralApplicationItems().setAppRespGeneralApplicationReceivedFrom(
                    ApplicantAndRespondentEvidenceParty.RESPONDENT.getValue());
            }
        });
        log.info("reached intervener1GeneralApplications in updateGeneralApplicationCollection Data on case 1688466084529064");
        List<GeneralApplicationCollectionData> intervener1GeneralApplications =
            generalApplications.stream().filter(ga -> INTERVENER1
                .equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())).collect(
                Collectors.toList());
        List<GeneralApplicationCollectionData> intervener2GeneralApplications =
            generalApplications.stream().filter(ga -> INTERVENER2
                .equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())).collect(
                Collectors.toList());
        List<GeneralApplicationCollectionData> intervener3GeneralApplications =
            generalApplications.stream().filter(ga -> INTERVENER3
                .equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())).collect(
                Collectors.toList());
        List<GeneralApplicationCollectionData> intervener4GeneralApplications =
            generalApplications.stream().filter(ga -> INTERVENER4
                .equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())).collect(
                Collectors.toList());
        caseData.getGeneralApplicationWrapper().setGeneralApplications(
            helper.convertToGeneralApplicationsCollection(generalApplications));
        if (caseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications() != null
            && !caseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications().isEmpty()) {
            caseData.getGeneralApplicationWrapper().setIntervener1GeneralApplications(
                helper.convertToGeneralApplicationsCollection(intervener1GeneralApplications));
        }
        if (caseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications() != null
            && !caseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications().isEmpty()) {
            caseData.getGeneralApplicationWrapper().setIntervener2GeneralApplications(
                helper.convertToGeneralApplicationsCollection(intervener2GeneralApplications));
        }
        if (caseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications() != null
            && !caseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications().isEmpty()) {
            caseData.getGeneralApplicationWrapper().setIntervener3GeneralApplications(
                helper.convertToGeneralApplicationsCollection(intervener3GeneralApplications));
        }
        if (caseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications() != null
            && !caseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications().isEmpty()) {
            caseData.getGeneralApplicationWrapper().setIntervener4GeneralApplications(
                helper.convertToGeneralApplicationsCollection(intervener4GeneralApplications));
        }
        if (caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications() != null
            && !caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().isEmpty()) {
            caseData.getGeneralApplicationWrapper().setAppRespGeneralApplications(
                helper.convertToGeneralApplicationsCollection(appRespGeneralApplications));
        }
    }

    public void checkIfApplicationCompleted(FinremCaseDetails caseDetails, List<String> errors,
                                            List<GeneralApplicationsCollection> generalApplications,
                                            List<GeneralApplicationsCollection> generalApplicationsBefore) {
        if (generalApplicationsBefore != null && generalApplications != null
            && (generalApplicationsBefore.size() == generalApplications.size())) {
            log.info("Please complete the general application for case Id {}", caseDetails.getId());
            errors.add("Any changes to an existing General Applications will not be saved. "
                + "Please add a new General Application in order to progress.");
        }
        if ((generalApplications == null || generalApplications.isEmpty())) {
            log.info("Please complete the general application for case Id {}", caseDetails.getId());
            errors.add("Please complete the General Application. No information has been entered for this application.");
        }
    }

    public void updateIntervenerDirectionsDocumentCollection(GeneralApplicationWrapper wrapper,
                                                             CaseDocument caseDocument) {
        IntervenerCaseDocument gaCaseDocument = IntervenerCaseDocument.builder().build();
        IntervenerCaseDocumentCollection gaCaseDocumentCollection = IntervenerCaseDocumentCollection.builder().build();
        List<IntervenerCaseDocumentCollection> gaDocumentCollectionList = new ArrayList<>();
        List<IntervenerCaseDocumentCollection> existingGeneralApplicationDocuments =
            wrapper.getGeneralApplicationIntvrDocuments();
        if (existingGeneralApplicationDocuments != null && existingGeneralApplicationDocuments.size() > 0) {
            gaCaseDocument.setDocument(caseDocument);
            gaCaseDocumentCollection.setValue(gaCaseDocument);
            if (existingGeneralApplicationDocuments.stream().filter(
                ga -> ga.getValue().getDocument().getDocumentUrl().equals(
                    caseDocument.getDocumentUrl())).count() < 1) {
                existingGeneralApplicationDocuments.add(gaCaseDocumentCollection);
            }
            wrapper.setGeneralApplicationIntvrDocuments(existingGeneralApplicationDocuments);
        } else {
            gaCaseDocument.setDocument(caseDocument);
            gaCaseDocumentCollection.setValue(gaCaseDocument);
            gaDocumentCollectionList.add(gaCaseDocumentCollection);
            wrapper.setGeneralApplicationIntvrDocuments(gaDocumentCollectionList);
        }
    }
}
