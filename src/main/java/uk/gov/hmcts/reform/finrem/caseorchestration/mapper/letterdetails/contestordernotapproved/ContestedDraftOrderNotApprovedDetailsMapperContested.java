package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ContestedDraftOrderNotApprovedDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class ContestedDraftOrderNotApprovedDetailsMapperContested extends ContestedAbstractLetterDetailsMapper {

    public ContestedDraftOrderNotApprovedDetailsMapperContested(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                                                CourtListWrapper courtList) {
        return ContestedDraftOrderNotApprovedDetails.builder()
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .court(courtDetailsMapper.getCourtDetails(courtList).getCourtName())
            .judgeDetails(getJudgeDetails(caseDetails))
            .contestOrderNotApprovedRefusalReasons(getFormattedRefusalReasons(caseDetails))
            .civilPartnership(YesOrNo.getYesOrNo(caseDetails.getData().getCivilPartnership()))
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .refusalOrderDate(String.valueOf(caseDetails.getData().getRefusalOrderDate()))
            .build();
    }

    private String getJudgeDetails(FinremCaseDetails<FinremCaseDataContested> caseDetails) {
        return StringUtils.joinWith(" ",
            caseDetails.getData().getRefusalOrderJudgeType().getValue(),
            caseDetails.getData().getRefusalOrderJudgeName());
    }

    private String getFormattedRefusalReasons(FinremCaseDetails<FinremCaseDataContested> caseDetails) {
        FinremCaseDataContested caseData = caseDetails.getData();
        List<String> refusalReasons = Optional.ofNullable(caseData.getJudgeNotApprovedReasons())
            .orElse(new ArrayList<>())
            .stream()
            .map(reason -> reason.getValue().getJudgeNotApprovedReasons())
            .toList();

        StringBuilder formattedRefusalReasons = new StringBuilder();
        refusalReasons.forEach(reason -> {
            if (formattedRefusalReasons.length() > 0) {
                formattedRefusalReasons.append('\n');
            }
            formattedRefusalReasons.append("- ");
            formattedRefusalReasons.append(reason);
        });
        return formattedRefusalReasons.toString();
    }
}
