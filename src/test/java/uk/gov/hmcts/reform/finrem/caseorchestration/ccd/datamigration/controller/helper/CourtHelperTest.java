package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller.helper;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionHighCourtFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALDERSHOT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BARNSTAPLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BARROW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BASINGSTOKE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BATH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BLACKBURN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BLACKPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BODMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BOURNEMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BURY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CAERNARFON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CAMBRIDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CARLISLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHELMSFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXETER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GLOUCESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HERTFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IPSWICH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISLE_OF_WIGHT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LEYLAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LUTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MILTON_KEYNES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MOLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTH_WALES_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORWICH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OXFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PETERBOROUGH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PLYMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PORTSMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTATYN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.READING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REEDLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SALISBURY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SLOUGH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHAMPTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWINDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TAUNTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TORQUAY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRURO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WATFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WELSHPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WESTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WEST_CUMBRIA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WEYMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WINCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WREXHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.YEOVIL;

public class CourtHelperTest {

    @Test
    public void northWalesCourts() {
        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NORTHWALES, NORTH_WALES_COURTLIST,
            WREXHAM, "Wrexham County Court and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NORTHWALES, NORTH_WALES_COURTLIST,
            CAERNARFON, "Caernarfon Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NORTHWALES, NORTH_WALES_COURTLIST,
            PRESTATYN, "Prestatyn Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NORTHWALES, NORTH_WALES_COURTLIST,
            WELSHPOOL, "Welshpool Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NORTHWALES, NORTH_WALES_COURTLIST,
            MOLD, "Mold County");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NORTHWALES, NORTH_WALES_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NORTHWALES, NORTH_WALES_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void thamesvalleyCourts() {
        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, THAMESVALLEY, THAMESVALLEY_COURTLIST,
            OXFORD, "Oxford Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, THAMESVALLEY, THAMESVALLEY_COURTLIST,
            READING, "Reading County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, THAMESVALLEY, THAMESVALLEY_COURTLIST,
            MILTON_KEYNES, "Milton Keynes County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, THAMESVALLEY, THAMESVALLEY_COURTLIST,
            SLOUGH, "Slough County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, THAMESVALLEY, THAMESVALLEY_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, THAMESVALLEY, THAMESVALLEY_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void bristolCourts() {
        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, BRISTOLFRC, BRISTOL_COURTLIST,
            BRISTOL, "Bristol Civil and Family Justice Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, BRISTOLFRC, BRISTOL_COURTLIST,
            GLOUCESTER, "Gloucester and Cheltenham County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, BRISTOLFRC, BRISTOL_COURTLIST,
            SWINDON, "Swindon Combined Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, BRISTOLFRC, BRISTOL_COURTLIST,
            SALISBURY, "Salisbury Law Courts");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, BRISTOLFRC, BRISTOL_COURTLIST,
            BATH, "Bath Law Courts");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, BRISTOLFRC, BRISTOL_COURTLIST,
            WESTON, "Weston Super Mare County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, BRISTOLFRC, BRISTOL_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, BRISTOLFRC, BRISTOL_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void devonCourts() {
        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            PLYMOUTH, "Plymouth Combined Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            EXETER, "Exeter Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            TAUNTON, "Taunton Crown, County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            TORQUAY, "Torquay and Newton Abbot County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            BARNSTAPLE, "Barnstaple Magistrates, County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            TRURO, "Truro County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            YEOVIL, "Yeovil County, Family and Magistrates Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            BODMIN, "Bodmin County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DEVON, DEVON_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void dorsetCourts() {
        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            BOURNEMOUTH, "Bournemouth and Poole County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            WEYMOUTH, "Weymouth Combined Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            WINCHESTER, "Winchester Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            PORTSMOUTH, "Portsmouth Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            SOUTHAMPTON, "Southampton Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            ALDERSHOT, "Aldershot Justice Centre");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            BASINGSTOKE, "Basingstoke County and Family Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            ISLE_OF_WIGHT, "Newport (Isle of Wight) Combined Court");

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(SOUTHWEST, SOUTHWEST_FRC_LIST, DORSET, DORSET_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void bedfordshireCourts() {
        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            PETERBOROUGH, "Peterborough Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            CAMBRIDGE, "Cambridge County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            BURY, "Bury St Edmunds County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            NORWICH, "Norwich Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            IPSWICH, "Ipswich County Court and Family Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            CHELMSFORD, "Chelmsford Justice Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            SOUTHEND, "Southend County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            BEDFORD, "Bedford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            LUTON, "Luton Justice Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            HERTFORD, "Hertford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            WATFORD, "Watford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void lancashireCourtList() {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LANCASHIRE, LANCASHIRE_COURTLIST,
            PRESTON, "Preston Designated Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LANCASHIRE, LANCASHIRE_COURTLIST,
            BLACKBURN, "Blackburn Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LANCASHIRE, LANCASHIRE_COURTLIST,
            BLACKPOOL, "Blackpool Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LANCASHIRE, LANCASHIRE_COURTLIST,
            LANCASTER, "Lancaster Courthouse");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LANCASHIRE, LANCASHIRE_COURTLIST,
            LEYLAND, "Leyland Family Hearing Centre");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LANCASHIRE, LANCASHIRE_COURTLIST,
            REEDLEY, "Reedley Family Hearing Centre");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LANCASHIRE, LANCASHIRE_COURTLIST,
            BARROW, "Barrow in Furness County and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LANCASHIRE, LANCASHIRE_COURTLIST,
            CARLISLE, "Carlisle Combined Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LANCASHIRE, LANCASHIRE_COURTLIST,
            WEST_CUMBRIA, "West Cumbria Courthouse");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void newportCourts() {
        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_1", "Newport Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_2", "Cardiff Civil and Family Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_3", "Merthyr Tydfil Combined Court Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_4", "Pontypridd County and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_5", "Blackwood Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, "invalid", NEWPORT_COURTLIST,
            "FR_newport_hc_list_5", EMPTY);

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void swanseaCourts() {
        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_1", "Swansea Civil and Family Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_2", "Aberystwyth Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_3", "Haverfordwest County and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_4", "Carmarthen County and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_5", "Llanelli Law Courts");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_6", "Port Talbot Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, "invalid", SWANSEA_COURTLIST,
            "FR_swansea_hc_list_6", EMPTY);

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void kentCourts() {
        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENTFRC, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_1", "Canterbury Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_1", "Canterbury Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_2", "Maidstone Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_3", "Dartford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_4", "Medway County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_5", "Guildford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_6", "Staines County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_7", "Brighton County and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_8", "Worthing County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_9", "Hastings County Court and Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_10", "Horsham County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, "invalid", KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_10", EMPTY);

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_11", "Thanet Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void cleavelandCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_1", "Newcastle Civil and Family Courts and Tribunals Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_2", "Durham Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_3", "Sunderland County and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_4", "Middlesbrough County Court at Teesside Combined Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_5", "Gateshead County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_6", "South Shields County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_7", "North Shields County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_8", "Darlington County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_8", "Darlington County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, "invalid", CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_8", EMPTY);

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void nwYorkshireCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_1", "Harrogate Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_2", "Bradford Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_3", "Huddersfield County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_4", "Wakefield Civil and Family Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_5", "York County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_6", "Scarborough Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_7", "Skipton County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_8", "Leeds Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            EMPTY, EMPTY);

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, "invalid", EMPTY,
            EMPTY, EMPTY);

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, EMPTY,
            "invalid", EMPTY);
    }

    @Test
    public void hsYorkshireCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_1", "Sheffield Family Hearing Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_2", "Kingston-upon-Hull Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_3", "Doncaster Justice Centre North");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_4", "Great Grimsby Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_5", "Barnsley Law Courts");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, "invalid", HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_5", EMPTY);

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void liverpoolCourtList() {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_1", "Liverpool Civil and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_2", "Chester Civil and Family Justice Centre");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_3", "Crewe County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_4", "St. Helens County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_5", "Birkenhead County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, "invalid", LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_1", EMPTY);

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void manchesterCourtList() {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_1", "Manchester County and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_2", "Stockport County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_3", "Wigan County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, "invalid", MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_3", EMPTY);

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void londonCourtListTest() {
        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_1", "Bromley County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_2", "Croydon County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_3", "Edmonton County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_4", "Kingston-upon-thames County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_5", "Romford County and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_6", "Barnet Civil and Family Courts Centre");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_8", "Brentford County and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_9", "Central Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_11", "East London Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_14", "Uxbridge County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_16", "Willesden County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_17", "The Royal Courts of Justice");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, "invalid", CFC_COURTLIST,
            "FR_s_CFCList_1", EMPTY);

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void birminghamCourtListTest() {
        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_1", "Birmingham Civil and Family Justice Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_2", "Coventry Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_3", "Telford County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_4", "Wolverhampton Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_5", "Dudley County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_6", "Walsall County and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_7", "Stoke on Trent Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_8", "Worcester Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_9", "Stafford Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_10", "Hereford County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, "invalid", BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_10", EMPTY);

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void nottinghamCourtListTest() {

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_1", "Nottingham County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_2", "Derby Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_3", "Leicester County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_4", "Lincoln County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_5", "Northampton Crown, County and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_6", "Chesterfield County Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_7", "Mansfield Magistrates and County Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_8", "Boston County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "INVALID", EMPTY);

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, "invalid", NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_8", EMPTY);

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            EMPTY, EMPTY);
    }

    @Test
    public void highCourtListTest() {
        verifyCorrectCourtReturned(HIGHCOURT, HIGHCOURT_FRC_LIST, HIGHCOURT, HIGHCOURT_COURTLIST,
            "FR_highCourtList_1", "High Court Family Division");

        verifyCorrectCourtReturned(HIGHCOURT, HIGHCOURT_FRC_LIST, "invalid", HIGHCOURT_COURTLIST,
            "FR_highCourtList_1", EMPTY);

        verifyCorrectCourtReturned(HIGHCOURT, HIGHCOURT_FRC_LIST, HIGHCOURT, HIGHCOURT_COURTLIST,
            "invalid", EMPTY);

        verifyCorrectCourtReturned(HIGHCOURT, HIGHCOURT_FRC_LIST, HIGHCOURT, HIGHCOURT_COURTLIST,
            EMPTY, EMPTY);
    }

    private CaseDetails getCaseDetailsWithAllocatedValues(String region, String subRegionListName, String subRegion,
                                                          String courtListName, String court) {
        CaseDetails details = defaultConsentedCaseDetails();
        details.getData().put(REGION, region);
        details.getData().put(subRegionListName, subRegion);
        details.getData().put(courtListName, court);
        return details;
    }

    private void verifyCorrectCourtReturned(String region, String subRegionListName, String subRegion,
                                            String courtListName, String court, String expectedValue) {
        CaseDetails details = getCaseDetailsWithAllocatedValues(region, subRegionListName, subRegion, courtListName,
            court);
        String selectedCourt = CourtHelper.getSelectedCourt(details);
        MatcherAssert.assertThat(selectedCourt, is(expectedValue));
    }

    @Test
    public void interim_walesFRCCourts() {
        verifyCorrectInterimHearingCourtReturned(WALES, INTERIM_WALES_FRC_LIST, NEWPORT);
        verifyCorrectInterimHearingCourtReturned(WALES, INTERIM_WALES_FRC_LIST, SWANSEA);
        verifyCorrectInterimHearingCourtReturned(WALES, INTERIM_WALES_FRC_LIST, NORTHWALES);
    }

    @Test
    public void interim_southWestFRCCourts() {
        verifyCorrectInterimHearingCourtReturned(SOUTHWEST, INTERIM_SOUTHWEST_FRC_LIST, DEVON);
        verifyCorrectInterimHearingCourtReturned(SOUTHWEST, INTERIM_SOUTHWEST_FRC_LIST, DORSET);
        verifyCorrectInterimHearingCourtReturned(SOUTHWEST, INTERIM_SOUTHWEST_FRC_LIST, BRISTOLFRC);
    }

    @Test
    public void interim_southEastFRCCourts() {
        verifyCorrectInterimHearingCourtReturned(SOUTHEAST, INTERIM_SOUTHEAST_FRC_LIST, KENT);
        verifyCorrectInterimHearingCourtReturned(SOUTHEAST, INTERIM_SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
        verifyCorrectInterimHearingCourtReturned(SOUTHEAST, INTERIM_SOUTHEAST_FRC_LIST, THAMESVALLEY);
    }

    @Test
    public void interim_northEastFRCCourts() {
        verifyCorrectInterimHearingCourtReturned(NORTHEAST, INTERIM_NORTHEAST_FRC_LIST, CLEAVELAND);
        verifyCorrectInterimHearingCourtReturned(NORTHEAST, INTERIM_NORTHEAST_FRC_LIST, NWYORKSHIRE);
        verifyCorrectInterimHearingCourtReturned(NORTHEAST, INTERIM_NORTHEAST_FRC_LIST, HSYORKSHIRE);
    }

    @Test
    public void interim_northWestFRCCourts() {
        verifyCorrectInterimHearingCourtReturned(NORTHWEST, INTERIM_NORTHWEST_FRC_LIST, LIVERPOOL);
        verifyCorrectInterimHearingCourtReturned(NORTHWEST, INTERIM_NORTHWEST_FRC_LIST, MANCHESTER);
        verifyCorrectInterimHearingCourtReturned(NORTHWEST, INTERIM_NORTHWEST_FRC_LIST, LANCASHIRE);
    }

    @Test
    public void interim_londonFRCCourts() {
        verifyCorrectInterimHearingCourtReturned(LONDON, INTERIM_LONDON_FRC_LIST, CFC);
    }

    @Test
    public void interim_midlandsFRCCourts() {
        verifyCorrectInterimHearingCourtReturned(MIDLANDS, INTERIM_MIDLANDS_FRC_LIST, NOTTINGHAM);
        verifyCorrectInterimHearingCourtReturned(MIDLANDS, INTERIM_MIDLANDS_FRC_LIST, BIRMINGHAM);
    }

    @Test
    public void interim_highCourtFRCCourts() {
        verifyCorrectInterimHearingCourtReturned(HIGHCOURT, INTERIM_HIGHCOURT_FRC_LIST, HIGHCOURT);
        verifyCorrectInterimHearingCourtReturned("highCourt", INTERIM_HIGHCOURT_FRC_LIST, HIGHCOURT);
        verifyCorrectInterimHearingCourtReturned("invalid", INTERIM_HIGHCOURT_FRC_LIST, EMPTY);
    }

    private void verifyCorrectInterimHearingCourtReturned(final String region, final String subRegionListName, final String subRegion) {
        Map<String, Object> interimCaseData = getCaseDetailsWithAllocatedValuesForInterimHearing(region, subRegionListName, subRegion);
        String selectedInterimHearingFrc = CourtHelper.getSelectedInterimHearingFrc(interimCaseData);
        MatcherAssert.assertThat(selectedInterimHearingFrc, is(subRegion));
    }

    private Map<String, Object> getCaseDetailsWithAllocatedValuesForInterimHearing(String region, String subRegionListName, String subRegion) {
        CaseDetails details = defaultConsentedCaseDetails();
        details.getData().put(INTERIM_HEARING_REGION_LIST, region);
        details.getData().put(subRegionListName, subRegion);
        return details.getData();
    }


    @Test
    public void walesFRCCourts() {
        verifyCorrectHearingCourtReturned(WALES, HEARING_WALES_FRC_LIST, NEWPORT);
        verifyCorrectHearingCourtReturned(WALES, HEARING_WALES_FRC_LIST, SWANSEA);
        verifyCorrectHearingCourtReturned(WALES, HEARING_WALES_FRC_LIST, NORTHWALES);
    }

    @Test
    public void verifyFinremWalesCourtFRCCourts() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.WALES);
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setWalesFrcList(RegionWalesFrc.NORTH_WALES);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(NORTHWALES));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setWalesFrcList(RegionWalesFrc.NEWPORT);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(NEWPORT));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setWalesFrcList(RegionWalesFrc.SWANSEA);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(SWANSEA));
    }

    @Test
    public void southWestFRCCourts() {
        verifyCorrectHearingCourtReturned(SOUTHWEST, HEARING_SOUTHWEST_FRC_LIST, DEVON);
        verifyCorrectHearingCourtReturned(SOUTHWEST, HEARING_SOUTHWEST_FRC_LIST, DORSET);
        verifyCorrectHearingCourtReturned(SOUTHWEST, HEARING_SOUTHWEST_FRC_LIST, BRISTOLFRC);
    }

    @Test
    public void verifyFinremSouthWestFRCCourts() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.SOUTHWEST);
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setSouthWestFrcList(RegionSouthWestFrc.DEVON);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(DEVON));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setSouthWestFrcList(RegionSouthWestFrc.DORSET);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(DORSET));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setSouthWestFrcList(RegionSouthWestFrc.BRISTOL);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(BRISTOLFRC));
    }

    @Test
    public void southEastFRCCourts() {
        verifyCorrectHearingCourtReturned(SOUTHEAST, HEARING_SOUTHEAST_FRC_LIST, KENT);
        verifyCorrectHearingCourtReturned(SOUTHEAST, HEARING_SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
        verifyCorrectHearingCourtReturned(SOUTHEAST, HEARING_SOUTHEAST_FRC_LIST, THAMESVALLEY);
    }

    @Test
    public void verifyFinremSouthEastFRCCourts() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.SOUTHEAST);
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setSouthEastFrcList(RegionSouthEastFrc.KENT_FRC);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(KENT));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setSouthEastFrcList(RegionSouthEastFrc.BEDFORDSHIRE);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(BEDFORDSHIRE));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setSouthEastFrcList(RegionSouthEastFrc.THAMES_VALLEY);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(THAMESVALLEY));
    }

    @Test
    public void northEastFRCCourts() {
        verifyCorrectHearingCourtReturned(NORTHEAST, HEARING_NORTHEAST_FRC_LIST, CLEAVELAND);
        verifyCorrectHearingCourtReturned(NORTHEAST, HEARING_NORTHEAST_FRC_LIST, NWYORKSHIRE);
        verifyCorrectHearingCourtReturned(NORTHEAST, HEARING_NORTHEAST_FRC_LIST, HSYORKSHIRE);
    }

    @Test
    public void verifyFinremNorthEastFRCCourts() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.NORTHEAST);
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setNorthEastFrcList(RegionNorthEastFrc.CLEAVELAND);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(CLEAVELAND));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setNorthEastFrcList(RegionNorthEastFrc.NW_YORKSHIRE);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(NWYORKSHIRE));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setNorthEastFrcList(RegionNorthEastFrc.HS_YORKSHIRE);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(HSYORKSHIRE));
    }

    @Test
    public void northWestFRCCourts() {
        verifyCorrectHearingCourtReturned(NORTHWEST, HEARING_NORTHWEST_FRC_LIST, LIVERPOOL);
        verifyCorrectHearingCourtReturned(NORTHWEST, HEARING_NORTHWEST_FRC_LIST, MANCHESTER);
        verifyCorrectHearingCourtReturned(NORTHWEST, HEARING_NORTHWEST_FRC_LIST, LANCASHIRE);
    }

    @Test
    public void verifyFinremNorthWestFRCCourts() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.NORTHWEST);
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setNorthWestFrcList(RegionNorthWestFrc.LANCASHIRE);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(LANCASHIRE));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setNorthWestFrcList(RegionNorthWestFrc.MANCHESTER);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(MANCHESTER));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setNorthWestFrcList(RegionNorthWestFrc.LIVERPOOL);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(LIVERPOOL));
    }

    @Test
    public void londonFRCCourts() {
        verifyCorrectHearingCourtReturned(LONDON, HEARING_LONDON_FRC_LIST, CFC);
    }

    @Test
    public void verifyFinremLondonFRCCourts() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.LONDON);
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setLondonFrcList(RegionLondonFrc.LONDON);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(CFC));
    }

    @Test
    public void midlandsFRCCourts() {
        verifyCorrectHearingCourtReturned(MIDLANDS, HEARING_MIDLANDS_FRC_LIST, NOTTINGHAM);
        verifyCorrectHearingCourtReturned(MIDLANDS, HEARING_MIDLANDS_FRC_LIST, BIRMINGHAM);
    }

    @Test
    public void verifyMidlandsFRCCourts() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.MIDLANDS);
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(NOTTINGHAM));
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.BIRMINGHAM);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(BIRMINGHAM));
    }

    @Test
    public void highCourtFRCCourts() {
        verifyCorrectHearingCourtReturned(HIGHCOURT, HEARING_HIGHCOURT_FRC_LIST, HIGHCOURT);
        verifyCorrectHearingCourtReturned("highCourt", HEARING_HIGHCOURT_FRC_LIST, HIGHCOURT);
        verifyCorrectHearingCourtReturned("invalid", HEARING_HIGHCOURT_FRC_LIST, EMPTY);
    }

    @Test
    public void verifyFinremHighCourtFRCCourts() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.HIGHCOURT);
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setHighCourtFrcList(RegionHighCourtFrc.HIGHCOURT);
        MatcherAssert.assertThat(CourtHelper.getSelectedFrc(finremCaseDetails), is(HIGHCOURT));
    }


    private void verifyCorrectHearingCourtReturned(final String region, final String subRegionListName, final String subRegion) {
        Map<String, Object> caseData = getCaseDetailsWithAllocatedValuesForHearing(region, subRegionListName, subRegion);
        String selectedHearingFrc = CourtHelper.getSelectedHearingFrc(caseData);
        MatcherAssert.assertThat(selectedHearingFrc, is(subRegion));
    }

    private Map<String, Object> getCaseDetailsWithAllocatedValuesForHearing(String region, String subRegionListName, String subRegion) {
        CaseDetails details = defaultConsentedCaseDetails();
        details.getData().put(HEARING_REGION_LIST, region);
        details.getData().put(subRegionListName, subRegion);
        return details.getData();
    }
}
