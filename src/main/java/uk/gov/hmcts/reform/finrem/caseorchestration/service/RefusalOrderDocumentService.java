package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.rejectedorder.RejectedOrderDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.OrderRefusalCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderDocumentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class RefusalOrderDocumentService {

    private static final String DOCUMENT_COMMENT = "System Generated";

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final RejectedOrderDetailsMapper rejectedOrderDetailsMapper;

    private final Function<Pair<FinremCaseDetails, String>, Document> generateDocument = this::applyGenerateRefusalOrder;
    private final Function<Document, UploadOrderCollection> createConsentOrderData = this::applyCreateConsentOrderCollection;

    public FinremCaseData generateConsentOrderNotApproved(String authorisationToken, final FinremCaseDetails caseDetails) {

        Document refusalOrder = generateDocument.apply(Pair.of(caseDetails, authorisationToken));
        UploadOrderCollection consentOrderData = createConsentOrderData.apply(refusalOrder);
        FinremCaseData caseData = populateConsentOrderData(consentOrderData, caseDetails);

        return copyToOrderRefusalCollection(caseData);
    }

    public FinremCaseData previewConsentOrderNotApproved(String authorisationToken, FinremCaseDetails caseDetails) {

        Document refusalOrder = generateDocument.apply(Pair.of(caseDetails, authorisationToken));
        return populateConsentOrderNotApproved(refusalOrder, caseDetails);
    }

    private FinremCaseData populateConsentOrderNotApproved(Document caseDocument, FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.setOrderRefusalPreviewDocument(caseDocument);
        return caseData;
    }

    private FinremCaseData populateConsentOrderData(UploadOrderCollection consentOrderData, FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();

        Optional.ofNullable(caseData.getUploadOrder()).ifPresentOrElse(
            collection -> collection.add(consentOrderData),
            () -> caseData.setUploadOrder(Lists.newArrayList(consentOrderData)));


        if (caseData.isConsentedInContestedCase()) {
            ConsentOrderCollection consentOrder = getConsentOrder(consentOrderData);

            Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders()).ifPresentOrElse(
                collection -> collection.add(consentOrder),
                () -> caseData.getConsentOrderWrapper().setConsentedNotApprovedOrders(Lists.newArrayList(consentOrder)));
        }

        return caseData;
    }

    private ConsentOrderCollection getConsentOrder(UploadOrderCollection consentOrderData) {
        return ConsentOrderCollection.builder()
            .value(ConsentOrder.builder()
                .consentOrder(consentOrderData.getValue().getDocumentLink())
                .build()).build();
    }

    private Document applyGenerateRefusalOrder(Pair<FinremCaseDetails, String> data) {
        Map<String, Object> refusalOrderDetailsMap = rejectedOrderDetailsMapper.getDocumentTemplateDetailsAsMap(data.getLeft(),
                data.getLeft().getCaseData().getRegionWrapper().getDefaultCourtList());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(data.getRight(), refusalOrderDetailsMap,
            documentConfiguration.getRejectedOrderTemplate(),
            documentConfiguration.getRejectedOrderFileName());
    }


    private UploadOrderCollection applyCreateConsentOrderCollection(Document document) {
        return UploadOrderCollection.builder()
            .value(UploadOrder.builder()
                .documentType(UploadOrderDocumentType.GENERAL_ORDER)
                .documentComment(DOCUMENT_COMMENT)
                .documentLink(document)
                .documentDateAdded(LocalDate.now())
                .build())
            .build();
    }

    private FinremCaseData copyToOrderRefusalCollection(FinremCaseData caseData) {
        if (nonNull(caseData.getOrderRefusalCollectionNew())) {
            List<OrderRefusalCollection> orderRefusalNew = caseData.getOrderRefusalCollectionNew();

            Optional.ofNullable(caseData.getOrderRefusalCollection()).ifPresentOrElse(
                collection -> collection.addAll(orderRefusalNew),
                () -> caseData.setOrderRefusalCollection(orderRefusalNew));

            caseData.setOrderRefusalCollectionNew(new ArrayList<>());
        }

        return caseData;
    }
}
