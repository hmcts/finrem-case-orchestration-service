package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestorderapproved.ContestOrderApprovedLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestedOrderApprovedLetterService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final ContestOrderApprovedLetterDetailsMapper contestOrderApprovedLetterDetailsMapper;

    public void generateAndStoreContestedOrderApprovedLetter(FinremCaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> letterDetailsMap = contestOrderApprovedLetterDetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails,
                caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        Document approvedOrderCoverLetter = genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            letterDetailsMap,
            documentConfiguration.getContestedOrderApprovedCoverLetterTemplate(),
            documentConfiguration.getContestedOrderApprovedCoverLetterFileName());
        log.info("Approved order cover letter generated: Filename = {}, url = {}, binUrl = {}",
            approvedOrderCoverLetter.getFilename(), approvedOrderCoverLetter.getUrl(), approvedOrderCoverLetter.getBinaryUrl());

        caseDetails.getCaseData().setOrderApprovedCoverLetter(approvedOrderCoverLetter);
    }
}
