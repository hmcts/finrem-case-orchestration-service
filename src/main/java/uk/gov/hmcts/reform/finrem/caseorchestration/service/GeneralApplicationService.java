package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationSuportingDocumentItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationSupportingDocumentData;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TRACKING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_USER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_TWO;

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

    public Map<String, Object> updateGeneralApplications(CallbackRequest callbackRequest, String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        List<GeneralApplicationCollectionData> generalApplicationListBefore = helper.getGeneralApplicationList(caseDetailsBefore.getData());
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseDetails.getData());

        String initialCollectionId = Objects.toString(caseDetails.getData().get(GENERAL_APPLICATION_TRACKING), null);

        List<GeneralApplicationCollectionData> interimGeneralApplicationList = generalApplicationList.stream()
            .filter(f -> generalApplicationListBefore.stream().map(GeneralApplicationCollectionData::getId)
                .noneMatch(i -> i.equals(f.getId()))).collect(Collectors.toList());

        List<GeneralApplicationCollectionData> processableList = interimGeneralApplicationList.stream()
            .filter(f -> !(initialCollectionId != null && initialCollectionId.equals(f.getId()))).collect(Collectors.toList());

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(GENERAL_APPLICATION_PRE_STATE, caseDetailsBefore.getState());

        String caseId = caseDetails.getId().toString();
        log.info("Processing general application for case id {}", caseId);
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

        List<GeneralApplicationCollectionData> applicationCollectionDataList = generalApplicationCollectionDataList.stream()
            .sorted(helper::getCompareTo)
            .collect(Collectors.toList());

        caseData.put(GENERAL_APPLICATION_COLLECTION, applicationCollectionDataList);
        return caseData;
    }

    public String getActiveUser(String caseId, String userAuthorisation) {
        String logMessage = "Logged in user role {} caseId {}";
        String activeUserCaseRole = accessService.getActiveUserCaseRole(String.valueOf(caseId), userAuthorisation);
        if (activeUserCaseRole.contains(CaseRole.APP_SOLICITOR.getValue())) {
            log.info(logMessage, APPLICANT, caseId);
            return APPLICANT;
        } else if (activeUserCaseRole.contains(CaseRole.RESP_SOLICITOR.getValue())) {
            log.info(logMessage, RESPONDENT, caseId);
            return RESPONDENT;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_1.getValue())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_1.getValue())) {
            log.info(logMessage, INTERVENER_ONE, caseId);
            return INTERVENER1;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_2.getValue())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_2.getValue())) {
            log.info(logMessage, INTERVENER_TWO, caseId);
            return INTERVENER2;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_3.getValue())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_3.getValue())) {
            log.info(logMessage, INTERVENER_THREE, caseId);
            return INTERVENER3;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_4.getValue())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_4.getValue())) {
            log.info(logMessage, INTERVENER_FOUR, caseId);
            return INTERVENER4;
        }
        return activeUserCaseRole;
    }

    private GeneralApplicationCollectionData setUserAndDate(CaseDetails caseDetails,
                                                            GeneralApplicationCollectionData items,
                                                            String userAuthorisation) {
        String caseId = caseDetails.getId().toString();
        log.info("Setting user and date for new application {}", caseId);
        GeneralApplicationItems generalApplicationItems = items.getGeneralApplicationItems();
        generalApplicationItems.setGeneralApplicationCreatedBy(idamService.getIdamFullName(userAuthorisation));
        generalApplicationItems.setGeneralApplicationUserRole(getActiveUser(caseId, userAuthorisation));
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

        List<GeneralApplicationSupportingDocumentData> gaSupportDocuments = generalApplicationItems.getGaSupportDocuments();
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

    private GeneralApplicationSupportingDocumentData processSupportingDocuments(GeneralApplicationSuportingDocumentItems sdItems,
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
            documentHelper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT)), authorisationToken, caseId);
        caseData.put(GENERAL_APPLICATION_DOCUMENT_LATEST, applicationDocument);

        if (caseData.get(GENERAL_APPLICATION_DRAFT_ORDER) != null) {
            CaseDocument draftOrderPdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
                documentHelper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER)), authorisationToken, caseId);
            caseData.put(GENERAL_APPLICATION_DRAFT_ORDER, draftOrderPdfDocument);
        }
        updateGeneralApplicationDocumentCollection(caseData, applicationDocument);
    }

    private void updateGeneralApplicationDocumentCollection(Map<String, Object> caseData, CaseDocument applicationDocument) {
        GeneralApplication generalApplication = GeneralApplication.builder().generalApplicationDocument(applicationDocument).build();

        List<GeneralApplicationData> generalApplicationList = Optional.ofNullable(caseData.get(GENERAL_APPLICATION_DOCUMENT_COLLECTION))
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

    public List<GeneralApplicationCollectionData> getGeneralApplicationsForUserRole(String loggedInUserCaseRole, List<GeneralApplicationCollectionData> existingGeneralApplication) {
        if (loggedInUserCaseRole == APPLICANT) {
            return existingGeneralApplication.stream()
                .filter(ga -> APPLICANT.equals(ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom())
                    || ga.getGeneralApplicationItems().getApplicantGeneralApplicationReceivedFrom() != null)
                .collect(
                    Collectors.toList());
        } else if (loggedInUserCaseRole == RESPONDENT) {
            return existingGeneralApplication.stream()
                .filter(ga -> RESPONDENT.equals(ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom())
                    || ga.getGeneralApplicationItems().getRespondentGeneralApplicationReceivedFrom() != null)
                .collect(
                    Collectors.toList());
        } else if (loggedInUserCaseRole == INTERVENER1) {
            return existingGeneralApplication.stream()
                .filter(ga -> INTERVENER1.equals(ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom())
                    || ga.getGeneralApplicationItems().getIntervener1GeneralApplicationReceivedFrom() != null)
                .collect(
                    Collectors.toList());
        } else if (loggedInUserCaseRole == INTERVENER2) {
            return existingGeneralApplication.stream()
                .filter(ga -> INTERVENER2.equals(ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom())
                    || ga.getGeneralApplicationItems().getIntervener2GeneralApplicationReceivedFrom() != null)
                .collect(
                    Collectors.toList());
        } else if (loggedInUserCaseRole == INTERVENER3) {
            return existingGeneralApplication.stream()
                .filter(ga -> INTERVENER3.equals(ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom())
                    || ga.getGeneralApplicationItems().getIntervener3GeneralApplicationReceivedFrom() != null)
                .collect(
                    Collectors.toList());
        } else if (loggedInUserCaseRole == INTERVENER4) {
            return existingGeneralApplication.stream()
                .filter(ga -> INTERVENER4.equals(ga.getGeneralApplicationItems().getGeneralApplicationReceivedFrom())
                    || ga.getGeneralApplicationItems().getIntervener4GeneralApplicationReceivedFrom() != null)
                .collect(
                    Collectors.toList());
        }
        return existingGeneralApplication;
    }

    public void updateCaseDataStart(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        Stream.of(GENERAL_APPLICATION_RECEIVED_FROM,
            GENERAL_APPLICATION_HEARING_REQUIRED,
            GENERAL_APPLICATION_TIME_ESTIMATE,
            GENERAL_APPLICATION_SPECIAL_MEASURES,
            GENERAL_APPLICATION_DOCUMENT,
            GENERAL_APPLICATION_DRAFT_ORDER,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT
        ).forEach(caseData::remove);
        caseData.put(GENERAL_APPLICATION_CREATED_BY, idamService.getIdamFullName(authorisationToken));
//        caseData.put(GENERAL_APPLICATION_USER_ROLE, getActiveUser(caseDetails.getId().toString(), authorisationToken));
    }
}
