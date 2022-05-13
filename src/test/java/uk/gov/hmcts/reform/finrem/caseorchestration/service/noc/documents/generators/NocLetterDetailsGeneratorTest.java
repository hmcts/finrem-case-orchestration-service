package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
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

@RunWith(MockitoJUnitRunner.class)
public class NocLetterDetailsGeneratorTest {

    protected static final String APPLICANT_FULL_NAME = "applicantFullName";
    protected static final String RESPONDENT_FULL_NAME_CONTESTED = "respondentFullNameContested";
    protected static final String RESPONDENT_FULL_NAME_CONSENTED = "respondentFullNameConsented";
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String ADDRESSEE_NAME = "addresseeName";

    protected static final String ORGANISATION_ADDED_NAME = "organisationAdded";
    protected static final String ORGANISATION_REMOVED_NAME = "organisationRemoved";
    protected static final String ORGANISATION_ID_ADDED = "A31PTVA";
    protected static final String ORGANISATION_ID_REMOVED = "A31PTVAR";

    @Mock
    private AddresseeGeneratorService addresseeBuilder;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private PrdOrganisationService prdOrganisationService;

    @InjectMocks
    private NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator;
    private CaseDetails caseDetails;
    private CaseDetails caseDetailsBefore;
    private RepresentationUpdate representationUpdate;
    private ChangedRepresentative changedRepresentativeRemoved;
    private ChangedRepresentative changedRepresentativeAdded;

    @Before
    public void setUpTest() {
        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke.json",
            new ObjectMapper());
        caseDetailsBefore = caseDetailsFromResource(
            "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-before.json",
            new ObjectMapper());
        when(documentHelper.getApplicantFullName(any(CaseDetails.class))).thenReturn(APPLICANT_FULL_NAME);
        when(documentHelper.getRespondentFullNameContested(any(CaseDetails.class))).thenReturn(RESPONDENT_FULL_NAME_CONTESTED);
        when(documentHelper.getRespondentFullNameConsented(any(CaseDetails.class))).thenReturn(RESPONDENT_FULL_NAME_CONSENTED);

        representationUpdate = buildChangeOfRepresentation();
    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForApplicantWhenSolicitorAdded() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);
        when(addresseeBuilder.generateAddressee(caseDetailsBefore, changedRepresentativeAdded, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());
        when(prdOrganisationService.findOrganisationByOrgId(ORGANISATION_ID_ADDED))
            .thenReturn(OrganisationsResponse.builder().name(ORGANISATION_ADDED_NAME).build());

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            noticeOfChangeLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, buildChangeOfRepresentation(),
                DocumentHelper.PaperNotificationRecipient.APPLICANT,
                NoticeType.ADD);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.ADD, Boolean.FALSE);
        assertContestedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);

    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForApplicantWhenSolicitorRemoved() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.TRUE);
        when(addresseeBuilder.generateAddressee(caseDetails, changedRepresentativeRemoved, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());
        when(prdOrganisationService.findOrganisationByOrgId(ORGANISATION_ID_REMOVED))
            .thenReturn(OrganisationsResponse.builder().name(ORGANISATION_REMOVED_NAME).build());

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            noticeOfChangeLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, representationUpdate,
                DocumentHelper.PaperNotificationRecipient.APPLICANT,
                NoticeType.REMOVE);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.REMOVE, Boolean.TRUE);
        assertConsentedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);


    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForSolicitorWhenAdded() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);
        when(addresseeBuilder.generateAddressee(caseDetailsBefore, changedRepresentativeAdded, DocumentHelper.PaperNotificationRecipient.SOLICITOR))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());
        when(prdOrganisationService.findOrganisationByOrgId(ORGANISATION_ID_ADDED))
            .thenReturn(OrganisationsResponse.builder().name(ORGANISATION_ADDED_NAME).build());

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            noticeOfChangeLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, buildChangeOfRepresentation(),
                DocumentHelper.PaperNotificationRecipient.SOLICITOR,
                NoticeType.ADD);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.ADD, Boolean.FALSE);
        assertContestedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);
        assertThat(noticeOfChangeLetterDetails.getNoticeOfChangeText(),
            is("Your notice of change has been completed successfully. You can now view your client's case."));

    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForSolicitorWhenRemoved() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);
        when(addresseeBuilder.generateAddressee(caseDetails, changedRepresentativeRemoved, DocumentHelper.PaperNotificationRecipient.SOLICITOR))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());
        when(prdOrganisationService.findOrganisationByOrgId(ORGANISATION_ID_REMOVED))
            .thenReturn(OrganisationsResponse.builder().name(ORGANISATION_REMOVED_NAME).build());

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            noticeOfChangeLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, buildChangeOfRepresentation(),
                DocumentHelper.PaperNotificationRecipient.SOLICITOR,
                NoticeType.REMOVE);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.REMOVE, Boolean.FALSE);
        assertContestedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);
        assertThat(noticeOfChangeLetterDetails.getNoticeOfChangeText(),
            is("You've completed notice of acting on this, your access to this case has now been revoked."));

    }

    private void assertLetterDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails,
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

    private void assertContestedCourtDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {
        Map<String, Object> courtDetails = noticeOfChangeLetterDetails.getCourtDetails();
        assertThat(courtDetails.get(COURT_DETAILS_NAME_KEY), is("Central Family Court"));
        assertThat(courtDetails.get(COURT_DETAILS_ADDRESS_KEY),
            is("Central Family Court, First Avenue House, 42-49 High Holborn, London WC1V 6NP"));
        assertThat(courtDetails.get(COURT_DETAILS_PHONE_KEY), is("0207 421 8594"));
        assertThat(courtDetails.get(COURT_DETAILS_EMAIL_KEY), is("cfc.fru@justice.gov.uk"));
    }

    private void assertConsentedCourtDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {
        Map<String, Object> courtDetails = noticeOfChangeLetterDetails.getCourtDetails();
        assertThat(courtDetails.get(COURT_DETAILS_NAME_KEY), is("Family Court at the Courts and Tribunal Service Centre"));
        assertThat(courtDetails.get(COURT_DETAILS_ADDRESS_KEY), is("PO Box 12746, Harlow, CM20 9QZ"));
        assertThat(courtDetails.get(COURT_DETAILS_PHONE_KEY), is("0300 303 0642"));
        assertThat(courtDetails.get(COURT_DETAILS_EMAIL_KEY), is("contactFinancialRemedy@justice.gov.uk"));
    }

    private void assertAddresseeDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {
        Addressee addressee = noticeOfChangeLetterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(ADDRESSEE_NAME));
    }

    private RepresentationUpdate buildChangeOfRepresentation() {
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