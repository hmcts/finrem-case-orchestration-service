package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalletter.GeneralLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetter;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.DefaultCourtListWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.ADDRESS_MAP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.getAddressToCaseDataMapping;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterService {

    private final GenericDocumentService genericDocumentService;
    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final GeneralLetterDetailsMapper generalLetterDetailsMapper;

    final BiPredicate<Field, Address> isAddressFieldEmpty = (field, address) -> {
        try {
            field.setAccessible(true);
            return !nullToEmpty(field.get(address)).isEmpty();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    };

    public void previewGeneralLetter(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating General letter preview for Case ID: {}", caseDetails.getId());
        Document generalLetterDocument = generateGeneralLetterDocument(caseDetails, authorisationToken);
        caseDetails.getCaseData().getGeneralLetterWrapper().setGeneralLetterPreview(generalLetterDocument);
    }

    public void createGeneralLetter(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating General letter for Case ID: {}", caseDetails.getId());
        Document document = generateGeneralLetterDocument(caseDetails, authorisationToken);
        addGeneralLetterToCaseData(caseDetails, document);
        printLatestGeneralLetter(caseDetails);
    }

    private Document generateGeneralLetterDocument(FinremCaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> placeholdersMap = generalLetterDetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails, new DefaultCourtListWrapper());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, placeholdersMap,
            documentConfiguration.getGeneralLetterTemplate(), documentConfiguration.getGeneralLetterFileName());
    }

    private void addGeneralLetterToCaseData(FinremCaseDetails caseDetails, Document document) {
        GeneralLetterCollection generalLetter = GeneralLetterCollection.builder()
            .value(GeneralLetter.builder()
                .generatedLetter(document)
                .build()).build();

        FinremCaseData caseData = caseDetails.getCaseData();

        List<GeneralLetterCollection> generalLetters = Optional.ofNullable(caseData.getGeneralLetterWrapper()
                .getGeneralLetterCollection()).orElse(new ArrayList<>());

        generalLetters.add(generalLetter);
        caseData.getGeneralLetterWrapper().setGeneralLetterCollection(generalLetters);
    }

    public List<String> getCaseDataErrorsForCreatingPreviewOrFinalLetter(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getCaseData();

        GeneralLetterAddressToType generalLetterAddressTo = data.getGeneralLetterWrapper().getGeneralLetterAddressTo();
        Address recipientAddress = (Address) getAddressToCaseDataMapping(data).get(ADDRESS_MAP).get(generalLetterAddressTo);
        if (isAddressEmpty(recipientAddress)) {
            return List.of(String.format("Address is missing for recipient type %s", generalLetterAddressTo.getValue()));
        } else {
            return emptyList();
        }
    }

    private UUID printLatestGeneralLetter(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<GeneralLetterCollection> generalLetterCollection = caseData.getGeneralLetterWrapper().getGeneralLetterCollection();

        if (!CollectionUtils.isEmpty(generalLetterCollection)) {
            Document generalLetter = Iterables.getLast(generalLetterCollection).getValue().getGeneratedLetter();
            return bulkPrintService.sendDocumentForPrint(generalLetter, caseDetails);
        }

        return null;
    }

    private boolean isAddressEmpty(Address address) {
        if (address == null) {
            return true;
        }

        return Stream.of(Address.class.getDeclaredFields())
            .filter(field -> isAddressFieldEmpty.test(field, address))
            .collect(toList())
            .size() == 0;
    }
}
