package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformacontested;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.BenefitPayment;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamExemption;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamUrgencyReason;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.PropertyAdjustmentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.MiamWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.getYesOrNo;

@Component
public class ContestedMiniFormADetailsMapper extends AbstractLetterDetailsMapper {

    public ContestedMiniFormADetailsMapper(CourtDetailsMapper courtDetailsMapper,
                                           ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        ContactDetailsWrapper contactDetails = caseDetails.getCaseData().getContactDetailsWrapper();
        MiamWrapper miamDetails = caseDetails.getCaseData().getMiamWrapper();
        FinremCaseData caseData = caseDetails.getCaseData();

        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder =
            setApplicantDetails(ContestedMiniFormADetails.builder(), contactDetails, caseData);

        builder = setRespondentDetails(builder, contactDetails, caseData);
        builder = setNatureApplicationDetails(builder, caseData);
        builder = setCoreCaseData(builder, caseData, contactDetails);
        builder = setMiamDetails(builder, caseData, miamDetails);

        return builder.build();
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setApplicantDetails(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        ContactDetailsWrapper contactDetails,
        FinremCaseData caseData) {

        return builder
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .applicantAddress(contactDetails.getApplicantAddress())
            .applicantPhone(contactDetails.getApplicantPhone())
            .applicantEmail(contactDetails.getApplicantEmail())
            .applicantAddressConfidential(getYesOrNo(contactDetails.getApplicantAddressHiddenFromRespondent()))
            .applicantRepresented(getYesOrNo(contactDetails.getApplicantRepresented()))
            .applicantSolicitorName(caseData.getAppSolicitorName())
            .applicantSolicitorAddress(contactDetails.getApplicantSolicitorAddress())
            .applicantSolicitorFirm(contactDetails.getApplicantSolicitorFirm())
            .solicitorReference(contactDetails.getSolicitorReference());
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setRespondentDetails(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        ContactDetailsWrapper contactDetails,
        FinremCaseData caseData) {

        return builder
            .respondentFMName(contactDetails.getRespondentFmName())
            .respondentLName(contactDetails.getRespondentLname())
            .respondentAddress(contactDetails.getRespondentAddress())
            .respondentPhone(contactDetails.getRespondentPhone())
            .respondentEmail(contactDetails.getRespondentEmail())
            .respondentAddressConfidential(getYesOrNo(contactDetails.getRespondentAddressHiddenFromApplicant()))
            .respondentRepresented(getYesOrNo(contactDetails.getContestedRespondentRepresented()))
            .respondentSolicitorName(caseData.getRespondentSolicitorName())
            .respondentSolicitorAddress(contactDetails.getRespondentSolicitorAddress())
            .respondentSolicitorFirm(contactDetails.getRespondentSolicitorFirm())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .respondentSolicitorPhone(contactDetails.getRespondentSolicitorPhone())
            .respondentSolicitorEmail(contactDetails.getRespondentSolicitorEmail());
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setNatureApplicationDetails(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        FinremCaseData caseData) {
        return builder
            .natureOfApplicationChecklist(getNatureOfApplicationChecklist(caseData))
            .natureOfApplication7(caseData.getNatureApplicationWrapper().getNatureOfApplication7())
            .mortgageDetail(caseData.getMortgageDetail())
            .propertyAddress(caseData.getPropertyAddress())
            .propertyAdjustmentOrderDetail(getPropertyAdjustmentOrderDetailCollection(caseData))
            .paymentForChildrenDecision(getYesOrNo(caseData.getPaymentForChildrenDecision()))
            .benefitForChildrenDecision(getYesOrNo(caseData.getBenefitForChildrenDecision()))
            .benefitPaymentChecklist(getBenefitPaymentChecklist(caseData));
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setCoreCaseData(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        FinremCaseData caseData,
        ContactDetailsWrapper contactDetails) {
        return builder
            .fastTrackDecision(getYesOrNo(caseData.getFastTrackDecision()))
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .issueDate(String.valueOf(caseData.getIssueDate()))
            .authorisationName(caseData.getAuthorisationName())
            .authorisationFirm(contactDetails.getSolicitorFirm())
            .authorisation2b(caseData.getAuthorisation2b())
            .authorisation3(String.valueOf(caseData.getAuthorisation3()));
    }

    private ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder setMiamDetails(
        ContestedMiniFormADetails.ContestedMiniFormADetailsBuilder builder,
        FinremCaseData caseData,
        MiamWrapper miamDetails) {
        return builder
            .claimingExemptionMiam(getYesOrNo(miamDetails.getClaimingExemptionMiam()))
            .familyMediatorMiam(getYesOrNo(miamDetails.getFamilyMediatorMiam()))
            .applicantAttendedMiam(getYesOrNo(miamDetails.getApplicantAttendedMiam()))
            .miamExemptionsChecklist(getMiamExemptionsChecklist(caseData))
            .miamDomesticViolenceChecklist(getMiamDomesticViolenceChecklist(caseData))
            .miamUrgencyReasonChecklist(getMiamUrgencyReasonsChecklist(caseData))
            .miamPreviousAttendanceChecklist(getMiamPreviousAttendanceChecklist(miamDetails))
            .miamOtherGroundsChecklist(getMiamOtherGroundsChecklist(miamDetails));
    }

    private String getMiamOtherGroundsChecklist(MiamWrapper miamDetails) {
        return Optional.ofNullable(miamDetails.getMiamOtherGroundsChecklist())
            .map(MiamOtherGrounds::getText)
            .orElse(null);
    }

    private String getMiamPreviousAttendanceChecklist(MiamWrapper miamDetails) {
        return Optional.ofNullable(miamDetails.getMiamPreviousAttendanceChecklist())
            .map(MiamPreviousAttendance::getText)
            .orElse(null);
    }

    private List<String> getNatureOfApplicationChecklist(FinremCaseData caseData) {
        List<NatureApplication> natureApplicationList = caseData.getNatureApplicationWrapper()
            .getNatureOfApplicationChecklist();

        return Optional.ofNullable(natureApplicationList).orElse(new ArrayList<>()).stream()
            .map(NatureApplication::getText)
            .collect(Collectors.toList());
    }

    private List<String> getBenefitPaymentChecklist(FinremCaseData caseData) {
        List<BenefitPayment> benefitPaymentChecklist = caseData.getBenefitPaymentChecklist();

        return Optional.ofNullable(benefitPaymentChecklist).orElse(new ArrayList<>()).stream()
            .map(BenefitPayment::getText)
            .collect(Collectors.toList());
    }

    private List<String> getMiamExemptionsChecklist(FinremCaseData caseData) {
        List<MiamExemption> miamExemptions = caseData.getMiamWrapper().getMiamExemptionsChecklist();

        return Optional.ofNullable(miamExemptions).orElse(new ArrayList<>()).stream()
            .map(MiamExemption::getText)
            .collect(Collectors.toList());
    }

    private List<String> getMiamDomesticViolenceChecklist(FinremCaseData caseData) {
        List<MiamDomesticViolence> domesticViolenceCheklist = caseData.getMiamWrapper().getMiamDomesticViolenceChecklist();

        return Optional.ofNullable(domesticViolenceCheklist).orElse(new ArrayList<>()).stream()
            .map(MiamDomesticViolence::getText)
            .collect(Collectors.toList());
    }

    private List<String> getMiamUrgencyReasonsChecklist(FinremCaseData caseData) {
        List<MiamUrgencyReason> miamUrgencyReasons = caseData.getMiamWrapper().getMiamUrgencyReasonChecklist();

        return Optional.ofNullable(miamUrgencyReasons).orElse(new ArrayList<>()).stream()
            .map(MiamUrgencyReason::getText)
            .collect(Collectors.toList());
    }

    private List<PropertyAdjustmentOrderCollection> getPropertyAdjustmentOrderDetailCollection(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getPropertyAdjustmentOrderDetail()).orElse(new ArrayList<>());
    }
}
