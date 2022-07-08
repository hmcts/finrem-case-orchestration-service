package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved.ContestedDraftOrderNotApprovedDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedRefusalOrderData;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RefusalOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.RefusalOrderCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestedDraftOrderNotApprovedService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;
    private final ObjectMapper objectMapper;
    private final ContestedDraftOrderNotApprovedDetailsMapper contestedDraftOrderNotApprovedDetailsMapper;

    private BiFunction<FinremCaseDetails, String, Document> generateDocument = this::applyGenerateDocument;
    private Function<FinremCaseDetails, Map<String, Object>> addExtraFields = this::applyAddExtraFields;

    public FinremCaseData createRefusalOrder(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating General Order for Case ID: {}", caseDetails.getId());

        return generateDocument
            .andThen(data -> previewRefusalOrderData(data, caseDetails))
            .apply(documentHelper.deepCopy(caseDetails, FinremCaseDetails.class), authorisationToken);
    }

    private Document applyGenerateDocument(FinremCaseDetails caseDetails, String authorisationToken) {
        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            addExtraFields.apply(caseDetails),
            documentConfiguration.getContestedDraftOrderNotApprovedTemplate(),
            documentConfiguration.getContestedDraftOrderNotApprovedFileName());
    }

    private FinremCaseData previewRefusalOrderData(Document refusalOrderData, FinremCaseDetails caseDetails) {
        caseDetails.getCaseData().setRefusalOrderPreviewDocument(refusalOrderData);
        return caseDetails.getCaseData();
    }

    private Map<String, Object> applyAddExtraFields(FinremCaseDetails caseDetails) {

        return contestedDraftOrderNotApprovedDetailsMapper.getConsentOrderApprovedLetterDetailsAsMap(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());
    }

    public FinremCaseData populateRefusalOrderCollection(FinremCaseDetails caseDetails) {

        FinremCaseData caseData = caseDetails.getCaseData();
        Document refusalOrder = caseData.getRefusalOrderPreviewDocument();
        caseData.setLatestRefusalOrder(refusalOrder);
        List<RefusalOrderCollection> refusalOrderCollection = Optional.ofNullable(caseData.getRefusalOrderCollection())
            .orElse(new ArrayList<>());

        refusalOrderCollection.add(
            RefusalOrderCollection.builder()
                .value(RefusalOrder.builder()
                    .refusalOrderAdditionalDocument(refusalOrder)
                    .build())
                .build());

        caseData.setRefusalOrderCollection(refusalOrderCollection);

        return caseData;
    }

    private List<ContestedRefusalOrderData> convertToRefusalOrderContestedList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public Optional<CaseDocument> getLatestRefusalReason(CaseDetails caseDetails) {
        if (isNull(caseDetails.getData().get(CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION))) {
            log.warn("Refusal order not found for printing for case");
            return Optional.empty();
        }

        List<ContestedRefusalOrderData> refusalOrderList = Optional.ofNullable(caseDetails.getData()
            .get(CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION))
            .map(this::convertToRefusalOrderContestedList)
            .orElse(new ArrayList<>());

        return Optional.of(refusalOrderList.get(refusalOrderList.size() - 1)
            .getContestedRefusalOrder()
            .getRefusalOrderAdditionalDocument());
    }
}
