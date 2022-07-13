package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class ContestedDraftOrderNotApprovedDetailsMapper extends AbstractLetterDetailsMapper {

    public ContestedDraftOrderNotApprovedDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        return ContestedDraftOrderNotApprovedDetails.builder()
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .court(courtDetailsMapper.getCourtDetails(courtList).getCourtName())
            .judgeDetails(getJudgeDetails(caseDetails))
            .contestOrderNotApprovedRefusalReasons(getFormattedRefusalReasons(caseDetails))
            .build();
    }

    private String getJudgeDetails(FinremCaseDetails caseDetails) {
        return StringUtils.joinWith(" ",
            caseDetails.getCaseData().getRefusalOrderJudgeType().getValue(),
            caseDetails.getCaseData().getRefusalOrderJudgeName());
    }

    private String getFormattedRefusalReasons(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<String> refusalReasons = caseData.getJudgeNotApprovedReasons().stream()
            .map(reason -> reason.getValue().getJudgeNotApprovedReasons())
            .collect(toList());

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
