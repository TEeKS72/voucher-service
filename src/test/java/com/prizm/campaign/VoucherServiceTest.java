package com.prizm.campaign;

import com.prizm.campaign.dto.RedeemResponse;
import com.prizm.campaign.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class VoucherServiceTest {

    @Autowired
    private VoucherService voucherService;

    @Test
    void redeemActiveVoucherSucceeds() {
        RedeemResponse res = voucherService.redeem("RAYA-0001", "user-1");
        assertEquals("OK", res.getResult());
    }

    @Test
    void redeemAlreadyRedeemedVoucherFails() {
        RedeemResponse res = voucherService.redeem("RAYA-0004", "user-2");
        assertEquals("FAILED", res.getResult());
    }

    @Test
    void redeemUnknownCodeFails() {
        RedeemResponse res = voucherService.redeem("NOPE-9999", "user-3");
        assertEquals("FAILED", res.getResult());
    }

    @Test
    void redeemFailsWhenUserReachesCampaignLimit() {
        RedeemResponse res = voucherService.redeem("RAYA-0002", "user-99");

        assertEquals("FAILED", res.getResult());
        assertEquals("User has reached redemption limit for this campaign", res.getMessage());
    }

    @Test
    void redeemSucceedsWhenUserIsUnderCampaignLimitThenFailsOnNextAttempt() {
        RedeemResponse first = voucherService.redeem("RAYA-0003", "user-55");
        assertEquals("OK", first.getResult());

        RedeemResponse second = voucherService.redeem("RAYA-0006", "user-55");
        assertEquals("OK", second.getResult());

        RedeemResponse third = voucherService.redeem("RAYA-0002", "user-55");
        assertEquals("FAILED", third.getResult());
        assertEquals("User has reached redemption limit for this campaign", third.getMessage());
    }

    @Test
    void redeemSucceedsOnDifferentCampaignAfterReachingLimitOnFirstCampaign() {
        RedeemResponse blocked = voucherService.redeem("RAYA-0002", "user-99");
        assertEquals("FAILED", blocked.getResult());
        assertEquals("User has reached redemption limit for this campaign", blocked.getMessage());
        RedeemResponse allowed = voucherService.redeem("MRDK-0001", "user-99");
        assertEquals("OK", allowed.getResult());
    }
}
