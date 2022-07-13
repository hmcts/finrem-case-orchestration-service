package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TransferLocalCourtEmail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TransferLocalCourtEmailData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRANSFER_COURTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRANSFER_COURTS_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRANSFER_COURTS_INSTRUCTIONS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRANSFER_COURTS_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferCourtService {

    private final ObjectMapper objectMapper;

    public void storeTransferToCourtEmail(CaseDetails caseDetails) {
        log.info("Storing general email for Case ID: {}", caseDetails.getId());

        TransferLocalCourtEmail transferCourtEmail = createTransferCourtEmail(caseDetails);
        addTransferCourtEmailToCollection(caseDetails, transferCourtEmail);
    }

    private TransferLocalCourtEmail createTransferCourtEmail(CaseDetails caseDetails) {
        TransferLocalCourtEmailData emailData = new TransferLocalCourtEmailData();
        emailData.setCourtName(Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_NAME)));
        emailData.setCourtEmail(Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_EMAIL)));
        emailData.setCourtInstructions(Objects.toString(caseDetails.getData().get(TRANSFER_COURTS_INSTRUCTIONS)));

        TransferLocalCourtEmail email = new TransferLocalCourtEmail();
        email.setId(UUID.randomUUID().toString());
        email.setTransferLocalCourtEmailData(emailData);
        return email;
    }

    private void addTransferCourtEmailToCollection(CaseDetails caseDetails, TransferLocalCourtEmail transferCourtEmail) {
        Map<String, Object> caseData = caseDetails.getData();

        List<TransferLocalCourtEmail> transferLocalCourtEmailList = Optional.ofNullable(caseData.get(TRANSFER_COURTS_COLLECTION))
            .map(this::convertToTransferToCourtEmailList)
            .orElse(new ArrayList<>());

        transferLocalCourtEmailList.add(transferCourtEmail);
        caseDetails.getData().put(TRANSFER_COURTS_COLLECTION, transferLocalCourtEmailList);
    }

    private List<TransferLocalCourtEmail> convertToTransferToCourtEmailList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {});
    }
}
