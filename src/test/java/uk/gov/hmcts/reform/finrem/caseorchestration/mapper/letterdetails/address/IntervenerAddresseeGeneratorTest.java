package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IntervenerAddresseeGeneratorTest {

    private ObjectMapper objectMapper;
    IntervenerOneLetterAddresseeGenerator intervenerOneAddresseeGenerator;
    IntervenerTwoLetterAddresseeGenerator intervenerTwoAddresseeGenerator;
    IntervenerThreeLetterAddresseeGenerator intervenerThreeAddresseeGenerator;
    IntervenerFourLetterAddresseeGenerator intervenerFourAddresseeGenerator;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        intervenerOneAddresseeGenerator = new IntervenerOneLetterAddresseeGenerator(objectMapper);
        intervenerTwoAddresseeGenerator = new IntervenerTwoLetterAddresseeGenerator(objectMapper);
        intervenerThreeAddresseeGenerator = new IntervenerThreeLetterAddresseeGenerator(objectMapper);
        intervenerFourAddresseeGenerator = new IntervenerFourLetterAddresseeGenerator(objectMapper);
    }

    @Test
    public void getIntervenerFieldName() {
        assertEquals("intervener1", intervenerOneAddresseeGenerator.getIntervenerFieldName());
        assertEquals("intervener2", intervenerTwoAddresseeGenerator.getIntervenerFieldName());
        assertEquals("intervener3", intervenerThreeAddresseeGenerator.getIntervenerFieldName());
        assertEquals("intervener4", intervenerFourAddresseeGenerator.getIntervenerFieldName());
    }

    @Test
    public void shouldGenerateAddresseeFromCaseDetailsIntervener1() {
        AddresseeDetails addresseeDetails = intervenerOneAddresseeGenerator.generate(
            buildCaseDetails("intervener1", IntervenerOneWrapper.builder().intervenerName("intervener1 name").intervenerAddress(
                Address.builder().addressLine1("intervener1 address line 1")
                    .addressLine2("intervener1 address line 2").postCode("intervener1 postcode").build()).build()));
        assertNotNull(addresseeDetails);
        assertEquals("intervener1 name", addresseeDetails.getAddresseeName());
        assertEquals("intervener1 address line 1", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertEquals("intervener1 address line 2", addresseeDetails.getAddressToSendTo().get("AddressLine2"));
        assertEquals("intervener1 postcode", addresseeDetails.getAddressToSendTo().get("PostCode"));
    }

    @Test
    public void shouldGenerateAddresseeFromCaseDetailsIntervener2() {
        AddresseeDetails addresseeDetails = intervenerTwoAddresseeGenerator.generate(
            buildCaseDetails("intervener2", IntervenerTwoWrapper.builder().intervenerName("intervener2 name").intervenerAddress(
                Address.builder().addressLine1("intervener2 address line 1")
                    .addressLine2("intervener2 address line 2").postCode("intervener2 postcode").build()).build()));
        assertNotNull(addresseeDetails);
        assertEquals("intervener2 name", addresseeDetails.getAddresseeName());
        assertEquals("intervener2 address line 1", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertEquals("intervener2 address line 2", addresseeDetails.getAddressToSendTo().get("AddressLine2"));
        assertEquals("intervener2 postcode", addresseeDetails.getAddressToSendTo().get("PostCode"));
    }


    @Test
    public void shouldGenerateAddresseeFromCaseDetailsIntervener3() {
        AddresseeDetails addresseeDetails = intervenerThreeAddresseeGenerator.generate(
            buildCaseDetails("intervener3", IntervenerThreeWrapper.builder().intervenerName("intervener3 name").intervenerAddress(
                Address.builder().addressLine1("intervener3 address line 1")
                    .addressLine2("intervener3 address line 2").postCode("intervener3 postcode").build()).build()));
        assertNotNull(addresseeDetails);
        assertEquals("intervener3 name", addresseeDetails.getAddresseeName());
        assertEquals("intervener3 address line 1", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertEquals("intervener3 address line 2", addresseeDetails.getAddressToSendTo().get("AddressLine2"));
        assertEquals("intervener3 postcode", addresseeDetails.getAddressToSendTo().get("PostCode"));
    }

    @Test
    public void shouldGenerateAddresseeFromCaseDetailsIntervener4() {
        AddresseeDetails addresseeDetails = intervenerFourAddresseeGenerator.generate(
            buildCaseDetails("intervener4", IntervenerThreeWrapper.builder().intervenerName("intervener4 name").intervenerAddress(
                Address.builder().addressLine1("intervener4 address line 1")
                    .addressLine2("intervener4 address line 2").postCode("intervener4 postcode").build()).build()));
        assertNotNull(addresseeDetails);
        assertEquals("intervener4 name", addresseeDetails.getAddresseeName());
        assertEquals("intervener4 address line 1", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertEquals("intervener4 address line 2", addresseeDetails.getAddressToSendTo().get("AddressLine2"));
        assertEquals("intervener4 postcode", addresseeDetails.getAddressToSendTo().get("PostCode"));
    }


    @Test
    public void shouldGenerateAddresseeFromFinremCaseDetailsIntervener1() {
        FinremCaseDetails finremCaseDetails = buildFinremCaseDetails();
        finremCaseDetails.getData().setIntervenerOneWrapper(IntervenerOneWrapper.builder().intervenerName("intervener1 name").intervenerAddress(
            Address.builder().addressLine1("intervener1 address line 1")
                .addressLine2("intervener1 address line 2").postCode("intervener1 postcode").build()).build());
        AddresseeDetails addresseeDetails = intervenerOneAddresseeGenerator.generate(finremCaseDetails);

        assertEquals("intervener1 name", addresseeDetails.getAddresseeName());
        assertEquals("intervener1 address line 1", addresseeDetails.getFinremAddressToSendTo().getAddressLine1());
        assertEquals("intervener1 address line 2", addresseeDetails.getFinremAddressToSendTo().getAddressLine2());
        assertEquals("intervener1 postcode", addresseeDetails.getFinremAddressToSendTo().getPostCode());
    }

    @Test
    public void shouldGenerateAddresseeFromFinremCaseDetailsIntervener2() {
        FinremCaseDetails finremCaseDetails = buildFinremCaseDetails();
        finremCaseDetails.getData().setIntervenerTwoWrapper(IntervenerTwoWrapper.builder().intervenerName("intervener2 name").intervenerAddress(
            Address.builder().addressLine1("intervener2 address line 1")
                .addressLine2("intervener2 address line 2").postCode("intervener2 postcode").build()).build());
        AddresseeDetails addresseeDetails = intervenerTwoAddresseeGenerator.generate(finremCaseDetails);
        assertNotNull(addresseeDetails);
        assertEquals("intervener2 name", addresseeDetails.getAddresseeName());
        assertEquals("intervener2 address line 1", addresseeDetails.getFinremAddressToSendTo().getAddressLine1());
        assertEquals("intervener2 address line 2", addresseeDetails.getFinremAddressToSendTo().getAddressLine2());
        assertEquals("intervener2 postcode", addresseeDetails.getFinremAddressToSendTo().getPostCode());
        ;
    }

    @Test
    public void shouldGenerateAddresseeFromFinremCaseDetailsIntervener3() {
        FinremCaseDetails finremCaseDetails = buildFinremCaseDetails();
        finremCaseDetails.getData().setIntervenerThreeWrapper(IntervenerThreeWrapper.builder().intervenerName("intervener3 name").intervenerAddress(
            Address.builder().addressLine1("intervener3 address line 1")
                .addressLine2("intervener3 address line 2").postCode("intervener3 postcode").build()).build());
        AddresseeDetails addresseeDetails = intervenerThreeAddresseeGenerator.generate(finremCaseDetails);
        assertNotNull(addresseeDetails);
        assertEquals("intervener3 name", addresseeDetails.getAddresseeName());
        assertEquals("intervener3 address line 1", addresseeDetails.getFinremAddressToSendTo().getAddressLine1());
        assertEquals("intervener3 address line 2", addresseeDetails.getFinremAddressToSendTo().getAddressLine2());
        assertEquals("intervener3 postcode", addresseeDetails.getFinremAddressToSendTo().getPostCode());
        ;
    }

    @Test
    public void shouldGenerateAddresseeFromFinremCaseDetailsIntervener4() {
        FinremCaseDetails finremCaseDetails = buildFinremCaseDetails();
        finremCaseDetails.getData().setIntervenerFourWrapper(IntervenerFourWrapper.builder().intervenerName("intervener4 name").intervenerAddress(
            Address.builder().addressLine1("intervener4 address line 1")
                .addressLine2("intervener4 address line 2").postCode("intervener4 postcode").build()).build());
        AddresseeDetails addresseeDetails = intervenerFourAddresseeGenerator.generate(finremCaseDetails);
        assertNotNull(addresseeDetails);
        assertEquals("intervener4 name", addresseeDetails.getAddresseeName());
        assertEquals("intervener4 address line 1", addresseeDetails.getFinremAddressToSendTo().getAddressLine1());
        assertEquals("intervener4 address line 2", addresseeDetails.getFinremAddressToSendTo().getAddressLine2());
        assertEquals("intervener4 postcode", addresseeDetails.getFinremAddressToSendTo().getPostCode());
        ;
    }

    private CaseDetails buildCaseDetails(String intervenerField, IntervenerWrapper intervenerWrapper) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(intervenerField, objectMapper.convertValue(intervenerWrapper, Map.class));
        return CaseDetails.builder().id(1234L).data(caseData).build();
    }


    private FinremCaseDetails buildFinremCaseDetails() {
        return FinremCaseDetails.builder().id(1234L).data(FinremCaseData.builder().build()).build();
    }
}
