package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.Field.LINE_1;
import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.Field.POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Slf4j
public abstract class AbstractLetterHandler implements LetterHandler {

    protected final NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator;
    protected final NocDocumentService nocDocumentService;
    protected final BulkPrintService bulkPrintService;

    private final NoticeType noticeType;
    private final DocumentHelper.PaperNotificationRecipient recipient;
    public static final String COR_APPLICANT = "Applicant";
    public static final String COR_RESPONDENT = "Respondent";

    public AbstractLetterHandler(
        NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator, NocDocumentService nocDocumentService,
        BulkPrintService bulkPrintService,
        NoticeType noticeType, DocumentHelper.PaperNotificationRecipient recipient) {

        this.noticeOfChangeLetterDetailsGenerator = noticeOfChangeLetterDetailsGenerator;
        this.bulkPrintService = bulkPrintService;
        this.noticeType = noticeType;
        this.recipient = recipient;
        this.nocDocumentService = nocDocumentService;
    }

    public void handle(CaseDetails caseDetails, CaseDetails caseDetailsBefore, String authToken) {
        log.info("In the LetterHandler for Recipient {} and Notice Type {} ", recipient, noticeType);
        Optional<NoticeOfChangeLetterDetails> noticeOfChangeLetterDetails = getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore);
        noticeOfChangeLetterDetails.ifPresent(letter -> {
            log.info("Got the letter details now call the document service to generate the Case Document ");
            CaseDocument caseDocument = nocDocumentService.generateNoticeOfChangeLetter(authToken, letter);
            log.info("Generated the case document now send to bulk print");
            UUID uuid = bulkPrintService.sendDocumentForPrint(caseDocument, caseDetails);
            log.info("Document sent to bulkprint with UUID {}", uuid);
        });
    }

    private Optional<NoticeOfChangeLetterDetails> getNoticeOfChangeLetterDetails(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {

        RepresentationUpdate representationUpdate = getLatestRepresentationUpdate(caseDetails);
        if (representationUpdate != null) {
            log.info("Got the representationUpdate");
            ChangedRepresentative changedRepresentative = getChangedRepresentative(representationUpdate);
            if (recipient == DocumentHelper.PaperNotificationRecipient.SOLICITOR || areOrganisationDetailsPopulated(changedRepresentative)) {
                CaseDetails caseDetailsToUse = noticeType == NoticeType.ADD ? caseDetailsBefore : caseDetails;
                if (shouldALetterBeSent(representationUpdate, caseDetailsToUse)) {
                    log.info("The recipient is a {} with an address", recipient);
                    return
                        Optional.ofNullable(
                            noticeOfChangeLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, representationUpdate, recipient,
                                noticeType));
                }
            }
        }
        return Optional.empty();
    }

    protected boolean areOrganisationDetailsPopulated(ChangedRepresentative changedRepresentative) {
        log.info("Check if the organisation details are populated for Changed Representative");
        return changedRepresentative != null && changedRepresentative.getOrganisation() != null
            && changedRepresentative.getOrganisation().getOrganisationID() != null;
    }

    protected ChangedRepresentative getChangedRepresentative(RepresentationUpdate representationUpdate) {
        return noticeType == NoticeType.ADD ? representationUpdate.getAdded() : representationUpdate.getRemoved();
    }

    protected RepresentationUpdate getLatestRepresentationUpdate(CaseDetails caseDetails) {
        log.info("Get the latest Representation Update");
        List<Element<RepresentationUpdate>> representationUpdates = new ObjectMapper().registerModule(new JavaTimeModule())
            .convertValue(caseDetails.getData().get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {
            });
        return Collections.max(representationUpdates, Comparator.comparing(representationUpdate -> representationUpdate.getValue().getDate()))
            .getValue();
    }

    protected boolean isApplicant(RepresentationUpdate representationUpdate) {
        return representationUpdate.getParty().equals(COR_APPLICANT);
    }

    protected boolean isCaseFieldPopulated(CaseDetails caseDetails, String caseDataField) {
        return StringUtils.isNotEmpty(nullToEmpty(caseDetails.getData().get(caseDataField)));
    }

    protected boolean isAddressFieldPopulated(CaseDetails caseDetails, String addressField) {
        Map addressMap = (Map) caseDetails.getData().get(addressField);
        return ObjectUtils.isNotEmpty(addressMap)
            && StringUtils.isNotBlank((String) addressMap.get(LINE_1))
            && StringUtils.isNotBlank((String) addressMap.get(POSTCODE));
    }

    protected abstract boolean shouldALetterBeSent(RepresentationUpdate representationUpdate, CaseDetails caseDetailsToUse);
}
