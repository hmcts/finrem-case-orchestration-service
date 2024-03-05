package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.Optional;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.CORRESPONDENCE_OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.CORRESPONDENCE_RESPONDENT;

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
        categorisePreviewDocument(wrapper);
        categoriseGeneralLetters(wrapper);
        categoriseUploadedDocuments(wrapper);
    }

    private void categorisePreviewDocument(GeneralLetterWrapper wrapper) {
        Optional.ofNullable(wrapper.getGeneralLetterPreview())
            .filter(previewDocument -> previewDocument.getCategoryId() == null)
            .ifPresent(previewDocument -> previewDocument.setCategoryId(
                ADMINISTRATIVE_DOCUMENTS_TRANSITIONAL.getDocumentCategoryId()));
    }

    private void categoriseGeneralLetters(GeneralLetterWrapper wrapper) {
        Optional.ofNullable(wrapper.getGeneralLetterCollection())
            .ifPresent(generalLetters -> generalLetters.stream()
                .map(GeneralLetterCollection::getValue)
                .map(GeneralLetter::getGeneratedLetter)
                .filter(generatedLetter -> generatedLetter.getCategoryId() == null)
                .forEach(generatedLetter -> generatedLetter.setCategoryId(
                    getGeneratedLetterCategoryId(getSelectedRoleCode(wrapper)))));
    }

    private void categoriseUploadedDocuments(GeneralLetterWrapper wrapper) {
        Optional.ofNullable(wrapper.getGeneralLetterUploadedDocuments())
            .ifPresent(uploadedDocuments -> uploadedDocuments.stream()
                .filter(document -> document.getValue().getCategoryId() == null)
                .forEach(document -> document.getValue().setCategoryId(
                    getGeneratedLetterCategoryId(getSelectedRoleCode(wrapper)))));
    }

    private static String getSelectedRoleCode(GeneralLetterWrapper wrapper) {
        return wrapper.getGeneralLetterAddressee() != null ? wrapper.getGeneralLetterAddressee().getValue().getCode() : "";
    }

    private String getGeneratedLetterCategoryId(String roleCode) {
        switch (roleCode) {
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
                return CORRESPONDENCE_OTHER.getDocumentCategoryId();
            }
        }
    }
}
