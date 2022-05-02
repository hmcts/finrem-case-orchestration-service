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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address.AddresseeGeneratorService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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

    @Mock
    private AddresseeGeneratorService addresseeBuilder;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private CaseDataService caseDataService;

    @InjectMocks
    private NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator;
    private CaseDetails caseDetails;
    private RepresentationUpdate representationUpdate;

    @Before
    public void setUpTest() {
        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc-letter-notifications-add-and-revoke.json", new ObjectMapper());
        when(documentHelper.getApplicantFullName(caseDetails)).thenReturn(APPLICANT_FULL_NAME);
        when(documentHelper.getRespondentFullNameContested(caseDetails)).thenReturn(RESPONDENT_FULL_NAME_CONTESTED);
        when(documentHelper.getRespondentFullNameConsented(caseDetails)).thenReturn(RESPONDENT_FULL_NAME_CONSENTED);
        when(addresseeBuilder.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());

        representationUpdate = buildChangeOfRepresentation();
    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForApplicantWhenSolicitorAdded() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);
        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            noticeOfChangeLetterDetailsGenerator.generate(caseDetails, buildChangeOfRepresentation(),
                DocumentHelper.PaperNotificationRecipient.APPLICANT,
                NoticeType.ADD);

        assertLetterDetails(representationUpdate, noticeOfChangeLetterDetails, NoticeType.ADD, Boolean.FALSE);
        assertContestedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);

    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForApplicantWhenSolicitorRemoved() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.TRUE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, DocumentHelper.PaperNotificationRecipient.APPLICANT,
                NoticeType.REMOVE);

        assertLetterDetails(representationUpdate, noticeOfChangeLetterDetails, NoticeType.REMOVE, Boolean.TRUE);
        assertConsentedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);

    }

    private void assertLetterDetails(RepresentationUpdate representationUpdate, NoticeOfChangeLetterDetails noticeOfChangeLetterDetails,
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
            ? representationUpdate.getAdded().getOrganisation().getOrganisationName()
            : representationUpdate.getRemoved().getOrganisation().getOrganisationName()));
    }

    private void assertContestedCourtDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {
        Map<String, Object> courtDetails = noticeOfChangeLetterDetails.getCourtDetails();
        assertThat(courtDetails.get(COURT_DETAILS_NAME_KEY), is("Central Family Court"));
        assertThat(courtDetails.get(COURT_DETAILS_ADDRESS_KEY), is("Central Family Court, First Avenue House, 42-49 High Holborn, London WC1V 6NP"));
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
        return RepresentationUpdate.builder()
            .party("applicant")
            .clientName("John Smith")
            .by("Sir Solicitor")
            .via("Notice of Change")
            .date(LocalDate.now())
            .added(ChangedRepresentative.builder()
                .name("Sir Solicitor")
                .email("sirsolicitor1@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVA")
                    .organisationName("FRApplicantSolicitorFirm")
                    .build()).build())
            .removed(ChangedRepresentative.builder()
                .name("Sir Solicitor Remove")
                .email("sirsolicitor1@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVAR")
                    .organisationName("FRApplicantSolicitorFirmRemoved")
                    .build()).build()).build();
    }

}