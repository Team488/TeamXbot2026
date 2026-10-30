# Plan: Electrical Contract Tool on the Web

Status: phase 1 (local demo) built 2026-10-27. On 2026-10-29 the hosting target
changed to **AWS, deployed with CDK** (decision 10), and phase 1b ported the demo to
the AWS stack the same day. Phases 2–4 are not started. See §7–8.

## 1. Goal and constraints

Replace the locally run electrical report tool with a hosted web app that Xbot
students can open in a browser and edit together.

- Users: Xbot students (electrical and programming), plus mentors.
- Students edit shared data, sometimes at the same time.
- Hosting: static page on GitHub Pages; backend on AWS (Lambda + DynamoDB), with
  infrastructure defined and deployed with AWS CDK, the team's preferred deployment
  tool. The original recommendation was Cloudflare Workers + D1 (§7).
- Mentor priority: easy student access matters more than low maintenance.

## 2. What the tool does today

- Entry point: `src/main/java/electrical/report/Main.java` (~750 lines), run with
  `./gradlew electricalReport` (a `JavaExec` task in `build.gradle`). It writes
  `electrical-report.txt` (or the file given with `-f <file>`).
  - **There is no `.exe`.** The repo has no jpackage or launch4j config, no packaging
    step in any commit, no GitHub releases, no CI step, and no `.exe` in the local
    Team488 checkouts. The "exe" is this Gradle task, run by whoever has a dev setup
    (programmers and mentors with a JDK and a clone).
- Input: a hard-coded `Contract2026` instance, read by reflection. `autoDiscoverDevices()`
  calls every public getter on the contract and sorts what it gets back by type:
  - `CANMotorControllerInfo`: name, motor type, CAN bus, CAN ID, PDH port
  - `DeviceInfo`: CAN sensors and encoders (bus and ID), DIO and PWM channels,
    VRM outputs (`PowerSource.VRM1_*`)
  - `IMUInfo`
  - Swerve getters, called once per `SwerveInstance`
- Report-only data defined in `Contract2026`:
  - `getCanBusConnectionOrder()`: physical daisy-chain position of each CAN device
  - `getAdditionalPDHConnections()`: non-motor loads on PDH ports (RoboRio, VRM1,
    radio, buck converters, and so on)
  - `getAdditionalPowerBranches()`: converter to downstream device (buck → OrangePi,
    buck-boost → Orin, and so on)
  - `getVrm1_*()`: VRM output assignments
- Output sections: devices per CAN bus, CAN connection order, DIO, PWM, PDH ports
  (0–23), VRM ports, power branches, other power sources, and totals.
- Checks it runs:
  - more than one motor on a PDH port
  - a motor and a non-motor sharing a PDH port
  - conflicts on VRM ports
  - CAN devices missing from the connection order
  - mismatches between the connection order and the getters

The whole tool is a reader and checker. Students "edit" today by changing the Java
code, which means a Git checkout, a JDK, Gradle, and knowing Java syntax. That's the
access barrier the web version removes.

**Key finding:** the robot code never reads PDH, VRM, power-branch or daisy-chain data.
A grep of `src/main/java/competition` finds no PDH port use outside the contract.
Those fields exist only for the report. The robot code does use CAN bus and ID, DIO
and PWM channels, motor type, inversion and current limits.

## 3. Source of truth (central decision)

Options considered:

| Option | Students edit | Robot code stays correct | Complexity |
|---|---|---|---|
| A. Java remains the only source, web is read-only | No (fails requirement 2) | Yes | Low |
| B. Web DB is the only source, generates Java/JSON for robot | Yes | Only if the sync to git is reliable | High: an automated PR bot, plus a merge with code review |
| **C. Split ownership by field (recommended)** | Yes, for electrical-only data | Yes | Medium |

**Recommendation: C, split ownership.**

- **Java owns code-consumed fields.** These are: device name, CAN bus and ID, DIO
  and PWM channel, motor type, inversion and current limits. A GitHub Action runs
  the tool in a new `--json` mode on every push and uploads a *code snapshot* to the
  backend. The web shows these fields read-only, with the git SHA they came from.
- **The web DB owns electrical-only fields.** These are:
  - PDH port per device
  - VRM outputs
  - power branches
  - CAN daisy-chain order
  - devices the code doesn't know about (radio, Ethernet switch, Orin, OrangePis,
    buck converters, fans)
  - breaker size, wire gauge and notes (new fields students will want)
- **Proposed code changes.** A student can flag a code-owned field, for example
  "move ShooterLeft to CAN 27". The flag appears as a pending item with a diff against
  the snapshot. A programmer applies it in Java, and the next snapshot clears it. The
  robot code stays reviewable in git.
