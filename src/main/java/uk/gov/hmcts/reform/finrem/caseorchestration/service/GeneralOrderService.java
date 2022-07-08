package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalorder.GeneralOrderDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsentedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderContestedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_ADDRESS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralOrderService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;
    private final GeneralOrderDetailsMapper generalOrderDetailsMapper;

    private final BiFunction<FinremCaseDetails, String, Document> generateDocument = this::applyGenerateDocument;
    private final Function<FinremCaseDetails, Map<String, Object>> addExtraFields = this::applyAddExtraFields;

    public FinremCaseData createGeneralOrder(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating General Order for Case ID: {}", caseDetails.getId());

        return generateDocument
            .andThen(data -> setGeneralOrderPreviewDocument(data, caseDetails))
            .apply(documentHelper.deepCopy(caseDetails, FinremCaseDetails.class), authorisationToken);
    }

    public BulkPrintDocument getLatestGeneralOrderAsBulkPrintDocument(Map<String, Object> caseData) {
        CaseDocument latestGeneralOrder = documentHelper.getLatestGeneralOrder(caseData);
        return latestGeneralOrder != null
            ? BulkPrintDocument.builder().binaryFileUrl(latestGeneralOrder.getDocumentBinaryUrl()).build()
            : null;
    }

    public BulkPrintDocument getLatestGeneralOrderAsBulkPrintDocument(FinremCaseData caseData) {
        Document latestGeneralOrder = caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();
        return latestGeneralOrder != null
            ? BulkPrintDocument.builder().binaryFileUrl(latestGeneralOrder.getBinaryUrl()).build()
            : null;
    }

    private Document applyGenerateDocument(FinremCaseDetails caseDetails, String authorisationToken) {
        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            addExtraFields.apply(caseDetails),
            documentConfiguration.getGeneralOrderTemplate(),
            documentConfiguration.getGeneralOrderFileName());
    }

    private Map<String, Object> applyAddExtraFields(FinremCaseDetails caseDetails) {
        return generalOrderDetailsMapper.getGeneralOrderDetailsAsMap(
            caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());
    }

    private FinremCaseData setGeneralOrderPreviewDocument(Document generalOrderData, FinremCaseDetails caseDetails) {
        caseDetails.getCaseData().getGeneralOrderWrapper().setGeneralOrderPreviewDocument(generalOrderData);
        return caseDetails.getCaseData();
    }

    public Map<String, Object> populateGeneralOrderCollection(CaseDetails caseDetails) {
        caseDetails.getData().put(GENERAL_ORDER_LATEST_DOCUMENT,
            documentHelper.convertToCaseDocument(caseDetails.getData().get(GENERAL_ORDER_PREVIEW_DOCUMENT)));
        if (caseDataService.isConsentedApplication(caseDetails)) {
            return populateGeneralOrderCollectionConsented(caseDetails);
        } else {
            return populateGeneralOrderCollectionContested(caseDetails);
        }
    }

    private Map<String, Object> populateGeneralOrderCollectionConsented(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();

        GeneralOrderConsented generalOrder =
            new GeneralOrderConsented(documentHelper.convertToCaseDocument(caseData.get(GENERAL_ORDER_PREVIEW_DOCUMENT)),
                getAddressToFormatted(caseData));

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

        GeneralOrderContested generalOrder =
            new GeneralOrderContested(documentHelper.convertToCaseDocument(caseData.get(GENERAL_ORDER_PREVIEW_DOCUMENT)),
                getAddressToFormatted(caseData));

        GeneralOrderContestedData contestedData = new GeneralOrderContestedData(UUID.randomUUID().toString(), generalOrder);

        if (caseDataService.isConsentedInContestedCase(caseDetails)) {
            List<GeneralOrderContestedData> generalOrderList = Optional.ofNullable(caseData.get(GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED))
                .map(this::convertToGeneralOrderContestedList)
                .orElse(new ArrayList<>());
            generalOrderList.add(contestedData);

            caseData.put(GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED, generalOrderList);

        } else {
            List<GeneralOrderContestedData> generalOrderList = Optional.ofNullable(caseData.get(GENERAL_ORDER_COLLECTION_CONTESTED))
                .map(this::convertToGeneralOrderContestedList)
                .orElse(new ArrayList<>());
            generalOrderList.add(contestedData);

            caseData.put(GENERAL_ORDER_COLLECTION_CONTESTED, generalOrderList);
        }

        return caseData;
    }

    private List<GeneralOrderConsentedData> convertToGeneralOrderConsentedList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private List<GeneralOrderContestedData> convertToGeneralOrderContestedList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private String getAddressToFormatted(Map<String, Object> caseData) {
        String storedValue = String.valueOf(caseData.get(GENERAL_ORDER_ADDRESS_TO));
        if ("applicant".equals(storedValue)) {
            return "Applicant";
        } else if ("applicantSolicitor".equals(storedValue)) {
            return "Applicant Solicitor";
        } else if ("respondentSolicitor".equals(storedValue)) {
            return "Respondent Solicitor";
        } else {
            return "";
        }
    }
}