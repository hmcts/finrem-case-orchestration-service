package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsentedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderContestedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralOrderService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    private BiFunction<CaseDetails, String, CaseDocument> generateDocument = this::applyGenerateDocument;
    private Function<CaseDocument, GeneralOrder> createGeneralOrderData = this::applyGeneralOrderData;
    private UnaryOperator<CaseDetails> addExtraFields = this::applyAddExtraFields;

    public Map<String, Object> createGeneralOrder(String authorisationToken, CaseDetails caseDetails) {

        log.info("Generating General Order for Case ID: {}", caseDetails.getId());
        return generateDocument
            .andThen(createGeneralOrderData)
            .andThen(data -> populateGeneralOrderData(data, caseDetails))
            .apply(documentHelper.deepCopy(caseDetails, CaseDetails.class), authorisationToken);
    }

    private CaseDocument applyGenerateDocument(CaseDetails caseDetails, String authorisationToken) {
        return genericDocumentService.generateDocument(authorisationToken, addExtraFields.apply(caseDetails),
            documentConfiguration.getGeneralOrderTemplate(),
            documentConfiguration.getGeneralOrderFileName());
    }

    private GeneralOrder applyGeneralOrderData(CaseDocument caseDocument) {
        return new GeneralOrder(caseDocument);
    }

    private CaseDetails applyAddExtraFields(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        data.put("ccdCaseNumber", caseDetails.getId());

        return caseDetails;
    }

    private Map<String, Object> populateGeneralOrderData(GeneralOrder generalOrderData, CaseDetails caseDetails) {
        caseDetails.getData().put(GENERAL_ORDER, generalOrderData);
        return caseDetails.getData();
    }

    public Map<String, Object> populateGeneralOrderCollection(CaseDetails caseDetails) {
        if (isConsentedApplication(caseDetails)) {
            return populateGeneralOrderCollectionConsented(caseDetails);
        } else {
            return populateGeneralOrderCollectionContested(caseDetails);
        }
    }

    private Map<String, Object> populateGeneralOrderCollectionConsented(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();

        GeneralOrderConsented generalOrder = new GeneralOrderConsented(documentHelper.convertToCaseDocument(caseData.get(GENERAL_ORDER)));

        GeneralOrderConsentedData consentedData = new GeneralOrderConsentedData(UUID.randomUUID().toString(), generalOrder);

        List<GeneralOrderConsentedData> generalOrderList = Optional.ofNullable(caseData.get(GENERAL_ORDER_COLLECTION_CONSENTED))
            .map(this::convertToGeneralOrderConsentedList)
            .orElse(new ArrayList<>());
        generalOrderList.add(consentedData);

        caseData.put(GENERAL_ORDER_COLLECTION_CONSENTED, generalOrderList);
        return caseData;
    }

    private Map<String, Object> populateGeneralOrderCollectionContested(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();

        GeneralOrderContested generalOrder = new GeneralOrderContested(documentHelper.convertToCaseDocument(caseData.get(GENERAL_ORDER)));

        GeneralOrderContestedData contestedData = new GeneralOrderContestedData(UUID.randomUUID().toString(), generalOrder);

        List<GeneralOrderContestedData> generalOrderList = Optional.ofNullable(caseData.get(GENERAL_ORDER_COLLECTION_CONTESTED))
            .map(this::convertToGeneralOrderContestedList)
            .orElse(new ArrayList<>());
        generalOrderList.add(contestedData);

        caseData.put(GENERAL_ORDER_COLLECTION_CONTESTED, generalOrderList);
        return caseData;
    }

    private List<GeneralOrderConsentedData> convertToGeneralOrderConsentedList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<GeneralOrderConsentedData>>() {
        });
    }

    private List<GeneralOrderContestedData> convertToGeneralOrderContestedList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<GeneralOrderContestedData>>() {
        });
    }
}
