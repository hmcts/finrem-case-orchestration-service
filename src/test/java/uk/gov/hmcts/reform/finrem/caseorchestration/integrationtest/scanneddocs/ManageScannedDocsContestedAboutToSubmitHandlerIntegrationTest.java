package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.scanneddocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs.ManageScannedDocsContestedAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentFdrDocumentCategoriser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ContestedAboutToSubmitHandlerTestConfiguration.class,
    ManageScannedDocsContestedAboutToSubmitHandler.class,
    ApplicantFdrDocumentCategoriser.class, RespondentFdrDocumentCategoriser.class,
    ObjectMapper.class, FinremCaseDetailsMapper.class })
public class ManageScannedDocsContestedAboutToSubmitHandlerIntegrationTest {

    @Autowired
    private List<DocumentHandler> documentHandlers;

    @Autowired
    private ManageScannedDocsContestedAboutToSubmitHandler handler;

    @ParameterizedTest
    @MethodSource
    void testHandle(CaseDocumentType caseDocumentType, boolean isConfidential, boolean isFdr,
                    CaseDocumentParty caseDocumentParty, String other) {
        FinremCaseData caseData = createCaseData(caseDocumentType, isConfidential, isFdr, caseDocumentParty, other);
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);

        var response = handler.handle(request, "some-token");

        // check scanned doc is no longer in case data i.e. it has been processed and moved to another
        // document collection
        List<ScannedDocumentCollection> scannedDocumentCollections = response.getData().getScannedDocuments();
        assertThat(scannedDocumentCollections).isEmpty();
        assertThat(documentHandlers).isNotEmpty();
    }

    private static Stream<Arguments> testHandle() {
        List<Arguments> data = new ArrayList<>();
        data.add(noOptions(CaseDocumentType.ATTENDANCE_SHEETS));
        data.addAll(allOptions(CaseDocumentType.BILL_OF_COSTS));
        data.addAll(allOptions(CaseDocumentType.CASE_SUMMARY));
        data.addAll(allOptions(CaseDocumentType.CERTIFICATES_OF_SERVICE));
        data.addAll(allOptions(CaseDocumentType.CONDITIONAL_ORDER));
        data.addAll(allOptions(CaseDocumentType.LETTER_FROM_APPLICANT)); // Correspondence
        data.addAll(allOptions(CaseDocumentType.STATEMENT_OF_ISSUES));
        data.addAll(allOptions(CaseDocumentType.CHRONOLOGY));
        data.addAll(allOptions(CaseDocumentType.ES1));
        data.addAll(allOptions(CaseDocumentType.ES2));
        data.addAll(allOptions(CaseDocumentType.EXPERT_EVIDENCE));
        data.addAll(allOptions(CaseDocumentType.VALUATION_REPORT)); // Family home valuation
        data.addAll(allOptions(CaseDocumentType.FINAL_ORDER));
        data.addAll(allOptions(CaseDocumentType.FM5));
        data.addAll(allOptions(CaseDocumentType.APPLICANT_FORM_E)); // Form E & Exhibits
        data.addAll(allOptions(CaseDocumentType.FORM_G));
        data.addAll(allOptions(CaseDocumentType.FORM_H));
        data.addAll(allOptions(CaseDocumentType.HOUSING_PARTICULARS));
        data.add(noOptions(CaseDocumentType.JUDICIAL_NOTES));
        data.add(noOptions(CaseDocumentType.JUDGMENT));
        data.addAll(allOptions(CaseDocumentType.MORTGAGE_CAPACITIES));
        data.addAll(allOptions(CaseDocumentType.OFFERS)); // Open Offers
        data.addAll(allOptions(CaseDocumentType.PENSION_REPORT));
        data.addAll(allOptions(CaseDocumentType.POSITION_STATEMENT_SKELETON_ARGUMENT));
        data.addAll(allOptions(CaseDocumentType.POINTS_OF_CLAIM_OR_DEFENCE));
        data.addAll(allOptions(CaseDocumentType.PRE_HEARING_DRAFT_ORDER));
        data.addAll(allOptions(CaseDocumentType.QUESTIONNAIRE));
        data.addAll(allOptions(CaseDocumentType.REPLY_TO_QUESTIONNAIRE));
        data.addAll(allOptions(CaseDocumentType.STATEMENT_AFFIDAVIT));
        data.add(noOptions(CaseDocumentType.TRANSCRIPT));
        data.add(noOptions(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS));
        data.addAll(allOptions(CaseDocumentType.WITNESS_STATEMENT_AFFIDAVIT));
        data.add(noOptions(CaseDocumentType.WITNESS_SUMMONS));
        data.addAll(allOptionsDocumentTypeOther());
        data.addAll(allOptions(CaseDocumentType.TRIAL_BUNDLE));

        return data.stream();
    }

    private static Arguments noOptions(CaseDocumentType caseDocumentType) {
        return Arguments.of(caseDocumentType, false, false, null, null);
    }

    private static List<Arguments> allOptions(CaseDocumentType caseDocumentType) {
        List<Arguments> data = new ArrayList<>();
        data.add(Arguments.of(caseDocumentType, true, false, null, null));
        data.add(Arguments.of(caseDocumentType, false, true, null, null));
        data.addAll(allParties(caseDocumentType));
        return data;
    }

    private static List<Arguments> allOptionsDocumentTypeOther() {
        List<Arguments> data = new ArrayList<>();
        data.add(Arguments.of(CaseDocumentType.OTHER, true, false, null, null));
        data.add(Arguments.of(CaseDocumentType.OTHER, false, true, null, null));
        data.addAll(allParties(CaseDocumentType.OTHER, "Unknown Type"));
        return data;
    }

    private static List<Arguments> allParties(CaseDocumentType caseDocumentType) {
        return allParties(caseDocumentType, null);
    }

    private static List<Arguments> allParties(CaseDocumentType caseDocumentType, String other) {
        return Arrays.stream(CaseDocumentParty.values())
            .map(party -> Arguments.of(caseDocumentType, false, false, party, other))
            .toList();
    }

    private FinremCaseData createCaseData(CaseDocumentType caseDocumentType, boolean isConfidential,
                                          boolean isFdr, CaseDocumentParty caseDocumentParty, String other) {
        ScannedDocumentCollection scannedDocumentCollection = ScannedDocumentCollection.builder()
            .id("1")
            .value(ScannedDocument.builder()
                .url(CaseDocument.builder().build())
                .build())
            .build();

        UploadCaseDocument uploadCaseDocument = UploadCaseDocument.builder()
            .caseDocuments(scannedDocumentCollection.getValue().getUrl())
            .caseDocumentType(caseDocumentType)
            .caseDocumentConfidentiality(isConfidential ? YesOrNo.YES : YesOrNo.NO)
            .caseDocumentFdr(isFdr ? YesOrNo.YES : YesOrNo.NO)
            .caseDocumentParty(caseDocumentParty)
            .caseDocumentOther(other)
            .build();

        ManageScannedDocument manageScannedDocument = ManageScannedDocument.builder()
            .selectForUpdate(YesOrNo.YES)
            .uploadCaseDocument(uploadCaseDocument)
            .build();

        ManageScannedDocumentCollection manageScannedDocumentCollection = ManageScannedDocumentCollection.builder()
            .id("1")
            .manageScannedDocument(manageScannedDocument)
            .build();

        return FinremCaseData.builder()
            .scannedDocuments(Lists.newArrayList(scannedDocumentCollection))
            .manageScannedDocumentCollection(Lists.newArrayList(manageScannedDocumentCollection))
            .build();
    }
}
