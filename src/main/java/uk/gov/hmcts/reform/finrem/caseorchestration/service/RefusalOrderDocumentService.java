package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrderDocumentType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildConsentedFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefusalOrderDocumentService {

    private static final String DOCUMENT_COMMENT = "System Generated";
    private static final Map<String, String> REFUSAL_KEYS =
        Map.of("Transferred to Applicant’s home Court", "Transferred to Applicant home Court - A",
            "Transferred to Applicant's home Court", "Transferred to Applicant home Court - B"
        );
    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;
    private final ConsentedApplicationHelper consentedApplicationHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final IdamService idamService;

    public FinremCaseData processConsentOrderNotApproved(FinremCaseDetails caseDetails, String authorisationToken) {
        CaseDocument refusalOrder = generateRefusalOrder(caseDetails, authorisationToken);
        FinremCaseData finremCaseData = caseDetails.getData();

        List<UploadOrderCollection> uploadOrders = Optional.ofNullable(finremCaseData.getUploadOrder())
            .orElse(new ArrayList<>());
        uploadOrders.add(UploadOrderCollection.builder()
            .id(UUID.randomUUID().toString())
            .value(getUploadOrder(refusalOrder))
            .build());
        finremCaseData.setUploadOrder(uploadOrders);
        if (caseDataService.isConsentedInContestedCase(caseDetails)) {
            List<ConsentOrderCollection> consentedNotApprovedOrders = Optional.ofNullable(finremCaseData
                .getConsentOrderWrapper().getConsentedNotApprovedOrders()).orElse(new ArrayList<>());
            ApprovedOrder approvedOrder = ApprovedOrder.builder().consentOrder(refusalOrder).build();
            ConsentOrderCollection consentOrderCollection = ConsentOrderCollection
                .builder()
                .approvedOrder(approvedOrder)
                .build();
            consentedNotApprovedOrders.add(consentOrderCollection);
            finremCaseData.getConsentOrderWrapper().setConsentedNotApprovedOrders(consentedNotApprovedOrders);
        }
        return addToOrderRefusalCollection(finremCaseData);
    }

    private UploadOrder getUploadOrder(CaseDocument refusalOrder) {
        return UploadOrder
            .builder()
            .documentType(UploadOrderDocumentType.GENERAL_ORDER)
            .documentDateAdded(LocalDate.now())
            .documentLink(refusalOrder)
            .documentComment(DOCUMENT_COMMENT)
            .build();
    }

    private FinremCaseData addToOrderRefusalCollection(FinremCaseData caseData) {
        OrderRefusalCollection orderRefusalCollectionNew = caseData.getOrderRefusalCollectionNew();
        List<OrderRefusalCollection> refusalCollections
            = Optional.ofNullable(caseData.getOrderRefusalCollection()).orElse(new ArrayList<>());
        OrderRefusalCollection refusalCollection = OrderRefusalCollection.builder()
            .value(orderRefusalCollectionNew.getValue())
            .build();
        refusalCollections.add(refusalCollection);
        caseData.setOrderRefusalCollection(refusalCollections);
        caseData.setOrderRefusalCollectionNew(null);
        return caseData;
    }

    public FinremCaseData previewConsentOrderNotApproved(String authorisationToken, FinremCaseDetails caseDetails) {
        CaseDocument refusalOrder = generateRefusalOrder(caseDetails, authorisationToken);
        FinremCaseData finremCaseData = caseDetails.getData();
        finremCaseData.setOrderRefusalPreviewDocument(refusalOrder);
        return finremCaseData;
    }


    public CaseDocument generateRefusalOrder(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        String rejectOrderFileName;
        if (Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(finremCaseData))) {
            rejectOrderFileName = documentConfiguration.getRejectedVariationOrderFileName();
        } else {
            rejectOrderFileName = documentConfiguration.getRejectedOrderFileName();
        }
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);

        OrderRefusalCollection orderRefusalCollectionNew = convertToRefusalOrder(caseDetailsCopy.getData().get("orderRefusalCollectionNew"));
        List<OrderRefusalOption> optionList = orderRefusalCollectionNew.getValue().getOrderRefusal();
        List<OrderRefusalOption> optionListTranslated = new ArrayList<>();
        optionList.forEach(s -> optionListTranslated.add(OrderRefusalOption
            .getOrderRefusalOption(REFUSAL_KEYS.getOrDefault(s.getId(), s.getId()))));
        orderRefusalCollectionNew.getValue().setOrderRefusal(optionListTranslated);
        caseDetailsCopy.getData().put("orderRefusalCollectionNew", orderRefusalCollectionNew);

        return genericDocumentService.generateDocument(authorisationToken,
            applyAddExtraFields(caseDetailsCopy),
            documentConfiguration.getRejectedOrderTemplate(finremCaseDetails),
            getRejectOrderFileNameWithDateTimeStamp(rejectOrderFileName));
    }

    private String getRejectOrderFileNameWithDateTimeStamp(String rejectOrderFileName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        LocalDateTime now = LocalDateTime.now();
        String dateTimeString = now.format(formatter);
        return new StringBuilder(rejectOrderFileName)
            .insert(rejectOrderFileName.length() - 4, "-" + dateTimeString).toString();
    }

    private OrderRefusalCollection convertToRefusalOrder(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }


    private CaseDetails applyAddExtraFields(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put("ApplicantName", documentHelper.getApplicantFullName(caseDetails));
        caseData.put("RefusalOrderHeader", "Sitting in the Family Court");
        if (caseDataService.isConsentedApplication(caseDetails)) {
            caseData.put("RespondentName", documentHelper.getRespondentFullNameConsented(caseDetails));
            caseData.put("CourtName", "SITTING in private");
            caseData.put("courtDetails", buildConsentedFrcCourtDetails());
        } else {
            caseData.put("RespondentName", documentHelper.getRespondentFullNameContested(caseDetails));
            caseData.put("CourtName", "SITTING AT the Family Court at the "
                + ContestedCourtHelper.getSelectedCourt(caseDetails));
            caseData.put("courtDetails", buildFrcCourtDetails(caseData));
        }
        if (Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(caseData))) {
            caseData.put("orderType", "variation");
        } else {
            caseData.put("orderType", "consent");
        }
        return caseDetails;
    }

    public FinremCaseData setDefaults(FinremCaseData caseData, String userAuthorisation) {
        OrderRefusalHolder refusalHolder = OrderRefusalHolder.builder()
            .orderRefusalDate(LocalDate.now())
            .orderRefusalJudgeName(idamService.getIdamFullName(userAuthorisation)).build();
        OrderRefusalCollection orderRefusalCollection = OrderRefusalCollection.builder().value(refusalHolder).build();
        caseData.setOrderRefusalCollectionNew(orderRefusalCollection);
        return caseData;
    }
}
