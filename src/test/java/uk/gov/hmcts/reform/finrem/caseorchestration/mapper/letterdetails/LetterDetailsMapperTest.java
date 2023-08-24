package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.BasicLetterDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_CARE_OF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.formatAddressForLetterPrinting;

public class LetterDetailsMapperTest extends ContestedContestedAbstractLetterDetailsMapperTest {

    public static final String TEST_JSON_CONTESTED = "/fixtures/basic-letter-details.json";

    @Autowired
    private LetterDetailsMapper letterDetailsMapper;

    @Before
    public void setUp() throws Exception {
        setCaseDetails(TEST_JSON_CONTESTED);
    }

    @Test
    public void givenAppSolRecipient_whenBuildLetterDetails_thenReturnExpectedLetterDetails() {
        BasicLetterDetails expected = getExpectedBasicLetterDetails("SolicitorName",
            "50 ApplicantSolicitor Street",
            DocumentHelper.PaperNotificationRecipient.APP_SOLICITOR);

        BasicLetterDetails actual = letterDetailsMapper.buildLetterDetails(caseDetails,
            DocumentHelper.PaperNotificationRecipient.APPLICANT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    public void givenApplicantRecipient_whenBuildLetterDetails_thenReturnExpectedLetterDetails() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setSolicitorReference("");
        BasicLetterDetails expected = getExpectedBasicLetterDetails("Applicant Name",
            "50 Applicant Street",
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        BasicLetterDetails actual = letterDetailsMapper.buildLetterDetails(caseDetails,
            DocumentHelper.PaperNotificationRecipient.APPLICANT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    public void givenRespSolRecipient_whenBuildLetterDetails_thenReturnExpectedLetterDetails() {
        BasicLetterDetails expected = getExpectedBasicLetterDetails("RespSolicitorName",
            "50 RespondentSolicitor Street",
            DocumentHelper.PaperNotificationRecipient.RESP_SOLICITOR);

        BasicLetterDetails actual = letterDetailsMapper.buildLetterDetails(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    public void givenRespondentRecipient_whenBuildLetterDetails_thenReturnExpectedLetterDetails() {
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorReference("");
        BasicLetterDetails expected = getExpectedBasicLetterDetails("Respondent Name",
            "50 Respondent Street",
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        BasicLetterDetails actual = letterDetailsMapper.buildLetterDetails(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetDetailsAsMap_thenReturnExpectedLetterDetails() {
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorReference("");
        BasicLetterDetails expected = getExpectedBasicLetterDetails("Respondent Name",
            "50 Respondent Street",
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        Map<String, Object> placeholdersMap = letterDetailsMapper.getLetterDetailsAsMap(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        Map<String, Object> actualData = getCaseData(placeholdersMap);
        Map<String, Object> courtDetails = (Map<String, Object>) actualData.get("courtDetails");

        assertThat(actualData, allOf(
            hasEntry("applicantName", expected.getApplicantName()),
            hasEntry("respondentName", expected.getRespondentName()),
            hasEntry("caseNumber", expected.getCaseNumber()),
            hasKey("courtDetails")
        ));

        assertThat(courtDetails, allOf(
            hasEntry("courtName", expected.getCourtDetails().getCourtName()),
            hasEntry("courtAddress", expected.getCourtDetails().getCourtAddress())
        ));
    }

    private BasicLetterDetails getExpectedBasicLetterDetails(String name,
                                                             String addressLine1,
                                                             DocumentHelper.PaperNotificationRecipient recipient) {
        return BasicLetterDetails.builder()
            .applicantName("Applicant Name")
            .respondentName("Respondent Name")
            .divorceCaseNumber("DD12D12345")
            .ctscContactDetails(getCtscContactDetails())
            .addressee(getAddressee(name, addressLine1))
            .courtDetails(getCourtDetails())
            .letterDate(String.valueOf(LocalDate.now()))
            .reference(getReference(recipient))
            .caseNumber("1596638099618923")
            .orderType("consent")
            .build();
    }

    private Addressee getAddressee(String name, String addressLine1) {
        return Addressee.builder()
            .name(name)
            .formattedAddress(formatAddressForLetterPrinting(getAddress(addressLine1)))
            .build();
    }

    private Address getAddress(String addressLine1) {
        return Address.builder()
            .addressLine1(addressLine1)
            .addressLine2("Line2")
            .addressLine3("Line3")
            .postTown("London")
            .postCode("SE12 9SE")
            .country("United Kingdom")
            .county("Greater London")
            .build();
    }

    private String getReference(DocumentHelper.PaperNotificationRecipient recipient) {
        return switch (recipient) {
            case APPLICANT, RESPONDENT -> "";
            case APP_SOLICITOR -> "SolicitorReference";
            case RESP_SOLICITOR -> "RespSolicitorReference";
            default -> throw new IllegalStateException();
        };
    }

    private CtscContactDetails getCtscContactDetails() {
        return CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();
    }

    private FrcCourtDetails getCourtDetails() {
        DefaultCourtListWrapper courtListWrapper = new DefaultCourtListWrapper();
        courtListWrapper.setBristolCourtList(BristolCourt.BRISTOL_CIVIL_AND_FAMILY_JUSTICE_CENTRE);
        return new CourtDetailsMapper(new ObjectMapper()).getCourtDetails(courtListWrapper);
    }
}