## What I changed and why
- Added `redemption_limit_per_user` (INT, default 2) to the `campaign` table in `schema.sql`, and set it explicitly to 2 for all seeded campaigns in `data.sql`, per the brief.
- Added the corresponding field + getter/setter to `Campaign.java`.
- Added `countByCampaignIdAndUserId(campaignId, userId)` to `RedemptionRepository`
- In `VoucherService.redeem()`, added a check immediately after the stock check and before any mutation: count the user's existing redemptions for this campaign, and reject with `"User has reached redemption limit for this campaign"` if the count is already at or above the campaign's limit.
- Added tests covering:
  - a user already at the campaign limit
  - reaching the limit across different voucher codes in the same campaign
  - the limit being isolated between different campaigns

## What I noticed but deliberately did not change
- `VoucherService.redeem()` has no `@Transactional` and it updates the voucher, stock, and redemption separately. With concurrent requests, two users could potentially redeem the same stock, and two requests from the same user could potentially bypass the redemption limit. I left this unchanged because making the flow atomic would require a larger change to the existing transaction and locking strategy.
- `AuditClient` contains a hardcoded API key. In a production environment, this should be moved to configuration/secrets management and rotated. I left it unchanged because it is outside the scope of the feature.
- The application uses an in-memory H2 database and does not use Flyway or Liquibase. This is suitable for the exercise because the database is recreated on startup, so I left the existing setup unchanged.

## What I'd do next with another day
- Make the redemption flow concurrency-safe using appropriate transaction boundaries and database-level locking/constraints.
- Move the audit API key to configuration/secrets management.
- Add an integration test through the HTTP API to cover the controller and request/response flow, since the current tests call the service layer directly.
- Review the existing transaction boundaries and error handling around voucher updates, stock updates, redemption creation, and the audit call.

## Reflection

**What did I get wrong first, and how did I notice?**
I initially tested the redemption flow with a voucher that was already redeemed in the seed data. I noticed the failure didn't match the behavior I was trying to test, so I checked `data.sql` and the existing redemption records. I realized I needed to use an active voucher from the same campaign to properly test the redemption-limit logic.

**Which AI suggestion did I reject, and why?**
The AI suggested adding repository assertions to each test to explicitly verify the voucher-to-campaign mapping. I rejected this because the mapping is already defined by the seed data, and I wanted the tests to focus on the redemption behavior rather than duplicate fixture setup.

**What took the longest?**
Understanding the existing codebase and tracing the redemption flow took the most time. I wanted to understand how campaigns, vouchers, and redemption records were already represented before deciding where to add the new limit, rather than introducing a separate design that duplicated existing data.