- **Phase 2 cleanup.** After a season of parallel use, remove the report-only
  methods (`getCanBusConnectionOrder`, `getAdditionalPDHConnections`,
  `getAdditionalPowerBranches`, `getVrm1_*`) from `Contract2026`. Also drop the PDH
  port from the `CANMotorControllerInfo` constructors, which needs a
  SeriouslyCommonLib decision because that type lives there. Then one fact has one
  home.
  - **Decided (Q3):** keep the PDH port field in SeriouslyCommonLib and ignore it. No
    shared-library change; phase 2 only removes the report-only methods from
    `Contract2026`.
- **Decided (Q2):** students propose CAN ID and channel changes, and a programmer
  makes them through normal Java code review. No PR bot.
- **Device join key: `(bus, canId)` for CAN devices, otherwise the device name.** A
  student-added device has no code identity until a programmer adds it to Java. The
  importer then links the two by name, and the UI asks for confirmation if the names
  differ.

## 4. Data model (DynamoDB, single table)

The tool should outlive one season, so every item belongs to a robot. Phase 1 used
D1 (SQLite) tables; on AWS, the same entities become items in one DynamoDB table
(`pk`, `sk`). The whole data set is a few hundred small items, so a single `Query`
on the robot's partition returns everything the UI needs.

| Entity | `pk` | `sk` | Main attributes |
|---|---|---|---|
| Robot | `ROBOT#1` | `META` | name, season, contract_class, `rev` (bumped on every write), `seeded`, `nextId` |
| Device | `ROBOT#1` | `DEVICE#<id>` | name, kind, code_key, in_code, `supply`, notes, version, updated_by/at |
| Power port | `ROBOT#1` | `PORT#PDH05`, `PORT#VRM1_12V_2A` | breaker_a, wire_awg, notes, version, … |
| CAN chain | `ROBOT#1` | `CHAIN#Canivore` | order (list of code_keys), version, … |
| Code proposal | `ROBOT#1` | `PROPOSAL#<id>` | device_id, field, from_val, to_val, note, status, version, … |
| Code snapshot | `SNAP#1` | `<imported_at>` | git_sha, source, json (a few KB; well under the 400 KB item limit) |
| Audit entry | `AUDIT#1` | `<at>#<id>` | entity, entity_key, before, after, user |

- **What powers a device** is `device.supply`: a PDH or VRM port, `RIO`, or
  `dev:<id>` for a converter branch. A port item holds only breaker, gauge and notes.
  (This replaced the original `power_port.device_id` and `power_branch` design during
  phase 1.)
- Numeric IDs (`dev:<id>` references) come from an atomic counter on the robot's
  `META` item, so existing references and the web code stay unchanged.
- **Decided (Q7):** breaker rating and wire gauge are in from launch. Photos come
  later (S3).
- **Decided (Q4):** launch with one robot, the 2026 robot. Another robot is another
  `ROBOT#<n>` partition.
- PDH ports 0–23 and the VRM outputs are pre-created items, so a student "assigns" by
  updating an item and never creates one.
- Conflicting states are allowed in the DB, for example two motors on one port. The
  checks are warnings, and a half-finished rewire is a real state. The UI shows them
  in a validation panel, as the report does today.

## 5. Simultaneous edits

- **Edits are per item with optimistic concurrency.** Every editable item carries a
  `version`, and the client sends the version it last saw. The write is one DynamoDB
  `TransactWriteItems` call:
  1. update the item with the condition `version = :seen`, setting
     `version = version + 1`
  2. put the audit entry
  3. add 1 to `rev` on the robot's `META` item

  If the condition fails, nothing is written, and the API returns 409 with the
  current item (`ReturnValuesOnConditionCheckFailure = ALL_OLD`). The UI shows "Sam
  changed this to PDH14 a moment ago. Keep theirs / overwrite?" Edits to different
  ports never collide. This is stronger than the phase 1 D1 version, where the audit
  write and the rev bump were a separate step.
- **Daisy-chain order is one list per bus**, because reordering is a whole-list
  operation. The same version check applies: on conflict, reload and redo the move.
- **Live updates come from polling, not websockets.** The client polls
  `GET /robots/:id/rev` every 10 s while the tab is visible and refetches when `rev`
  changes. That's simple and cheap (see §7). API Gateway websockets would give
  instant updates, but they add complexity that isn't needed for a dozen students.
- **History and undo come from the audit entries.** They record who changed what and
  when, and let a mentor revert a bad edit. This matters because the login in §6 is
  deliberately light.

