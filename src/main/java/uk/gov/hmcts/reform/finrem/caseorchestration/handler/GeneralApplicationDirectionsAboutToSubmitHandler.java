package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;

@Slf4j
@Service
public class GeneralApplicationDirectionsAboutToSubmitHandler extends FinremCallbackHandler<FinremCaseDataContested>{

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationDirectionsService service;
    private final GeneralApplicationService gaService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public GeneralApplicationDirectionsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                            GeneralApplicationHelper helper,
                                                            GeneralApplicationDirectionsService service,
                                                            GeneralApplicationService gaService) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
        this.gaService = gaService;
        this.finremCaseDetailsMapper = finremCaseDetailsMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType) && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_DIRECTIONS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle(
        FinremCallbackRequest<FinremCaseDataContested> callbackRequest, String userAuthorisation) {
        FinremCaseDetails<FinremCaseDataContested> caseDetails = callbackRequest.getCaseDetails();
        log.info("Processing About to Submit callback for event {} with Case ID : {}",
            EventType.GENERAL_APPLICATION_DIRECTIONS, callbackRequest.getCaseDetails().getId());

        FinremCaseDataContested caseData = caseDetails.getData();
        helper.populateGeneralApplicationSender(caseData,
            caseData.getGeneralApplicationWrapper().getGeneralApplications());

        List<BulkPrintDocument> documents = new ArrayList<>();
        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(caseData,
            GENERAL_APPLICATION_COLLECTION);
        if (existingList.isEmpty()
            && caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            migrateExistingApplication(caseDetails, documents, userAuthorisation);

        } else {
            updateApplications(caseDetails, documents, userAuthorisation);
        }
        List<String> errors = new ArrayList<>();

        try {
            service.submitCollectionGeneralApplicationDirections(caseDetails, documents, userAuthorisation);
        } catch (InvalidCaseDataException invalidCaseDataException) {
            errors.add(invalidCaseDataException.getMessage());
        }

        String postState = service.getEventPostState(caseDetails, userAuthorisation);

        log.info("Post state {} for caseId {}", postState, caseDetails.getId());
        if (postState != null) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder().data(caseData)
                .errors(errors).state(postState).build();
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder().data(caseData).errors(errors).build();
    }

    private void migrateExistingApplication(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                            List<BulkPrintDocument> bulkPrintDocuments,
                                            String userAuthorisation) {
        FinremCaseDataContested caseData = caseDetails.getData();
        List<GeneralApplicationCollectionData> existingGeneralApplication =
            helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        String caseId = caseDetails.getId().toString();
        log.info("Migrating existing general application to collection for case id {}", caseId);
        GeneralApplicationCollectionData data = helper.migrateExistingGeneralApplication(
            caseData, userAuthorisation, caseId);
        if (data != null) {
            String status = Objects.toString(caseData.getGeneralApplicationWrapper()
                .getGeneralApplicationOutcome(), null);
            log.info("In migration outcome decision {} for general application for Case ID: {} Event type {}",
                status, caseId, EventType.GENERAL_APPLICATION_DIRECTIONS);
            setStatusForNonCollAndBulkPrintDocuments(caseDetails,
                data, bulkPrintDocuments, status, userAuthorisation);
            existingGeneralApplication.add(data);
            gaService.updateGeneralApplicationCollectionData(existingGeneralApplication, caseData);
        }
        helper.deleteNonCollectionGeneralApplication(caseData);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(null);
    }

    private void updateApplications(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                    List<BulkPrintDocument> bulkPrintDocuments,
                                    String userAuthorisation) {
        FinremCaseDataContested caseData = caseDetails.getData();
        List<GeneralApplicationCollectionData> existingList =
            helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper()
            .getGeneralApplicationDirectionsList());

        String[] choice = dynamicList.getValueCode().split("#");
        final String status = choice[1];
        final String valueCode = choice[0];

        final List<GeneralApplicationCollectionData> applicationCollectionDataList
            = existingList.stream().map(ga -> setStatusAndBulkPrintDouments(caseDetails,
                ga, valueCode, status, bulkPrintDocuments, userAuthorisation))
            .sorted(helper::getCompareTo).toList();

        log.info("applicationCollectionDataList : {} caseId {}", applicationCollectionDataList.size(), caseDetails.getId());
        gaService.updateGeneralApplicationCollectionData(applicationCollectionDataList, caseData);
        caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(
            ga -> ga.getValue().setAppRespGeneralApplicationReceivedFrom(null));
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(null);
    }

    private GeneralApplicationCollectionData setStatusAndBulkPrintDouments(FinremCaseDetails caseDetails,
                                                                           GeneralApplicationCollectionData data,
                                                                           String code, String status,
                                                                           List<BulkPrintDocument> bulkPrintDocuments,
                                                                           String userAuthorisation) {
        if (code.equals(data.getId())) {
            return setStatusForNonCollAndBulkPrintDocuments(caseDetails, data, bulkPrintDocuments, status, userAuthorisation);
        }
        return data;
    }

    private GeneralApplicationCollectionData setStatusForNonCollAndBulkPrintDocuments(
        FinremCaseDetails finremCaseDetails, GeneralApplicationCollectionData data,
        List<BulkPrintDocument> bulkPrintDocuments, String status, String userAuthorisation) {

        GeneralApplicationItems items = data.getGeneralApplicationItems();
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        CaseDocument caseDocument = service.getBulkPrintDocument(caseDetails, userAuthorisation);
        items.setGeneralApplicationDirectionsDocument(caseDocument);
        items.setGeneralApplicationOutcomeOther(Objects.toString(
            caseDetails.getData().get(GENERAL_APPLICATION_OUTCOME_OTHER), null));
        String gaElementStatus = status != null ? status : items.getGeneralApplicationStatus();

        String caseId = caseDetails.getId().toString();
        log.info("status {} for general application for Case ID: {} Event type {}", status, caseId,
            EventType.GENERAL_APPLICATION_DIRECTIONS);

        switch (gaElementStatus.toLowerCase()) {
            case "approved" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.DIRECTION_APPROVED.getId());
            case "not approved" ->
                items.setGeneralApplicationStatus(GeneralApplicationStatus.DIRECTION_NOT_APPROVED.getId());
            case "other" -> items.setGeneralApplicationStatus(GeneralApplicationStatus.DIRECTION_OTHER.getId());
            default -> throw new IllegalStateException("Unexpected value: " + items.getGeneralApplicationStatus());
        }

        final BulkPrintDocument bpDoc = BulkPrintDocument.builder()
            .binaryFileUrl(items.getGeneralApplicationDirectionsDocument().getDocumentBinaryUrl())
            .fileName(items.getGeneralApplicationDirectionsDocument().getDocumentFilename())
            .build();
        bulkPrintDocuments.add(bpDoc);

        log.info("items getGeneralApplicationDocument {}, for caseId {}",
            items.getGeneralApplicationDocument(), caseId);

        if (items.getGeneralApplicationDocument() != null) {
            items.setGeneralApplicationDocument(
                helper.getPdfDocument(items.getGeneralApplicationDocument(), userAuthorisation, caseId));
            final BulkPrintDocument genDoc = BulkPrintDocument.builder()
                .binaryFileUrl(items.getGeneralApplicationDocument().getDocumentBinaryUrl())
                .fileName(items.getGeneralApplicationDocument().getDocumentFilename())
                .build();
            log.info("GeneralApplicationDocument {}, BulkPrintDocument {} for caseId {}",
                items.getGeneralApplicationDocument(), genDoc, caseId);
            bulkPrintDocuments.add(genDoc);
        }

        if (items.getGeneralApplicationDraftOrder() != null) {
            items.setGeneralApplicationDraftOrder(
                helper.getPdfDocument(items.getGeneralApplicationDraftOrder(), userAuthorisation, caseId));
            final BulkPrintDocument draftDoc = BulkPrintDocument.builder()
                .binaryFileUrl(items.getGeneralApplicationDraftOrder().getDocumentBinaryUrl())
                .fileName(items.getGeneralApplicationDraftOrder().getDocumentFilename())
                .build();
            bulkPrintDocuments.add(draftDoc);
        }

        if (items.getGeneralApplicationSender().getValue().getCode().equalsIgnoreCase(INTERVENER1)
            || items.getGeneralApplicationSender().getValue().getCode().equalsIgnoreCase(INTERVENER2)
            || items.getGeneralApplicationSender().getValue().getCode().equalsIgnoreCase(INTERVENER3)
            || items.getGeneralApplicationSender().getValue().getCode().equalsIgnoreCase(INTERVENER4)) {
            gaService.updateIntervenerDirectionsOrders(items, finremCaseDetails);
        }
        return data;
    }
}
