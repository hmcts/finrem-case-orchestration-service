package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddresseeTest {

    @Test
    public void checkAllStatusValues() {

        Addressee addressee = Addressee.builder()
                .name("appRespondentFMName")
                .formattedAddress("1 Victoria Street" + "\n" + "Westminster" + "\n" + "" + "\n" + "Greater London"
                        + "\n" + "UK" + "\n" + "London" + "\n" + "SE1")
                .build();

        assertEquals("appRespondentFMName", addressee.getName());
        assertEquals("1 Victoria Street" + "\n" + "Westminster" + "\n" + "" + "\n" + "Greater London"
                + "\n" + "UK" + "\n" + "London" + "\n" + "SE1", addressee.getFormattedAddress());
    }
}