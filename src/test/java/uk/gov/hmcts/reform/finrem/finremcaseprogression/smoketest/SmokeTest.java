package uk.gov.hmcts.reform.finrem.finremcaseprogression.smoketest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.runner.RunWith;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.controllers.CcdCallbackController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmokeTest {

    @Autowired
    private CcdCallbackController ccdCallbackController;

    @Test
    public void contexLoads() {
        assertThat(ccdCallbackController).isNotNull();
    }
}
