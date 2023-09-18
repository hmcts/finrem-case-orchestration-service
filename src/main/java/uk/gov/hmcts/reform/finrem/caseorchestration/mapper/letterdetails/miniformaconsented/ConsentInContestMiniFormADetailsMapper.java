package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentNatureOfApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.MiniFormADetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class ConsentInContestMiniFormADetailsMapper extends AbstractLetterDetailsMapper {

    private final ConsentedApplicationHelper consentedApplicationHelper;

    public ConsentInContestMiniFormADetailsMapper(CourtDetailsMapper courtDetailsMapper,
                                                  ObjectMapper objectMapper,
                                                  ConsentedApplicationHelper consentedApplicationHelper) {
        super(courtDetailsMapper, objectMapper);
        this.consentedApplicationHelper = consentedApplicationHelper;
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtWrapper courtList) {
        FinremCaseData caseData = caseDetails.getData();
        MiniFormADetails.MiniFormADetailsBuilder builder = setApplicantFields(MiniFormADetails.builder(), caseDetails);
        builder = setRespondentFields(builder, caseDetails);
        builder = setNatureApplicationFields(builder, caseData);
        builder = setOtherData(builder, caseData);
        return builder.build();
    }

    private MiniFormADetails.MiniFormADetailsBuilder setApplicantFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                        FinremCaseDetails caseDetails) {
        ContactDetailsWrapper contactDetails = caseDetails.getData().getContactDetailsWrapper();
        FinremCaseData caseData = caseDetails.getData();
        return builder
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .solicitorName(caseData.getAppSolicitorName())
            .solicitorAddress(caseData.getAppSolicitorAddress())
            .solicitorReference(caseData.getContactDetailsWrapper().getSolicitorReference())
            .solicitorFirm(contactDetails.getApplicantSolicitorFirm());
    }

    private MiniFormADetails.MiniFormADetailsBuilder setRespondentFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                         FinremCaseDetails caseDetails) {
        ContactDetailsWrapper contactDetails = caseDetails.getData().getContactDetailsWrapper();
        FinremCaseData caseData = caseDetails.getData();
        return builder
            .appRespondentFmName(contactDetails.getRespondentFmName())
            .appRespondentLName(contactDetails.getRespondentLname())
            .respondentSolicitorAddress(contactDetails.getRespondentSolicitorAddress())
            .respondentSolicitorEmail(contactDetails.getRespondentSolicitorEmail())
            .respondentSolicitorName(caseData.getRespondentSolicitorName())
            .respondentSolicitorFirm(contactDetails.getRespondentSolicitorFirm())
            .respondentSolicitorPhone(contactDetails.getRespondentSolicitorPhone())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .respondentAddress(contactDetails.getRespondentAddress())
            .respondentEmail(contactDetails.getRespondentEmail())
            .respondentPhone(contactDetails.getRespondentPhone())
            .appRespondentRep(YesOrNo.getYesOrNo(contactDetails.getContestedRespondentRepresented()))
            .respondentAddressConfidential(YesOrNo.getYesOrNo(contactDetails.getRespondentAddressHiddenFromApplicant()));
    }

    private MiniFormADetails.MiniFormADetailsBuilder setNatureApplicationFields(MiniFormADetails.MiniFormADetailsBuilder builder,
                                                                                FinremCaseData caseData) {

        return builder
            .natureOfApplication2(getNatureOfApplication2ListAsString(caseData))
            .natureOfApplication3a(caseData.getConsentOrderWrapper().getConsentNatureOfApplicationAddress())
            .natureOfApplication3b(caseData.getConsentOrderWrapper().getConsentNatureOfApplicationMortgage())
            .natureOfApplication5(YesOrNo.getYesOrNo(caseData.getConsentOrderWrapper().getConsentNatureOfApplication5()))
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
            .orderForChildrenQuestion1(YesOrNo.getYesOrNo(caseData.getConsentOrderWrapper().getConsentOrderForChildrenQuestion1()))
            .orderType(consentedApplicationHelper.getOrderType(caseData));
    }

    private List<String> getNatureOfApplication2ListAsString(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentNatureOfApplicationChecklist())
            .orElse(new ArrayList<>())
            .stream()
            .map(NatureApplication::getText)
            .toList();
    }

    private List<String> getNatureOfApplication6ListAsString(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentNatureOfApplication6())
            .orElse(new ArrayList<>())
            .stream()
            .map(ConsentNatureOfApplication::getId)
            .toList();
    }
}