## 6. Student login and access

Ranked by friction, lowest first:

**Decided (Q5): option 1.** Public read-only view; edits need the passcode.

1. **Recommended: a team passcode plus a display name.**
   - Anyone with the link can read; the robot's wiring isn't secret.
   - To edit, a student types the team passcode once and picks their name.
   - The API returns a signed token, an HMAC over name and expiry valid for about
     90 days. The client keeps it in `localStorage` and sends it as an
     `Authorization` header, so GitHub Pages and the API sit on different origins
     with no third-party-cookie problems.
   - A mentor changes the passcode, an SSM Parameter Store secret, each season or
     after a leak.
   - Abuse risk is low, and the audit log plus revert covers mistakes.
2. **Amazon Cognito with email sign-in** (one-time codes or passwords; free for
   this many users). Real per-person identity, with mentors keeping an allowlist.
   The drawbacks: more setup (user pool, hosted login or a custom login form), and
   students sign in per device. This is the upgrade path if passcode sharing becomes
   a problem. (On Cloudflare the equivalent was Access with an email one-time PIN.)
3. **GitHub OAuth limited to Team488 org members.** Clean for programmers, but many
   electrical students don't have GitHub accounts or org membership. That's high
   friction for the main users, so not recommended.

## 7. Hosting and cost

**Decided (10, 2026-10-29): AWS, with infrastructure in AWS CDK.** CDK is the team's
preferred deployment tool. Cloudflare Workers + D1 was the original pick: no credit
card and no rewrite of the phase 1 database code. AWS costs a card on file, a
database-layer port (phase 1b) and more setup, in exchange for the team's standard
tooling.

| Piece | Choice | Free tier / cost (check current AWS terms before building) | Expected use |
|---|---|---|---|
| Front end | GitHub Pages, static HTML + JS | Free for public repos; 1 GB site, ~100 GB/mo soft bandwidth | Tiny |
| API | Lambda (Node.js 22, TypeScript) behind a **Lambda Function URL** | Always free: 1M requests and 400k GB-s per month | 20 students × 1 poll/10 s × 3 h ≈ 22k requests/day at peak; well under 1M/month |
| DB | DynamoDB, on-demand capacity, point-in-time recovery on | Always free: 25 GB storage; on-demand reads and writes cost cents at this volume | A few hundred items, a few hundred writes a day |
| Secrets | SSM Parameter Store, SecureString | Standard parameters are free | 3 values |
| Logs | CloudWatch Logs, 30-day retention | 5 GB/month free | Small |
| Deploy | AWS CDK (TypeScript) | CDK is free; `cdk bootstrap` creates a small S3 bucket (cents/month) | — |
| Backup | DynamoDB point-in-time recovery (35 days) + nightly JSON export to git | PITR is billed per GB, cents at this size | — |

Expected total: **under $1/month** after any new-account credits run out. Guard
rails, all in the CDK stack:
- an AWS Budget with an email alert at $5/month to the team address
- Lambda reserved concurrency of about 5, so a runaway client or abuse can't scale
  cost. New accounts sometimes have a total concurrency quota too low to reserve
  any; if so, leave it off and rely on the budget alert.
- the DynamoDB table retained when the stack is deleted, so the data survives
  `cdk destroy`

Notes:
- **AWS free-tier terms changed in mid-2025.** New accounts start on a credit-based
  free plan and must upgrade to a paid plan to keep running after it ends. The
  always-free allowances (Lambda, DynamoDB storage) continue. Confirm current terms
  when the account is opened.
- **Function URL instead of API Gateway.** API Gateway's free tier lasts only the
  first 12 months; a Function URL is free and handles CORS itself. If you later want
  a custom domain or per-client throttling, add an HTTP API in front.
- **Region:** `us-west-2` (Oregon), closest to Seattle.
- **Repo:** keep the tool in its own repo, `Team488/electrical-contract-web`, not
  this season repo. The tool persists across seasons, and each season's robot repo
  just pushes snapshots to it.
- **Front-end stack:** plain HTML and ES modules with vendored Preact + htm and no
  build step. A student can edit it on GitHub's web editor, and Pages deploys it.
- **API language (decided, Q9):** TypeScript. The CDK app is TypeScript too, so one
  toolchain covers both.
- **Front end (decided, Q8):** GitHub Pages. (CloudFront + S3 from the same CDK stack
  would give one origin, but costs more setup for no gain here.)

## 8. Implementation phases

Code lives in a separate local repo, `C:\Users\tjbla\Code\Team488\electrical-contract-web`,
which has no remote until phase 2. Run steps and the data flow are in its
`README.md`.

