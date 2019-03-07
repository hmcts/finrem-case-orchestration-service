package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class CaseDataTest {
    protected CaseData data;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        data = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/case-data.json").toURI()), CaseData.class);
    }

    @Test
    public void shouldCreateCaseDataFromJson() {
        assertCaseData();
    }

    protected void assertCaseData() {
        assertSolicitorDetails();
        assertDivorceDetails();
        assertApplicantDetails();
        assertRespondentDetails();
        assertRespondentSolicitorDetails();
        assertRespondentContactDetails();
        assertNatureOfApplication();
        assertOrderForChildren();
        assertAuthorisation();
        assertConsentOrder();
        assertD81();
        assertOptionalDocs();
        assertPayment();
        assertOrderDetails();
    }

    private void assertSolicitorDetails() {
        assertThat(data.getSolicitorName(), is("Solictor"));
        assertThat(data.getSolicitorFirm(), is("FirmA"));
        assertThat(data.getSolicitorReference(), is("LL01"));
        assertThat(data.getSolicitorAddress().getAddressLine1(), is("line1"));
        assertThat(data.getSolicitorAddress().getAddressLine2(), is("line2"));
        assertThat(data.getSolicitorAddress().getAddressLine3(), is("line3"));
        assertThat(data.getSolicitorAddress().getPostTown(), is("london"));
        assertThat(data.getSolicitorAddress().getPostCode(), is("b1 1ab"));
        assertThat(data.getSolicitorAddress().getCounty(), is("essex"));
        assertThat(data.getSolicitorAddress().getCountry(), is("UK"));
        assertThat(data.getSolicitorPhone(), is("1234"));
        assertThat(data.getSolicitorEmail(), is("test@admin.com"));
        assertThat(data.getSolicitorDXnumber(), is("dx1"));
        assertThat(data.getSolicitorAgreeToReceiveEmails(), is("No"));
    }

    private void assertDivorceDetails() {
        assertThat(data.getDivorceCaseNumber(), is("DD12D12345"));
        assertThat(data.getDivorceStageReached(), is("Decree Nisi"));
        assertThat(data.getDivorceUploadEvidence1().getDocumentFilename(), is("file1"));
        assertThat(data.getDivorceUploadEvidence1().getDocumentUrl(), is("http://file1"));
        assertThat(data.getDivorceUploadEvidence1().getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(data.getDivorceDecreeNisiDate(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getDivorceUploadEvidence2().getDocumentFilename(), is("file2"));
        assertThat(data.getDivorceUploadEvidence2().getDocumentUrl(), is("http://file2"));
        assertThat(data.getDivorceUploadEvidence2().getDocumentBinaryUrl(), is("http://file2.binary"));
        assertThat(data.getDivorceDecreeAbsoluteDate(), is(Date.valueOf("2010-01-01")));
    }

    private void assertApplicantDetails() {
        assertThat(data.getApplicantFMName(), is("Poor"));
        assertThat(data.getApplicantLName(), is("Guy"));
    }

    private void assertRespondentDetails() {
        assertThat(data.getAppRespondentFMName(), is("john"));
        assertThat(data.getAppRespondentLName(), is("smith"));
        assertThat(data.getAppRespondentRep(), is("No"));
    }

    private void assertRespondentSolicitorDetails() {
        assertThat(data.getRespondentSolicitorName(), is(nullValue()));
        assertThat(data.getRespondentSolicitorFirm(), is(nullValue()));
        assertThat(data.getRespondentSolicitorReference(), is(nullValue()));
        assertThat(data.getRespondentSolicitorAddress(), is(nullValue()));
        assertThat(data.getRespondentSolicitorPhone(), is(nullValue()));
        assertThat(data.getRespondentSolicitorEmail(), is(nullValue()));
        assertThat(data.getRespondentSolicitorDxNumber(), is(nullValue()));
    }

    private void assertRespondentContactDetails() {
        assertThat(data.getRespondentPhone(), is("9963472494"));
        assertThat(data.getRespondentEmail(), is("test@test.com"));
        assertThat(data.getRespondentAddress().getAddressLine1(), is("line1"));
        assertThat(data.getRespondentAddress().getAddressLine2(), is("line2"));
        assertThat(data.getRespondentAddress().getAddressLine3(), is("line3"));
        assertThat(data.getRespondentAddress().getPostTown(), is("london"));
        assertThat(data.getRespondentAddress().getPostCode(), is("b1 1ab"));
        assertThat(data.getRespondentAddress().getCounty(), is("essex"));
        assertThat(data.getRespondentAddress().getCountry(), is("UK"));
    }

    private void assertNatureOfApplication() {
        assertThat(data.getNatureOfApplication2(), hasItems("item1", "item2"));
        assertThat(data.getNatureOfApplication3a(), is("test"));
        assertThat(data.getNatureOfApplication3b(), is("test"));
    }

    private void assertOrderForChildren() {
        assertThat(data.getOrderForChildrenQuestion1(), is("Yes"));
        assertThat(data.getNatureOfApplication5(), is("No"));
        assertThat(data.getNatureOfApplication6(), hasItems("item1", "item2"));
        assertThat(data.getNatureOfApplication7(), is("test"));
    }

    private void assertAuthorisation() {
        assertThat(data.getAuthorisationName(), is("test"));
        assertThat(data.getAuthorisationFirm(), is("test"));
        assertThat(data.getAuthorisation2b(), is("test"));
        assertThat(data.getAuthorisation3(), is(Date.valueOf("2010-01-01")));
    }

    private void assertConsentOrder() {
        assertThat(data.getConsentOrder().getDocumentFilename(), is("file1"));
        assertThat(data.getConsentOrder().getDocumentUrl(), is("http://file1"));
        assertThat(data.getConsentOrder().getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(data.getConsentOrderText().getDocumentFilename(), is("file1"));
        assertThat(data.getConsentOrderText().getDocumentUrl(), is("http://file1"));
        assertThat(data.getConsentOrderText().getDocumentBinaryUrl(), is("http://file1.binary"));
    }


    private void assertD81() {
        assertThat(data.getD81Question(), is("No"));
        assertThat(data.getD81Joint().getDocumentFilename(), is("file1"));
        assertThat(data.getD81Joint().getDocumentUrl(), is("http://file1"));
        assertThat(data.getD81Joint().getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(data.getD81Applicant().getDocumentFilename(), is("file1"));
        assertThat(data.getD81Applicant().getDocumentUrl(), is("http://file1"));
        assertThat(data.getD81Applicant().getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(data.getD81Respondent().getDocumentFilename(), is("file1"));
        assertThat(data.getD81Respondent().getDocumentUrl(), is("http://file1"));
        assertThat(data.getD81Respondent().getDocumentBinaryUrl(), is("http://file1.binary"));
    }

    private void assertOptionalDocs() {
        assertThat(data.getPensionCollection().size(), is(1));
        DocumentData doc = data.getPensionCollection().get(0);
        assertThat(doc.getId(), is("1"));
        assertThat(doc.getDocumentType().getTypeOfDocument(), is("pdf"));
        assertThat(doc.getDocumentType().getUploadedDocument().getDocumentUrl(), is("http://file1"));
        assertThat(doc.getDocumentType().getUploadedDocument().getDocumentFilename(), is("file1"));
        assertThat(doc.getDocumentType().getUploadedDocument().getDocumentBinaryUrl(), is("http://file1.binary"));

        assertThat(data.getOtherCollection().size(), is(1));
        DocumentData otherDoc = data.getOtherCollection().get(0);
        assertThat(otherDoc.getId(), is("1"));
        assertThat(otherDoc.getDocumentType().getTypeOfDocument(), is("pdf"));
        assertThat(otherDoc.getDocumentType().getUploadedDocument().getDocumentUrl(), is("http://file1"));
        assertThat(otherDoc.getDocumentType().getUploadedDocument().getDocumentFilename(), is("file1"));
        assertThat(otherDoc.getDocumentType().getUploadedDocument().getDocumentBinaryUrl(), is("http://file1.binary"));
    }

    private void assertPayment() {
        assertThat(data.getHelpWithFeesQuestion(), is("No"));
        assertThat(data.getHwfNumber(), is(nullValue()));
        assertThat(data.getPbaNumber(), is("PBA123456"));
        assertThat(data.getPbaReference(), is("ABCD"));
    }

    private void assertOrderDetails() {
        assertThat(data.getOrderDirection(), is("test"));
        assertThat(data.getOrderDirectionOpt1().getDocumentUrl(), is("http://file1"));
        assertThat(data.getOrderDirectionOpt1().getDocumentFilename(), is("file1"));
        assertThat(data.getOrderDirectionOpt1().getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(data.getOrderDirectionOpt2(), is("test"));
        assertThat(data.getOrderDirectionAbsolute(), is("test"));
        assertThat(data.getOrderDirectionJudge(), is("test"));
        assertThat(data.getOrderDirectionJudgeName(), is("test"));
        assertThat(data.getOrderDirectionDate(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getOrderDirectionAddComments(), is("test"));

        assertThat(data.getOrderRefusalCollection().size(), is(1));
        OrderRefusalData order = data.getOrderRefusalCollection().get(0);
        assertThat(order.getId(), is("1"));
        assertThat(order.getOrderRefusal().getOrderRefusal(), hasItems("Other"));
        assertThat(order.getOrderRefusal().getOrderRefusalDate(), is(Date.valueOf("2003-02-01")));
        assertThat(order.getOrderRefusal().getOrderRefusalDocs().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getOrderRefusal().getOrderRefusalDocs().getDocumentFilename(), is("doc1"));
        assertThat(order.getOrderRefusal().getOrderRefusalDocs().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getOrderRefusal().getOrderRefusalJudge(), is("District Judge"));
        assertThat(order.getOrderRefusal().getOrderRefusalOther(), is("test1"));
        assertThat(order.getOrderRefusal().getOrderRefusalJudgeName(), is("test3"));
        assertThat(order.getOrderRefusal().getOrderRefusalAddComments(), is("comment1"));

        assertThat(data.getDueDate(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getIssueDate(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getAssignedToJudge(), is("judge1"));
        assertThat(data.getAssignedToJudgeReason(), is("test"));

        assertThat(data.getReferToJudgeDate(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getReferToJudgeDate(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getReferToJudgeText(), is("test"));
        assertThat(data.getReferToJudgeDateFromAwaitingResponse(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getReferToJudgeTextFromAwaitingResponse(), is("test"));
        assertThat(data.getReferToJudgeDateFromConsOrdApproved(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getReferToJudgeTextFromConsOrdApproved(), is("test"));
        assertThat(data.getReferToJudgeDateFromConsOrdMade(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getReferToJudgeTextFromConsOrdMade(), is("test"));
        assertThat(data.getReferToJudgeDateFromOrderMade(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getReferToJudgeTextFromOrderMade(), is("test"));
        assertThat(data.getReferToJudgeDateFromClose(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getReferToJudgeTextFromClose(), is("test"));
        assertThat(data.getReferToJudgeDateFromRespondToOrder(), is(Date.valueOf("2010-01-01")));
        assertThat(data.getReferToJudgeTextFromRespondToOrder(), is("test"));

        assertThat(data.getUploadConsentOrderDocuments().size(), is(1));
        ConsentOrderData consOrder = data.getUploadConsentOrderDocuments().get(0);
        assertThat(consOrder.getId(), is("1"));
        assertThat(consOrder.getConsentOrder().getDocumentType(), is("pdf"));
        assertThat(consOrder.getConsentOrder().getDocumentLink().getDocumentUrl(), is("http://doc1"));
        assertThat(consOrder.getConsentOrder().getDocumentLink().getDocumentFilename(), is("doc1"));
        assertThat(consOrder.getConsentOrder().getDocumentLink().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(consOrder.getConsentOrder().getDocumentEmailContent(), is("email-content"));
        assertThat(consOrder.getConsentOrder().getDocumentDateAdded(), is(Date.valueOf("2010-01-02")));
        assertThat(consOrder.getConsentOrder().getDocumentComment(), is("doc-comment"));
        assertThat(consOrder.getConsentOrder().getDocumentFileName(), is("file1"));

        assertThat(data.getUploadOrder().size(), is(1));
        ConsentOrderData uploadOrder = data.getUploadConsentOrderDocuments().get(0);
        assertThat(uploadOrder.getId(), is("1"));
        assertThat(uploadOrder.getConsentOrder().getDocumentType(), is("pdf"));
        assertThat(uploadOrder.getConsentOrder().getDocumentLink().getDocumentUrl(), is("http://doc1"));
        assertThat(uploadOrder.getConsentOrder().getDocumentLink().getDocumentFilename(), is("doc1"));
        assertThat(uploadOrder.getConsentOrder().getDocumentLink().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(uploadOrder.getConsentOrder().getDocumentEmailContent(), is("email-content"));
        assertThat(uploadOrder.getConsentOrder().getDocumentDateAdded(), is(Date.valueOf("2010-01-02")));
        assertThat(uploadOrder.getConsentOrder().getDocumentComment(), is("doc-comment"));
        assertThat(uploadOrder.getConsentOrder().getDocumentFileName(), is("file1"));


        assertThat(data.getUploadDocuments().size(), is(1));
        ConsentOrderData uploadDoc = data.getUploadDocuments().get(0);
        assertThat(uploadDoc.getId(), is("1"));
        assertThat(uploadDoc.getConsentOrder().getDocumentType(), is("pdf"));
        assertThat(uploadDoc.getConsentOrder().getDocumentLink().getDocumentUrl(), is("http://doc1"));
        assertThat(uploadDoc.getConsentOrder().getDocumentLink().getDocumentFilename(), is("doc1"));
        assertThat(uploadDoc.getConsentOrder().getDocumentLink().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(uploadDoc.getConsentOrder().getDocumentEmailContent(), is("email-content"));
        assertThat(uploadDoc.getConsentOrder().getDocumentDateAdded(), is(Date.valueOf("2010-01-02")));
        assertThat(uploadDoc.getConsentOrder().getDocumentComment(), is("doc-comment"));
        assertThat(uploadDoc.getConsentOrder().getDocumentFileName(), is("file1"));


        assertThat(data.getGeneralOrderCollection().size(), is(1));
        GeneralOrderData genOrder = data.getGeneralOrderCollection().get(0);
        assertThat(genOrder.getId(), is("1"));
        assertThat(genOrder.getGeneralOrder().getGeneralOrder(), is("order1"));
        assertThat(genOrder.getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentUrl(), is("http://doc1"));
        assertThat(genOrder.getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentFilename(), is("doc1"));
        assertThat(genOrder.getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(genOrder.getGeneralOrder().getGeneralOrderJudgeType(), is("district judge"));
        assertThat(genOrder.getGeneralOrder().getGeneralOrderJudgeName(), is("judge1"));
        assertThat(genOrder.getGeneralOrder().getGeneralOrderDate(), is(Date.valueOf("2010-01-02")));
        assertThat(genOrder.getGeneralOrder().getGeneralOrderComments(), is("comment1"));

        assertThat(data.getRespondToOrderDocuments().size(), is(1));
        RespondToOrderData respOrder = data.getRespondToOrderDocuments().get(0);
        assertThat(respOrder.getId(), is("1"));
        assertThat(respOrder.getRespondToOrder().getDocumentType(), is("pdf"));
        assertThat(respOrder.getRespondToOrder().getDocumentLink().getDocumentUrl(), is("http://doc1"));
        assertThat(respOrder.getRespondToOrder().getDocumentLink().getDocumentFilename(), is("doc1"));
        assertThat(respOrder.getRespondToOrder().getDocumentLink().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(respOrder.getRespondToOrder().getDocumentDateAdded(), is(Date.valueOf("2010-01-02")));
        assertThat(respOrder.getRespondToOrder().getDocumentFileName(), is("file1"));

        assertThat(data.getAmendedConsentOrderCollection().size(), is(1));
        AmendedConsentOrderData amendOrder = data.getAmendedConsentOrderCollection().get(0);
        assertThat(amendOrder.getId(), is("1"));
        assertThat(amendOrder.getAmendedConsentOrder().getAmendedConsentOrder().getDocumentUrl(), is("http://doc1"));
        assertThat(amendOrder.getAmendedConsentOrder().getAmendedConsentOrder().getDocumentFilename(), is("doc1"));
        assertThat(amendOrder.getAmendedConsentOrder().getAmendedConsentOrder().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(amendOrder.getAmendedConsentOrder().getAmendedConsentOrderDate(), is(Date.valueOf("2010-01-02")));
    }

}