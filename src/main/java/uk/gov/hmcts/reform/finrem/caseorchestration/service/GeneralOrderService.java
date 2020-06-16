package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER;

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
        GeneralOrder generalOrder = new GeneralOrder();
        generalOrder.setGeneralOrder(caseDocument);

        //GeneralOrderData generalOrderData = new GeneralOrderData();
        //generalOrderData.setGeneralOrder(generalOrder);

        return generalOrder;
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
}
