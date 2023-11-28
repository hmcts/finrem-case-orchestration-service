package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.ADMINISTRATIVE_DOCUMENTS_TRANSITIONAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.CORRESPONDENCE_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.CORRESPONDENCE_INTERVENER_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.CORRESPONDENCE_INTERVENER_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.CORRESPONDENCE_INTERVENER_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.CORRESPONDENCE_INTERVENER_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.CORRESPONDENCE_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService.CORRESPONDENCE;

@Component
@Slf4j
public class CreateGeneralLetterDocumentCategoriser extends DocumentCategoriser {

    @Autowired
    public CreateGeneralLetterDocumentCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        log.info("Categorising general letter documents based on the recipient role for case with Case ID: {}",
            finremCaseData.getCcdCaseId());
        GeneralLetterWrapper wrapper = finremCaseData.getGeneralLetterWrapper();
        List<GeneralLetterCollection> generalLetters =
            wrapper.getGeneralLetterCollection();
        CaseDocument previewDocument = wrapper.getGeneralLetterPreview();
        if (previewDocument != null
            && previewDocument.getCategoryId() == null) {
            previewDocument.setCategoryId(
                ADMINISTRATIVE_DOCUMENTS_TRANSITIONAL.getDocumentCategoryId());
        }
        if (generalLetters != null && !generalLetters.isEmpty()) {
            for (GeneralLetterCollection generalLetter : generalLetters) {
                CaseDocument generatedLetter = generalLetter.getValue().getGeneratedLetter();
                if (generatedLetter != null && generatedLetter.getCategoryId() == null) {
                    generatedLetter.setCategoryId(getGeneratedLetterCategoryId(
                        wrapper.getGeneralLetterAddressee().getValue().getCode()));
                }
            }
        }
        //TODO: Refactor added code, check if generalLetterPreview field should go into transitional,
        // should the category id for generalLetterPreview be set in the orchestrator since it can't be set in the
        // definitions case file common file, check the folder if other is the recipient
    }

    private String getGeneratedLetterCategoryId(String roleCode) {
        switch(roleCode) {
            case APPLICANT, APPLICANT_SOLICITOR -> {
                return CORRESPONDENCE_APPLICANT.getDocumentCategoryId();
            }
            case RESPONDENT, RESPONDENT_SOLICITOR -> {
                return CORRESPONDENCE_RESPONDENT.getDocumentCategoryId();
            }
            case INTERVENER1, INTERVENER1_SOLICITOR -> {
                return CORRESPONDENCE_INTERVENER_1.getDocumentCategoryId();
            }
            case INTERVENER2, INTERVENER2_SOLICITOR -> {
                return CORRESPONDENCE_INTERVENER_2.getDocumentCategoryId();
            }
            case INTERVENER3, INTERVENER3_SOLICITOR -> {
                return CORRESPONDENCE_INTERVENER_3.getDocumentCategoryId();
            }
            case INTERVENER4, INTERVENER4_SOLICITOR -> {
                return CORRESPONDENCE_INTERVENER_4.getDocumentCategoryId();
            }
            default -> {
                return CORRESPONDENCE;
            }
        }
    }
}
