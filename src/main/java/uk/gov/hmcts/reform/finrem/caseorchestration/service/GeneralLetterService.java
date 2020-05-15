package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    private BiFunction<CaseDetails, String, CaseDocument> generateDocument = this::applyGenerateDocument;
    private Function<CaseDocument, GeneralLetterData> createGeneralLetterData = this::applyGeneralLetterData;
    private UnaryOperator<CaseDetails> addExtraFields = this::applyAddExtraFields;

    public Map<String, Object> createGeneralLetter(String authorisationToken, CaseDetails caseDetails) {

        log.info("Generating General letter for Case ID: {}", caseDetails.getId());
        return generateDocument
            .andThen(createGeneralLetterData)
            .andThen(data -> populateGeneralLetterData(data, caseDetails))
            .apply(documentHelper.deepCopy(caseDetails, CaseDetails.class), authorisationToken);
    }

    private CaseDocument applyGenerateDocument(CaseDetails caseDetails, String authorisationToken) {
        return genericDocumentService.generateDocument(authorisationToken, addExtraFields.apply(caseDetails),
            documentConfiguration.getGeneralLetterTemplate(),
            documentConfiguration.getGeneralLetterFileName());
    }

    private GeneralLetterData applyGeneralLetterData(CaseDocument caseDocument) {
        GeneralLetter generalLetter = new GeneralLetter();
        generalLetter.setGeneratedLetter(caseDocument);

        GeneralLetterData generalLetterData = new GeneralLetterData();
        generalLetterData.setGeneralLetter(generalLetter);

        return generalLetterData;
    }

    private CaseDetails applyAddExtraFields(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        data.put("generalLetterCreatedDate", new Date());
        data.put("ccdCaseNumber", caseDetails.getId());

        return caseDetails;
    }

    private Map<String, Object> populateGeneralLetterData(GeneralLetterData generalLetterData, CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralLetterData> generalLetterDataList = Optional.ofNullable(caseData.get(GENERAL_LETTER))
            .map(this::convertToUploadOrderList)
            .orElse(new ArrayList<>());

        generalLetterDataList.add(generalLetterData);

        return ImmutableMap.of(GENERAL_LETTER, generalLetterDataList);
    }

    private List<GeneralLetterData> convertToUploadOrderList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<GeneralLetterData>>() {
        });
    }
}
