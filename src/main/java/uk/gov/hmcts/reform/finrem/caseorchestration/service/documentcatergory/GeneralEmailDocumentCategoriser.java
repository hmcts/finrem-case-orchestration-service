package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Component
@Slf4j
public class GeneralEmailDocumentCategoriser extends DocumentCategoriser {

    @Autowired
    public GeneralEmailDocumentCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        emptyIfNull(finremCaseData.getGeneralEmailWrapper().getGeneralEmailCollection())
            .stream()
            .map(GeneralEmailCollection::getValue)
            .filter(Objects::nonNull)
            .forEach(generalEmail -> categoriseGeneralEmailDocuments(generalEmail, finremCaseData));
    }

    private void categoriseGeneralEmailDocuments(GeneralEmailHolder generalEmail,
                                                 FinremCaseData finremCaseData) {
        String recipient = generalEmail.getGeneralEmailRecipient();
        List<DocumentCollectionItem> documents = generalEmail.getGeneralEmailUploadedDocuments();

        if (recipient == null || CollectionUtils.isEmpty(documents)) {
            return;
        }

        String categoryId = getGeneralEmailCategory(recipient, finremCaseData);
        documents.forEach(documentItem -> setCategoryIfMissing(documentItem, categoryId));
    }

    private void setCategoryIfMissing(DocumentCollectionItem documentItem, String categoryId) {
        if (documentItem == null || documentItem.getValue() == null) {
            return;
        }

        CaseDocument document = documentItem.getValue();

        if (document.getCategoryId() == null) {
            document.setCategoryId(categoryId);
        }
    }

    private String getGeneralEmailCategory(String emailAddress, FinremCaseData caseData) {
        IntervenerWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapperIfPopulated();
        IntervenerWrapper intervenerTwoWrapper = caseData.getIntervenerTwoWrapperIfPopulated();
        IntervenerWrapper intervenerThreeWrapper = caseData.getIntervenerThreeWrapperIfPopulated();
        IntervenerWrapper intervenerFourWrapper = caseData.getIntervenerFourWrapperIfPopulated();
        ContactDetailsWrapper detailsWrapper = caseData.getContactDetailsWrapper();

        return getCategoryBasedOnRecipientRole(emailAddress, detailsWrapper, intervenerOneWrapper,
            intervenerTwoWrapper, intervenerThreeWrapper, intervenerFourWrapper);
    }

    private String getCategoryBasedOnRecipientRole(String emailAddress, ContactDetailsWrapper detailsWrapper,
                                                   IntervenerWrapper intervenerOneWrapper, IntervenerWrapper intervenerTwoWrapper,
                                                   IntervenerWrapper intervenerThreeWrapper, IntervenerWrapper intervenerFourWrapper) {
        if (recipientHasApplicantRole(emailAddress, detailsWrapper)) {
            return DocumentCategory.COURT_CORRESPONDENCE_APPLICANT.getDocumentCategoryId();
        } else if (recipientHasRespondentRole(emailAddress, detailsWrapper)) {
            return DocumentCategory.COURT_CORRESPONDENCE_RESPONDENT.getDocumentCategoryId();
        } else if (recipientHasIntervenerRole(emailAddress, intervenerOneWrapper)) {
            return DocumentCategory.COURT_CORRESPONDENCE_INTERVENER_1.getDocumentCategoryId();
        } else if (recipientHasIntervenerRole(emailAddress, intervenerTwoWrapper)) {
            return DocumentCategory.COURT_CORRESPONDENCE_INTERVENER_2.getDocumentCategoryId();
        } else if (recipientHasIntervenerRole(emailAddress, intervenerThreeWrapper)) {
            return DocumentCategory.COURT_CORRESPONDENCE_INTERVENER_3.getDocumentCategoryId();
        } else if (recipientHasIntervenerRole(emailAddress, intervenerFourWrapper)) {
            return DocumentCategory.COURT_CORRESPONDENCE_INTERVENER_4.getDocumentCategoryId();
        } else {
            return DocumentCategory.COURT_CORRESPONDENCE_OTHER.getDocumentCategoryId();
        }
    }

    private boolean recipientHasIntervenerRole(String emailAddress, IntervenerWrapper intervenerOneWrapper) {
        return emailAddress.equals(getIntervenerEmail(intervenerOneWrapper))
            || emailAddress.equals(getIntervenerSolEmail(intervenerOneWrapper));
    }

    private boolean recipientHasRespondentRole(String emailAddress, ContactDetailsWrapper detailsWrapper) {
        return emailAddress.equals(getRespondentEmail(detailsWrapper))
            || emailAddress.equals(getRespondentSolEmail(detailsWrapper));
    }

    private boolean recipientHasApplicantRole(String emailAddress, ContactDetailsWrapper detailsWrapper) {
        return emailAddress.equals(getApplicantEmail(detailsWrapper))
            || emailAddress.equals(getApplicantSolEmail(detailsWrapper));
    }

    private String getApplicantEmail(ContactDetailsWrapper wrapper) {
        return wrapper == null ? null : wrapper.getApplicantEmail();
    }

    private String getApplicantSolEmail(ContactDetailsWrapper wrapper) {
        return wrapper == null ? null : wrapper.getApplicantSolicitorEmail();
    }

    private String getRespondentEmail(ContactDetailsWrapper wrapper) {
        return wrapper == null ? null : wrapper.getRespondentEmail();
    }

    private String getRespondentSolEmail(ContactDetailsWrapper wrapper) {
        return wrapper == null ? null : wrapper.getRespondentSolicitorEmail();
    }

    private String getIntervenerSolEmail(IntervenerWrapper wrapper) {
        return wrapper == null ? null : wrapper.getIntervenerSolEmail();
    }

    private String getIntervenerEmail(IntervenerWrapper wrapper) {
        return wrapper == null ? null : wrapper.getIntervenerEmail();
    }
}
