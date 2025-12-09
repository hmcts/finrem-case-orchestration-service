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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class ConsentOrderInContestedMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService bulkPrintDocumentService;

    public ConsentOrderInContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             BulkPrintDocumentService bulkPrintDocumentService) {
        super(finremCaseDetailsMapper);
        this.bulkPrintDocumentService = bulkPrintDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.CONSENT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        final String caseId = caseDetails.getCaseIdAsString();
        FinremCaseData caseData = caseDetails.getData();

        List<CaseDocument> caseDocumentList = new ArrayList<>();
        CaseDocument consentOrder = caseData.getConsentOrder();
        caseDocumentList.add(consentOrder);

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();
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
            getOtherDocuments(caseId, caseDocumentList, caseDataBefore, consentOrderWrapper);
        }

        getPensionDocuments(caseId, caseData, caseDocumentList, caseDataBefore);

        CaseDocument variationOrderDocument = caseData.getConsentVariationOrderDocument();
        if (variationOrderDocument != null) {
            caseDocumentList.add(variationOrderDocument);
        }

        List<String> errors = new ArrayList<>();
        caseDocumentList.forEach(doc -> bulkPrintDocumentService
            .validateEncryptionOnUploadedDocument(doc, caseId, errors, userAuthorisation));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    private void getOtherDocuments(String caseId,
                                   List<CaseDocument> caseDocumentList,
                                   FinremCaseData caseDataBefore,
                                   ConsentOrderWrapper consentOrderWrapper) {
        List<OtherDocumentCollection> otherCollection = emptyIfNull(consentOrderWrapper.getConsentOtherCollection());

        log.info("No. of current otherCollection {} for caseId {}", otherCollection.size(), caseId);
        ConsentOrderWrapper consentOrderWrapperBefore = caseDataBefore.getConsentOrderWrapper();
        if (consentOrderWrapperBefore != null) {
            List<OtherDocumentCollection> otherCollectionBefore = consentOrderWrapperBefore.getConsentOtherCollection();
            if (otherCollectionBefore != null && !otherCollectionBefore.isEmpty()) {
                log.info("No. of before otherCollectionBefore {} for caseId {}", otherCollectionBefore.size(), caseId);
                otherCollection.removeAll(otherCollectionBefore);
            }
        }
        log.info("No. of otherCollection {} to check for caseId {}", otherCollection.size(), caseId);
        otherCollection.forEach(obj -> caseDocumentList.add(obj.getValue().getUploadedDocument()));
    }

    private void getPensionDocuments(String caseId, FinremCaseData caseData,
                                     List<CaseDocument> caseDocumentList, FinremCaseData caseDataBefore) {
        List<PensionTypeCollection> consentPensionCollection = emptyIfNull(caseData.getConsentPensionCollection());
        log.info("No. of current consentPensionCollection {} for caseId {}", consentPensionCollection.size(), caseId);
        List<PensionTypeCollection> consentPensionsBefore = caseDataBefore.getConsentPensionCollection();
        if (consentPensionsBefore != null && !consentPensionsBefore.isEmpty()) {
            log.info("No. of before consentPensionsBefore {} for caseId {}", consentPensionsBefore.size(), caseId);
            consentPensionCollection.removeAll(consentPensionsBefore);
        }
        log.info("No. of consentPensionCollection {} to check for caseId {}", consentPensionCollection.size(), caseId);
        consentPensionCollection.forEach(obj -> caseDocumentList.add(obj.getTypedCaseDocument().getPensionDocument()));
    }
}
