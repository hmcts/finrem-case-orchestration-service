package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingActionService {

    private final ManageHearingsDocumentService manageHearingsDocumentService;

    public void performAddHearing(FinremCaseDetails finremCaseDetails, ManageHearingsWrapper hearingWrapper, String authToken) {
        Hearing hearing = finremCaseDetails.getData().getManageHearingsWrapper().getWorkingHearing();

        List<ManageHearingsCollectionItem> manageHearingsCollectionItemList = Optional.ofNullable(
                hearingWrapper.getHearings())
            .orElseGet(ArrayList::new);

        UUID manageHearingID = UUID.randomUUID();
        manageHearingsCollectionItemList.add(
            ManageHearingsCollectionItem.builder().id(manageHearingID).value(hearing).build()
        );
        hearingWrapper.setWorkingHearingId(manageHearingID);
        hearingWrapper.setHearings(manageHearingsCollectionItemList);

        CaseDocument hearingNotice = manageHearingsDocumentService
            .generateHearingNotice(hearing, finremCaseDetails, authToken);

        List<ManageHearingDocumentsCollectionItem> manageHearingDocuments = Optional.ofNullable(
                hearingWrapper.getHearingDocumentsCollection())
            .orElseGet(ArrayList::new);

        manageHearingDocuments.add(
            ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument
                    .builder()
                    .hearingId(manageHearingID)
                    .hearingDocument(hearingNotice)
                    .build())
                .build()
        );

        finremCaseDetails.getData().getManageHearingsWrapper()
            .setHearingDocumentsCollection(manageHearingDocuments);

        if (HearingType.FDA.equals(hearing.getHearingType())) {
            // Send hearing type specific notices
        }

    }
}
