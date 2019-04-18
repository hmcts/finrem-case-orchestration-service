package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;

@Service
public class HearingDocumentService extends AbstractDocumentService {

    private UnaryOperator<CaseDetails> addFastTrackFields = caseDetails -> {
        Map<String, Object> data = caseDetails.getData();
        data.put("formCCreatedDate", new Date());
        data.put("eventDatePlus21Days", asDate(LocalDate.now().plusDays(21)));

        return caseDetails;
    };

    private UnaryOperator<CaseDetails> addNonFastTrackFields = caseDetails -> {
        Map<String, Object> data = caseDetails.getData();

        String hearingDate = ObjectUtils.toString(data.get(HEARING_DATE));
        LocalDate hearingLocalDate = LocalDate.parse(hearingDate);

        data.put("formCCreatedDate", new Date());
        data.put("hearingDateLess35Days", asDate(hearingLocalDate.minusDays(35)));
        data.put("hearingDateLess14Days", asDate(hearingLocalDate.minusDays(14)));

        return caseDetails;
    };

    @Autowired
    public HearingDocumentService(DocumentGeneratorClient documentGeneratorClient,
                                  DocumentConfiguration config,
                                  ObjectMapper objectMapper) {
        super(documentGeneratorClient, config, objectMapper);
    }

    public Map<String, Object> generateHearingDocuments(String authorisationToken, CaseDetails caseDetails) {
        return Optional.of(Pair.of(copyOf(caseDetails), authorisationToken))
                .filter(pair -> pair.getLeft().getData().get("fastTrackDecision") != null)
                .map(this::courtCoverSheetDocuments)
                .orElseThrow(() -> new IllegalArgumentException("missing fastTrackDecision"));
    }

    private Map<String, Object> courtCoverSheetDocuments(Pair<CaseDetails, String> pair) {
        return Optional.of(pair)
                .filter(this::isFastTrackApplication)
                .map(this::generateFastTrackFormC)
                .orElseGet(() -> generateFormCAndG(pair));
    }

    private Map<String, Object> generateFormCAndG(Pair<CaseDetails, String> pair) {
        CompletableFuture<CaseDocument> formCNonFastTrack =
                supplyAsync(() -> generateDocument(pair.getRight(), addNonFastTrackFields.apply(pair.getLeft()),
                        config.getFormCNonFastTrackTemplate(), config.getFormCFileName()));

        CompletableFuture<CaseDocument> formG = supplyAsync(() -> generateDocument(pair.getRight(), pair.getLeft(),
                config.getFormGTemplate(), config.getFormGFileName()));

        return formCNonFastTrack
                .thenCombine(formG, this::createDocumentMap).join();
    }

    private Map<String, Object> createDocumentMap(CaseDocument formC, CaseDocument formG) {
        return ImmutableMap.of("formC", formC, "formG", formG);
    }

    private Map<String, Object> generateFastTrackFormC(Pair<CaseDetails, String> pair) {
        return ImmutableMap.of("formC",
                generateDocument(pair.getRight(), addFastTrackFields.apply(pair.getLeft()),
                        config.getFormCFastTrackTemplate(), config.getFormCFileName()));
    }

    private boolean isFastTrackApplication(Pair<CaseDetails, String> pair) {
        Map<String, Object> caseData = pair.getLeft().getData();
        String fastTrackDecision = (String) caseData.get("fastTrackDecision");

        return fastTrackDecision.toLowerCase().equals("yes");
    }

    private static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
