package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.BasicLetterDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_CARE_OF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.formatAddressForLetterPrinting;

@ExtendWith(MockitoExtension.class)
class LetterDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    public static final String TEST_JSON_CONTESTED = "/fixtures/basic-letter-details.json";

    @Mock
    private ConsentedApplicationHelper consentedApplicationHelper;

    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    private LetterDetailsMapper letterDetailsMapper;

    private CourtDetailsTemplateFields mockedCourtDetailsTemplateFields;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        setFinremCaseDetails(TEST_JSON_CONTESTED);

        letterDetailsMapper = new LetterDetailsMapper(mapper, courtDetailsMapper, consentedApplicationHelper);
        mockedCourtDetailsTemplateFields = mock(CourtDetailsTemplateFields.class);

        when(courtDetailsMapper.getCourtDetails(caseDetails.getData().getRegionWrapper().getDefaultCourtList()))
            .thenReturn(mockedCourtDetailsTemplateFields);
        when(consentedApplicationHelper.getOrderType(caseDetails.getData())).thenReturn("consent");
    }

    @Test
    void givenAppSolRecipient_whenBuildLetterDetails_thenReturnExpectedLetterDetails() {
        BasicLetterDetails expected = getExpectedBasicLetterDetails("SolicitorName",
            "50 ApplicantSolicitor Street",
            DocumentHelper.PaperNotificationRecipient.APP_SOLICITOR, mockedCourtDetailsTemplateFields);

        BasicLetterDetails actual = letterDetailsMapper.buildLetterDetails(caseDetails,
            DocumentHelper.PaperNotificationRecipient.APPLICANT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    void givenApplicantRecipient_whenBuildLetterDetails_thenReturnExpectedLetterDetails() {
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setSolicitorReference("");
        BasicLetterDetails expected = getExpectedBasicLetterDetails("Applicant Name",
            "50 Applicant Street",
            DocumentHelper.PaperNotificationRecipient.APPLICANT, mockedCourtDetailsTemplateFields);

        BasicLetterDetails actual = letterDetailsMapper.buildLetterDetails(caseDetails,
            DocumentHelper.PaperNotificationRecipient.APPLICANT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    void givenRespSolRecipient_whenBuildLetterDetails_thenReturnExpectedLetterDetails() {
        BasicLetterDetails expected = getExpectedBasicLetterDetails("RespSolicitorName",
            "50 RespondentSolicitor Street",
            DocumentHelper.PaperNotificationRecipient.RESP_SOLICITOR, mockedCourtDetailsTemplateFields);

        BasicLetterDetails actual = letterDetailsMapper.buildLetterDetails(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    void givenRespondentRecipient_whenBuildLetterDetails_thenReturnExpectedLetterDetails() {
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorReference("");
        BasicLetterDetails expected = getExpectedBasicLetterDetails("Respondent Name",
            "50 Respondent Street",
            DocumentHelper.PaperNotificationRecipient.RESPONDENT, mockedCourtDetailsTemplateFields);

        BasicLetterDetails actual = letterDetailsMapper.buildLetterDetails(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    void givenValidCaseData_whenGetDetailsAsMap_thenReturnExpectedLetterDetails() {
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorReference("");
        BasicLetterDetails expected = getExpectedBasicLetterDetails("Respondent Name",
            "50 Respondent Street",
            DocumentHelper.PaperNotificationRecipient.RESPONDENT, mockedCourtDetailsTemplateFields);

        Map<String, Object> placeholdersMap = letterDetailsMapper.getLetterDetailsAsMap(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        Map<String, Object> actualData = getCaseData(placeholdersMap);
        Map<String, Object> courtDetails = (Map<String, Object>) actualData.get("courtDetails");

        assertAll(
            () -> assertEquals(expected.getApplicantName(), actualData.get("applicantName")),
            () -> assertEquals(expected.getRespondentName(), actualData.get("respondentName")),
            () -> assertEquals(expected.getCaseNumber(), actualData.get("caseNumber")),
            () -> assertTrue(actualData.containsKey("courtDetails"))
        );

        assertAll(
            () -> assertEquals(
                expected.getCourtDetails().getCourtName(),
                courtDetails.get("courtName")
            ),
            () -> assertEquals(
                expected.getCourtDetails().getCourtAddress(),
                courtDetails.get("courtAddress")
            )
        );
    }

    private BasicLetterDetails getExpectedBasicLetterDetails(String name,
                                                             String addressLine1,
                                                             DocumentHelper.PaperNotificationRecipient recipient,
                                                             CourtDetailsTemplateFields mockedCourtDetailsTemplateFields) {
        return BasicLetterDetails.builder()
            .applicantName("Applicant Name")
            .respondentName("Respondent Name")
            .divorceCaseNumber("DD12D12345")
            .ctscContactDetails(getCtscContactDetails())
            .addressee(getAddressee(name, addressLine1))
            .courtDetails(mockedCourtDetailsTemplateFields)
            .letterDate(String.valueOf(LocalDate.now()))
            .reference(getReference(recipient))
            .caseNumber("1596638099618923")
            .orderType("consent")
            .build();
    }

    private Addressee getAddressee(String name, String addressLine1) {
        return Addressee.builder()
            .name(name)
            .formattedAddress(formatAddressForLetterPrinting(getAddress(addressLine1), false))
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
}
