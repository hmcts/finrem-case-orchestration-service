package uk.gov.hmcts.reform.finrem.caseorchestration.notifications;

import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;

import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;

public abstract class TestData {

    public static FinremCaseDetails getContestedFinremCaseDetails() {
        return getContestedFinremCaseDetails(getDefaultContestedFinremCaseData());
    }

    public static FinremCaseDetails getContestedFinremCaseDetails(FinremCaseData caseData) {
        return FinremCaseDetailsBuilderFactory.from(12345L, CaseType.CONTESTED, caseData).build();
    }

    public static FinremCaseData getDefaultContestedFinremCaseData() {

        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setApplicantFmName("Victoria");
        caseData.getContactDetailsWrapper().setApplicantLname("Goodman");
        caseData.getContactDetailsWrapper().setApplicantSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setApplicantSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        caseData.getContactDetailsWrapper().setRespondentFmName("David");
        caseData.getContactDetailsWrapper().setRespondentLname("Goodman");
        caseData.setCcdCaseType(CaseType.CONTESTED);
        return caseData;
    }

    public static FinremCaseData getDefaultConsentedFinremCaseData() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setAppRespondentFmName("David");
        caseData.getContactDetailsWrapper().setAppRespondentLName("Goodman");
        caseData.getContactDetailsWrapper().setApplicantFmName("Victoria");
        caseData.getContactDetailsWrapper().setApplicantLname("Goodman");
        caseData.getContactDetailsWrapper().setSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        return caseData;
    }

    public static FinremCaseDetails getConsentedFinremCaseDetails() {
        return getConsentedFinremCaseDetails(getDefaultConsentedFinremCaseData());
    }

    public static FinremCaseDetails getConsentedFinremCaseDetails(FinremCaseData caseData) {
        return FinremCaseDetailsBuilderFactory.from(12345L, CaseType.CONSENTED, caseData).build();
    }
}
