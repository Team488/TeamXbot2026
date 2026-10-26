# Plan: Electrical Contract Tool on the Web

Status: all open questions decided (2026-10-26). Ready to build once approved.

## 1. Goal and constraints

Replace the locally run electrical report tool with a hosted web app that Xbot
students can open in a browser and edit together.

- Users: Xbot students (electrical and programming), plus mentors.
- Students edit shared data, sometimes at the same time.
- Hosting: static page on GitHub Pages; backend on the easiest free tier
  (Cloudflare Workers + D1 recommended below).
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

## 4. Data model (Cloudflare D1, SQLite)

The tool should outlive one season, so every row hangs off a `robot`.

```
robot          (id, name, season, contract_class, rev)             -- rev bumps on every write
code_snapshot  (id, robot_id, git_sha, branch, imported_at, json)  -- latest = active
device         (id, robot_id, name, kind, code_key NULL,           -- kind: motor|can_sensor|dio|pwm|
                notes, version, updated_by, updated_at)            --   converter|load|controller
power_port     (robot_id, panel, port, device_id NULL, breaker_a,  -- panel: PDH|VRM1|VRM2|RIO
                wire_awg, notes, version, updated_by, updated_at)  -- PK (robot_id, panel, port)
power_branch   (id, robot_id, parent_device_id, child_device_id, version, ...)
can_chain      (robot_id, bus, version, order_json, updated_by, updated_at)  -- one row per bus
code_proposal  (id, robot_id, device_code_key, field, from_val, to_val, status, by, at)
audit_log      (id, robot_id, table_name, row_key, before_json, after_json, user, at)
```

- **Decided (Q7):** breaker rating (`breaker_a`) and wire gauge (`wire_awg`) are in
  from launch. Photos come later (likely R2 or Cloudflare Images; not in the free
  plan above).
- **Decided (Q4):** launch with one `robot` row, the 2026 robot. Other robots can be
  added later without schema changes.
- PDH ports 0–23 and the VRM outputs are pre-created rows, so a student "assigns" by
  updating a row and never inserts one.
- Conflicting states are allowed in the DB, for example two motors on one port. The
  current checks are warnings, and a half-finished rewire is a real state. The UI
  shows them in a validation panel, as the report does today.

## 5. Simultaneous edits

- **Edits are per row with optimistic concurrency.** Every editable row carries a
  `version`, and the client sends the version it last saw. The write is
  `UPDATE ... SET ..., version = version + 1 WHERE pk = ? AND version = ?`. If 0 rows
  change, the Worker returns 409 with the current row. The UI shows "Sam changed
  this to PDH14 a moment ago. Keep theirs / overwrite?" Edits to different ports
  never collide.
- **Daisy-chain order is one list per bus**, because reordering is a whole-list
  operation. The same version check applies: on conflict, reload and redo the drag.
- **Live updates come from polling, not websockets.** The client polls
  `GET /robots/:id/rev` every 10 s while the tab is visible and refetches when `rev`
  changes. That's simple and stays well inside the free limits (see §7). Durable
  Objects with websockets would give instant updates, but they add complexity that
  isn't needed for a dozen students.
- **History and undo come from `audit_log`.** It records who changed what and when,
  and lets a mentor revert a bad edit. This matters because the login in §6 is
  deliberately light.

## 6. Student login and access

Ranked by friction, lowest first:

**Decided (Q5): option 1.** Public read-only view; edits need the passcode.

1. **Recommended: a team passcode plus a display name.**
   - Anyone with the link can read; the robot's wiring isn't secret.
   - To edit, a student types the team passcode once and picks their name.
   - The Worker returns a signed token, an HMAC over name and expiry valid for about
     90 days. The client keeps it in `localStorage` and sends it as an
     `Authorization` header, so GitHub Pages and the Worker sit on different origins
     with no third-party-cookie problems.
   - A mentor changes the passcode, a Worker secret, each season or after a leak.
   - Abuse risk is low, and the audit log plus revert covers mistakes.
