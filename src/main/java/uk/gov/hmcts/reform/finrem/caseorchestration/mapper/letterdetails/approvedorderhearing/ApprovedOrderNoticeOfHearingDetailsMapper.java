package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.approvedorderhearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class ApprovedOrderNoticeOfHearingDetailsMapper extends AbstractLetterDetailsMapper {
    public ApprovedOrderNoticeOfHearingDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getCaseData();
        HearingDirectionDetail hearingDirectionDetail = getHearingDirectionDetail(caseDetails);
        FrcCourtDetails selectedFRCDetails = courtDetailsMapper.getCourtDetails(hearingDirectionDetail.getLocalCourt());

        return ApprovedOrderNoticeOfHearingDetails.builder()
            .hearingType(hearingDirectionDetail.getTypeOfHearing().getId())
            .hearingDate(hearingDirectionDetail.getDateOfHearing().toString())
            .hearingTime(hearingDirectionDetail.getHearingTime())
            .hearingLength(hearingDirectionDetail.getTimeEstimate())
            .additionalHearingDated(String.valueOf(new Date()))
            .courtName(selectedFRCDetails.getCourtName())
            .courtAddress(selectedFRCDetails.getCourtAddress())
            .courtEmail(selectedFRCDetails.getEmail())
            .courtPhone(selectedFRCDetails.getPhoneNumber())
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .ccdCaseNumber(caseDetails.getId())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .build();
    }

    private Optional<HearingDirectionDetail> getLatestAdditionalHearingDirections(FinremCaseDetails caseDetails) {
        List<HearingDirectionDetailsCollection> additionalHearingDetailsCollection =
            caseDetails.getCaseData().getHearingDirectionDetailsCollection();

        return additionalHearingDetailsCollection != null && !additionalHearingDetailsCollection.isEmpty()
            ? Optional.of(additionalHearingDetailsCollection.get(additionalHearingDetailsCollection.size() - 1).getValue())
            : Optional.empty();
    }

    private HearingDirectionDetail getHearingDirectionDetail(FinremCaseDetails caseDetails) {
        Optional<HearingDirectionDetail> hearingDirectionDetailOptional = getLatestAdditionalHearingDirections(caseDetails);

        if (hearingDirectionDetailOptional.isEmpty()) {
            throw new IllegalStateException("Invalid Case Data");
        }

        return hearingDirectionDetailOptional.get();
    }
}
