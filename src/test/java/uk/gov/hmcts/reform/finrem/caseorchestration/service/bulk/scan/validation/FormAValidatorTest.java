package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.validation;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.InputScannedDoc;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.InputScannedDocUrl;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.WARNINGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.COVER_LETTER_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.D81_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.DECREE_NISI_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.DRAFT_CONSENT_ORDER_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.FORM_A_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.FORM_E_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.OTHER_SUPPORT_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.P1_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.P2_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PPF1_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PPF2_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PPF_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ADDRESS_OF_PROPERTIES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_ADDRESS_COUNTRY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_ADDRESS_COUNTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_ADDRESS_LINE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_ADDRESS_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_ADDRESS_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_INTENDS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_PBA_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLYING_FOR_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.AUTHORISATION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.AUTHORISATION_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.AUTHORISATION_SIGNED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.AUTHORISATION_SIGNED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.AUTHORISATION_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.AUTHORISATION_SOLICITOR_POSITION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_REASON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DATE_OF_BIRTH_CHILD_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DATE_OF_BIRTH_CHILD_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_STAGE_REACHED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.GENDER_CHILD_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.GENDER_CHILD_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.HWF_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.MORTGAGE_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ORDER_FOR_CHILDREN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ORDER_FOR_CHILDREN_NO_AGREEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.PROVISION_MADE_FOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_ADDRESS_COUNTRY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_ADDRESS_COUNTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_ADDRESS_LINE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_ADDRESS_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_ADDRESS_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_FULL_NAME;

public class FormAValidatorTest {

    private final FormAValidator formAValidator = new FormAValidator();
    private List<OcrDataField> mandatoryFieldsWithValues;
    private List<OcrDataField> optionalFieldsWithValues;

    @Before
    public void setup() {
        mandatoryFieldsWithValues = asList(
                new OcrDataField(DIVORCE_CASE_NUMBER, "DD12D12345"),
                new OcrDataField(APPLICANT_FULL_NAME, "Peter Griffin"),
                new OcrDataField(RESPONDENT_FULL_NAME, "Louis Griffin"),
                new OcrDataField(PROVISION_MADE_FOR, "in connection with matrimonial or civil partnership proceedings"),
                new OcrDataField(NATURE_OF_APPLICATION, "a lump sum order, a pension Sharing Order"),
                new OcrDataField(APPLICANT_INTENDS_TO, "ApplyToCourtFor"),
                new OcrDataField(APPLYING_FOR_CONSENT_ORDER, "Yes"),
                new OcrDataField(DIVORCE_STAGE_REACHED, "Decree Nisi"),
                new OcrDataField(APPLICANT_REPRESENTED, "I am not represented by a solicitor in these proceedings"),
                new OcrDataField(AUTHORISATION_SIGNED, "Yes"),
                new OcrDataField(AUTHORISATION_SIGNED_BY, "Applicant's solicitor"),
                new OcrDataField(AUTHORISATION_DATE, "12/03/2020")
        );

        optionalFieldsWithValues = asList(
                new OcrDataField(HWF_NUMBER, "123456"),
                new OcrDataField(DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, "a lump sum order,a pension compensation sharing order"),
                new OcrDataField(APPLICANT_SOLICITOR_NAME, "Saul Call"),
                new OcrDataField(APPLICANT_SOLICITOR_FIRM, "Better Divorce Ltd"),
                new OcrDataField(APPLICANT_SOLICITOR_DX_NUMBER, "DX123"),
                new OcrDataField(APPLICANT_SOLICITOR_REFERENCE, "SOL-RED"),
                new OcrDataField(APPLICANT_PBA_NUMBER, "PBA123456"),
                new OcrDataField(APPLICANT_ADDRESS_LINE_1, "Road"),
                new OcrDataField(APPLICANT_ADDRESS_TOWN, "Manchester"),
                new OcrDataField(APPLICANT_ADDRESS_COUNTY, "There"),
                new OcrDataField(APPLICANT_ADDRESS_POSTCODE, "SW9 9SD"),
                new OcrDataField(APPLICANT_ADDRESS_COUNTRY, "UK"),
                new OcrDataField(APPLICANT_PHONE, "0712345654"),
                new OcrDataField(APPLICANT_EMAIL, "applicant@divorcity.com"),
                new OcrDataField(RESPONDENT_ADDRESS_LINE_1, "Avenue"),
                new OcrDataField(RESPONDENT_ADDRESS_TOWN, "Bristol"),
                new OcrDataField(RESPONDENT_ADDRESS_COUNTY, "Here"),
                new OcrDataField(RESPONDENT_ADDRESS_POSTCODE, "SW1 9SD"),
                new OcrDataField(RESPONDENT_ADDRESS_COUNTRY, "UK"),
                new OcrDataField(ADDRESS_OF_PROPERTIES, "26 Westminster Avenue"),
                new OcrDataField(MORTGAGE_DETAILS, "We paid for the house with our mortgage which we split"),
                new OcrDataField(ORDER_FOR_CHILDREN, "there is no agreement, but the applicant is applying for payments"),
                new OcrDataField(ORDER_FOR_CHILDREN_NO_AGREEMENT,
                        "in addition to child support maintenance already paid under a Child Support Agency assessment"),
                new OcrDataField(CHILD_SUPPORT_AGENCY_CALCULATION_MADE, "Yes"),
                new OcrDataField(CHILD_SUPPORT_AGENCY_CALCULATION_REASON, "Various reasons why I'm making this application"),
                new OcrDataField(AUTHORISATION_FIRM, "Better Divorce Ltd"),
                new OcrDataField(AUTHORISATION_SOLICITOR_ADDRESS, "1 Single Lane, Liverpool, LE5 AV2"),
                new OcrDataField(AUTHORISATION_SOLICITOR_POSITION, "I'm the CEO")
        );
    }

