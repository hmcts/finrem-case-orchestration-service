package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalorder.GeneralOrderDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralOrderCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralOrderService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final GeneralOrderDetailsMapper generalOrderDetailsMapper;

    private final BiFunction<FinremCaseDetails, String, Document> generateDocument = this::applyGenerateDocument;
    private final Function<FinremCaseDetails, Map<String, Object>> addExtraFields = this::applyAddExtraFields;

    public void createAndSetGeneralOrder(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating General Order for Case ID: {}", caseDetails.getId());

        Document generalOrderData = generateDocument.apply(caseDetails, authorisationToken);
        caseDetails.getCaseData().getGeneralOrderWrapper().setGeneralOrderPreviewDocument(generalOrderData);
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

    public void populateGeneralOrderCollection(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseData.getGeneralOrderWrapper().getGeneralOrderPreviewDocument());
        if (caseData.isConsentedApplication()) {
            populateGeneralOrderCollectionConsented(caseDetails);
        } else {
            populateGeneralOrderCollectionContested(caseDetails);
        }
    }

    private void populateGeneralOrderCollectionConsented(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        GeneralOrderCollection consentedData =  getGeneralOrderPreviewDocumentConsented(caseData);

        List<GeneralOrderCollection> generalOrderList =
            Optional.ofNullable(caseData.getGeneralOrderWrapper().getGeneralOrderCollection()).orElse(new ArrayList<>());

        generalOrderList.add(consentedData);
        caseData.getGeneralOrderWrapper().setGeneralOrderCollection(generalOrderList);
    }

    private GeneralOrderCollection getGeneralOrderPreviewDocumentConsented(FinremCaseData caseData) {
        return GeneralOrderCollection.builder()
            .value(GeneralOrder.builder()
                .generalOrderAddressTo(caseData.getGeneralOrderWrapper().getGeneralOrderAddressTo().getText())
                .generalOrderDocumentUpload(caseData.getGeneralOrderWrapper().getGeneralOrderPreviewDocument())
                .build())
            .build();
    }

    private void populateGeneralOrderCollectionContested(FinremCaseDetails caseDetails) {

        FinremCaseData caseData = caseDetails.getCaseData();
        ContestedGeneralOrderCollection contestedData = getContestedGeneralOrderPreviewDocument(caseData);

        if (caseData.isConsentedInContestedCase()) {
            List<ContestedGeneralOrderCollection> generalOrderList =
                Optional.ofNullable(caseData.getGeneralOrderWrapper().getGeneralOrdersConsent()).orElse(new ArrayList<>());

            generalOrderList.add(contestedData);
            caseData.getGeneralOrderWrapper().setGeneralOrdersConsent(generalOrderList);
            return;
        }

        List<ContestedGeneralOrderCollection> generalOrderList =
            Optional.ofNullable(caseData.getGeneralOrderWrapper().getGeneralOrders()).orElse(new ArrayList<>());
        generalOrderList.add(contestedData);

        caseData.getGeneralOrderWrapper().setGeneralOrders(generalOrderList);
    }

    private ContestedGeneralOrderCollection getContestedGeneralOrderPreviewDocument(FinremCaseData caseData) {
        return ContestedGeneralOrderCollection.builder()
            .value(ContestedGeneralOrder.builder()
                .generalOrderAddressTo(caseData.getGeneralOrderWrapper().getGeneralOrderAddressTo().getText())
                .additionalDocument(caseData.getGeneralOrderWrapper().getGeneralOrderPreviewDocument())
                .build())
            .build();
    }
}