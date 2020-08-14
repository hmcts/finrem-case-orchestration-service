package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SEOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_OTHER_COURTLIST;

public class ConsentedCourtHelperTest {

    private CaseDetails details;

    @Test
    public void newportCourts() {
        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newportList_1", "Newport Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newportList_2", "Cardiff Civil and Family Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newportList_3", "Merthyr Tydfil Combined Court Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newportList_4", "Pontypridd County and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newportList_5", "Blackwood Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, "invalid", NEWPORT_COURTLIST,
            "FR_newportList_5", "");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "invalid", "");
    }

    @Test
    public void walesOtherCourts() {
        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, OTHER, WALES_OTHER_COURTLIST,
            "FR_WList_1", "Prestatyn Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, OTHER, WALES_OTHER_COURTLIST,
            "FR_WList_2", "Welshpool Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, OTHER, WALES_OTHER_COURTLIST,
            "FR_WList_3", "Wrexham County Court and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, OTHER, WALES_OTHER_COURTLIST,
            "FR_WList_4", "Mold County");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, "invalid", WALES_OTHER_COURTLIST,
            "FR_WList_4", "");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, OTHER, WALES_OTHER_COURTLIST,
            "invalid", "");
    }

    @Test
    public void swanseaCourts() {
        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swanseaList_1", "Swansea Civil and Family Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swanseaList_2", "Aberystwyth Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swanseaList_3", "Haverfordwest County and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swanseaList_4", "Carmarthen County and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swanseaList_5", "Llanelli Law Courts");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swanseaList_6", "Port Talbot Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, "invalid", SWANSEA_COURTLIST,
            "FR_swanseaList_6", "");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "invalid", "");

    }

    @Test
    public void kentCourts() {
        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_1", "Canterbury Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_2", "Maidstone Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_3", "Dartford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_4", "Medway County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_5", "Guildford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_6", "Staines County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_7", "Brighton County and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_8", "Worthing County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_9", "Hastings County Court and Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surreyList_10", "Horsham County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, "invalid", KENTFRC_COURTLIST,
            "FR_kent_surreyList_10", "");


        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "invalid", "");
    }

    @Test
    public void seOtherCourts() {
        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_1", "Basildon Magistrates Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_2", "Bedford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_3", "Brighton County and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_4", "Bury St Edmunds County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_5", "Cambridge County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_6", "Chelmsford County Court and Family Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_7", "Colchester Magistrates Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_8", "Hertford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_9", "High Wycombe County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_10", "Ipswich County Court and Family Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_11", "Lewes Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_12", "Luton Justice Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_13", "Milton Keynes County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_14", "Norwich Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_15", "Oxford Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_16", "Peterborough Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_17", "Reading County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_18", "Slough County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_19", "Southend County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_20", "Watford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "FR_SEList_21", "Thanet County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, "invalid", SEOTHER_COURTLIST,
            "FR_SEList_21", "");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, OTHER, SEOTHER_COURTLIST,
            "invalid", "");
    }

    @Test
    public void swOtherCourts() {
        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_1", "Aldershot Justice Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_2", "Yeovil County, Family and Magistrates Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_3", "Winchester Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_4", "Weymouth Combined Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_5", "Weston-Super-Mare County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_6", "Truro County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_7", "Torquay and Newton Abbot County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_8", "Southampton County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_9", "Taunton Crown, County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_10", "Swindon Combined Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_11", "Salisbury Law Courts");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_12", "Portsmouth Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_13", "Plymouth Combined Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_14", "Isle of Wight Combined Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_15", "Gloucester and Cheltenham County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_16", "Exeter Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_17", "Bristol Civil and Family Justice Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_18", "Bodmin County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_19", "Basingstoke County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_20", "Bournemouth and Poole County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_21", "Bath Law Courts");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "FR_SWList_22", "Barnstaple Magistrates, County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, "invalid", SWOTHER_COURTLIST,
            "FR_SWList_22", "");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, OTHER, SWOTHER_COURTLIST,
            "invalid", "");
    }

    @Test
    public void cleavelandCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_clevelandList_1", "Newcastle upon Tyne Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_clevelandList_2", "Durham Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_clevelandList_3", "Sunderland County and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_clevelandList_4", "Middlesbrough County Court at Teesside Combined Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_clevelandList_5", "Gateshead County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_clevelandList_6", "South Shields County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_clevelandList_7", "North Shields County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_clevelandList_8", "Darlington County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, "invalid", CLEAVELAND_COURTLIST,
            "FR_clevelandList_8", "");


        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "invalid", "");
    }

    @Test
    public void nwYorkshireCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshireList_1", "Harrogate Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshireList_2", "Bradford Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshireList_3", "Huddersfield County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshireList_4", "Wakefield Civil and Family Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshireList_5", "York County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshireList_6", "Scarborough Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshireList_7", "Skipton County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshireList_8", "Leeds Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, "invalid", "",
            "FR_nw_yorkshireList_8", "");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, "",
            "invalid", "");
    }

    @Test
    public void hsYorkshireCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humberList_1", "Sheffield Family Hearing Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humberList_2", "Kingston-upon-Hull Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humberList_3", "Doncaster Justice Centre North");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humberList_4", "Great Grimsby Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humberList_5", "Barnsley Law Courts");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, "invalid", HSYORKSHIRE_COURTLIST,
            "FR_humberList_5", "");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "invalid", "");
    }

    @Test
    public void liverpoolCourtList() {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpoolList_1", "Liverpool Civil and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpoolList_2", "Chester Civil and Family Justice Centre");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpoolList_3", "Crewe County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpoolList_4", "St. Helens County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpoolList_5", "Birkenhead County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, "invalid", LIVERPOOL_COURTLIST,
            "FR_liverpoolList_1", "");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "invalid", "");
    }

    @Test
    public void manchesterCourtList() {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchesterList_1", "Manchester County and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchesterList_2", "Stockport County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchesterList_3", "Wigan County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "invalid", "");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, "invalid", MANCHESTER_COURTLIST,
            "FR_manchesterList_3", "");
    }

    @Test
    public void otherNwCourtList() {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, OTHER, NWOTHER_COURTLIST,
            "FR_NWList_1", "West Cumbria Courthouse");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, OTHER, NWOTHER_COURTLIST,
            "FR_NWList_2", "Preston Combined Court Centre");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, OTHER, NWOTHER_COURTLIST,
            "FR_NWList_3", "Lancaster Courthouse");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, OTHER, NWOTHER_COURTLIST,
            "FR_NWList_4", "Carlisle Combined Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, OTHER, NWOTHER_COURTLIST,
            "FR_NWList_5", "Burnley Combined Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, OTHER, NWOTHER_COURTLIST,
            "FR_NWList_6", "Blackpool Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, OTHER, NWOTHER_COURTLIST,
            "FR_NWList_7", "Blackburn Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, OTHER, NWOTHER_COURTLIST,
            "FR_NWList_8", "Barrow-in-Furness County and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, OTHER, NWOTHER_COURTLIST,
            "invalid", "");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, "invalid", NWOTHER_COURTLIST,
            "FR_NWList_8", "");
    }

    @Test
    public void londonCourtListTest() {
        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_11", "Bromley County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_10", "Croydon County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_9", "Edmonton County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_8", "Kingston-upon-thames County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_7", "Romford County and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_6", "Barnet Civil and Family Courts Centre");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_5", "Brentford County and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_1", "Central Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_4", "East London Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_3", "Uxbridge County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "FR_londonList_2", "Willesden County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, "invalid", LONDON_COURTLIST,
            "FR_londonList_1", "");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON, LONDON_COURTLIST,
            "invalid", "");
    }

    @Test
    public void birminghamCourtListTest() {
        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_1", "Birmingham Civil and Family Justice Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_2", "Coventry Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_3", "Telford County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_4", "Wolverhampton Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_5", "Dudley County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_6", "Walsall County and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_7", "Stoke on Trent Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_8", "Worcester Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_9", "Stafford Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birminghamList_10", "Hereford County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "invalid", "");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, "invalid", BIRMINGHAM_COURTLIST,
            "FR_birminghamList_10", "");

    }

    @Test
    public void nottinghamCourtListTest() {

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_nottinghamList_1", "Nottingham County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_nottinghamList_2", "Derby Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_nottinghamList_3", "Leicester County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_nottinghamList_4", "Lincoln County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_nottinghamList_5", "Northampton Crown, County and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_nottinghamList_6", "Chesterfield County Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_nottinghamList_7", "Mansfield Magistrates and County Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_nottinghamList_8", "Boston County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "INVALID", "");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, "invalid", NOTTINGHAM_COURTLIST,
            "FR_nottinghamList_8", "");
    }


    private CaseDetails getCaseDetailsWithAllocatedValues(String region, String subRegionListName, String subRegion,
                                                          String courtListName, String court) {
        CaseDetails details = defaultCaseDetails();
        details.getData().put(REGION, region);
        details.getData().put(subRegionListName, subRegion);
        details.getData().put(courtListName, court);
        return details;
    }

    private void verifyCorrectCourtReturned(String region, String subRegionListName, String subRegion,
                                                          String courtListName, String court, String expectedValue) {
        details = getCaseDetailsWithAllocatedValues(region, subRegionListName, subRegion, courtListName,
            court);
        assertThat(ConsentedCourtHelper.getSelectedCourt(details), is(expectedValue));
    }
}
