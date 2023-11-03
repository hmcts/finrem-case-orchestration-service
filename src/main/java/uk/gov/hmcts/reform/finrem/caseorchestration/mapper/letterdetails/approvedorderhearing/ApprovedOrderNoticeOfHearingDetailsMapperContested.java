package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.approvedorderhearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ApprovedOrderNoticeOfHearingDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
public class ApprovedOrderNoticeOfHearingDetailsMapperContested extends ContestedAbstractLetterDetailsMapper {
    public ApprovedOrderNoticeOfHearingDetailsMapperContested(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getData();
        HearingDirectionDetail hearingDirectionDetail = getHearingDirectionDetail(caseDetails);
        CourtDetailsTemplateFields selectedFRCDetails = courtDetailsMapper
            .getCourtDetails(hearingDirectionDetail.getLocalCourt().getDefaultCourtListWrapper());

        return ApprovedOrderNoticeOfHearingDetails.builder()
            .hearingType(getHearingType(hearingDirectionDetail))
            .hearingDate(nullToEmpty(hearingDirectionDetail.getDateOfHearing()))
            .hearingTime(hearingDirectionDetail.getHearingTime())
            .hearingVenue(selectedFRCDetails.getCourtContactDetailsAsOneLineAddressString())
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

    private String getHearingType(HearingDirectionDetail hearingDirectionDetail) {
        return Optional.ofNullable(hearingDirectionDetail.getTypeOfHearing()).map(HearingTypeDirection::getId).orElse(null);
    }

    private Optional<HearingDirectionDetail> getLatestAdditionalHearingDirections(FinremCaseDetails<FinremCaseDataContested> caseDetails) {
        List<HearingDirectionDetailsCollection> additionalHearingDetailsCollection =
            caseDetails.getData().getHearingDirectionDetailsCollection();

        return !CollectionUtils.isEmpty(additionalHearingDetailsCollection)
            ? Optional.of(Iterables.getLast(additionalHearingDetailsCollection).getValue())
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
