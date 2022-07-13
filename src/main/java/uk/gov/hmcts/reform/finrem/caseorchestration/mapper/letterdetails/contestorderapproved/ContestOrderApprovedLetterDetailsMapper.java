package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestorderapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

@Component
public class ContestOrderApprovedLetterDetailsMapper extends AbstractLetterDetailsMapper {

    public ContestOrderApprovedLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        return ContestOrderApprovedLetterDetails.builder()
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .court(courtDetailsMapper.getCourtDetails(courtList).getCourtName())
            .judgeDetails(getJudgeDetails(caseDetails))
            .build();
    }

    private String getJudgeDetails(FinremCaseDetails caseDetails) {
        return StringUtils.joinWith(" ",
            caseDetails.getCaseData().getOrderApprovedJudgeType().getValue(),
            caseDetails.getCaseData().getOrderApprovedJudgeName());
    }
}
