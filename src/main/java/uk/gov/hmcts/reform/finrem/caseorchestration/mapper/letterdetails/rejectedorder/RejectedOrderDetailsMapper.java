package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.rejectedorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.OrderRefusalCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

@Component
public class RejectedOrderDetailsMapper extends AbstractLetterDetailsMapper {

    public static final String REFUSAL_ORDER_HEADER =  "Sitting in the Family Court";
    public static final String CONTESTED_COURT_NAME = "SITTING AT the Family Court at the ";
    public static final String CONSENTED_COURT_NAME = "SITTING in private";

    private static final Map<String, String> REFUSAL_KEYS =
        ImmutableMap.of("Transferred to Applicant’s home Court", "Transferred to Applicant home Court - A",
            "Transferred to Applicant's home Court", "Transferred to Applicant home Court - B"
        );


    public RejectedOrderDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getCaseData();
        FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);
        return RejectedOrderDetails.builder()
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .civilPartnership(caseData.getCivilPartnership().getYesOrNo())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .refusalOrderHeader(REFUSAL_ORDER_HEADER)
            .courtDetails(courtDetails)
            .courtName(getCourtName(caseData))
            .orderRefusalCollectionNew(getTranslatedRefusalOrderCollection(caseData))
            .build();
    }

    private String getCourtName(FinremCaseData caseData) {
        return caseData.isContestedApplication()
            ? CONTESTED_COURT_NAME
            : CONSENTED_COURT_NAME;
    }

    private List<Element<TranslatedOrderRefusalDocument>> getTranslatedRefusalOrderCollection(FinremCaseData caseData) {

        return caseData.getOrderRefusalCollectionNew().stream().map(
            refusalOrder -> element(UUID.randomUUID(),
                TranslatedOrderRefusalDocument.builder()
                    .orderRefusal(getReasonsAsStringAndTranslate(refusalOrder))
                    .orderRefusalAddComments(refusalOrder.getValue().getOrderRefusalAddComments())
                    .orderRefusalAfterText(refusalOrder.getValue().getOrderRefusalAfterText())
                    .orderRefusalDate(refusalOrder.getValue().getOrderRefusalDate())
                    .orderRefusalJudge(refusalOrder.getValue().getOrderRefusalJudge())
                    .orderRefusalJudgeName(refusalOrder.getValue().getOrderRefusalJudgeName())
                    .orderRefusalDocs(refusalOrder.getValue().getOrderRefusalDocs())
                    .orderRefusalOther(refusalOrder.getValue().getOrderRefusalOther())
                    .build())
        ).collect(toList());
    }

    private List<String> getReasonsAsStringAndTranslate(OrderRefusalCollection refusalOrder) {
        return refusalOrder.getValue().getOrderRefusal().stream()
            .map(s -> REFUSAL_KEYS.getOrDefault(s.getId(), s.getId()))
            .collect(toList());
    }
}
