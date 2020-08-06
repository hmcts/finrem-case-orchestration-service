package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_REFUSAL_PREVIEW_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedInContestedCase;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderRefusalTranslator.copyToOrderRefusalCollection;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderRefusalTranslator.translateOrderRefusalCollection;

@Service
@RequiredArgsConstructor
public class RefusalOrderDocumentService {

    private static final String DOCUMENT_COMMENT = "System Generated";

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    private Function<Pair<CaseDetails, String>, CaseDocument> generateDocument = this::applyGenerateRefusalOrder;
    private Function<CaseDocument, ConsentOrderData> createConsentOrderData = this::applyCreateConsentOrderData;
    private UnaryOperator<CaseDetails> addExtraFields = this::applyAddExtraFields;

    public Map<String, Object> generateConsentOrderNotApproved(
        String authorisationToken, final CaseDetails caseDetails) {
        translateOrderRefusalCollection
            .andThen(generateDocument)
            .andThen(createConsentOrderData)
            .andThen(consentOrderData -> populateConsentOrderData(consentOrderData, caseDetails))
            .apply(Pair.of(documentHelper.deepCopy(caseDetails, CaseDetails.class), authorisationToken));
        return copyToOrderRefusalCollection(caseDetails);
    }

    public Map<String, Object> previewConsentOrderNotApproved(
        String authorisationToken, final CaseDetails caseDetails) {
        return translateOrderRefusalCollection
            .andThen(generateDocument)
            .andThen(caseDocument -> populateConsentOrderNotApproved(caseDocument, caseDetails))
            .apply(Pair.of(documentHelper.deepCopy(caseDetails, CaseDetails.class), authorisationToken));
    }

    private Map<String, Object> populateConsentOrderNotApproved(CaseDocument caseDocument, CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(ORDER_REFUSAL_PREVIEW_COLLECTION, caseDocument);
        return caseData;
    }


    private Map<String, Object> populateConsentOrderData(ConsentOrderData consentOrderData, CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<ConsentOrderData> uploadOrder = Optional.ofNullable(caseData.get(UPLOAD_ORDER))
            .map(this::convertToUploadOrderList)
            .orElse(new ArrayList<>());
        uploadOrder.add(consentOrderData);
        caseData.put(UPLOAD_ORDER, uploadOrder);

        if (isConsentedInContestedCase(caseDetails)) {
            ContestedConsentOrder consentOrder = new ContestedConsentOrder(consentOrderData.getConsentOrder().getDocumentLink());
            ContestedConsentOrderData contestedConsentOrderData = new ContestedConsentOrderData(UUID.randomUUID().toString(), consentOrder);
            List<ContestedConsentOrderData> consentOrders = Optional.ofNullable(caseData.get(CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION))
                .map(documentHelper::convertToContestedConsentOrderData)
                .orElse(new ArrayList<>(1));
            consentOrders.add(contestedConsentOrderData);
            caseData.put(CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION, consentOrders);
        }

        return caseData;
    }

    private CaseDocument applyGenerateRefusalOrder(Pair<CaseDetails, String> data) {
        return genericDocumentService.generateDocument(data.getRight(), addExtraFields.apply(data.getLeft()),
            documentConfiguration.getRejectedOrderTemplate(),
            documentConfiguration.getRejectedOrderFileName());
    }

    private ConsentOrderData applyCreateConsentOrderData(CaseDocument caseDocument) {
        ConsentOrder consentOrder = new ConsentOrder();
        consentOrder.setDocumentType(documentConfiguration.getRejectedOrderDocType());
        consentOrder.setDocumentDateAdded(new Date());
        consentOrder.setDocumentLink(caseDocument);
        consentOrder.setDocumentComment(DOCUMENT_COMMENT);

        ConsentOrderData consentOrderData = new ConsentOrderData();
        consentOrderData.setId(UUID.randomUUID().toString());
        consentOrderData.setConsentOrder(consentOrder);

        return consentOrderData;
    }

    private List<ConsentOrderData> convertToUploadOrderList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<ConsentOrderData>>() {
        });
    }

    private CaseDetails applyAddExtraFields(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put("ApplicantName", DocumentHelper.getApplicantFullName(caseDetails));
        caseData.put("RespondentName", isConsentedApplication(caseDetails)
            ? DocumentHelper.getRespondentFullNameConsented(caseDetails) :
            DocumentHelper.getRespondentFullNameContested(caseDetails));
        caseData.put("CourtName", isConsentedApplication(caseDetails)
            ? ConsentedCourtHelper.getSelectedCourt(caseDetails) :
            ContestedCourtHelper.getSelectedCourt(caseDetails));

        return caseDetails;
    }
}
