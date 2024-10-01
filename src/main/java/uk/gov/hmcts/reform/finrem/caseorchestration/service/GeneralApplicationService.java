package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;

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
    private final BulkPrintDocumentService service;
    private final GeneralApplicationsCategoriser generalApplicationsCategoriser;

    public FinremCaseData updateGeneralApplications(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        FinremCaseData caseData = caseDetails.getData();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();

        helper.populateGeneralApplicationSender(caseDataBefore, caseDataBefore
            .getGeneralApplicationWrapper().getGeneralApplications());

        List<GeneralApplicationCollectionData> generalApplicationListBefore =
            helper.getGeneralApplicationList(caseDataBefore, GENERAL_APPLICATION_COLLECTION);
        List<GeneralApplicationCollectionData> generalApplicationList =
            helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);

        log.info("GeneralApplicationService updateGeneralApplications generalApplicationsBefore list size: {} "
                + "generalApplications list size: {} for case ID: {}",
            ObjectUtils.isEmpty(generalApplicationListBefore) ? 0 : generalApplicationListBefore.size(),
            generalApplicationList.size(),
            caseDetails.getId());

        String initialCollectionId = Objects.toString(caseData.getGeneralApplicationWrapper()
            .getGeneralApplicationTracking(), null);

        String loggedInUserCaseRole = accessService.getActiveUser(String.valueOf(caseDetails.getId()), userAuthorisation);

        List<GeneralApplicationCollectionData> interimGeneralApplicationList = generalApplicationList.stream()
            .filter(f -> generalApplicationListBefore.stream().map(GeneralApplicationCollectionData::getId)
                .noneMatch(i -> i.equals(f.getId()))).toList();

        List<GeneralApplicationCollectionData> interimGeneralApplicationListForRoleType = new ArrayList<>();

        final List<GeneralApplicationCollectionData> processableList = interimGeneralApplicationList.stream()
            .filter(f -> !(initialCollectionId != null && initialCollectionId.equals(f.getId()))).toList();

        caseData.getGeneralApplicationWrapper().setGeneralApplicationPreState(caseDetailsBefore.getState().getStateId());

        String caseId = String.valueOf(caseDetails.getId());
        log.info("Processing general application for Case id {}", caseId);

        interimGeneralApplicationListForRoleType =
            getGeneralApplicationCollectionData(caseDetails, loggedInUserCaseRole, interimGeneralApplicationListForRoleType, caseData,
                caseDataBefore);

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
            processGeneralApplicationsForLitigants(userAuthorisation, caseDetails,
                initialCollectionId, loggedInUserCaseRole, interimGeneralApplicationListForRoleType, caseData,
                generalApplicationCollectionDataList);
        }
        List<GeneralApplicationCollectionData> applicationCollectionDataList =
            generalApplicationCollectionDataList.stream()
                .sorted(helper::getCompareTo)
                .toList();

        if (loggedInUserCaseRole.equalsIgnoreCase("Case")) {
            updateGeneralApplicationCollectionData(applicationCollectionDataList, caseDetails);
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

        generalApplicationsCategoriser.categorise(caseData);

        log.info("GeneralApplicationService updateGeneralApplications applicationCollectionDataList list size: {} for case ID: {}",
            applicationCollectionDataList.size(), caseDetails.getId());

        return caseData;
    }

    private void processGeneralApplicationsForLitigants(String userAuthorisation, FinremCaseDetails caseDetails,
                                                        String initialCollectionId, String loggedInUserCaseRole,
                                                        List<GeneralApplicationCollectionData> interimGeneralApplicationListForRoleType,
                                                        FinremCaseData caseData,
                                                        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList) {
        List<GeneralApplicationCollectionData> applicationsForRoleType;
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
                .toList();
        } else {
            applicationsForRoleType = generalApplicationCollectionDataList.stream()
                .filter(ga -> APPLICANT.equals(
                    ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())
                    || RESPONDENT.equals(
                    ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode()))
                .toList();
        }

        List<GeneralApplicationCollectionData> applicationCollectionDataListForRoleType =
            applicationsForRoleType.stream()
                .sorted(helper::getCompareTo)
                .toList();

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
            processGeneralApplicationForMainLititgants(caseData, applicationCollection);
        }
    }

    private static void processGeneralApplicationForMainLititgants(FinremCaseData caseData,
                                                                   List<GeneralApplicationsCollection> applicationCollection) {
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

    private List<GeneralApplicationCollectionData> getGeneralApplicationCollectionData(FinremCaseDetails caseDetails, String loggedInUserCaseRole,
         List<GeneralApplicationCollectionData> interimGeneralApplicationListForRoleType,
         FinremCaseData caseData, FinremCaseData caseDataBefore) {
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
            default -> log.info("The current user is a caseworker on Case ID: {}", caseDetails.getId());
        }
        return interimGeneralApplicationListForRoleType;
    }

    public List<GeneralApplicationCollectionData> getInterimGeneralApplicationList(
        String generalApplicationCollection, FinremCaseData caseData, FinremCaseData caseDataBefore) {
        List<GeneralApplicationCollectionData> generalApplicationListForRoleType =
            helper.getGeneralApplicationList(caseData, generalApplicationCollection);
        List<GeneralApplicationCollectionData> generalApplicationListBeforeForRoleType =
            helper.getGeneralApplicationList(caseDataBefore, generalApplicationCollection);
        return generalApplicationListForRoleType.stream()
            .filter(f -> generalApplicationListBeforeForRoleType.stream().map(GeneralApplicationCollectionData::getId)
                .noneMatch(i -> i.equals(f.getId()))).toList();
    }

    private GeneralApplicationCollectionData setUserAndDate(FinremCaseDetails caseDetails,
                                                            GeneralApplicationCollectionData items,
                                                            String userAuthorisation) {
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Setting user and date for new application  on Case ID {}", caseId);
        GeneralApplicationItems generalApplicationItems = items.getGeneralApplicationItems();
        generalApplicationItems.setGeneralApplicationCreatedBy(idamService.getIdamFullName(userAuthorisation));
        generalApplicationItems.setGeneralApplicationCreatedDate(LocalDate.now());
        CaseDocument caseDocument =
            convertToPdf(generalApplicationItems.getGeneralApplicationDocument(), userAuthorisation, caseId);
        generalApplicationItems.setGeneralApplicationDocument(caseDocument);
        generalApplicationItems.setGeneralApplicationStatus(GeneralApplicationStatus.CREATED.getId());
        if (generalApplicationItems.getGeneralApplicationDraftOrder() != null) {
            CaseDocument draftDocument =
                convertToPdf(generalApplicationItems.getGeneralApplicationDraftOrder(), userAuthorisation, caseId);
            generalApplicationItems.setGeneralApplicationDraftOrder(draftDocument);
        }

        List<GeneralApplicationSupportingDocumentData> gaSupportDocuments =
            generalApplicationItems.getGaSupportDocuments();
        if (gaSupportDocuments != null && !gaSupportDocuments.isEmpty()) {
            List<GeneralApplicationSupportingDocumentData> generalApplicationSupportingDocumentDataList
                = gaSupportDocuments.stream()
                .map(sd -> processSupportingDocuments(sd.getValue(), userAuthorisation, caseId))
                .toList();
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
            .supportDocument(convertToPdf(sdItems.getSupportDocument(), userAuthorisation, caseId))
            .build());
        return builder.build();
    }

    private CaseDocument convertToPdf(CaseDocument caseDocument, String userAuthorisation, String caseId) {
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
                                                       FinremCaseDetails caseDetails) {
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Updating the general application collections for Case Id {}", caseId);
        FinremCaseData caseData = caseDetails.getData();
        helper.populateGeneralApplicationDataSender(caseData, generalApplications);
        logGeneralApplications(generalApplications, caseId);

        List<GeneralApplicationCollectionData> appRespGeneralApplications =
            setGeneralApplicationEvidenceRecievedFrom(generalApplications);

        List<GeneralApplicationCollectionData> intervener1GeneralApplications =
            getIntervenerGeneralApplications(generalApplications, INTERVENER1);
        List<GeneralApplicationCollectionData> intervener2GeneralApplications =
            getIntervenerGeneralApplications(generalApplications, INTERVENER2);
        List<GeneralApplicationCollectionData> intervener3GeneralApplications =
            getIntervenerGeneralApplications(generalApplications, INTERVENER3);
        List<GeneralApplicationCollectionData> intervener4GeneralApplications =
            getIntervenerGeneralApplications(generalApplications, INTERVENER4);

        caseData.getGeneralApplicationWrapper().setGeneralApplications(
            helper.convertToGeneralApplicationsCollection(generalApplications));

        GeneralApplicationWrapper generalApplicationWrapper = caseData.getGeneralApplicationWrapper();

        convertToGeneralApplicationsCollections(generalApplicationWrapper, appRespGeneralApplications,
            intervener1GeneralApplications, intervener2GeneralApplications,
            intervener3GeneralApplications, intervener4GeneralApplications);

        generalApplicationsCategoriser.uncategoriseDuplicatedCollections(caseData);
    }


    public void checkIfApplicationCompleted(FinremCaseDetails caseDetails, List<String> errors,
                                            List<GeneralApplicationsCollection> generalApplications,
                                            List<GeneralApplicationsCollection> generalApplicationsBefore,
                                            String userAuthorisation) {
        String caseId = String.valueOf(caseDetails.getId());

        if ((generalApplications == null || generalApplications.isEmpty())) {
            log.info("Please complete the general application for Case ID: {}", caseDetails.getId());
            errors.add("Please complete the General Application. No information has been entered for this application.");
        } else {
            log.info("General application size {} for CaseId {}", generalApplications.size(), caseId);
            List<GeneralApplicationsCollection> generalApplicationsTemp = new ArrayList<>(generalApplications);
            if (generalApplicationsBefore != null && !generalApplicationsBefore.isEmpty()) {
                List<GeneralApplicationsCollection> generalApplicationsBeforeTemp = new ArrayList<>(generalApplicationsBefore);
                generalApplicationsTemp.removeAll(generalApplicationsBeforeTemp);
            }
            generalApplicationsTemp.forEach(ga -> {
                service.validateEncryptionOnUploadedDocument(ga.getValue().getGeneralApplicationDocument(),
                    caseId, errors, userAuthorisation);
                service.validateEncryptionOnUploadedDocument(ga.getValue().getGeneralApplicationDraftOrder(),
                    caseId, errors, userAuthorisation);
                List<GeneralApplicationSupportingDocumentData> gaSupportDocuments = ga.getValue().getGaSupportDocuments();
                if (gaSupportDocuments != null && !gaSupportDocuments.isEmpty()) {
                    gaSupportDocuments.forEach(doc -> service.validateEncryptionOnUploadedDocument(doc.getValue().getSupportDocument(),
                        caseId, errors, userAuthorisation));
                }
            });
        }

        if (generalApplicationsBefore != null && generalApplications != null
            && (generalApplicationsBefore.size() == generalApplications.size())) {
            log.info("Please complete the general application for Case ID: {}", caseDetails.getId());
            errors.add("Any changes to an existing General Applications will not be saved. "
                + "Please add a new General Application in order to progress.");
        }

    }

    public void updateIntervenerDirectionsOrders(GeneralApplicationItems items, FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        List<GeneralApplicationsCollection> intvOrders = new ArrayList<>();
        if (caseData.getGeneralApplicationWrapper().getGeneralApplicationIntvrOrders() != null
            && !caseData.getGeneralApplicationWrapper().getGeneralApplicationIntvrOrders().isEmpty()) {
            intvOrders.addAll(helper.convertToGeneralApplicationsCollection(
                caseData.getGeneralApplicationWrapper().getGeneralApplicationIntvrOrders()));
        }
        intvOrders.add(GeneralApplicationsCollection.builder().id(UUID.randomUUID()).value(items).build());
        caseData.getGeneralApplicationWrapper().setGeneralApplicationIntvrOrders(intvOrders);
    }

    private void convertToGeneralApplicationsCollections(GeneralApplicationWrapper wrapper,
                                                         List<GeneralApplicationCollectionData> appRespGeneralApplications,
                                                         List<GeneralApplicationCollectionData> intervener1GeneralApplications,
                                                         List<GeneralApplicationCollectionData> intervener2GeneralApplications,
                                                         List<GeneralApplicationCollectionData> intervener3GeneralApplications,
                                                         List<GeneralApplicationCollectionData> intervener4GeneralApplications) {
        if (intervener1GeneralApplications != null
            && !intervener1GeneralApplications.isEmpty()) {
            wrapper.setIntervener1GeneralApplications(
                helper.convertToGeneralApplicationsCollection(intervener1GeneralApplications));
        }
        if (intervener2GeneralApplications != null
            && !intervener2GeneralApplications.isEmpty()) {
            wrapper.setIntervener2GeneralApplications(
                helper.convertToGeneralApplicationsCollection(intervener2GeneralApplications));
        }
        if (intervener3GeneralApplications != null
            && !intervener3GeneralApplications.isEmpty()) {
            wrapper.setIntervener3GeneralApplications(
                helper.convertToGeneralApplicationsCollection(intervener3GeneralApplications));
        }
        if (intervener4GeneralApplications != null
            && !intervener4GeneralApplications.isEmpty()) {
            wrapper.setIntervener4GeneralApplications(
                helper.convertToGeneralApplicationsCollection(intervener4GeneralApplications));
        }
        if (appRespGeneralApplications != null
            && !appRespGeneralApplications.isEmpty()) {
            wrapper.setAppRespGeneralApplications(
                helper.convertToGeneralApplicationsCollection(appRespGeneralApplications));
            wrapper.getAppRespGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(null));
        }
    }

    private static List<GeneralApplicationCollectionData> getIntervenerGeneralApplications(List<GeneralApplicationCollectionData> generalApplications,
                                                                                           String intervener1) {
        return generalApplications.stream().filter(ga -> intervener1
            .equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())).toList();
    }

    private static List<GeneralApplicationCollectionData> setGeneralApplicationEvidenceRecievedFrom(
        List<GeneralApplicationCollectionData> generalApplications) {
        List<GeneralApplicationCollectionData> appRespGeneralApplications =
            generalApplications.stream().filter(ga ->
                APPLICANT.equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())
                    || RESPONDENT.equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode()
                )).toList();

        appRespGeneralApplications.forEach(ga -> {
            if (APPLICANT.equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())) {
                ga.getGeneralApplicationItems().setAppRespGeneralApplicationReceivedFrom(
                    ApplicantAndRespondentEvidenceParty.APPLICANT.getValue());
            } else if (RESPONDENT.equals(ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue().getCode())) {
                ga.getGeneralApplicationItems().setAppRespGeneralApplicationReceivedFrom(
                    ApplicantAndRespondentEvidenceParty.RESPONDENT.getValue());
            }
        });
        return appRespGeneralApplications;
    }

    private static void logGeneralApplications(List<GeneralApplicationCollectionData> generalApplications, String caseId) {
        generalApplications.forEach(ga -> {
            if (ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom() != null) {
                log.info("General application received from is {} on Case id {} with status {}",
                    ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom(),
                    caseId,
                    ga.getGeneralApplicationItems().getGeneralApplicationStatus());
            }
            if (ga.getGeneralApplicationItems().getGeneralApplicationSender() != null) {
                log.info("General application sender is {} on Case id {} with status {}",
                    ga.getGeneralApplicationItems().getGeneralApplicationSender().getValue(),
                    caseId,
                    ga.getGeneralApplicationItems().getGeneralApplicationStatus());
            }
        });
    }

}
