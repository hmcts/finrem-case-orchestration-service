package uk.gov.hmcts.reform.finrem.caseorchestration.service.shareddocuments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class QuestionnaireAnswersDocumentSharer extends DocumentSharer {

    @Autowired
    public QuestionnaireAnswersDocumentSharer(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void setRespondentSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setRespQaCollectionShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getRespQaCollectionShared();
    }


    @Override
    protected void setApplicantSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setAppQaCollectionShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv1QaShared();
    }

    @Override
    protected void setIntervenerOneSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv1QaShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv2QaShared();
    }

    @Override
    protected void setIntervenerTwoSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv2QaShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv3QaShared();
    }

    @Override
    protected void setIntervenerThreeSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv3QaShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv4QaShared();
    }

    @Override
    protected void setIntervenerFourSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv4QaShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getAppQaCollectionShared();
    }


    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv4Qa();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv3Qa();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv2Qa();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv1Qa();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getRespQaCollection();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getAppQaCollection();
    }

    @Override
    protected String getIntervenerFourCollectionCcdKey() {
        return INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerThreeCollectionCcdKey() {
        return INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerTwoCollectionCcdKey() {
        return INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerOneCollectionCcdKey() {
        return INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getRespondentCollectionCcdKey() {
        return RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getApplicantCollectionCcdKey() {
        return APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
    }
}
