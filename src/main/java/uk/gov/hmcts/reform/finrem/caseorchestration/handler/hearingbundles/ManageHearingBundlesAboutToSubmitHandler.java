package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hearingbundles;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class ManageHearingBundlesAboutToSubmitHandler extends FinremCallbackHandler {

    public ManageHearingBundlesAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_HEARING_BUNDLES.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Handling manage hearing bundles about to submit callback for case id: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        List<String> errors = new ArrayList<>();
        List<HearingUploadBundleCollection> hearingUploadBundleCollection = caseData.getHearingUploadBundle();
        sortHearingBundlesAndValidateForErrors(errors, hearingUploadBundleCollection);

        List<HearingUploadBundleCollection> fdrHearingUploadBundleCollections =
            Optional.ofNullable(caseData.getFdrHearingBundleCollections()).orElse(new ArrayList<>());
        fdrHearingUploadBundleCollections.clear();

        hearingUploadBundleCollection.forEach(hearingUploadBundle -> {
            if (hearingUploadBundle.getValue().getHearingBundleFdr() == YesOrNo.YES) {
                fdrHearingUploadBundleCollections.add(hearingUploadBundle);

            }
        });

        caseData.getHearingUploadBundle().removeAll(fdrHearingUploadBundleCollections);
        caseData.setFdrHearingBundleCollections(fdrHearingUploadBundleCollections);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().errors(errors).data(caseData).build();

    }

    protected void sortHearingBundlesAndValidateForErrors(List<String> errors, List<HearingUploadBundleCollection> hearingUploadBundleCollection) {
        if (!hearingUploadBundleCollection.isEmpty()) {

            hearingUploadBundleCollection.sort(Comparator.nullsLast((e1, e2) -> e2.getValue().getHearingBundleDate()
                .compareTo(e1.getValue().getHearingBundleDate())));

            hearingUploadBundleCollection.forEach(hearingUploadBundle -> {
                List<HearingBundleDocumentCollection> hearingBundleDocuments = hearingUploadBundle.getValue().getHearingBundleDocuments();
                hearingBundleDocuments.forEach(hearingUploadBundleData -> {
                    setUploadBundleDateAndValidateFilename(errors, hearingUploadBundleData);
                });
                hearingBundleDocuments.sort(Comparator.nullsLast((e1, e2) -> e2.getValue().getBundleUploadDate()
                    .compareTo(e1.getValue().getBundleUploadDate())));
                log.info("Sorted hearing bundle documents for case id: {}", hearingUploadBundle.getValue().getHearingBundleDate());

            });
        }
    }

    private static void setUploadBundleDateAndValidateFilename(List<String> errors, HearingBundleDocumentCollection hearingUploadBundleData) {
        if (hearingUploadBundleData.getValue().getBundleUploadDate() == null) {
            hearingUploadBundleData.getValue().setBundleUploadDate(LocalDateTime.now());
        }
        String documentFilename = hearingUploadBundleData.getValue().getBundleDocuments().getDocumentFilename();
        if (!documentFilename
            .toUpperCase(Locale.ENGLISH).endsWith(".PDF")) {
            errors.add(String.format("Uploaded bundle %s is not in expected format. Please upload bundle in pdf format.",
                documentFilename));
        }
    }


}
