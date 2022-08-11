package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved.ContestedDraftOrderNotApprovedDetailsMapper;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestedDraftOrderNotApprovedService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final ContestedDraftOrderNotApprovedDetailsMapper contestedDraftOrderNotApprovedDetailsMapper;

    private final BiFunction<FinremCaseDetails, String, Document> generateDocument = this::applyGenerateDocument;
    private final Function<FinremCaseDetails, Map<String, Object>> getDocumentDetailsMap = this::applyGetDocumentDetailsMap;

    public void createAndSetRefusalOrderPreviewDocument(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating General Order for Case ID: {}", caseDetails.getId());

        Document contestedDraftOrderNotApproved = generateDocument.apply(caseDetails, authorisationToken);
        log.info("Contest draft order not approved document generated: Filename = {}, url = {}, binUrl = {}",
            contestedDraftOrderNotApproved.getFilename(), contestedDraftOrderNotApproved.getUrl(), contestedDraftOrderNotApproved.getBinaryUrl());
        caseDetails.getCaseData().setRefusalOrderPreviewDocument(contestedDraftOrderNotApproved);
    }

    private Document applyGenerateDocument(FinremCaseDetails caseDetails, String authorisationToken) {

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            getDocumentDetailsMap.apply(caseDetails),
            documentConfiguration.getContestedDraftOrderNotApprovedTemplate(),
            documentConfiguration.getContestedDraftOrderNotApprovedFileName());
    }

    private Map<String, Object> applyGetDocumentDetailsMap(FinremCaseDetails caseDetails) {

        return contestedDraftOrderNotApprovedDetailsMapper.getDocumentTemplateDetailsAsMap(caseDetails,
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

    public Optional<Document> getLatestRefusalReason(FinremCaseDetails caseDetails) {
        if (isNull(caseDetails.getCaseData().getRefusalOrderCollection())) {
            log.warn("Refusal order not found for printing for case");
            return Optional.empty();
        }

        List<RefusalOrderCollection> refusalOrderList = Optional.ofNullable(
            caseDetails.getCaseData().getRefusalOrderCollection()).orElse(new ArrayList<>());

        return Optional.of(Iterables.getLast(refusalOrderList)
            .getValue()
            .getRefusalOrderAdditionalDocument());
    }
}
