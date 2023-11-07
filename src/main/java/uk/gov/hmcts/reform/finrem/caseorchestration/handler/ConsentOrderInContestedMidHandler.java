package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ConsentOrderInContestedMidHandler extends FinremCallbackHandler<FinremCaseDataContested> {

    private final BulkPrintDocumentService service;

    public ConsentOrderInContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             BulkPrintDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
                && CaseType.CONTESTED.equals(caseType)
                && (EventType.CONSENT_ORDER.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle(
        FinremCallbackRequest<FinremCaseDataContested> callbackRequest, String userAuthorisation) {

        FinremCaseDetails<FinremCaseDataContested> caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for case id: {}",
                EventType.CONSENT_ORDER, caseId);
        FinremCaseDataContested caseData = caseDetails.getData();

        List<CaseDocument> caseDocumentList = new ArrayList<>();
        CaseDocument consentOrder = caseData.getConsentOrder();
        caseDocumentList.add(consentOrder);

        ConsentOrderWrapper consentOrderWrapper = caseData.getConsentOrderWrapper();
        if (!ObjectUtils.isEmpty(consentOrderWrapper)) {
            CaseDocument consentD81Joint = consentOrderWrapper.getConsentD81Joint();
            if (consentD81Joint != null) {
                caseDocumentList.add(consentD81Joint);
            }
            CaseDocument consentD81Applicant = consentOrderWrapper.getConsentD81Applicant();
            if (consentD81Applicant != null) {
                caseDocumentList.add(consentD81Applicant);
            }
            CaseDocument consentD81Respondent = consentOrderWrapper.getConsentD81Respondent();
            if (consentD81Respondent != null) {
                caseDocumentList.add(consentD81Respondent);
            }
            List<OtherDocumentCollection> otherCollection = consentOrderWrapper.getConsentOtherCollection();
            if (otherCollection != null && !otherCollection.isEmpty()) {
                otherCollection.forEach(obj -> caseDocumentList.add(obj.getValue().getUploadedDocument()));
            }
        }

        List<PensionTypeCollection> consentPensionCollection = caseData.getConsentPensionCollection();
        if (consentPensionCollection != null && !consentPensionCollection.isEmpty()) {
            consentPensionCollection.forEach(obj -> caseDocumentList.add(obj.getTypedCaseDocument().getPensionDocument()));
        }

        CaseDocument variationOrderDocument = caseData.getConsentVariationOrderDocument();
        if (variationOrderDocument != null) {
            caseDocumentList.add(variationOrderDocument);
        }

        List<String> errors = new ArrayList<>();
        caseDocumentList.forEach(doc -> service.validateEncryptionOnUploadedDocument(doc,
                caseId, errors, userAuthorisation));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder().data(caseData).errors(errors).build();
    }
}