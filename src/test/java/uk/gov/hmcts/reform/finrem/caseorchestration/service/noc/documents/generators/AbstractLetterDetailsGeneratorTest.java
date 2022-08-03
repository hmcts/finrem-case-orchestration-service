package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.mockito.Mock;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address.AddresseeGeneratorService;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Organisation;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator.LETTER_DATE_FORMAT;

public class AbstractLetterDetailsGeneratorTest {

    protected static final String APPLICANT_FULL_NAME = "Poor Guy";
    protected static final String RESPONDENT_FULL_NAME_CONTESTED = "Mr Respondent Respondent";
    protected static final String RESPONDENT_FULL_NAME_CONSENTED = "respondent FullNameConsented";
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
    @Mock
    protected CourtDetailsMapper courtDetailsMapper;
    @Mock
    protected ObjectMapper mapper;

    protected FinremCaseDetails caseDetails;
    protected FinremCaseDetails caseDetailsBefore;
    protected RepresentationUpdate representationUpdate;
    protected ChangedRepresentative changedRepresentativeRemoved;
    protected ChangedRepresentative changedRepresentativeAdded;

    @Before
    public void setUpTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        caseDetails = finremCaseDetailsFromResource(
            getResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke.json"),
            objectMapper);
        caseDetailsBefore = finremCaseDetailsFromResource(
            getResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-before.json"),
            objectMapper);

        representationUpdate = buildChangeOfRepresentation();
    }

    protected void assertLetterDetails(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails,
                                       NoticeType noticeType, boolean isConsented) {
        assertThat(noticeOfChangeLetterDetails.getCaseNumber(), is(String.valueOf(caseDetails.getId())));
        assertThat(noticeOfChangeLetterDetails.getReference(), is(caseDetails.getCaseData().getContactDetailsWrapper().getSolicitorReference()));
        assertThat(noticeOfChangeLetterDetails.getDivorceCaseNumber(), is(caseDetails.getCaseData().getDivorceCaseNumber()));
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
        assertThat(courtDetails.get(COURT_DETAILS_PHONE_KEY), is(CTSC_PHONE_NUMBER));
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
            .name("John Smith")
            .by("Sir Solicitor")
            .via("Notice of Change")
            .date(LocalDateTime.now())
            .added(changedRepresentativeAdded)
            .removed(changedRepresentativeRemoved).build();
    }

    protected FrcCourtDetails getContestedFrcCourtDetails() {
        return FrcCourtDetails.builder()
            .courtName("Central Family Court")
            .courtAddress("Central Family Court, First Avenue House, 42-49 High Holborn, London WC1V 6NP")
            .phoneNumber("0300 303 0642")
            .email("cfc.fru@justice.gov.uk")
            .build();
    }

    protected HashMap<String, Object> getContestedFrcCourtDetailsAsMap() {
        return new ObjectMapper().convertValue(getContestedFrcCourtDetails(),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
    }

    protected String getResource(String resourcePath) throws IOException {
        File file = ResourceUtils.getFile(this.getClass().getResource(resourcePath));
        return new String(Files.readAllBytes(file.toPath()));
    }
}
