package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedApplicationNotApproved;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedApplicationNotApprovedListEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_DOCUMENT_PREVIEW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_OTHER_TEXT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_REASONS_FOR_REFUSAL;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestedNotApprovedService {

    public static final String REASON_FOR_REFUSAL = "reasonForRefusal";
    public static final String REASON_FOR_REFUSAL_OTHERS = "FR_ms_refusalReason_1";
    public static final String OTHERS_TEXT_ORDERS = "othersTextOrders";
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> reasonCodeToText = new ImmutableMap.Builder<String, String>()
        .put("FR_ms_refusalReason_2", "Please provide a breakdown of the pension values/property values as it is not possible"
            + " to understand the values of what each party will receive.")
        .put("FR_ms_refusalReason_3", "The proposed order does not appear to be fair taking account of S25 Matrimonial Causes Act 1973."
            + " The parties are requested to explain more fully the thinking behind the order and why it is fair.")
        .put("FR_ms_refusalReason_4", "Entire Divorce case to be transferred to the Applicant’s home court to consider listing directions.")
        .put("FR_ms_refusalReason_5", "Financial Remedy application to be transferred to the Applicant’s home court to consider listing directions.")
        .put("FR_ms_refusalReason_6", "Application should be fixed for hearing on first available date for 20 minutes when the Court will consider"
            + " whether the draft order should be approved. Both parties should attend and if they do not do so the Court may not approve the order.")
        .put("FR_ms_refusalReason_7", "The D81 form is incomplete.")
        .put("FR_ms_refusalReason_8", "It is unclear whether the Respondent has obtained independent legal advice.")
        .put("FR_ms_refusalReason_9", "The pension annex has not been attached.")
        .put("FR_ms_refusalReason_10", "Insufficient information has been provided as to the children’s housing needs and whether"
            + " they are met by the order.")
        .put("FR_ms_refusalReason_11", "Insufficient information has been provided as to the parties’ pension provision if the order were effected.")
        .put("FR_ms_refusalReason_12", "Insufficient information has been provided as to the justification for departure from equality of capital.")
        .put("FR_ms_refusalReason_13", "Insufficient information has been provided as to the parties’ housing needs and whether"
            + " they are met by the order.")
        .put("FR_ms_refusalReason_14", "Insufficient information has been provided as to the parties’ capital positions if the order were effected.")
        .build();

    public void previewDocument(String authorisationToken, CaseDetails caseDetails) {
        log.info("Generating contested not approved document preview for Case ID: {}", caseDetails.getId());
        CaseDocument applicationNotApprovedDocument = generateApplicationNotApprovedDocument(caseDetails, authorisationToken);
        caseDetails.getData().put(CONTESTED_APPLICATION_NOT_APPROVED_DOCUMENT_PREVIEW, applicationNotApprovedDocument);
    }

    public void addContestedNotApprovedEntry(CaseDetails caseDetails) {
        log.info("Adding contested not approved document for Case ID: {}", caseDetails.getId());
        addApplicationNotApprovedEntryToCaseData(caseDetails);
    }

    private CaseDocument generateApplicationNotApprovedDocument(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        prepareCaseDetailsForDocumentGeneration(caseDetailsCopy);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getContestedApplicationNotApprovedTemplate(),
            documentConfiguration.getContestedApplicationNotApprovedFileName());
    }

    private void prepareCaseDetailsForDocumentGeneration(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("ApplicantName", DocumentHelper.getApplicantFullName(caseDetails));
        caseData.put("RespondentName", DocumentHelper.getRespondentFullNameContested(caseDetails));
        caseData.put("Court", ContestedCourtHelper.getSelectedCourt(caseDetails));
        caseData.put("JudgeDetails",
            StringUtils.joinWith(" ",
                caseDetails.getData().get("applicationNotApprovedJudgeType"),
                caseDetails.getData().get("applicationNotApprovedJudgeName")));
        caseData.put("ContestOrderNotApprovedRefusalReasonsFormatted", formatRefusalReasons(caseDetails));
    }

    private String formatRefusalReasons(CaseDetails caseDetails) {
        Map<String, Object> caseData = documentHelper.deepCopy(caseDetails, CaseDetails.class).getData();
        List<String> refusalReasons = (List<String>) caseData.get(CONTESTED_APPLICATION_NOT_APPROVED_REASONS_FOR_REFUSAL);
        Collections.reverse(refusalReasons);

        StringBuilder formattedRefusalReasons = new StringBuilder();
        refusalReasons.forEach(reason -> {
            if (formattedRefusalReasons.length() > 0) {
                formattedRefusalReasons.append('\n');
            }
            formattedRefusalReasons.append("    \u2022 ");       // add bullet point prefix
            if (REASON_FOR_REFUSAL_OTHERS.equals(reason)) {
                formattedRefusalReasons.append(caseData.get(CONTESTED_APPLICATION_NOT_APPROVED_OTHER_TEXT));
            } else {
                formattedRefusalReasons.append(reasonCodeToText.get(reason));
            }
        });
        return formattedRefusalReasons.toString();
    }

    private void addApplicationNotApprovedEntryToCaseData(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        List applicationNotApprovedEntries = Optional.ofNullable(caseData.get(CONTESTED_APPLICATION_NOT_APPROVED))
            .map(documentHelper::convertToGenericList)
            .orElse(new ArrayList<>(1));

        applicationNotApprovedEntries.add(buildApplicationNotApprovedEntry(caseDetails));
        caseData.put(CONTESTED_APPLICATION_NOT_APPROVED, applicationNotApprovedEntries);
    }

    private ContestedApplicationNotApprovedListEntry buildApplicationNotApprovedEntry(CaseDetails caseDetails) {
        Map<String, Object> caseDataMappedToApplicationNotApproved = new ImmutableMap.Builder<String, String>()
            .put(CONTESTED_APPLICATION_NOT_APPROVED_REASONS_FOR_REFUSAL, REASON_FOR_REFUSAL)
            .put(CONTESTED_APPLICATION_NOT_APPROVED_OTHER_TEXT, OTHERS_TEXT_ORDERS)
            .put("applicationNotApprovedJudgeType", "judgeType")
            .put("applicationNotApprovedJudgeName", "judgeName")
            .put("applicationNotApprovedDateOfOrder", "dateOfOrder")
            .put(CONTESTED_APPLICATION_NOT_APPROVED_DOCUMENT_PREVIEW, "notApprovedDocument")
            .build()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, entry -> caseDetails.getData().get(entry.getKey())));

        removeOthersTextOrdersIfReasonOthersIsNotSelected(caseDataMappedToApplicationNotApproved);

        return ContestedApplicationNotApprovedListEntry.builder()
            .id(UUID.randomUUID().toString())
            .contestedApplicationNotApproved(
                objectMapper.convertValue(caseDataMappedToApplicationNotApproved, ContestedApplicationNotApproved.class))
            .build();
    }

    private void removeOthersTextOrdersIfReasonOthersIsNotSelected(Map<String, Object> applicationNotApprovedData) {
        List<String> refusalReasons = (List<String>) applicationNotApprovedData.get(REASON_FOR_REFUSAL);
        if (!refusalReasons.contains(REASON_FOR_REFUSAL_OTHERS)) {
            applicationNotApprovedData.remove(OTHERS_TEXT_ORDERS);
        }
    }
}
