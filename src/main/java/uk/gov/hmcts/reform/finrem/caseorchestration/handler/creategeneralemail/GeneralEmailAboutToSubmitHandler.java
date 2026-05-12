package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.InvalidEmailAddressException;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.SendEmailException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralEmailDocumentCategoriser;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class GeneralEmailAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final NotificationService notificationService;
    private final GeneralEmailService generalEmailService;
    private final GenericDocumentService genericDocumentService;
    private final GeneralEmailDocumentCategoriser generalEmailCategoriser;

    @Autowired
    public GeneralEmailAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                            NotificationService notificationService,
                                            GeneralEmailService generalEmailService,
                                            GenericDocumentService genericDocumentService,
                                            GeneralEmailDocumentCategoriser generalEmailCategoriser) {
        super(mapper);
        this.notificationService = notificationService;
        this.generalEmailService = generalEmailService;
        this.genericDocumentService = genericDocumentService;
        this.generalEmailCategoriser = generalEmailCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && EventType.CREATE_GENERAL_EMAIL.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        validateCaseData(callbackRequest);

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();

        convertEmailAttachmentsToPdfIfRequired(finremCaseData, userAuthorisation);
        generalEmailService.storeGeneralEmail(finremCaseData);

        List<String> errors = new ArrayList<>();
        sendGeneralEmail(finremCaseDetails, userAuthorisation, errors);
        if (!errors.isEmpty()) {
            return response(finremCaseData, null, errors);
        }

        if (!isConsented(finremCaseDetails)) {
            generalEmailCategoriser.categorise(finremCaseData);
        }

        return response(finremCaseData);
    }

    private void convertEmailAttachmentsToPdfIfRequired(FinremCaseData finremCaseData, String userAuthorisation) {
        emptyIfNull(finremCaseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocuments()).forEach(item -> {
            CaseDocument original = item.getValue();
            if (original != null) {
                CaseDocument pdfDocument =
                    genericDocumentService.convertDocumentIfNotPdfAlready(
                        original,
                        userAuthorisation,
                        finremCaseData.getCcdCaseType()
                    );

                item.setValue(pdfDocument);
            }
        });
    }

    private boolean isConsented(FinremCaseDetails finremCaseDetails) {
        return finremCaseDetails.isConsentedApplication();
    }

    private void sendGeneralEmail(FinremCaseDetails finremCaseDetails, String userAuthorisation, List<String> errors) {
        try {
            if (isConsented(finremCaseDetails)) {
                notificationService.sendConsentGeneralEmail(finremCaseDetails, userAuthorisation);
            } else {
                notificationService.sendContestedGeneralEmail(finremCaseDetails, userAuthorisation);
            }
        } catch (InvalidEmailAddressException e) {
            errors.add("Not a valid email address");
        } catch (SendEmailException e) {
            errors.add("An error occurred when sending the email");
        }
    }
}
