package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Slf4j
public abstract class AbstractLetterHandler implements LetterHandler {

    public static final String COR_APPLICANT = "Applicant";
    public static final String COR_RESPONDENT = "Respondent";
    protected final AbstractLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator;
    protected final NocDocumentService nocDocumentService;
    protected final BulkPrintService bulkPrintService;
    private final NoticeType noticeType;
    private final DocumentHelper.PaperNotificationRecipient recipient;

    public AbstractLetterHandler(
        AbstractLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator, NocDocumentService nocDocumentService,
        BulkPrintService bulkPrintService,
        NoticeType noticeType, DocumentHelper.PaperNotificationRecipient recipient) {

        this.noticeOfChangeLetterDetailsGenerator = noticeOfChangeLetterDetailsGenerator;
        this.bulkPrintService = bulkPrintService;
        this.noticeType = noticeType;
        this.recipient = recipient;
        this.nocDocumentService = nocDocumentService;
    }

    public void handle(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authToken) {
        log.info("In the LetterHandler for Recipient {} and Notice Type {} ", recipient, noticeType);
        Optional<NoticeOfChangeLetterDetails> noticeOfChangeLetterDetails = getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore);
        noticeOfChangeLetterDetails.ifPresent(letter -> {
            log.info("Got the letter details now call the document service to generate the Case Document ");
            Document caseDocument = nocDocumentService.generateNoticeOfChangeLetter(authToken, letter);
            log.info("Generated the case document now send to bulk print");
            UUID uuid = bulkPrintService.sendDocumentForPrint(caseDocument, caseDetails);
            log.info("Document sent to bulkprint with UUID {}", uuid);
        });
    }

    private Optional<NoticeOfChangeLetterDetails> getNoticeOfChangeLetterDetails(FinremCaseDetails caseDetails,
                                                                                 FinremCaseDetails caseDetailsBefore) {

        RepresentationUpdate representationUpdate = getLatestRepresentationUpdate(caseDetails);
        if (representationUpdate != null) {
            log.info("Got the representationUpdate");
            FinremCaseDetails caseDetailsToUse = noticeType == NoticeType.ADD ? caseDetailsBefore : caseDetails;
            FinremCaseDetails otherCaseDetails = noticeType == NoticeType.ADD ? caseDetails : caseDetailsBefore;
            if (changedRepresentativeIsPresent(representationUpdate)
                && shouldALetterBeSent(representationUpdate, caseDetailsToUse, otherCaseDetails)) {
                log.info("The recipient is a {} with an address", recipient);
                return
                    Optional.ofNullable(
                        noticeOfChangeLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, representationUpdate, recipient));
            }
        }
        return Optional.empty();
    }

    protected RepresentationUpdate getLatestRepresentationUpdate(FinremCaseDetails caseDetails) {
        log.info("Get the latest Representation Update");
        return Collections.max(caseDetails.getCaseData().getRepresentationUpdateHistory(), Comparator.comparing(
                representationUpdate -> representationUpdate.getValue().getDate()))
            .getValue();
    }

    protected boolean isApplicant(RepresentationUpdate representationUpdate) {
        return representationUpdate.getParty().equalsIgnoreCase(COR_APPLICANT);
    }

    protected boolean isCaseFieldPopulated(String caseDataField) {
        return StringUtils.isNotEmpty(nullToEmpty(caseDataField));
    }

    protected boolean isAddressFieldPopulated(Address address) {
        return ObjectUtils.isNotEmpty(address)
            && StringUtils.isNotBlank(address.getAddressLine1())
            && StringUtils.isNotBlank(address.getPostCode());
    }

    private boolean changedRepresentativeIsPresent(RepresentationUpdate representationUpdate) {
        ChangedRepresentative changedRepresentative = getChangedRepresentative(representationUpdate);
        return Optional.ofNullable(changedRepresentative).isPresent()
            && StringUtils.isNotBlank(changedRepresentative.getName());
    }

    private ChangedRepresentative getChangedRepresentative(RepresentationUpdate representationUpdate) {
        return noticeType == NoticeType.ADD ? representationUpdate.getAdded() : representationUpdate.getRemoved();
    }

    protected abstract boolean shouldALetterBeSent(RepresentationUpdate representationUpdate,
                                                   FinremCaseDetails caseDetailsToUse,
                                                   FinremCaseDetails otherCaseDetails);
}
