package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedRefusalOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedRefusalOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedRefusalOrderPreviewDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_REASONS_FOR_REFUSAL;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestedDraftOrderNotApprovedService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;
    private final ObjectMapper objectMapper;

    private BiFunction<CaseDetails, String, CaseDocument> generateDocument = this::applyGenerateDocument;
    private Function<CaseDocument, ContestedRefusalOrderPreviewDocument> createRefusalOrderData = this::applyRefusalOrderData;
    private UnaryOperator<CaseDetails> addExtraFields = this::applyAddExtraFields;

    public Map<String, Object> createRefusalOrder(String authorisationToken, CaseDetails caseDetails) {
        log.info("Generating General Order for Case ID: {}", caseDetails.getId());

        return generateDocument
            .andThen(createRefusalOrderData)
            .andThen(data -> previewRefusalOrderData(data, caseDetails))
            .apply(documentHelper.deepCopy(caseDetails, CaseDetails.class), authorisationToken);
    }

    private CaseDocument applyGenerateDocument(CaseDetails caseDetails, String authorisationToken) {
        return genericDocumentService.generateDocument(authorisationToken, addExtraFields.apply(caseDetails),
            documentConfiguration.getContestedDraftOrderNotApprovedTemplate(),
            documentConfiguration.getContestedDraftOrderNotApprovedFileName());
    }

    private Map<String, Object> previewRefusalOrderData(ContestedRefusalOrderPreviewDocument refusalOrderData, CaseDetails caseDetails) {
        caseDetails.getData().put(CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT, refusalOrderData.getRefusalOrder());
        return caseDetails.getData();
    }

    private ContestedRefusalOrderPreviewDocument applyRefusalOrderData(CaseDocument caseDocument) {
        return new ContestedRefusalOrderPreviewDocument(caseDocument);
    }

    private CaseDetails applyAddExtraFields(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put("ApplicantName", DocumentHelper.getApplicantFullName(caseDetails));
        caseData.put("RespondentName", DocumentHelper.getRespondentFullNameContested(caseDetails));
        caseData.put("Court", ContestedCourtHelper.getSelectedCourt(caseDetails));
        caseData.put("JudgeDetails",
            StringUtils.joinWith(" ",
                caseDetails.getData().get("refusalOrderJudgeType"),
                caseDetails.getData().get("refusalOrderJudgeName")));
        caseData.put("ContestOrderNotApprovedRefusalReasonsFormatted", formatRefusalReasons(caseDetails));

        return caseDetails;
    }

    private String formatRefusalReasons(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        List<Object> refusalReasons = (List<Object>) caseData.get(CONTESTED_APPLICATION_NOT_APPROVED_REASONS_FOR_REFUSAL);

        StringBuilder formattedRefusalReasons = new StringBuilder();
        refusalReasons.forEach(reason -> {
            if (formattedRefusalReasons.length() > 0) {
                formattedRefusalReasons.append('\n');
            }
            formattedRefusalReasons.append("- ");
            formattedRefusalReasons.append(((Map<String, Map>)reason).get("value").get("judgeNotApprovedReasons"));
        });
        return formattedRefusalReasons.toString();
    }

    public Map<String, Object> populateRefusalOrderCollection(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();

        caseDetails.getData().put(CONTESTED_APPLICATION_NOT_APPROVED_LATEST_DOCUMENT,
            documentHelper.convertToCaseDocument(caseDetails.getData().get(CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT)));

        ContestedRefusalOrder refusalOrder =
            new ContestedRefusalOrder(documentHelper.convertToCaseDocument(
                caseData.get(CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT)));

        ContestedRefusalOrderData contestedData = new ContestedRefusalOrderData(UUID.randomUUID().toString(), refusalOrder);

        List<ContestedRefusalOrderData> refusalOrderList = Optional.ofNullable(caseData.get(CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION))
            .map(this::convertToRefusalOrderContestedList)
            .orElse(new ArrayList<>());

        refusalOrderList.add(contestedData);
        caseData.put(CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION, refusalOrderList);

        return caseData;
    }

    private List<ContestedRefusalOrderData> convertToRefusalOrderContestedList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<ContestedRefusalOrderData>>() {});
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
