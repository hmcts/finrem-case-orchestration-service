package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChildrenOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentNatureOfApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.getYesOrNo;

@Component
public class ConsentInContestMiniFormADetailsMapper extends AbstractLetterDetailsMapper {

    public ConsentInContestMiniFormADetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getCaseData();
        MiniFormADetails.MiniFormADetailsBuilder builder = setApplicantFields(MiniFormADetails.builder(), caseDetails);
        builder = setRespondentFields(builder, caseDetails);
        builder = setNatureApplicationFields(builder, caseData);
        builder = setOtherData(builder, caseData);
        return builder.build();
    }

    private MiniFormADetails.MiniFormADetailsBuilder setApplicantFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                        FinremCaseDetails caseDetails) {
        ContactDetailsWrapper contactDetails = caseDetails.getCaseData().getContactDetailsWrapper();
        FinremCaseData caseData = caseDetails.getCaseData();
        return builder
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .appRespondentFmName(contactDetails.getRespondentFmName())
            .appRespondentLName(contactDetails.getRespondentLname())
            .solicitorName(caseData.getApplicantSolicitorName())
            .solicitorAddress(caseData.getApplicantSolicitorAddress())
            .solicitorFirm(contactDetails.getApplicantSolicitorFirm());
    }

    private MiniFormADetails.MiniFormADetailsBuilder setRespondentFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                         FinremCaseDetails caseDetails) {
        ContactDetailsWrapper contactDetails = caseDetails.getCaseData().getContactDetailsWrapper();
        FinremCaseData caseData = caseDetails.getCaseData();
        return builder
            .respondentSolicitorAddress(contactDetails.getRespondentSolicitorAddress())
            .respondentSolicitorEmail(contactDetails.getRespondentSolicitorEmail())
            .respondentSolicitorName(caseData.getRespondentSolicitorName())
            .respondentSolicitorFirm(contactDetails.getRespondentSolicitorFirm())
            .respondentSolicitorPhone(contactDetails.getRespondentSolicitorPhone())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .respondentAddress(contactDetails.getRespondentAddress())
            .respondentEmail(contactDetails.getRespondentEmail())
            .respondentPhone(contactDetails.getRespondentPhone())
            .appRespondentRep(getYesOrNo(contactDetails.getContestedRespondentRepresented()))
            .respondentAddressConfidential(getYesOrNo(contactDetails.getRespondentAddressHiddenFromApplicant()));
    }

    private MiniFormADetails.MiniFormADetailsBuilder setNatureApplicationFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                                FinremCaseData caseData) {

        return builder
            .natureOfApplication2(getNatureOfApplication2ListAsString(caseData))
            .natureOfApplication3a(caseData.getConsentOrderWrapper().getConsentNatureOfApplicationAddress())
            .natureOfApplication3b(caseData.getConsentOrderWrapper().getConsentNatureOfApplicationMortgage())
            .natureOfApplication5(getYesOrNo(caseData.getConsentOrderWrapper().getConsentNatureOfApplication5()))
            .natureOfApplication6(getNatureOfApplication6ListAsString(caseData))
            .natureOfApplication7(caseData.getConsentOrderWrapper().getConsentNatureOfApplication7());
    }

    private MiniFormADetails.MiniFormADetailsBuilder setOtherData(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                  FinremCaseData caseData) {
        return builder
            .authorisation2b(caseData.getAuthorisation2b())
            .authorisation3(String.valueOf(caseData.getAuthorisation3()))
            .authorisationName(caseData.getAuthorisationName())
            .authorisationFirm(caseData.getAuthorisationFirm())
            .issueDate(String.valueOf(caseData.getIssueDate()))
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .orderForChildrenQuestion1(getYesOrNo(caseData.getConsentOrderWrapper().getConsentOrderForChildrenQuestion1()));
    }

    private List<String> getNatureOfApplication2ListAsString(FinremCaseData caseData) {
        return  Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentNatureOfApplicationChecklist())
            .orElse(new ArrayList<>())
            .stream()
            .map(NatureApplication::getText)
            .collect(Collectors.toList());
    }

    private List<String> getNatureOfApplication6ListAsString(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentNatureOfApplication6())
            .orElse(new ArrayList<>())
            .stream()
            .map(ConsentNatureOfApplication::getId)
            .collect(Collectors.toList());
    }
}
