package uk.gov.hmcts.reform.finrem.caseorchestration.service.generator;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ParentLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

@Slf4j
public abstract class BaseContestedLetterDetailsGenerator<T> {

    public static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";

    protected final CaseDataService caseDataService;
    protected final DocumentHelper documentHelper;
    protected final LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper;

    protected BaseContestedLetterDetailsGenerator(CaseDataService caseDataService, DocumentHelper documentHelper,
                                                  LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper) {
        this.caseDataService = caseDataService;
        this.documentHelper = documentHelper;
        this.letterAddresseeGeneratorMapper = letterAddresseeGeneratorMapper;
    }

    public abstract ParentLetterDetails generate(CaseDetails caseDetails,
                                                 DocumentHelper.PaperNotificationRecipient recipient,
                                                 T additionalData);

    protected Addressee getAddressee(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        AddresseeDetails addresseeDetails = letterAddresseeGeneratorMapper.generate(caseDetails, recipient);
        return Addressee.builder().name(addresseeDetails.getAddresseeName())
            .formattedAddress(documentHelper.formatAddressForLetterPrinting(addresseeDetails.getAddressToSendTo())).build();
    }

}
