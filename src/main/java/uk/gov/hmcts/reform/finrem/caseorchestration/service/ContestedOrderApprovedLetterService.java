package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestedOrderApprovedLetterService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;

    public void generateAndStoreContestedOrderApprovedLetter(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        populateTemplateVariables(caseDetailsCopy);

        CaseDocument approvedOrderCoverLetter = genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getContestedOrderApprovedCoverLetterTemplate(),
            documentConfiguration.getContestedOrderApprovedCoverLetterFileName());

        caseDetails.getData().put(CONTESTED_ORDER_APPROVED_COVER_LETTER, approvedOrderCoverLetter);
    }

    private void populateTemplateVariables(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put("ApplicantName", documentHelper.getApplicantFullName(caseDetails));
        caseData.put("RespondentName", documentHelper.getRespondentFullNameContested(caseDetails));
        caseData.put("Court", ContestedCourtHelper.getSelectedCourt(caseDetails));
        caseData.put("JudgeDetails",
            StringUtils.joinWith(" ",
                caseDetails.getData().get(CONTESTED_ORDER_APPROVED_JUDGE_TYPE),
                caseDetails.getData().get(CONTESTED_ORDER_APPROVED_JUDGE_NAME)));
        caseData.put("letterDate", LocalDate.now());
    }
}
