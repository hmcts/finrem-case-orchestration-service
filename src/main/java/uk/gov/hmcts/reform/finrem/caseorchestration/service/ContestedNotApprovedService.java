package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedApplicationNotApproved;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedApplicationNotApprovedListEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_DOCUMENT_PREVIEW;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestedNotApprovedService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;
    private final ObjectMapper objectMapper;

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
        return genericDocumentService.generateDocument(authorisationToken, caseDetails,
            documentConfiguration.getGeneralLetterTemplate(), documentConfiguration.getGeneralLetterFileName());
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
            .put("applicationNotApprovedReasonForRefusal", "reasonForRefusal")
            .put("applicationNotApprovedOthersTextOrders", "othersTextOrders")
            .put("applicationNotApprovedJudgeType", "judgeType")
            .put("applicationNotApprovedJudgeName", "judgeName")
            .put("applicationNotApprovedDateOfOrder", "dateOfOrder")
            .put(CONTESTED_APPLICATION_NOT_APPROVED_DOCUMENT_PREVIEW, "notApprovedDocument")
            .build()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, entry -> caseDetails.getData().get(entry.getKey())));

        return ContestedApplicationNotApprovedListEntry.builder()
            .id(UUID.randomUUID().toString())
            .contestedApplicationNotApproved(
                objectMapper.convertValue(caseDataMappedToApplicationNotApproved, ContestedApplicationNotApproved.class))
            .build();
    }
}