2. **Cloudflare Access with an email one-time PIN** (Zero Trust free plan, up to 50
   users). Real per-person identity, with mentors keeping an email allowlist. The
   drawbacks: a student enters a code every session (session length is configurable),
   and it fits best when the front end is served by Cloudflare too, not GitHub Pages.
   This is the upgrade path if passcode sharing becomes a problem.
3. **GitHub OAuth limited to Team488 org members.** Clean for programmers, but many
   electrical students don't have GitHub accounts or org membership. That's high
   friction for the main users, so not recommended.

## 7. Hosting and free-tier limits

| Piece | Choice | Free-tier limits (check current numbers before building) | Expected use |
|---|---|---|---|
| Front end | GitHub Pages, static HTML + JS | Public repo (Team488 repos are public), 1 GB site, ~100 GB/mo soft bandwidth | Tiny |
| API | Cloudflare Worker | 100k requests/day, 10 ms CPU per request | 20 students × 1 poll/10 s × 3 h ≈ 22k/day at peak |
| DB | Cloudflare D1 | 5 GB, 5M rows read/day, 100k rows written/day | Hundreds of rows, a few hundred writes/day |
| Backup | Worker Cron Trigger + GitHub Action | D1 Time Travel restores up to 7 days on free | Nightly JSON export committed to git |

- **KV doesn't fit.** It is eventually consistent and allows about 1k writes/day on
  free, which is wrong for concurrent edits. Use D1.
- **Repo:** put the tool in its own repo, for example `Team488/electrical-contract-web`,
  not this season repo. The tool persists across seasons, and each season's robot
  repo just pushes snapshots to it.
- **Front-end stack:** plain HTML and ES modules, perhaps Preact + htm from a CDN,
  with no build step. A student can edit it on GitHub's web editor, and Pages deploys
  it.
- **Worker language (decided, Q9):** TypeScript.
- **Simpler alternative to consider:** serve the front end from the Worker's static
  assets instead of GitHub Pages. That gives one deploy, one origin, no CORS, and
  makes Cloudflare Access easy. The trade-off is that a Cloudflare account becomes
  the only place to edit. **Decided (Q8):** front end on GitHub Pages.

## 8. Migration steps

1. **Mentor action:** pick the owner of the Cloudflare
   account and the passcode secret, using a team email (Q6).
2. **Refactor the Java tool.**
   - Split `Main.java` into discovery, which returns a data model, and formatting.
   - Add `--json` output; keep the text report byte-identical for comparison.
   - This also gives a unit-testable discovery layer.
3. **Add a CI snapshot.** A GitHub Action on push to `main` and `ElectricalChecks`
   runs `./gradlew electricalReport --args="--json -f snapshot.json"` and POSTs the
   file to `/robots/:id/snapshot` with a CI secret.
4. **Stand up the backend.**
   - Create the Cloudflare account under the team email the mentors chose (Q6).
   - Write the D1 schema and the Worker CRUD + rev + auth endpoints, and set up
     Wrangler deploy from GitHub Actions.
5. **Seed the data.** A one-time import from the current `Contract2026` snapshot
   fills `device`, `power_port`, `power_branch` and `can_chain`. Then check that the
   web report matches `electrical-report.txt`.
6. **Build the front end.**
   - One tab per report section, with inline edit on PDH and VRM rows and a
     drag-reorder CAN chain.
   - A validation panel that ports the Java checks to JS and runs them in the
     browser.
   - A "code proposals" list.
   - A print view for the pit (Q1): browser print or "Save as PDF" of the full
     report with validation summary, timestamp and revision, plus a `.txt` download
     that reproduces `electrical-report.txt`.
7. **Pilot** with the electrical subteam on the 2026 robot, with the Java report
   still running in parallel.
8. **Cut over.** Students switch fully to the web app; keep `./gradlew electricalReport` as a developer
   and CI check, then do the phase 2 cleanup in §3.

Rough size: the Java refactor and CI take about 1–2 days, the Worker and schema about
2 days, the front end 3–5 days, and the pilot and fixes about 1 week elapsed.

## 9. Decisions (2026-10-26)

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
6. **Mentor action:** mentors pick the owner of the Cloudflare account and passcode,
   using a team email.
7. Add breaker rating and wire gauge now; photos later.
8. Front end on GitHub Pages.
9. Worker in TypeScript.
