package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestorderapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ContestOrderApprovedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.time.LocalDate;
import java.util.Optional;


@Component
public class ContestOrderApprovedLetterDetailsMapper extends AbstractLetterDetailsMapper {

    public ContestOrderApprovedLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtWrapper courtList) {
        return ContestOrderApprovedLetterDetails.builder()
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .court(courtDetailsMapper.getCourtDetails(courtList).getCourtName())
            .judgeDetails(getJudgeDetails(caseDetails))
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .orderApprovedDate(String.valueOf(caseDetails.getData().getOrderApprovedDate()))
            .civilPartnership(YesOrNo.getYesOrNo(caseDetails.getData().getCivilPartnership()))
            .letterDate(String.valueOf(LocalDate.now()))
            .build();
    }

    private String getJudgeDetails(FinremCaseDetails caseDetails) {
        return StringUtils.joinWith(" ",
            getOrderApprovedJudgeType(caseDetails),
            caseDetails.getData().getOrderApprovedJudgeName());
    }

    private String getOrderApprovedJudgeType(FinremCaseDetails caseDetails) {
        return Optional.ofNullable(caseDetails.getData().getOrderApprovedJudgeType()).map(JudgeType::getValue)
            .orElse("");
    }
}
