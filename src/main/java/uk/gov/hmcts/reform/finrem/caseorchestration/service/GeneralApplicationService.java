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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationService {

    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final IdamService idamService;
    private final GenericDocumentService genericDocumentService;
    private final GeneralApplicationHelper helper;

    public Map<String, Object> updateGeneralApplications(CallbackRequest callbackRequest, String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        List<GeneralApplicationCollectionData> generalApplicationListBefore = helper.getGeneralApplicationList(caseDetailsBefore.getData());
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseDetails.getData());

        String initialCollectionId  = Objects.toString(caseDetails.getData().get(GENERAL_APPLICATION_TRACKING), null);

        List<GeneralApplicationCollectionData> interimGeneralApplicationList = generalApplicationList.stream()
            .filter(f -> generalApplicationListBefore.stream().map(GeneralApplicationCollectionData::getId)
                .noneMatch(i -> i.equals(f.getId()))).toList();

        List<GeneralApplicationCollectionData> processableList = interimGeneralApplicationList.stream()
            .filter(f -> !(initialCollectionId != null && initialCollectionId.equals(f.getId()))).toList();

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(GENERAL_APPLICATION_PRE_STATE, caseDetailsBefore.getState());

        log.info("Processing general application for case id {}", caseDetails.getId());
        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList =
            processableList.stream().map(items -> setUserAndDate(caseDetails, items, userAuthorisation))
                .collect(Collectors.toList());

        if (!generalApplicationListBefore.isEmpty()) {
            generalApplicationCollectionDataList.addAll(generalApplicationListBefore);
        }

        if (initialCollectionId != null) {
            GeneralApplicationCollectionData originalGeneralApplicationList
                = helper.retrieveInitialGeneralApplicationData(caseData, initialCollectionId, userAuthorisation);
            generalApplicationCollectionDataList.add(originalGeneralApplicationList);
        }

        List<GeneralApplicationCollectionData> applicationCollectionDataList = generalApplicationCollectionDataList.stream()
            .sorted(helper::getCompareTo)
            .toList();

        caseData.put(GENERAL_APPLICATION_COLLECTION, applicationCollectionDataList);
        return caseData;
    }



    private GeneralApplicationCollectionData setUserAndDate(CaseDetails caseDetails,
                                                            GeneralApplicationCollectionData items,
                                                            String userAuthorisation) {
        log.info("Setting user and date for new application {}", caseDetails.getId());
        GeneralApplicationItems generalApplicationItems = items.getGeneralApplicationItems();
        generalApplicationItems.setGeneralApplicationCreatedBy(idamService.getIdamFullName(userAuthorisation));
        generalApplicationItems.setGeneralApplicationCreatedDate(LocalDate.now());
        CaseDocument caseDocument = covertToPdf(generalApplicationItems.getGeneralApplicationDocument(), userAuthorisation);
        generalApplicationItems.setGeneralApplicationDocument(caseDocument);
        generalApplicationItems.setGeneralApplicationStatus(GeneralApplicationStatus.CREATED.getId());
        if (generalApplicationItems.getGeneralApplicationDraftOrder() != null) {
            CaseDocument draftDocument = covertToPdf(generalApplicationItems.getGeneralApplicationDraftOrder(), userAuthorisation);
            generalApplicationItems.setGeneralApplicationDraftOrder(draftDocument);
        }

        List<GeneralApplicationSupportingDocumentData> gaSupportDocuments = generalApplicationItems.getGaSupportDocuments();
        if (gaSupportDocuments != null && !gaSupportDocuments.isEmpty()) {
            List<GeneralApplicationSupportingDocumentData> generalApplicationSupportingDocumentDataList
                = gaSupportDocuments.stream().map(sd -> processSupportingDocuments(sd.getValue(), userAuthorisation)).toList();
            generalApplicationItems.setGaSupportDocuments(generalApplicationSupportingDocumentDataList);
        }

        GeneralApplicationCollectionData.GeneralApplicationCollectionDataBuilder builder =
            GeneralApplicationCollectionData.builder();
        builder.id(UUID.randomUUID().toString());
        builder.generalApplicationItems(generalApplicationItems);

        return builder.build();
    }

    private GeneralApplicationSupportingDocumentData processSupportingDocuments(GeneralApplicationSuportingDocumentItems sdItems,
                                                                                String userAuthorisation) {
        GeneralApplicationSupportingDocumentData.GeneralApplicationSupportingDocumentDataBuilder builder =
            GeneralApplicationSupportingDocumentData.builder();
        builder.id(UUID.randomUUID().toString());
        builder.value(GeneralApplicationSuportingDocumentItems.builder()
            .supportDocument(covertToPdf(sdItems.getSupportDocument(), userAuthorisation))
            .build());
        return builder.build();
    }

    private CaseDocument covertToPdf(CaseDocument caseDocument, String userAuthorisation) {
        return genericDocumentService.convertDocumentIfNotPdfAlready(
            documentHelper.convertToCaseDocument(caseDocument), userAuthorisation);
    }



    public void updateCaseDataSubmit(Map<String, Object> caseData, CaseDetails caseDetailsBefore, String authorisationToken) {
        caseData.put(GENERAL_APPLICATION_PRE_STATE, caseDetailsBefore.getState());
        caseData.put(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE, LocalDate.now());

        CaseDocument applicationDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
            documentHelper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT)), authorisationToken);
        caseData.put(GENERAL_APPLICATION_DOCUMENT_LATEST, applicationDocument);

        if (caseData.get(GENERAL_APPLICATION_DRAFT_ORDER) != null) {
            CaseDocument draftOrderPdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
                documentHelper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER)), authorisationToken);
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
}
