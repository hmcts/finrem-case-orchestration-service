package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.InputScannedDoc;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.InputScannedDocUrl;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ComplexTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TypedCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.FormAToCaseTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ContactDetailsMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.BULK_SCAN_CASE_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED_PAPER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_AGREE_TO_RECEIVE_EMAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation.mappers.ContactDetailsMapperTest.assertTransformationForAddressIsValid;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation.mappers.ContactDetailsMapperTest.getOcrFieldsForAddresses;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation.mappers.ContactDetailsMapperTest.ocrDataFieldIndicatingApplicantIsRepresented;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE;

public class FormAToCaseTransformerTest {

    private final BulkScanFormTransformer formAToCaseTransformer = new FormAToCaseTransformer();

    @Test
    public void shouldTransformFieldsAccordingly() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField(OcrFieldName.DIVORCE_CASE_NUMBER, "1234567890"),
            new OcrDataField(OcrFieldName.HWF_NUMBER, "123456"),
            new OcrDataField(OcrFieldName.APPLICANT_FULL_NAME, "Peter Griffin"),
            new OcrDataField(OcrFieldName.RESPONDENT_FULL_NAME, "Louis Griffin"),
            new OcrDataField(OcrFieldName.PROVISION_MADE_FOR, "in connection with matrimonial or civil partnership proceedings"),
            new OcrDataField(OcrFieldName.NATURE_OF_APPLICATION, "Periodical Payment Order, Pension Attachment Order"),
            new OcrDataField(OcrFieldName.APPLICANT_INTENDS_TO, "ApplyToCourtFor"),
            new OcrDataField(OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, "a lump sum order, a pension sharing order"),
            new OcrDataField(OcrFieldName.APPLYING_FOR_CONSENT_ORDER, "Yes"),
            new OcrDataField(OcrFieldName.DIVORCE_STAGE_REACHED, "Decree Nisi"),
            new OcrDataField(OcrFieldName.APPLICANT_REPRESENTED, "I am not represented by a solicitor in these proceedings"),
            new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_NAME, "Saul Call"),
            new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_FIRM, "Better Divorce Ltd"),
            new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_DX_NUMBER, "DX123"),
            new OcrDataField(OcrFieldName.APPLICANT_SOLICITOR_REFERENCE, "SOL-RED"),
            new OcrDataField(OcrFieldName.APPLICANT_PBA_NUMBER, "PBA123456"),
            new OcrDataField(OcrFieldName.APPLICANT_PHONE, "0712345654"),
            new OcrDataField(OcrFieldName.APPLICANT_EMAIL, "applicant@divorcity.com"),
            new OcrDataField(OcrFieldName.ADDRESS_OF_PROPERTIES, "The address of other properties"),
            new OcrDataField(OcrFieldName.MORTGAGE_DETAILS, "Various details of our mortgage"),
            new OcrDataField(OcrFieldName.ORDER_FOR_CHILDREN,
                "there is a written agreement made on or after 5 April 1993 about maintenance for the benefit of children"),
            new OcrDataField(OcrFieldName.ORDER_FOR_CHILDREN_NO_AGREEMENT,
                "in addition to child support maintenance already paid under a Child Support Agency assessment"),
            new OcrDataField(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_MADE, "Yes"),
            new OcrDataField(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_REASON, "Random reason that explains calculation"),
            new OcrDataField(OcrFieldName.AUTHORISATION_NAME, "Saul B. Kol"),
            new OcrDataField(OcrFieldName.AUTHORISATION_FIRM, "Better Divorce Ltd"),
            new OcrDataField(OcrFieldName.AUTHORISATION_SOLICITOR_ADDRESS, "1 Single Lane, Liverpool, LE5 AV2"),
            new OcrDataField(OcrFieldName.AUTHORISATION_SIGNED, "Yes"),
            new OcrDataField(OcrFieldName.AUTHORISATION_SIGNED_BY, "Applicant's solicitor"),
            new OcrDataField(OcrFieldName.AUTHORISATION_DATE, "12/03/2020"),
            new OcrDataField(OcrFieldName.AUTHORISATION_SOLICITOR_POSITION, "I'm the CEO"),
            new OcrDataField(OcrFieldName.NAME_CHILD_1, "Johny Bravo"),
            new OcrDataField(OcrFieldName.GENDER_CHILD_1, "male"),
            new OcrDataField(OcrFieldName.DATE_OF_BIRTH_CHILD_1, "12/03/2000"),
            new OcrDataField(OcrFieldName.RELATIONSHIP_TO_APPLICANT_CHILD_1, "son"),
            new OcrDataField(OcrFieldName.RELATIONSHIP_TO_RESPONDENT_CHILD_1, "SON"),
            new OcrDataField(OcrFieldName.COUNTRY_CHILD_1, "New Zeeland"),
            new OcrDataField(OcrFieldName.NAME_CHILD_2, "Anne Shirley"),
            new OcrDataField(OcrFieldName.GENDER_CHILD_2, "female"),
            new OcrDataField(OcrFieldName.DATE_OF_BIRTH_CHILD_2, "12/03/1895"),
            new OcrDataField(OcrFieldName.RELATIONSHIP_TO_APPLICANT_CHILD_2, "daughter"),
            new OcrDataField(OcrFieldName.RELATIONSHIP_TO_RESPONDENT_CHILD_2, "Daughter"),
            new OcrDataField(OcrFieldName.COUNTRY_CHILD_2, "Canada")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry(DIVORCE_CASE_NUMBER, "1234567890"),
            hasEntry("HWFNumber", "123456"),
            hasEntry("applicantFMName", "Peter"),
            hasEntry("applicantLName", "Griffin"),
            hasEntry("appRespondentFMName", "Louis"),
            hasEntry("appRespondentLName", "Griffin"),
            hasEntry("provisionMadeFor", "matrimonialOrCivilPartnershipProceedings"),
            hasEntry("applicantIntendsTo", "ApplyToCourtFor"),
            hasEntry("applyingForConsentOrder", "Yes"),
            hasEntry("divorceStageReached", "Decree Nisi"),
            hasEntry(APPLICANT_REPRESENTED_PAPER, "FR_applicant_represented_1"),
            hasEntry(SOLICITOR_NAME, "Saul Call"),
            hasEntry(SOLICITOR_FIRM, "Better Divorce Ltd"),
            hasEntry("solicitorDXnumber", "DX123"),
            hasEntry(SOLICITOR_REFERENCE, "SOL-RED"),
            hasEntry(PBA_NUMBER, "PBA123456"),
            hasEntry("applicantPhone", "0712345654"),
            hasEntry("applicantEmail", "applicant@divorcity.com"),

            hasEntry("natureOfApplication3a", "The address of other properties"),
            hasEntry("natureOfApplication3b", "Various details of our mortgage"),
            hasEntry("natureOfApplication5b",
                "FR_nature_of_application_2"),
            hasEntry("orderForChildrenQuestion1", "Yes"),
            hasEntry("ChildSupportAgencyCalculationMade", "Yes"),
            hasEntry("ChildSupportAgencyCalculationReason", "Random reason that explains calculation"),
            hasEntry("authorisationName", "Saul B. Kol"),
            hasEntry("authorisationFirm", "Better Divorce Ltd"),
            hasEntry("authorisationSolicitorAddress", "1 Single Lane, Liverpool, LE5 AV2"),
            hasEntry("authorisationSigned", "Yes"),
            hasEntry("authorisationSignedBy", "Applicant's solicitor"),
            hasEntry("authorisation3", "2020-03-12"),
            hasEntry("authorisation2b", "I'm the CEO")
        ));

        assertChildrenInfo(transformedCaseData);

        assertThat(transformedCaseData.get("natureOfApplication2"), is(asList("Periodical Payment Order", "Pension Attachment Order")));
        assertThat(transformedCaseData.get("dischargePeriodicalPaymentSubstituteFor"), is(asList("lumpSumOrder", "pensionSharingOrder")));
        assertThat(transformedCaseData.get("natureOfApplication6"), is(singletonList("In addition to child support")));
    }

    @Test
    public void shouldNotReturnUnexpectedField() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(
            new OcrDataField("UnexpectedName", "UnexpectedValue")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE)
        ));
    }

    @Test
    public void shouldNotReturnExpectedFieldsWithNullValue() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(asList(
            new OcrDataField(APPLICANT_FULL_NAME, null),
            new OcrDataField(DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, null)
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE)
        ));
    }

    @Test
    public void shouldTransformAddressesWhenCitizensRepresented() {
        List<OcrDataField> ocrFields = getOcrFieldsForAddresses();
        ocrFields.add(new OcrDataField(OcrFieldName.RESPONDENT_SOLICITOR_NAME, "Mr John Solicitor"));
        ocrFields.add(ocrDataFieldIndicatingApplicantIsRepresented());

        ExceptionRecord incomingExceptionRecord = createExceptionRecord(ocrFields);

        assertTransformationForAddressIsValid(
            formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord),
            ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR,
            ContactDetailsMapper.CcdFields.RESPONDENT_SOLICITOR,
            ContactDetailsMapper.CcdFields.APPLICANT,
            ContactDetailsMapper.CcdFields.RESPONDENT
        );
    }

    @Test
    public void shouldTransformAddressesWhenCitizensNotRepresented() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(getOcrFieldsForAddresses());

        assertTransformationForAddressIsValid(
            formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord),
            ContactDetailsMapper.CcdFields.APPLICANT,
            ContactDetailsMapper.CcdFields.RESPONDENT,
            ContactDetailsMapper.CcdFields.APPLICANT_SOLICITOR,
            ContactDetailsMapper.CcdFields.RESPONDENT_SOLICITOR
        );
    }

    @Test
    public void shouldSetOrderForChildrenQuestion1ToYesIfOrderForChildrenFieldIsPopulated() {
        Map<String, Object> optionOneTransformedData = formAToCaseTransformer.transformIntoCaseData(createExceptionRecord(
            singletonList(new OcrDataField("OrderForChildren",
                "there is a written agreement made before 5 April 1993 about maintenance for the benefit of children"))));
        assertThat(optionOneTransformedData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry("natureOfApplication5b", "FR_nature_of_application_1"),
            hasEntry("orderForChildrenQuestion1", "Yes")
        ));

        Map<String, Object> optionTwoTransformedData = formAToCaseTransformer.transformIntoCaseData(createExceptionRecord(
            singletonList(new OcrDataField("OrderForChildren",
                "there is a written agreement made on or after 5 April 1993 about maintenance for the benefit of children"))));
        assertThat(optionTwoTransformedData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry("natureOfApplication5b", "FR_nature_of_application_2"),
            hasEntry("orderForChildrenQuestion1", YES_VALUE)
        ));

        Map<String, Object> optionThreeTransformedData = formAToCaseTransformer.transformIntoCaseData(createExceptionRecord(
            singletonList(new OcrDataField("OrderForChildren", "there is no agreement, but the applicant is applying for payments"))));
        assertThat(optionThreeTransformedData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry("natureOfApplication5b", "FR_nature_of_application_3"),
            hasEntry("orderForChildrenQuestion1", YES_VALUE)
        ));
    }

    @Test
    public void shouldNotSetOrderForChildrenQuestion1IfOrderForChildrenFieldIsNotPopulated() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(
            new OcrDataField("OrderForChildren", "")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry("natureOfApplication5b", ""),
            not(hasKey("orderForChildrenQuestion1"))
        ));
    }

    @Test
    public void shouldSetSolicitorAgreeToReceiveEmailsToYesIfSolicitorEmailIsPopulated() {
        Map<String, Object> convertedCcdData = formAToCaseTransformer.transformIntoCaseData(createExceptionRecord(
            asList(
                new OcrDataField("ApplicantEmail", TEST_SOLICITOR_EMAIL),
                ocrDataFieldIndicatingApplicantIsRepresented()
            )
        ));

        assertThat(convertedCcdData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL),
            hasEntry(SOLICITOR_AGREE_TO_RECEIVE_EMAILS, YES_VALUE)
        ));
    }

    @Test
    public void shouldSetSolicitorAgreeToReceiveEmailsToNoIfSolicitorEmailIsNotPopulated() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(
            new OcrDataField("ApplicantSolicitorEmail", "")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry(SOLICITOR_AGREE_TO_RECEIVE_EMAILS, NO_VALUE)
        ));
    }

    @Test
    public void shouldSetRespondentRepresentedToYesIfRespSolicitorNameIsPopulated() {
        Map<String, Object> convertedCcdData = formAToCaseTransformer.transformIntoCaseData(createExceptionRecord(
            singletonList(new OcrDataField("RespondentSolicitorName", TEST_SOLICITOR_NAME))));

        assertThat(convertedCcdData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry(RESP_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
            hasEntry(RESPONDENT_REPRESENTED, YES_VALUE)
        ));
    }

    @Test
    public void shouldSetRespondentRepresentedToNoIfRespSolicitorNameIsNotPopulated() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(
            new OcrDataField("RespondentSolicitorName", "")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry(RESP_SOLICITOR_NAME, ""),
            hasEntry(RESPONDENT_REPRESENTED, NO_VALUE)
        ));
    }

    @Test
    public void shouldTransformOrderForChildrenNoAgreementReasons() {
        ExceptionRecord exceptionRecord =
            createExceptionRecord(singletonList(new OcrDataField("OrderForChildrenNoAgreement", "for a stepchild or stepchildren, "
                + "in addition to child support maintenance already paid under a Child Support Agency assessment, "
                + "to meet expenses arising from a child’s disability, "
                + "to meet expenses incurred by a child in being educated or training for work, "
                + "when either the child or the person with care of the child or the absent parent of the "
                + "child is not habitually resident in the United Kingdom"
            )));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat((List<String>) transformedCaseData.get("natureOfApplication6"), hasItems(
            "Step Child or Step Children",
            "In addition to child support",
            "disability expenses",
            "training",
            "When not habitually resident"
        ));
    }

    @Test
    public void shouldTransform_ProvisionMadeFor() {
        Map<String, Object> transformedCaseDataOptionOne = formAToCaseTransformer.transformIntoCaseData(
            createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.PROVISION_MADE_FOR,
                "in connection with matrimonial or civil partnership proceedings"))));
        assertThat(transformedCaseDataOptionOne.get("provisionMadeFor"), is("matrimonialOrCivilPartnershipProceedings"));

        Map<String, Object> transformedCaseDataOptionTwo = formAToCaseTransformer.transformIntoCaseData(
            createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.PROVISION_MADE_FOR,
                "under paragraphs 1 or 2 of Schedule 1 to the Children Act 1989"))));
        assertThat(transformedCaseDataOptionTwo.get("provisionMadeFor"), is("childrenAct1989"));
    }

    @Test
    public void shouldTransform_ApplicantRepresentedPaper_AndSetApplicantRepresentedCorrectly() {
        Map<String, Object> transformedCaseDataOptionOne = formAToCaseTransformer.transformIntoCaseData(
            createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.APPLICANT_REPRESENTED,
                "I am not represented by a solicitor in these proceedings"))));
        assertThat(transformedCaseDataOptionOne.get(APPLICANT_REPRESENTED_PAPER), is("FR_applicant_represented_1"));
        assertThat(transformedCaseDataOptionOne.get(APPLICANT_REPRESENTED), is(NO_VALUE));

        Map<String, Object> transformedCaseDataOptionTwo = formAToCaseTransformer.transformIntoCaseData(
            createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.APPLICANT_REPRESENTED,
                "I am not represented by a solicitor in these proceedings but am receiving advice from a solicitor"))));
        assertThat(transformedCaseDataOptionTwo.get(APPLICANT_REPRESENTED_PAPER), is("FR_applicant_represented_2"));
        assertThat(transformedCaseDataOptionTwo.get(APPLICANT_REPRESENTED), is(NO_VALUE));

        Map<String, Object> transformedCaseDataOptionThree = formAToCaseTransformer.transformIntoCaseData(
            createExceptionRecord(singletonList(new OcrDataField(OcrFieldName.APPLICANT_REPRESENTED,
                "I am represented by a solicitor in these proceedings, who has signed Section 5 and all "
                    + "documents for my attention should be sent to my solicitor whose details are as follows"))));
        assertThat(transformedCaseDataOptionThree.get(APPLICANT_REPRESENTED_PAPER), is("FR_applicant_represented_3"));
        assertThat(transformedCaseDataOptionThree.get(APPLICANT_REPRESENTED), is(YES_VALUE));
    }

    @Test
    public void shouldTransformEmptyAuthorisationSignedToNo() {
        assertOnSingleFieldTransformationResult(
            OcrFieldName.AUTHORISATION_SIGNED, "",
            "authorisationSigned", NO_VALUE);
    }

    @Test
    public void shouldTransformAnyAuthorisationSignedToYes() {
        assertOnSingleFieldTransformationResult(
            OcrFieldName.AUTHORISATION_SIGNED, "Any non-empty value should become yes",
            "authorisationSigned", YES_VALUE);
    }

    @Test
    public void shouldTransformScannedDocuments() {
        List<InputScannedDoc> scannedDocuments = new ArrayList<>();
        scannedDocuments.add(InputScannedDoc.builder().subtype("D81").document(new InputScannedDocUrl("http://url/d81-1", "http://binUrl/d81-1/binary", "d81-1.pdf")).build());
        scannedDocuments.add(InputScannedDoc.builder().subtype("D81").document(new InputScannedDocUrl("http://url/d81-2", "http://binUrl/d81-2/binary", "d81-2.pdf")).build());
        scannedDocuments.add(createDoc("FormA"));
        scannedDocuments.add(createDoc("P1"));
        scannedDocuments.add(createDoc("PPF1"));
        scannedDocuments.add(createDoc("P2"));
        scannedDocuments.add(createDoc("PPF2"));
        scannedDocuments.add(createDoc("PPF"));
        scannedDocuments.add(createDoc("FormE"));
        scannedDocuments.add(createDoc("CoverLetter"));
        scannedDocuments.add(createDoc("OtherSupportDocuments"));
        scannedDocuments.add(createDoc("DraftConsentOrder"));
        scannedDocuments.add(createDoc("DecreeNisi"));
        scannedDocuments.add(createDoc("DecreeAbsolute"));
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .id(TEST_CASE_ID)
            .scannedDocuments(scannedDocuments)
            .ocrDataFields(emptyList())
            .build();

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, hasKey("formA"));
        assertDocumentsMatchExpectations((CaseDocument) transformedCaseData.get("formA"), "FormA");

        assertThat(transformedCaseData, hasKey("scannedD81s"));
        ComplexTypeCollection<CaseDocument> d81Documents = (ComplexTypeCollection<CaseDocument>) transformedCaseData.get("scannedD81s");
        assertThat(d81Documents, hasSize(2));
        CaseDocument d81DocumentsItem = d81Documents.getItem(0);
        assertThat(d81DocumentsItem.getDocumentUrl(), is("http://url/d81-1"));
        assertThat(d81DocumentsItem.getDocumentBinaryUrl(), is("http://binUrl/d81-1/binary"));
        assertThat(d81DocumentsItem.getDocumentFilename(), is("d81-1.pdf"));
        d81DocumentsItem = d81Documents.getItem(1);
        assertThat(d81DocumentsItem.getDocumentUrl(), is("http://url/d81-2"));
        assertThat(d81DocumentsItem.getDocumentBinaryUrl(), is("http://binUrl/d81-2/binary"));
        assertThat(d81DocumentsItem.getDocumentFilename(), is("d81-2.pdf"));

        assertThat(transformedCaseData, hasKey("pensionCollection"));
        ComplexTypeCollection<TypedCaseDocument> pensionDocuments = (ComplexTypeCollection<TypedCaseDocument>) transformedCaseData.get("pensionCollection");
        assertThat(pensionDocuments, hasSize(5));
        TypedCaseDocument typedCaseDocument = pensionDocuments.getItem(0);
        assertThat(typedCaseDocument.getTypeOfDocument(), is("Form P1"));
        assertDocumentsMatchExpectations(typedCaseDocument.getPensionDocument(), "P1");
        typedCaseDocument = pensionDocuments.getItem(1);
        assertThat(typedCaseDocument.getTypeOfDocument(), is("Form PPF1"));
        assertDocumentsMatchExpectations(typedCaseDocument.getPensionDocument(), "PPF1");
        typedCaseDocument = pensionDocuments.getItem(2);
        assertThat(typedCaseDocument.getTypeOfDocument(), is("Form P2"));
        assertDocumentsMatchExpectations(typedCaseDocument.getPensionDocument(), "P2");
        typedCaseDocument = pensionDocuments.getItem(3);
        assertThat(typedCaseDocument.getTypeOfDocument(), is("Form PPF2"));
        assertDocumentsMatchExpectations(typedCaseDocument.getPensionDocument(), "PPF2");
        typedCaseDocument = pensionDocuments.getItem(4);
        assertThat(typedCaseDocument.getTypeOfDocument(), is("Form PPF"));
        assertDocumentsMatchExpectations(typedCaseDocument.getPensionDocument(), "PPF");

        assertThat(transformedCaseData, hasKey("otherCollection"));
        ComplexTypeCollection<TypedCaseDocument> otherCollection = (ComplexTypeCollection<TypedCaseDocument>) transformedCaseData.get("otherCollection");
        assertThat(otherCollection, hasSize(3));
        TypedCaseDocument otherTypedDocument = otherCollection.getItem(0);
        assertThat(otherTypedDocument.getTypeOfDocument(), is("Other"));
        assertDocumentsMatchExpectations(otherTypedDocument.getPensionDocument(), "FormE");
        otherTypedDocument = otherCollection.getItem(1);
        assertThat(otherTypedDocument.getTypeOfDocument(), is("Letter"));
        assertDocumentsMatchExpectations(otherTypedDocument.getPensionDocument(), "CoverLetter");
        otherTypedDocument = otherCollection.getItem(2);
        assertThat(otherTypedDocument.getTypeOfDocument(), is("Other"));
        assertDocumentsMatchExpectations(otherTypedDocument.getPensionDocument(), "OtherSupportDocuments");

        assertThat(transformedCaseData, hasKey("consentOrder"));
        assertThat(transformedCaseData, hasKey("latestConsentOrder"));
        CaseDocument draftConsentOrder = (CaseDocument) transformedCaseData.get("consentOrder");
        assertThat(draftConsentOrder, equalTo(transformedCaseData.get("latestConsentOrder")));
        assertDocumentsMatchExpectations(draftConsentOrder, "DraftConsentOrder");

        assertThat(transformedCaseData, hasKey("divorceUploadEvidence1"));
        assertDocumentsMatchExpectations((CaseDocument) transformedCaseData.get("divorceUploadEvidence1"), "DecreeNisi");

        assertThat(transformedCaseData, hasKey("divorceUploadEvidence2"));
        assertDocumentsMatchExpectations((CaseDocument) transformedCaseData.get("divorceUploadEvidence2"), "DecreeAbsolute");
    }

    private void assertDocumentsMatchExpectations(CaseDocument decreeAbsoluteDocument, String subType) {
        assertThat(decreeAbsoluteDocument.getDocumentUrl(), is("http://url/" + subType));
        assertThat(decreeAbsoluteDocument.getDocumentBinaryUrl(), is("http://binUrl/" + subType + "/binary"));
        assertThat(decreeAbsoluteDocument.getDocumentFilename(), is(subType + ".pdf"));
    }

    private InputScannedDoc createDoc(String formSubType) {
        return InputScannedDoc.builder().subtype(formSubType).document(new InputScannedDocUrl("http://url/" + formSubType, "http://binUrl/" + formSubType + "/binary", formSubType + ".pdf")).build();
    }

    private void assertOnSingleFieldTransformationResult(String ocrFieldName, String ocrFieldValue, String ccdFieldName, String ccdFieldValue) {
        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(
            createExceptionRecord(asList(new OcrDataField(ocrFieldName, ocrFieldValue))));

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(PAPER_APPLICATION, YES_VALUE),
            hasEntry(ccdFieldName, ccdFieldValue)
        ));
    }

    private static ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id(TEST_CASE_ID).ocrDataFields(ocrDataFields).build();
    }

    private void assertChildrenInfo(Map<String, Object> transformedCaseData) {
        ComplexTypeCollection<ChildInfo> children = (ComplexTypeCollection<ChildInfo>) transformedCaseData.get("childrenInfo");

        assertChild(children.getItem(0), asList("Johny Bravo", "2000-03-12", "male", "son", "SON", "New Zeeland"));
        assertChild(children.getItem(1), asList("Anne Shirley", "1895-03-12", "female", "daughter", "Daughter", "Canada"));
    }

    private void assertChild(ChildInfo child, List<String> values) {
        assertThat(child.getName(), is(values.get(0)));
        assertThat(child.getDateOfBirth(), is(values.get(1)));
        assertThat(child.getGender(), is(values.get(2)));
        assertThat(child.getRelationshipToApplicant(), is(values.get(3)));
        assertThat(child.getRelationshipToRespondent(), is(values.get(4)));
        assertThat(child.getCountryOfResidence(), is(values.get(5)));
    }

}