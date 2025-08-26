package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;

@Slf4j
@Service
public class GeneralApplicationDirectionsNewEventAboutToSubmitHandler extends FinremCallbackHandler implements CallbackHandler<FinremCaseData> {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationDirectionsService service;
    private final GeneralApplicationService gaService;
    private final ManageHearingActionService manageHearingActionService;
    private final GeneralApplicationsCategoriser generalApplicationsCategoriser;

    public GeneralApplicationDirectionsNewEventAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                                    GeneralApplicationHelper helper,
                                                                    GeneralApplicationDirectionsService service,
                                                                    GeneralApplicationService gaService,
                                                                    ManageHearingActionService manageHearingActionService,
                                                                    GeneralApplicationsCategoriser generalApplicationsCategoriser) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
        this.gaService = gaService;
        this.manageHearingActionService = manageHearingActionService;
        this.generalApplicationsCategoriser = generalApplicationsCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_DIRECTIONS_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequest, String userAuthorisation) {

        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        //Invoke performAddHearing when hearing is required
        if (service.isHearingRequired(caseDetails)) {
            manageHearingActionService.performAddHearing(caseDetails, userAuthorisation);
            manageHearingActionService.updateTabData(caseData);
        }

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

        log.info("Post state {} for Case ID: {}", postState, caseDetails.getId());
        generalApplicationsCategoriser.categorise(caseData);
        if (postState != null) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                .errors(errors).state(postState).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    private void migrateExistingApplication(FinremCaseDetails caseDetails,
                                            List<BulkPrintDocument> bulkPrintDocuments,
                                            String userAuthorisation) {
        FinremCaseData caseData = caseDetails.getData();
        List<GeneralApplicationCollectionData> existingGeneralApplication =
            helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        String caseId = caseDetails.getId().toString();
        log.info("Map existing general application to collection for Case ID: {}", caseId);
        GeneralApplicationCollectionData data = helper.mapExistingGeneralApplicationToData(
            caseData, userAuthorisation, caseId);
        if (data != null) {
            String status = Objects.toString(caseData.getGeneralApplicationWrapper()
                .getGeneralApplicationOutcome(), null);
            log.info("In map outcome decision {} for general application for Case ID: {} Event type {}",
                status, caseId, EventType.GENERAL_APPLICATION_DIRECTIONS);
            setStatusForNonCollAndBulkPrintDocuments(caseDetails,
                data, bulkPrintDocuments, status, userAuthorisation);
            existingGeneralApplication.add(data);
            gaService.updateGeneralApplicationCollectionData(existingGeneralApplication, caseDetails);
        }
        helper.deleteNonCollectionGeneralApplication(caseData);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(null);
    }

    private void updateApplications(FinremCaseDetails caseDetails,
                                    List<BulkPrintDocument> bulkPrintDocuments,
                                    String userAuthorisation) {
        FinremCaseData caseData = caseDetails.getData();
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
        gaService.updateGeneralApplicationCollectionData(applicationCollectionDataList, caseDetails);
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

        //Generate GAD document
        CaseDocument caseDocument = service.generateGeneralApplicationDirectionsDocument(userAuthorisation, finremCaseDetails);

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        items.setGeneralApplicationDirectionsDocument(caseDocument);
        String gaElementStatus = status != null ? status : items.getGeneralApplicationStatus();

        String caseId = String.valueOf(caseDetails.getId());
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

        log.info("items getGeneralApplicationDocument {}, for case ID: {}",
            items.getGeneralApplicationDocument(), caseId);

        if (items.getGeneralApplicationDocument() != null) {
            items.setGeneralApplicationDocument(
                helper.getPdfDocument(items.getGeneralApplicationDocument(), userAuthorisation, caseId));
            final BulkPrintDocument genDoc = BulkPrintDocument.builder()
                .binaryFileUrl(items.getGeneralApplicationDocument().getDocumentBinaryUrl())
                .fileName(items.getGeneralApplicationDocument().getDocumentFilename())
                .build();
            log.info("GeneralApplicationDocument {}, BulkPrintDocument {} for Case ID: {}",
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
