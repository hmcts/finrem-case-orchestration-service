package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FdrDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour.IntervenerFourFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour.IntervenerFourOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone.IntervenerOneFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone.IntervenerOneOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree.IntervenerThreeFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree.IntervenerThreeOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo.IntervenerTwoFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo.IntervenerTwoOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.SendOrdersCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CfvUpdateTaskDocumentCategoriser {

    // DFR-2916
    private final UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;
    // DFR-2917, DFR-2955
    private final GeneralApplicationsCategoriser generalApplicationsCategoriser;
    // DFR-2918
    private final SendOrdersCategoriser sendOrdersCategoriser;
    // DFR-2923
    private final ApplicantOtherDocumentsHandler applicantOtherDocumentsHandler;
    private final RespondentOtherDocumentsHandler respondentOtherDocumentsHandler;
    private final IntervenerOneOtherDocumentsHandler intervenerOneOtherDocumentsHandler;
    private final IntervenerTwoOtherDocumentsHandler intervenerTwoOtherDocumentsHandler;
    private final IntervenerThreeOtherDocumentsHandler intervenerThreeOtherDocumentsHandler;
    private final IntervenerFourOtherDocumentsHandler intervenerFourOtherDocumentsHandler;
    // DFR-2947
    private final FdrDocumentsHandler fdrDocumentsHandler;
    private final IntervenerOneFdrHandler intervenerOneFdrHandler;
    private final IntervenerTwoFdrHandler intervenerTwoFdrHandler;
    private final IntervenerThreeFdrHandler intervenerThreeFdrHandler;
    private final IntervenerFourFdrHandler intervenerFourFdrHandler;

    private List<DocumentHandler> documentHandlers;
    private List<DocumentCategoriser> documentCategorisers;

    @PostConstruct
    void populateLists() {
        documentHandlers = List.of(
            applicantOtherDocumentsHandler, respondentOtherDocumentsHandler,
            intervenerOneOtherDocumentsHandler, intervenerTwoOtherDocumentsHandler,
            intervenerThreeOtherDocumentsHandler, intervenerFourOtherDocumentsHandler,
            fdrDocumentsHandler, intervenerOneFdrHandler,
            intervenerTwoFdrHandler, intervenerThreeFdrHandler,
            intervenerFourFdrHandler
        );

        documentCategorisers = List.of(
            sendOrdersCategoriser, uploadedDraftOrderCategoriser,
            generalApplicationsCategoriser
        );
    }

    public void categoriseDocuments(FinremCaseData caseData) {
        documentCategorisers.forEach(dc -> dc.categorise(caseData));
        documentHandlers.forEach(dh -> dh.assignDocumentCategoryToUploadDocumentsCollection(caseData));
    }
}
