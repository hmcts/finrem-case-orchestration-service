package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalletter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralLetterDetails;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.buildCtscContactDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.formatAddressForLetterPrinting;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER_RECIPIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;

public class GeneralLetterDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    private static final String TEST_JSON = "/fixtures/general-letter.json";

    private static final String APPLICANT_FULL_NAME = "Poor Guy";
    private static final String RESPONDENT_FULL_NAME = "test Korivi";
    private static final String APPLICANT_SOLICITOR_LABEL = "Applicant Solicitor";
    private static final String RESPONDENT_SOLICITOR_LABEL = "Respondent Solicitor";
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
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(APPLICANT_SOLICITOR).label(APPLICANT_SOLICITOR_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems()).value(chosenOption).build();
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        DocumentTemplateDetails actual = generalLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralLetterDetails(GeneralLetterAddressToType.APPLICANT_SOLICITOR);

        assertTemplateFields(actual, expected);
    }

    @Test
    public void givenRespSolRecipient_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(RESPONDENT_SOLICITOR).label(RESPONDENT_SOLICITOR_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems()).value(chosenOption).build();
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorName(RESPONDENT_SOLICITOR_NAME);

        DocumentTemplateDetails actual = generalLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralLetterDetails(GeneralLetterAddressToType.RESPONDENT_SOLICITOR);

        assertTemplateFields(actual, expected);
    }

    @Test
    public void givenRespondentRecipient_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(RESPONDENT).label(RESPONDENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems()).value(chosenOption).build();
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressTo(GeneralLetterAddressToType.RESPONDENT);

        DocumentTemplateDetails actual = generalLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralLetterDetails(GeneralLetterAddressToType.RESPONDENT);

        assertTemplateFields(actual, expected);
    }

    @Test
    public void givenOtherRecipient_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems()).value(chosenOption).build();
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterRecipient(OTHER_NAME);

        DocumentTemplateDetails actual = generalLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralLetterDetails(GeneralLetterAddressToType.OTHER);

        assertTemplateFields(actual, expected);
    }

    private void assertTemplateFields(DocumentTemplateDetails actual, DocumentTemplateDetails expected) {
        Arrays.stream(actual.getClass().getDeclaredFields())
            .forEach(field -> {
                if (!field.getName().contains(GENERAL_LETTER_CREATED_DATE)) {
                    field.setAccessible(true);
                    try {
                        assertEquals(field.get(expected), field.get(actual));
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

    private List<DynamicRadioListElement> getDynamicRadioListItems() {
        return List.of(
            DynamicRadioListElement.builder().code(APPLICANT).label(APPLICANT).build(),
            DynamicRadioListElement.builder().code(APPLICANT_SOLICITOR).label(APPLICANT_SOLICITOR_LABEL).build(),
            DynamicRadioListElement.builder().code(RESPONDENT).label(RESPONDENT).build(),
            DynamicRadioListElement.builder().code(RESPONDENT_SOLICITOR).label(RESPONDENT_SOLICITOR_LABEL).build(),
            DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build());
    }
}