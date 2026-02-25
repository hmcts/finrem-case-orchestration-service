package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
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
        String caseId = String.valueOf(caseDetails.getId());

        helper.populateGeneralApplicationSender(caseDataBefore, caseDataBefore
            .getGeneralApplicationWrapper().getGeneralApplications());

        List<GeneralApplicationCollectionData> generalApplicationListBefore =
            helper.getGeneralApplicationList(caseDataBefore, GENERAL_APPLICATION_COLLECTION);
        List<GeneralApplicationCollectionData> generalApplicationList =
            helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);

        log.info("Case ID: {} GeneralApplicationService updateGeneralApplications generalApplicationsBefore list size: {} "
                + "generalApplications list size: {}", caseId,
            ObjectUtils.isEmpty(generalApplicationListBefore) ? 0 : generalApplicationListBefore.size(),
            generalApplicationList.size());

        String initialCollectionId = Objects.toString(caseData.getGeneralApplicationWrapper()
            .getGeneralApplicationTracking(), null);

        List<GeneralApplicationCollectionData> interimGeneralApplicationList = generalApplicationList.stream()
            .filter(f -> generalApplicationListBefore.stream().map(GeneralApplicationCollectionData::getId)
                .noneMatch(i -> i.equals(f.getId()))).toList();
        log.info("Case ID: {} interimGeneralApplicationList: {}", caseDetails.getId(), interimGeneralApplicationList.size());

        List<GeneralApplicationCollectionData> interimGeneralApplicationListForRoleType = new ArrayList<>();

        final List<GeneralApplicationCollectionData> processableList = interimGeneralApplicationList.stream()
            .filter(f -> !(initialCollectionId != null && initialCollectionId.equals(f.getId()))).toList();
        log.info("Case ID: {} processableList: {}", caseDetails.getId(), processableList.size());

        caseData.getGeneralApplicationWrapper().setGeneralApplicationPreState(caseDetailsBefore.getState().getStateId());

        String loggedInUserCaseRole = accessService.getActiveUser(String.valueOf(caseDetails.getId()), userAuthorisation);
        interimGeneralApplicationListForRoleType = getGeneralApplicationCollectionData(caseDetails,
            loggedInUserCaseRole, interimGeneralApplicationListForRoleType, caseData, caseDataBefore);
        log.info("Case ID: {} interimGeneralApplicationListForRoleType: {}", caseDetails.getId(),
            interimGeneralApplicationListForRoleType.size());

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList =
            processableList.stream().map(items -> setUserAndDate(caseDetails, items, userAuthorisation))
                .collect(Collectors.toList());
        log.info("Case ID: {} generalApplicationCollectionDataList: {}", caseDetails.getId(),
            generalApplicationCollectionDataList.size());

        if (!generalApplicationListBefore.isEmpty()) {
            log.info("Case ID: {} Adding generalApplicationListBefore: {}", caseDetails.getId(),
                generalApplicationListBefore.size());
            generalApplicationCollectionDataList.addAll(generalApplicationListBefore);
        }

        if (initialCollectionId != null) {
            GeneralApplicationCollectionData originalGeneralApplicationList
                = helper.retrieveInitialGeneralApplicationData(
                caseDetails,
                initialCollectionId,
                userAuthorisation);
            log.info("Case ID: {} Adding initialCollectionId: {}", caseDetails.getId(), initialCollectionId);
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
        log.info("Case ID: {} PGAFL Start generalApplicationCollectionDataList: {}", caseDetails.getId(),
            generalApplicationCollectionDataList.size());
        log.info("Case ID: {} PGAFL loggedInUserCaseRole: {}", caseDetails.getId(), loggedInUserCaseRole);

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
        log.info("Case ID: {} PGAFL applicationCollectionDataListForRoleType {}", caseDetails.getId(),
            applicationCollectionDataListForRoleType.size());

        List<GeneralApplicationsCollection> applicationCollection = helper.convertToGeneralApplicationsCollection(
            applicationCollectionDataListForRoleType);
        log.info("Case ID: {} PGAFL applicationCollection {}", caseDetails.getId(), applicationCollection.size());

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

        log.info("Case ID: {} PGAFL End generalApplicationCollectionDataList: {}", caseDetails.getId(),
            generalApplicationCollectionDataList.size());
        log.info("Case ID: {} PGAFL End applicationCollection {}", caseDetails.getId(), applicationCollection.size());
    }

    private void processGeneralApplicationForMainLititgants(FinremCaseData caseData,
                                                            List<GeneralApplicationsCollection> applicationCollection) {
        List<GeneralApplicationsCollection> appRespCollection = new ArrayList<>();
        appRespCollection.addAll(applicationCollection);
        appRespCollection.forEach(ga -> {
            String receivedFrom = ga.getValue().getGeneralApplicationSender().getValue().getCode();
            if (APPLICANT.equals(receivedFrom)) {
                log.info("Set received from as Applicant");
                ga.getValue().setAppRespGeneralApplicationReceivedFrom(
                    ApplicantAndRespondentEvidenceParty.APPLICANT.getValue());
            } else if (RESPONDENT.equals(receivedFrom)) {
                log.info("Set received from as Respondent");
                ga.getValue().setAppRespGeneralApplicationReceivedFrom(
                    ApplicantAndRespondentEvidenceParty.RESPONDENT.getValue());
            }
        });
        appRespCollection.forEach(ga -> ga.getValue().setGeneralApplicationSender(null));
        caseData.getGeneralApplicationWrapper().setAppRespGeneralApplications(applicationCollection);
        log.info("GA for Applicant/Respondent size: {}", applicationCollection.size());
    }

    private List<GeneralApplicationCollectionData> getGeneralApplicationCollectionData(
        FinremCaseDetails caseDetails, String loggedInUserCaseRole,
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
        log.info("Case ID: {} Setting user and date for new application", caseId);
        GeneralApplicationItems generalApplicationItems = items.getGeneralApplicationItems();
        generalApplicationItems.setGeneralApplicationCreatedBy(idamService.getIdamFullName(userAuthorisation));
        generalApplicationItems.setGeneralApplicationCreatedDate(LocalDate.now());
        CaseDocument caseDocument =
            convertToPdf(caseDetails, generalApplicationItems.getGeneralApplicationDocument(), userAuthorisation);
        generalApplicationItems.setGeneralApplicationDocument(caseDocument);
        generalApplicationItems.setGeneralApplicationStatus(GeneralApplicationStatus.CREATED.getId());
        if (generalApplicationItems.getGeneralApplicationDraftOrder() != null) {
            CaseDocument draftDocument =
                convertToPdf(caseDetails, generalApplicationItems.getGeneralApplicationDraftOrder(), userAuthorisation);
            generalApplicationItems.setGeneralApplicationDraftOrder(draftDocument);
        }

        List<GeneralApplicationSupportingDocumentData> gaSupportDocuments =
            generalApplicationItems.getGaSupportDocuments();
        if (gaSupportDocuments != null && !gaSupportDocuments.isEmpty()) {
            List<GeneralApplicationSupportingDocumentData> generalApplicationSupportingDocumentDataList
                = gaSupportDocuments.stream()
                .map(sd -> processSupportingDocuments(sd.getValue()))
                .toList();
            generalApplicationItems.setGaSupportDocuments(generalApplicationSupportingDocumentDataList);
        }

        String uuid = UUID.randomUUID().toString();
        log.info("Case ID: {} GA adding UUID {}", caseId, uuid);

        GeneralApplicationCollectionData.GeneralApplicationCollectionDataBuilder builder =
            GeneralApplicationCollectionData.builder();
        builder.id(uuid);
        builder.generalApplicationItems(generalApplicationItems);

        return builder.build();
    }

    private GeneralApplicationSupportingDocumentData processSupportingDocuments(
        GeneralApplicationSuportingDocumentItems sdItems) {
        GeneralApplicationSupportingDocumentData.GeneralApplicationSupportingDocumentDataBuilder builder =
            GeneralApplicationSupportingDocumentData.builder();
        builder.id(UUID.randomUUID().toString());
        builder.value(GeneralApplicationSuportingDocumentItems.builder()
            .supportDocument(sdItems.getSupportDocument())
            .build());
        return builder.build();
    }

    private CaseDocument convertToPdf(FinremCaseDetails caseDetails, CaseDocument caseDocument, String userAuthorisation) {
        return genericDocumentService.convertDocumentIfNotPdfAlready(
            documentHelper.convertToCaseDocument(caseDocument), userAuthorisation, caseDetails.getCaseType());
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

    /**
     * Validates if the general application is completed and checks for document encryption.
     * Adds error messages to the provided list if validation fails.
     *
     * @param caseDetails               the details of the current case
     * @param errors                    the list to collect validation errors
     * @param generalApplications       the current general applications
     * @param generalApplicationsBefore the previous state of general applications
     * @param userAuthorisation         the user authorisation token
     */
    public void checkIfApplicationCompleted(FinremCaseDetails caseDetails,
                                            List<String> errors,
                                            List<GeneralApplicationsCollection> generalApplications,
                                            List<GeneralApplicationsCollection> generalApplicationsBefore,
                                            String userAuthorisation) {

        String caseId = String.valueOf(caseDetails.getId());

        if (CollectionUtils.isEmpty(generalApplications)) {
            errors.add("Please complete the General Application. No information has been entered for this application.");
            return;
        }

        if (generalApplicationsBefore != null && generalApplicationsBefore.size() == generalApplications.size()) {
            errors.add("Any changes to an existing General Applications will not be saved. "
                + "Please add a new General Application in order to progress.");
            return;
        }

        log.info("General application size {} for CaseId {}", generalApplications.size(), caseId);

        // Determine which general applications need validation (new or modified)
        List<GeneralApplicationsCollection> generalApplicationsToValidate =
            determineGeneralApplicationsToValidate(generalApplications, generalApplicationsBefore);

        log.info("CaseId {} validating encryption for {} GA(s) (new/changed only)",
            caseId, generalApplicationsToValidate.size());

        validateEncryptionForGeneralApplications(
            generalApplicationsToValidate, caseId, errors, userAuthorisation
        );
    }

    /**
     * Determines which general applications need validation by comparing the current and previous states.
     * Returns a list of applications that are either new or have modified documents.
     *
     * @param current the current list of general applications
     * @param before  the previous list of general applications
     * @return a list of general applications to validate
     */
    private List<GeneralApplicationsCollection> determineGeneralApplicationsToValidate(
        List<GeneralApplicationsCollection> current,
        List<GeneralApplicationsCollection> before) {

        if (CollectionUtils.isEmpty(before)) {
            // No previous state -> validate everything
            return current;
        }

        Map<UUID, String> previousStateByGaId = buildPreviousDocumentStateMap(before);

        return current.stream()
            .filter(ga -> isNewOrModified(ga, previousStateByGaId))
            .toList();
    }

    /**
     * Builds a map of previous document state signatures for general applications.
     * The map key is the application UUID, and the value is the document state signature.
     * This enables efficient comparison to detect changes in application documents.
     *
     * @param before the list of previous general applications
     * @return a map of application UUID to document state signature
     */
    private Map<UUID, String> buildPreviousDocumentStateMap(
        List<GeneralApplicationsCollection> before) {

        return before.stream()
            .collect(Collectors.toMap(
                GeneralApplicationsCollection::getId,
                ga -> buildDocumentStateSignature(ga.getValue()),
                (a, b) -> a
            ));
    }

    /**
     * Determines if the provided general application is new or has been modified.
     * Compares the current document state signature with the previous one.
     *
     * @param currentGa           the current general application collection
     * @param previousStateByGaId a map of previous document state signatures by application ID
     * @return true if the application is new or its documents have changed; false otherwise
     */
    private boolean isNewOrModified(GeneralApplicationsCollection currentGa,
                                    Map<UUID, String> previousStateByGaId) {

        String previousState = previousStateByGaId.get(currentGa.getId());
        String currentState = buildDocumentStateSignature(currentGa.getValue());

        // Return true if this is a new application (no previous state)
        if (previousState == null) {
            return true;
        }

        // Return true if the document state has changed
        return !previousState.equals(currentState);
    }

    /**
     * Builds a unique signature string representing the state of a general application's documents.
     * The signature includes URLs of the main document, draft order, and supporting documents.
     * Used to detect changes in document state for validation purposes.
     *
     * @param generalApplicationItems the general application items to generate the signature for
     * @return a signature string representing the document state
     */
    private String buildDocumentStateSignature(GeneralApplicationItems generalApplicationItems) {

        String main = extractDocumentUrl(generalApplicationItems.getGeneralApplicationDocument());
        String draft = extractDocumentUrl(generalApplicationItems.getGeneralApplicationDraftOrder());

        List<GeneralApplicationSupportingDocumentData> support = generalApplicationItems.getGaSupportDocuments();
        String supportUrls = "";
        if (CollectionUtils.isNotEmpty(support)) {
            supportUrls = support.stream()
                .map(d -> extractDocumentUrl(d.getValue().getSupportDocument()))
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining("|"));
        }

        return String.join("||",
            Objects.toString(main, ""),
            Objects.toString(draft, ""),
            supportUrls
        );
    }

    /**
     * Validates encryption for all documents in the provided general applications.
     * Checks the main document, draft order, and supporting documents for each application.
     * Adds error messages to the provided list if any document fails encryption validation.
     *
     * @param generalApplications the list of general applications to validate
     * @param caseId              the identifier of the current case
     * @param errors              the list to collect validation errors
     * @param userAuthorisation   the user authorisation token
     */
    private void validateEncryptionForGeneralApplications(
        List<GeneralApplicationsCollection> generalApplications,
        String caseId,
        List<String> errors,
        String userAuthorisation) {

        generalApplications.stream()
            .map(GeneralApplicationsCollection::getValue)
            .forEach(items -> {

                //Validate encryption on general application document and draft order (if present)
                validateEncryptionOnDocument(
                    items.getGeneralApplicationDocument(), caseId, errors, userAuthorisation
                );

                validateEncryptionOnDocument(
                    items.getGeneralApplicationDraftOrder(), caseId, errors, userAuthorisation
                );

                //validate encryption on supporting documents (if present)
                Optional.ofNullable(items.getGaSupportDocuments())
                    .stream()
                    .flatMap(List::stream)
                    .map(GeneralApplicationSupportingDocumentData::getValue)
                    .map(GeneralApplicationSuportingDocumentItems::getSupportDocument)
                    .forEach(doc ->
                        validateEncryptionOnDocument(doc, caseId, errors, userAuthorisation)
                    );
            });
    }

    private String extractDocumentUrl(CaseDocument doc) {
        return doc == null ? null : doc.getDocumentUrl();
    }

    /**
     * Validates encryption on the provided document if it is not null.
     *
     * @param document          the document to validate
     * @param caseId            the case identifier
     * @param errors            the list to collect errors
     * @param userAuthorisation the user authorisation token
     */
    private void validateEncryptionOnDocument(CaseDocument document, String caseId, List<String> errors, String
        userAuthorisation) {
        if (document != null) {
            service.validateEncryptionOnUploadedDocument(document, caseId, errors, userAuthorisation);
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

    private static List<GeneralApplicationCollectionData> getIntervenerGeneralApplications(
        List<GeneralApplicationCollectionData> generalApplications,
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
