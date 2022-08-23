package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalletter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralLetterDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterAddressToType;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.buildCtscContactDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.formatAddressForLetterPrinting;

public class GeneralLetterDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    private static final String TEST_JSON = "/fixtures/general-letter.json";

    private static final String APPLICANT_FULL_NAME = "Poor Guy";
    private static final String RESPONDENT_FULL_NAME = "test Korivi";
    private static final String CCD_CASE_NUMBER = "1234567890";
    private static final String APP_SOLICITOR_NAME = "Solictor";
    private static final String RESPONDENT_SOLICITOR_NAME = "RespondentSolicitor";
    private static final String OTHER_NAME = "Other";
    public static final String GENERAL_LETTER_CREATED_DATE = "generalLetterCreatedDate";

    @Autowired
    private GeneralLetterDetailsMapper generalLetterDetailsMapper;

    @Before
    public void setUp() throws Exception {
        setCaseDetails(TEST_JSON);
    }

    @Test
    public void givenAppSolRecipient_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        DocumentTemplateDetails actual = generalLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralLetterDetails(GeneralLetterAddressToType.APPLICANT_SOLICITOR);

        assertTemplateFields(actual, expected);
    }

    @Test
    public void givenRespSolRecipient_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        caseDetails.getCaseData().getGeneralLetterWrapper().setGeneralLetterAddressTo(GeneralLetterAddressToType.RESPONDENT_SOLICITOR);
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentSolicitorName(RESPONDENT_SOLICITOR_NAME);

        DocumentTemplateDetails actual = generalLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralLetterDetails(GeneralLetterAddressToType.RESPONDENT_SOLICITOR);

        assertTemplateFields(actual, expected);
    }

    @Test
    public void givenRespondentRecipient_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        caseDetails.getCaseData().getGeneralLetterWrapper().setGeneralLetterAddressTo(GeneralLetterAddressToType.RESPONDENT);

        DocumentTemplateDetails actual = generalLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralLetterDetails(GeneralLetterAddressToType.RESPONDENT);

        assertTemplateFields(actual, expected);
    }

    @Test
    public void givenOtherRecipient_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        caseDetails.getCaseData().getGeneralLetterWrapper().setGeneralLetterAddressTo(GeneralLetterAddressToType.OTHER);
        caseDetails.getCaseData().getGeneralLetterWrapper().setGeneralLetterRecipient(OTHER_NAME);

        DocumentTemplateDetails actual = generalLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralLetterDetails(GeneralLetterAddressToType.OTHER);

        assertTemplateFields(actual, expected);
    }

    private void assertTemplateFields(DocumentTemplateDetails actual, DocumentTemplateDetails expected) {
        Arrays.stream(actual.getClass().getDeclaredFields())
            .forEach(field -> {
                if (!field.getName().contains(GENERAL_LETTER_CREATED_DATE)) {
                    field.setAccessible(true);
                    try {
                        assertEquals(field.get(actual), field.get(expected));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException();
                    }
                }
            });
    }

    private GeneralLetterDetails getExpectedGeneralLetterDetails(GeneralLetterAddressToType recipient) {
        return GeneralLetterDetails.builder()
            .applicantFullName(APPLICANT_FULL_NAME)
            .respondentFullName(RESPONDENT_FULL_NAME)
            .solicitorReference(getReference(recipient))
            .ccdCaseNumber(CCD_CASE_NUMBER)
            .generalLetterCreatedDate(new Date())
            .addressee(getAddressee(recipient))
            .generalLetterBody("Test content for the letter body")
            .ctscContactDetails(buildCtscContactDetails())
            .build();
    }

    private Addressee getAddressee(GeneralLetterAddressToType recipient) {
        switch (recipient) {
            case APPLICANT_SOLICITOR -> {
                return buildAddressee(APP_SOLICITOR_NAME, "50 Applicant Solicitor Street");
            }
            case RESPONDENT_SOLICITOR -> {
                return buildAddressee(RESPONDENT_SOLICITOR_NAME, "50 Respondent Solicitor Street");
            }
            case RESPONDENT -> {
                return buildAddressee(RESPONDENT_FULL_NAME, "50 Respondent Street");
            }
            case OTHER -> {
                return buildAddressee(OTHER_NAME, "50 Applicant Solicitor Street");
            }
            default -> throw new IllegalStateException();
        }
    }

    private String getReference(GeneralLetterAddressToType recipient) {
        switch (recipient) {
            case APPLICANT_SOLICITOR -> {
                return "LL01";
            }
            case RESPONDENT_SOLICITOR -> {
                return "RSP-LL01";
            }
            case RESPONDENT, OTHER -> {
                return "";
            }
            default -> throw new IllegalStateException();
        }
    }

    private Addressee buildAddressee(String name, String addressLine1) {
        return Addressee.builder()
            .name(name)
            .formattedAddress(formatAddressForLetterPrinting(getAddress(addressLine1)))
            .build();
    }

    private Address getAddress(String addressLine1) {
        return Address.builder()
            .addressLine1(addressLine1)
            .addressLine2("Second Address Line")
            .addressLine3("Third Address Line")
            .postTown("London")
            .county("Greater London")
            .postCode("SE12 9SE")
            .country("United Kingdom")
            .build();
    }
}