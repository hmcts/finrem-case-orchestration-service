package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.validation;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ManageDocumentsHandlerValidatorTest {

    public static final String AUTH_TOKEN = "AuthTokien";
    public static final String DOCUMENT_URL_TEST = "document/url/test";
    public static final String CHOOSE_A_DIFFERENT_PARTY = " not present on the case, do you want to continue?";
    public static final String INTERVENER_1 = "Intervener 1 ";
    public static final String INTERVENER_2 = "Intervener 2 ";
    public static final String INTERVENER_3 = "Intervener 3 ";
    public static final String INTERVENER_4 = "Intervener 4 ";

    ManageDocumentsHandlerValidator manageDocumentsHandlerValidator = new ManageDocumentsHandlerValidator();
    private final List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();


    @Test
    public void givenInCorrectInterverner1Selected_thenNoValidationError() {
        testIntervernerNotValid(CaseDocumentParty.INTERVENER_ONE, INTERVENER_1);
    }

    @Test
    public void givenInCorrectInterverner2Selected_thenNoValidationError() {
        testIntervernerNotValid(CaseDocumentParty.INTERVENER_TWO, INTERVENER_2);
    }

    @Test
    public void givenInCorrectInterverner3Selected_thenNoValidationError() {
        testIntervernerNotValid(CaseDocumentParty.INTERVENER_THREE, INTERVENER_3);
    }

    @Test
    public void givenInCorrectInterverner4Selected_thenNoValidationError() {
        testIntervernerNotValid(CaseDocumentParty.INTERVENER_FOUR, INTERVENER_4);
    }

    @Test
    public void givenCorrectPartiesAreSelected_thenNoValidationError() {
        setUpAddedDocuments();
        FinremCaseData caseData = FinremCaseData.builder()
            .intervenerOneWrapper(IntervenerOneWrapper.builder().intervenerName("Intervener 1").build())
            .manageScannedDocumentCollection(screenUploadDocumentList).build();
        caseData.getManageScannedDocumentCollection().get(0).getUploadCaseDocument()
            .setCaseDocumentParty(CaseDocumentParty.INTERVENER_ONE);

        List<String> warnings = new ArrayList<>();

        manageDocumentsHandlerValidator.validateSelectedIntervenerParties(caseData, caseData.getManageScannedDocumentCollection(), warnings);

        assertEquals(warnings.size(), 0);
    }

    private void testIntervernerNotValid(CaseDocumentParty intervenerParty, String intervenerText) {
        setUpAddedDocuments();
        FinremCaseData caseData = FinremCaseData.builder().manageScannedDocumentCollection(screenUploadDocumentList).build();
        caseData.getManageScannedDocumentCollection().get(0).getUploadCaseDocument()
            .setCaseDocumentParty(intervenerParty);
        List<String> warnings = new ArrayList<>();

        manageDocumentsHandlerValidator.validateSelectedIntervenerParties(caseData, caseData.getManageScannedDocumentCollection(), warnings);

        assertEquals(warnings.get(0), intervenerText + CHOOSE_A_DIFFERENT_PARTY);
    }

    private void setUpAddedDocuments() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_OF_ISSUES,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.YES, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
    }


    private UploadCaseDocumentCollection createContestedUploadDocumentItem(CaseDocumentType type,
                                                                           CaseDocumentParty party,
                                                                           YesOrNo isConfidential,
                                                                           YesOrNo isFdr,
                                                                           String other) {
        UUID uuid = UUID.randomUUID();

        return UploadCaseDocumentCollection.builder()
            .id(uuid.toString())
            .uploadCaseDocument(UploadCaseDocument
                .builder()
                .caseDocuments(CaseDocument.builder().documentUrl(DOCUMENT_URL_TEST).build())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidentiality(isConfidential)
                .caseDocumentOther(other)
                .caseDocumentFdr(isFdr)
                .hearingDetails(null)
                .caseDocumentUploadDateTime(LocalDateTime.now())
                .build())
            .build();
    }

}
