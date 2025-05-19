package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ManageHearingsAboutToSubmitHandler  extends FinremCallbackHandler {

    private final ValidateHearingService validateHearingService;
    private final ManageHearingsDocumentService manageHearingsDocumentService;

    public ManageHearingsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, ValidateHearingService validateHearingService, ManageHearingsDocumentService manageHearingsDocumentService) {
        super(finremCaseDetailsMapper);
        this.validateHearingService = validateHearingService;
        this.manageHearingsDocumentService = manageHearingsDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_HEARINGS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        FinremCaseData finremCaseData = finremCaseDetails.getData();

        ManageHearingsAction actionSelection = finremCaseData.getManageHearingsWrapper().getManageHearingsActionSelection();
        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();

        List<String> warnings = new ArrayList<>();

        if (ManageHearingsAction.ADD_HEARING.equals(actionSelection)) {
            //TODO: Return if warnings to avoid duplicate document generation
            warnings = validateHearingService.validateManageHearingWarnings(finremCaseDetails.getData(),
                manageHearingsWrapper.getWorkingHearing().getHearingType());

            performAddHearing(finremCaseDetails, manageHearingsWrapper, userAuthorisation);
        }

        manageHearingsWrapper.setWorkingHearing(null);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).warnings(warnings).build();
    }

    private void performAddHearing(FinremCaseDetails finremCaseDetails, ManageHearingsWrapper hearingWrapper, String authToken) {
        Hearing hearing = finremCaseDetails.getData().getManageHearingsWrapper().getWorkingHearing();

        List<ManageHearingsCollectionItem> manageHearingsCollectionItemList = Optional.ofNullable(
                hearingWrapper.getHearings())
            .orElseGet(ArrayList::new);

        UUID manageHearingID = UUID.randomUUID();
        manageHearingsCollectionItemList.add(
            ManageHearingsCollectionItem.builder().id(manageHearingID).value(hearing).build()
        );
        hearingWrapper.setWorkingHearingId(manageHearingID);
        hearingWrapper.setHearings(manageHearingsCollectionItemList);

        CaseDocument hearingNotice = manageHearingsDocumentService
            .generateHearingNotice(hearing, finremCaseDetails, authToken);

        List<ManageHearingDocumentsCollectionItem> manageHearingDocuments = Optional.ofNullable(
                hearingWrapper.getHearingDocumentsCollection())
            .orElseGet(ArrayList::new);

        manageHearingDocuments.add(
            ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument
                    .builder()
                    .hearingId(manageHearingID)
                    .hearingDocument(hearingNotice)
                    .build())
                .build()
        );

        finremCaseDetails.getData().getManageHearingsWrapper()
            .setHearingDocumentsCollection(manageHearingDocuments);

        //TODO: Generate other documents documents
        if (HearingType.FDA.equals(hearing.getHearingType())) {
            // TODO: Only generate if FORM C is not already generated
            // hearingDocumentService.generateHearingDocuments(userAuthorisation, caseDetails);
        }
    }
}