    @Test
    public void shouldPassValidationForValidMandatoryAndOptionalFields() {
        List<OcrDataField> ocrDataFields = new ArrayList<>(mandatoryFieldsWithValues);
        ocrDataFields.addAll(optionalFieldsWithValues);
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(ocrDataFields);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldsAreMissing() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(emptyList());

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), warningMessagesForMissingOrEmptyFields());
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldsArePresentButEmpty() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(
                mandatoryFieldsWithValues.stream()
                        .map(emptyValueOcrDataField)
                        .collect(Collectors.toList()));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), warningMessagesForMissingOrEmptyFields());
    }

    @Test
    public void shouldPassValidationForOptionalEmptyFields() {
        List<OcrDataField> optionalFieldsWithEmptyValues = optionalFieldsWithValues.stream()
                .map(emptyValueOcrDataField)
                .collect(Collectors.toList());

        List<OcrDataField> ocrDataFields = new ArrayList<>(mandatoryFieldsWithValues);
        ocrDataFields.addAll(optionalFieldsWithEmptyValues);
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(ocrDataFields);
        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailFieldsHavingInvalidValues() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(asList(
                new OcrDataField(HWF_NUMBER, "12345"),
                new OcrDataField(APPLICANT_FULL_NAME, "Peter"),
                new OcrDataField(RESPONDENT_FULL_NAME, "Louis"),
                new OcrDataField(PROVISION_MADE_FOR, "Onions"),
                new OcrDataField(NATURE_OF_APPLICATION, "Mountains, Forest"),
                new OcrDataField(APPLICANT_INTENDS_TO, "have breakfast"),
                new OcrDataField(DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, "house with pool"),
                new OcrDataField(APPLYING_FOR_CONSENT_ORDER, "No"),
                new OcrDataField(DIVORCE_STAGE_REACHED, "The cree"),
                new OcrDataField(APPLICANT_REPRESENTED, "It's wrong!"),
                new OcrDataField(APPLICANT_EMAIL, "peter@com"),
                new OcrDataField(ORDER_FOR_CHILDREN, "Not a valid order for children"),
                new OcrDataField(ORDER_FOR_CHILDREN_NO_AGREEMENT, "Not a valid reason for no agreement"),
                new OcrDataField(CHILD_SUPPORT_AGENCY_CALCULATION_MADE, "Decision not yet made"),
                new OcrDataField(AUTHORISATION_SIGNED_BY, "My cat"),
                new OcrDataField(AUTHORISATION_DATE, "Date in the moonlight"),
                new OcrDataField(DATE_OF_BIRTH_CHILD_1, "20 may 2010"),
                new OcrDataField(DATE_OF_BIRTH_CHILD_2, "yesterday"),
                new OcrDataField(GENDER_CHILD_1, "book"),
                new OcrDataField(GENDER_CHILD_2, "pokemon")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings().size(), is(22));
        assertThat(validationResult.getWarnings(), hasItems(
                mandatoryFieldIsMissing.apply(DIVORCE_CASE_NUMBER),
                "HWFNumber is usually 6 digits",
                mustHaveAtLeastTwoNames(APPLICANT_FULL_NAME),
                mustHaveAtLeastTwoNames(RESPONDENT_FULL_NAME),
                mustBeOneOf(PROVISION_MADE_FOR,
                        "in connection with matrimonial or civil partnership proceedings",
                        "under paragraphs 1 or 2 of Schedule 1 to the Children Act 1989"),

                containsValueThatIsNotAccepted(NATURE_OF_APPLICATION),
                mustBeOneOf(APPLICANT_INTENDS_TO,
                        "ApplyToCourtFor",
                        "ProceedWithApplication",
                        "ApplyToVary",
                        "ApplyToDischargePeriodicalPaymentOrder"),
                containsValueThatIsNotAccepted(DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE),
                APPLYING_FOR_CONSENT_ORDER + " only accepts value of \"Yes\"",
                mustBeOneOf(DIVORCE_STAGE_REACHED, "Decree Nisi", "Decree Absolute", "Petition Issued"),
                mustBeOneOf(APPLICANT_REPRESENTED,
                        "I am not represented by a solicitor in these proceedings",
                        "I am not represented by a solicitor in these proceedings but am receiving advice from a solicitor",
                        "I am represented by a solicitor in these proceedings, who has signed Section 5"
                                + " and all documents for my attention should be sent to my solicitor whose details are as follows"),
                notInValidFormat(APPLICANT_EMAIL),
                mustBeOneOf(ORDER_FOR_CHILDREN,
                        "there is a written agreement made before 5 April 1993 about maintenance for the benefit of children",
                        "there is a written agreement made on or after 5 April 1993 about maintenance for the benefit of children",
                        "there is no agreement, but the applicant is applying for payments"),
                mustBeOneOf(ORDER_FOR_CHILDREN_NO_AGREEMENT,
                        "for a stepchild or stepchildren",
                        "in addition to child support maintenance already paid under a Child Support Agency assessment",
                        "to meet expenses arising from a child’s disability",
                        "to meet expenses incurred by a child in being educated or training for work",
                        "when either the child or the person with care of the child "
                                + "or the absent parent of the child is not habitually resident in the United Kingdom"),
                CHILD_SUPPORT_AGENCY_CALCULATION_MADE + " must be \"Yes\", \"No\" or left blank",
                mustBeOneOf(AUTHORISATION_SIGNED_BY,
                        "Applicant",
                        "Litigation Friend",
                        "Applicant's solicitor"),
                AUTHORISATION_DATE + " must be a valid date",
                DATE_OF_BIRTH_CHILD_1 + " must be a valid date",
                DATE_OF_BIRTH_CHILD_2 + " must be a valid date",
                GENDER_CHILD_1 + " must be \"Male\", \"Female\" or left blank",
                GENDER_CHILD_2 + " must be \"Male\", \"Female\" or left blank"
        ));
    }

    @Test
    public void shouldPassValidationForValuesWeDoNotSupportYet() {
        String domesticViolenceValue = "ArrestedRelevantDomesticViolenceOffence, "
                + "invalid, insert random here,"
                + "UndertakingSection46Or63EFamilyLawActOrScotlandNorthernIrelandProtectiveInjunction";

        String urgencyValue = "RiskLifeLibertyPhysicalSafety";
        String previousAttendanceValue = "AnotherDisputeeResolutionn";

        List<OcrDataField> ocrDataFields = new ArrayList<>(mandatoryFieldsWithValues);
        ocrDataFields.addAll(asList(
                new OcrDataField("MIAMDomesticViolenceChecklist", domesticViolenceValue),
                new OcrDataField("MIAMUrgencyChecklist", urgencyValue),
                new OcrDataField("MIAMPreviousAttendanceChecklist", previousAttendanceValue)
        ));

        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(ocrDataFields);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
    }

    @Test
    public void shouldValidateDivorceCaseNumber() {
        OcrValidationResult ocrInvalidResult = formAValidator.validateBulkScanForm(asList(
                new OcrDataField(DIVORCE_CASE_NUMBER, "1234567890")
        ));
        assertThat(ocrInvalidResult.getWarnings(), hasItem("divorceCaseNumber is not in a valid format"));

        OcrValidationResult ocrValidResult = formAValidator.validateBulkScanForm(asList(
                new OcrDataField(DIVORCE_CASE_NUMBER, "DD12D12345")
        ));
        assertThat(ocrValidResult.getWarnings(), not(hasItem("divorceCaseNumber is not in a valid format")));
    }

    @Test
    public void shouldReturnSuccessResponseWhenCorrectDocumentsAreAttached() {

        List<InputScannedDoc> scannedDocuments = new ArrayList<>();

        scannedDocuments.add(createDoc(FORM_A_DOCUMENT));
        scannedDocuments.add(createDoc(D81_DOCUMENT));
        scannedDocuments.add(createDoc(DRAFT_CONSENT_ORDER_DOCUMENT));
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .id(TEST_CASE_ID)
            .scannedDocuments(scannedDocuments)
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResult documentValidationResult = formAValidator.validateFormAScannedDocuments(exceptionRecord);

        assertThat(documentValidationResult.getStatus(), is(SUCCESS));
        assertThat(documentValidationResult.getWarnings(), is(emptyList()));
        assertThat(documentValidationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldProduceWarningWhenMultipleFormADocumentsAreAttached() {
        List<InputScannedDoc> scannedDocuments = new ArrayList<>();

        scannedDocuments.add(createDoc(FORM_A_DOCUMENT));
        scannedDocuments.add(createDoc(FORM_A_DOCUMENT));
        scannedDocuments.add(createDoc(D81_DOCUMENT));
        scannedDocuments.add(createDoc(DRAFT_CONSENT_ORDER_DOCUMENT));
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .id(TEST_CASE_ID)
            .scannedDocuments(scannedDocuments)
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResult documentValidationResult = formAValidator.validateFormAScannedDocuments(exceptionRecord);

        assertThat(documentValidationResult.getStatus(), is(WARNINGS));
        assertThat(documentValidationResult.getWarnings(), hasItem("Must be only a single document with subtype of 'FormA'"));
        assertThat(documentValidationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldProduceWarningWhenMultipleDraftConsentOrderDocumentsAreAttached() {
        List<InputScannedDoc> scannedDocuments = new ArrayList<>();

        scannedDocuments.add(createDoc(FORM_A_DOCUMENT));
        scannedDocuments.add(createDoc(D81_DOCUMENT));
        scannedDocuments.add(createDoc(DRAFT_CONSENT_ORDER_DOCUMENT));
        scannedDocuments.add(createDoc(DRAFT_CONSENT_ORDER_DOCUMENT));
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .id(TEST_CASE_ID)
            .scannedDocuments(scannedDocuments)
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResult documentValidationResult = formAValidator.validateFormAScannedDocuments(exceptionRecord);

        assertThat(documentValidationResult.getStatus(), is(WARNINGS));
        assertThat(documentValidationResult.getWarnings(), hasItem("Must be only a single document with subtype of 'DraftConsentOrder'"));
        assertThat(documentValidationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldProduceWarningWhenNoD81DocumentIsAttached() {
        List<InputScannedDoc> scannedDocuments = new ArrayList<>();

        scannedDocuments.add(createDoc(FORM_A_DOCUMENT));
        scannedDocuments.add(createDoc(DRAFT_CONSENT_ORDER_DOCUMENT));
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .id(TEST_CASE_ID)
            .scannedDocuments(scannedDocuments)
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResult documentValidationResult = formAValidator.validateFormAScannedDocuments(exceptionRecord);

        assertThat(documentValidationResult.getStatus(), is(WARNINGS));
        assertThat(documentValidationResult.getWarnings(), hasItem("Must be at least one document with subtype of 'D81'"));
        assertThat(documentValidationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldReturnSuccessResponseWhenAllDocumentsAttachedHaveCorrectMandatoryFields() {

        List<InputScannedDoc> scannedDocuments = new ArrayList<>();
        scannedDocuments.add(createDoc(FORM_A_DOCUMENT));
        scannedDocuments.add(createDoc(D81_DOCUMENT));
        scannedDocuments.add(createDoc(DRAFT_CONSENT_ORDER_DOCUMENT));

        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .id(TEST_CASE_ID)
            .scannedDocuments(scannedDocuments)
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResult documentValidationResult = formAValidator.validateFormAScannedDocuments(exceptionRecord);

        assertThat(documentValidationResult.getStatus(), is(SUCCESS));
        assertThat(documentValidationResult.getWarnings(), is(emptyList()));
        assertThat(documentValidationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldReturnSuccessResponseWhenDocumentsAttachedHaveCorrectSubTypes() {

        List<InputScannedDoc> scannedDocuments = new ArrayList<>();
        scannedDocuments.add(createDoc(D81_DOCUMENT));
        scannedDocuments.add(createDoc(FORM_A_DOCUMENT));
        scannedDocuments.add(createDoc(P1_DOCUMENT));
        scannedDocuments.add(createDoc(PPF1_DOCUMENT));
        scannedDocuments.add(createDoc(P2_DOCUMENT));
        scannedDocuments.add(createDoc(PPF2_DOCUMENT));
        scannedDocuments.add(createDoc(PPF_DOCUMENT));
        scannedDocuments.add(createDoc(FORM_E_DOCUMENT));
        scannedDocuments.add(createDoc(COVER_LETTER_DOCUMENT));
        scannedDocuments.add(createDoc(OTHER_SUPPORT_DOCUMENTS));
        scannedDocuments.add(createDoc(DRAFT_CONSENT_ORDER_DOCUMENT));
        scannedDocuments.add(createDoc(DECREE_NISI_DOCUMENT));
        scannedDocuments.add(createDoc(DECREE_ABSOLUTE_DOCUMENT));

        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .id(TEST_CASE_ID)
            .scannedDocuments(scannedDocuments)
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResult documentValidationResult = formAValidator.validateFormAScannedDocuments(exceptionRecord);

        assertThat(documentValidationResult.getStatus(), is(SUCCESS));
        assertThat(documentValidationResult.getWarnings(), is(emptyList()));
        assertThat(documentValidationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldProduceWarningWhenDocumentWithWrongSubTypeIsAttached() {
        List<InputScannedDoc> scannedDocuments = new ArrayList<>();

        scannedDocuments.add(createDoc(D81_DOCUMENT));
        scannedDocuments.add(createDoc(FORM_A_DOCUMENT));
        scannedDocuments.add(createDoc(DRAFT_CONSENT_ORDER_DOCUMENT));
        scannedDocuments.add(createDoc("PassportPhoto"));
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .id(TEST_CASE_ID)
            .scannedDocuments(scannedDocuments)
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResult documentValidationResult = formAValidator.validateFormAScannedDocuments(exceptionRecord);

        assertThat(documentValidationResult.getStatus(), is(WARNINGS));
        assertThat(documentValidationResult.getWarnings(), hasItem("Document sub-type not accepted: \"PassportPhoto\""));
        assertThat(documentValidationResult.getErrors(), is(emptyList()));
    }

    private String notInValidFormat(String fieldName) {
        return String.format("%s is not in a valid format", fieldName);
    }

    private String containsValueThatIsNotAccepted(String fieldName) {
        return String.format("%s contains a value that is not accepted", fieldName);
    }

    private String mustHaveAtLeastTwoNames(String fieldName) {
        return String.format("%s must contain a firstname and a lastname", fieldName);
    }

    private String mustBeOneOf(String fieldName, String... values) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s must be \"%s\"", fieldName, values[0]));
        for (int i = 1; i < values.length - 1; i++) {
            stringBuilder.append(String.format(", \"%s\"", values[i]));
        }
        if (values.length > 1) {
            stringBuilder.append(String.format(" or \"%s\"", values[values.length - 1]));
        }
        return stringBuilder.toString();
    }

    private Matcher<List<String>> warningMessagesForMissingOrEmptyFields() {
        List<String> mandatoryFields = mandatoryFieldsWithValues.stream().map(OcrDataField::getName).collect(Collectors.toList());
        return allOf(
                hasItems(mandatoryFields.stream()
                        .map(mandatoryFieldIsMissing)
                        .toArray(String[]::new))
        );
    }

    private Function<String, String> mandatoryFieldIsMissing = fieldName -> String.format("Mandatory field \"%s\" is missing", fieldName);
    private Function<OcrDataField, OcrDataField> emptyValueOcrDataField = dataField -> new OcrDataField(dataField.getName(), "");

    private InputScannedDoc createDoc(String formSubType) {
        return InputScannedDoc.builder()
            .type("Form")
            .subtype(formSubType)
            .document(
                new InputScannedDocUrl(
                    "http://url/" + formSubType,
                    "http://binUrl/" + formSubType + "/binary",
                    formSubType + ".pdf"))
            .build();
    }
}
