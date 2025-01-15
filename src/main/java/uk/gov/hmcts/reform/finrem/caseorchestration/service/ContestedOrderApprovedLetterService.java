package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LETTER_DATE_FORMAT;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestedOrderApprovedLetterService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;
    private final FinremCaseDetailsMapper mapper;

    public void generateAndStoreContestedOrderApprovedLetter(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, null, authorisationToken);
    }

    /**
     * Generates and stores the contested order approved cover letter for a given case.
     *
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Maps the {@link FinremCaseDetails} to a {@link CaseDetails} object.</li>
     *     <li>Creates a deep copy of the case details to avoid modifying the original data.</li>
     *     <li>Populates template variables using the provided judge details.</li>
     *     <li>Generates the approved order cover letter document using the configured template.</li>
     *     <li>Stores the generated document in the case data.</li>
     * </ul>
     *
     * @param finremCaseDetails  the case details containing data for generating the cover letter
     * @param judgeDetails       the details of the judge approving the order. It can be null.
     * @param authorisationToken the authorisation token for document generation service
     */
    public void generateAndStoreContestedOrderApprovedLetter(FinremCaseDetails finremCaseDetails, String judgeDetails, String authorisationToken) {
        CaseDetails caseDetails = mapper.mapToCaseDetails(finremCaseDetails);
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);

        populateTemplateVariables(caseDetailsCopy, judgeDetails);

        CaseDocument approvedOrderCoverLetter = genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getContestedOrderApprovedCoverLetterTemplate(caseDetails),
            documentConfiguration.getContestedOrderApprovedCoverLetterFileName());

        finremCaseDetails.getData().setOrderApprovedCoverLetter(approvedOrderCoverLetter);
    }

    public void generateAndStoreContestedOrderApprovedLetter(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        populateTemplateVariables(caseDetailsCopy, null);

        CaseDocument approvedOrderCoverLetter = genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getContestedOrderApprovedCoverLetterTemplate(caseDetails),
            documentConfiguration.getContestedOrderApprovedCoverLetterFileName());

        caseDetails.getData().put(CONTESTED_ORDER_APPROVED_COVER_LETTER, approvedOrderCoverLetter);
    }

    private void populateTemplateVariables(CaseDetails caseDetails, String judgeDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put("ApplicantName", documentHelper.getApplicantFullName(caseDetails));
        caseData.put("RespondentName", documentHelper.getRespondentFullNameContested(caseDetails));
        caseData.put("Court", CourtHelper.getSelectedCourt(caseDetails));
        caseData.put("JudgeDetails", judgeDetails == null
            ? StringUtils.joinWith(" ",
                caseDetails.getData().get(CONTESTED_ORDER_APPROVED_JUDGE_TYPE),
                caseDetails.getData().get(CONTESTED_ORDER_APPROVED_JUDGE_NAME))
            : judgeDetails);
        caseData.put("letterDate", DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()));
    }
}