### Phase 1: local demo for the 2026 robot (done 2026-10-27, Cloudflare stack)

Everything runs on one laptop. `wrangler dev` runs the API as a Cloudflare Worker
against a local SQLite copy of D1.

- **Java exporter.** `Main.java` has a `--json` mode, run as
  `./gradlew electricalReport --args="--json -f snapshot.json"`. It uses the same
  discovery as the text report, and the text output is unchanged (checked by diff).
  On branch `ElectricalWeb`, with `main` merged in.
- **API.** TypeScript, in `worker/src`:
  - versioned writes that return 409 with the current row when someone saved first
  - an audit log with undo
  - a revision counter that pages poll
  - passcode login that returns an HMAC token
  - snapshot import that seeds the wiring tables on first import only
  - 12 vitest tests
- **Front end.** Plain ES modules with vendored Preact/htm, in `web/`:
  - tabs for Checks, CAN, CAN chain (↑/↓ reorder), PDH, VRM, Devices & branches,
    DIO/PWM, Code changes and History
  - a conflict dialog when two people edit the same thing
  - print/PDF and `.txt` export
- **Launcher.** `scripts/dev.ps1` with `-Snapshot`, `-Reset` and `-Lan`.
- **New checks:**
  - breaker size over the PDH channel limit (40 A on ports 0–19, 15 A on 20–23)
  - wire gauge too thin for the breaker. The gauge table in `web/validate.js` needs
    checking against the current game manual.

### Phase 1b: port the backend to the AWS stack (done 2026-10-29; local, no AWS account)

Done as described below, in `api/` and `infra/`. The Cloudflare `worker/` is removed.
Results:
- 15 API tests pass against DynamoDB Local: the original 12, plus a true
  simultaneous-save race (exactly one save wins), real ids for seeded converters, and
  the Lambda Function URL adapter.
- 4 CDK template tests pass, and `cdk synth` produces a 20 KB Lambda bundle. That
  bundle was run against DynamoDB Local and answered correctly.
- The browser demo works through `scripts/dev.ps1`, against the current `main`
  contract (24 CAN, 1 DIO, 2 PWM devices). DynamoDB Local (v3.3.2) runs on the WPILib
  JDK.
- Small differences from the steps below:
  - The undo route is now `/api/robots/:id/audit/:auditId/revert`.
  - CDK settings live in `infra/cdk.json`, including the budget email, the
    reserved-concurrency cap and the OIDC options.
  - Reserved concurrency is off unless set, because new accounts often can't reserve
    any.

Planned steps:

Same features and front end; only the backend changes. Still runs on one laptop.

1. **Split the API from the platform.**
   - `api/src/handlers.ts`: the existing router and validation, unchanged, working
     on standard `Request`/`Response` objects.
   - `api/src/store.ts`: a `Store` interface covering what the handlers need: state,
     rev, audit list, versioned update, insert, delete, revert and snapshot import.
   - `api/src/dynamo-store.ts`: the DynamoDB implementation (§4–5), using AWS SDK v3
     (`@aws-sdk/lib-dynamodb`).
   - `api/src/lambda.ts`: adapts a Function URL event to a `Request` and the
     `Response` back. `api/src/local.ts`: a Node HTTP server wrapping the same
     handlers for the demo.
   - `auth.ts`: swap the Workers-only `crypto.subtle.timingSafeEqual` for Node's
     `crypto.timingSafeEqual`. Secrets come from SSM on AWS and from a local `.env`
     file in the demo.
2. **Local database:** DynamoDB Local, the official jar. It runs on Java, which every
   robot programmer already has, so there's no Docker. `scripts/dev.ps1` starts it,
   creates the table, starts the Node API on port 8787 and the static site on 8000;
   `-Snapshot`, `-Reset` and `-Lan` keep working.
3. **Tests:** port the 12 API tests to run against DynamoDB Local (vitest global
   setup starts it and creates a fresh table per test file).
4. **Retire the Cloudflare code:** remove `worker/`, `wrangler.toml`, the D1
   migration and the Wrangler dependencies once the AWS version passes the same
   tests and the same browser demo.
5. **CDK app** in `infra/` (TypeScript), written and checked with `cdk synth`; no
   deploy yet. One stack, `ElectricalContractStack`:
   - DynamoDB table (`pk`/`sk`, on-demand, PITR, `RemovalPolicy.RETAIN`)
   - `NodejsFunction` for the API (Node.js 22, arm64, 256 MB, esbuild bundling) with
     read/write on the table and read on the SSM parameters
   - Function URL (auth `NONE`; the API does its own token check), with CORS allowing
     the GitHub Pages origin and `localhost:8000`
   - CloudWatch log retention of 30 days, reserved concurrency (see §7), and an
     `AWS::Budgets` budget with an email alert
   - stack output: the Function URL
   - a construct for the GitHub Actions OIDC role, used in phase 3

