package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.mockito.Mock;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address.AddresseeGeneratorService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator.LETTER_DATE_FORMAT;

public class AbstractLetterDetailsGeneratorTest {

    protected static final String APPLICANT_FULL_NAME = "applicantFullName";
    protected static final String RESPONDENT_FULL_NAME_CONTESTED = "respondentFullNameContested";
    protected static final String RESPONDENT_FULL_NAME_CONSENTED = "respondentFullNameConsented";
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String ADDRESSEE_NAME = "addresseeName";

    protected static final String ORGANISATION_ADDED_NAME = "test firm";
    protected static final String ORGANISATION_REMOVED_NAME = "FRApplicantSolicitorFirmRemoved";
    protected static final String ORGANISATION_ID_ADDED = "A31PTVA";
    protected static final String ORGANISATION_ID_REMOVED = "A31PTVAR";

    @Mock
    protected AddresseeGeneratorService addresseeGeneratorService;
    @Mock
    protected DocumentHelper documentHelper;
    @Mock
    protected CaseDataService caseDataService;

    protected CaseDetails caseDetails;
    protected CaseDetails caseDetailsBefore;
    protected RepresentationUpdate representationUpdate;
    protected ChangedRepresentative changedRepresentativeRemoved;
    protected ChangedRepresentative changedRepresentativeAdded;

    @Before
    public void setUpTest() {
        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke.json",
            new ObjectMapper());
        caseDetailsBefore = caseDetailsFromResource(
            "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-before.json",
            new ObjectMapper());
        when(documentHelper.getApplicantFullName(any(CaseDetails.class))).thenReturn(APPLICANT_FULL_NAME);

        representationUpdate = buildChangeOfRepresentation();
    }


    protected void assertLetterDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails,
                                       NoticeType noticeType, boolean isConsented) {
        assertThat(noticeOfChangeLetterDetails.getCaseNumber(), is(caseDetails.getId().toString()));
        assertThat(noticeOfChangeLetterDetails.getReference(), is(caseDetails.getData().get(SOLICITOR_REFERENCE).toString()));
        assertThat(noticeOfChangeLetterDetails.getReference(), is(caseDetails.getData().get(SOLICITOR_REFERENCE).toString()));
        assertThat(noticeOfChangeLetterDetails.getDivorceCaseNumber(), is(caseDetails.getData().get(DIVORCE_CASE_NUMBER).toString()));
        assertThat(noticeOfChangeLetterDetails.getLetterDate(), is(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now())));
        assertThat(noticeOfChangeLetterDetails.getApplicantName(), is(APPLICANT_FULL_NAME));
        assertThat(noticeOfChangeLetterDetails.getRespondentName(),
            is(isConsented ? RESPONDENT_FULL_NAME_CONSENTED : RESPONDENT_FULL_NAME_CONTESTED));

        assertThat(noticeOfChangeLetterDetails.getSolicitorFirmName(), is(noticeType == NoticeType.ADD
            ? ORGANISATION_ADDED_NAME
            : ORGANISATION_REMOVED_NAME));
    }

    protected void assertContestedCourtDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {
        Map<String, Object> courtDetails = noticeOfChangeLetterDetails.getCourtDetails();
        assertThat(courtDetails.get(COURT_DETAILS_NAME_KEY), is("Central Family Court"));
        assertThat(courtDetails.get(COURT_DETAILS_ADDRESS_KEY),
            is("Central Family Court, First Avenue House, 42-49 High Holborn, London WC1V 6NP"));
        assertThat(courtDetails.get(COURT_DETAILS_PHONE_KEY), is("0207 421 8594"));
        assertThat(courtDetails.get(COURT_DETAILS_EMAIL_KEY), is("cfc.fru@justice.gov.uk"));
    }

    protected void assertConsentedCourtDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {
        Map<String, Object> courtDetails = noticeOfChangeLetterDetails.getCourtDetails();
        assertThat(courtDetails.get(COURT_DETAILS_NAME_KEY), is("Family Court at the Courts and Tribunal Service Centre"));
        assertThat(courtDetails.get(COURT_DETAILS_ADDRESS_KEY), is("PO Box 12746, Harlow, CM20 9QZ"));
        assertThat(courtDetails.get(COURT_DETAILS_PHONE_KEY), is("0300 303 0642"));
        assertThat(courtDetails.get(COURT_DETAILS_EMAIL_KEY), is("contactFinancialRemedy@justice.gov.uk"));
    }

    protected void assertAddresseeDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {
        Addressee addressee = noticeOfChangeLetterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(ADDRESSEE_NAME));
    }

    protected RepresentationUpdate buildChangeOfRepresentation() {
        changedRepresentativeRemoved = ChangedRepresentative.builder()
            .name("Sir Solicitor Remove")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_REMOVED)
                .organisationName("FRApplicantSolicitorFirmRemoved")
                .build()).build();
        changedRepresentativeAdded = ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_ADDED)
                .organisationName("FRApplicantSolicitorFirm")
                .build()).build();
        return RepresentationUpdate.builder()
            .party("applicant")
            .clientName("John Smith")
            .by("Sir Solicitor")
            .via("Notice of Change")
            .date(LocalDateTime.now())
            .added(changedRepresentativeAdded)
            .removed(changedRepresentativeRemoved).build();
    }
}