Rough size: 2–3 days, mostly the DynamoDB store and its tests. Verify with the same
browser demo as phase 1, run against the new local stack.

### Phase 2: deploy to AWS and GitHub Pages

This waits on the mentor action (decision 6). Every step creates or publishes
something outside the laptop, so each one gets confirmed first.

1. **Account (mentor action):** a mentor opens the AWS account with the team email
   and a team card, turns on MFA for the root user, and creates an IAM Identity
   Center user for deploying. Configure the AWS CLI with `aws configure sso`.
2. **Secrets:** create the SSM SecureString parameters `/electrical-contract/PASSCODE`,
   `/electrical-contract/TOKEN_SECRET` and `/electrical-contract/SNAPSHOT_KEY`. Do
   this by hand or with `aws ssm put-parameter`, because CDK can't create
   SecureStrings and secrets don't belong in the template.
3. `cd infra; npx cdk bootstrap aws://<account>/us-west-2`, then `npx cdk deploy`.
   Note the Function URL output.
4. Create `Team488/electrical-contract-web`, which needs a Team488 org admin. Push
   the repo and turn on Pages from `web/`. Set `API_BASE` in `web/config.js` to the
   Function URL.
5. Import a fresh snapshot into the hosted database. Demo data is throwaway.
6. Verify by rerunning the demo against the hosted URLs from a phone off the team
   Wi-Fi. Then check that the budget alert email arrives (send a test notification).

### Phase 3: automation

- **Snapshot on push.** A TeamXbot2026 GitHub Action runs on push to `main` when
  `electrical_contract/**` or `electrical/report/**` changes. It runs the JSON
  exporter and POSTs the result to `/snapshot` using a `SNAPSHOT_KEY` repo secret.
- **Infrastructure deploy.** An `electrical-contract-web` Action runs
  `npx cdk deploy --require-approval never` on push to `main`. It authenticates with
  **GitHub OIDC** (the role from phase 1b, trusted only for this repo's `main`), so
  no long-lived AWS keys are stored in GitHub. Pages deploys itself.
- **Nightly backup.** A scheduled Action exports the robot's data as JSON through the
  API and commits it to the repo, which gives a git history of the wiring. DynamoDB
  PITR covers the last 35 days.

### Phase 4: pilot and cutover

1. Pilot with the electrical subteam on the 2026 robot. The Gradle report keeps
   running as a cross-check.
2. Students move fully to the web. `./gradlew electricalReport` stays as a developer
   and CI tool.
3. Do the §3 cleanup: remove the report-only methods from `Contract2026`
   (`getCanBusConnectionOrder`, `getAdditionalPDHConnections`,
   `getAdditionalPowerBranches`, `getVrm1_*`; some now live in `GeneralContract`).
   The PDH field in SeriouslyCommonLib stays (Q3).

### Later

- Photos, stored in S3.
- Cognito email login, if the passcode spreads too widely.
- More robots or seasons, which are new `ROBOT#<n>` partitions.

## 9. Decisions (2026-10-26, updated 2026-10-29)

1. **The `.exe`, users and offline access.** No `.exe` exists; the report runs as
   `./gradlew electricalReport` on a dev machine (§2). Students never run anything
   locally: their access is 100% the web interface. The Gradle task stays as a
   developer and CI tool. For the pit, where venues often have no internet, the web
   app provides a **printable/PDF export** of the full report: a print-styled page
   (browser "Save as PDF") that matches `electrical-report.txt`, plus a validation
   summary and a timestamp and revision. Teams print it or save the PDF before the
   event. No offline web app (PWA).
2. Students propose CAN ID and channel changes; Java code review applies them. No PR
   bot.
3. Keep the PDH port field in SeriouslyCommonLib and ignore it. No shared-lib change.
4. Launch with the 2026 robot only.
5. Public read-only view; edits need the passcode.
6. **Mentor action:** mentors pick the owner of the AWS account and passcode, using a
   team email (originally the Cloudflare account; see 10).
7. Add breaker rating and wire gauge now; photos later.
8. Front end on GitHub Pages.
9. API in TypeScript.
10. **(2026-10-29)** Host the backend on **AWS** (Lambda + DynamoDB) instead of
    Cloudflare, with infrastructure defined and deployed with **AWS CDK**, the team's
    preferred deployment tool. Adds phase 1b, which ports the phase 1 backend.
