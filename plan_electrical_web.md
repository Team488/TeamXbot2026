# Plan: Electrical Contract Tool on the Web

Status: phase 1 (local demo) built 2026-10-27; phases 2–4 not started. See §8.

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

## 8. Implementation phases

Code lives in a separate local repo, `C:\Users\tjbla\Code\Team488\electrical-contract-web`
(`worker/`, `web/`, `scripts/`), which has no remote until phase 2. Run steps and the
data flow are in its `README.md`.

### Phase 1: local demo for the 2026 robot (done 2026-10-27)

Everything runs on one laptop, with no Cloudflare account. `wrangler dev` runs the
Worker against a local SQLite copy of D1. The same code, schema and migrations deploy
unchanged in phase 2.

- **Java exporter.** `Main.java` has a `--json` mode, run as
  `./gradlew electricalReport --args="--json -f snapshot.json"`. It uses the same
  discovery as the text report, and the text output is unchanged (checked by diff).
- **Worker API.** TypeScript, in `worker/src`:
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
- **Differences from §4–5:**
  - Each device stores what powers it in `device.supply` (a PDH or VRM port, `RIO`,
    or `dev:<id>` for a converter). That replaces `power_port.device_id` and the
    `power_branch` table; `power_port` now holds only breaker, AWG and notes.
  - The CAN chain uses ↑/↓ buttons instead of drag-and-drop, so it works on phones.
- **New checks:**
  - breaker size over the PDH channel limit (40 A on ports 0–19, 15 A on 20–23)
  - wire gauge too thin for the breaker. The gauge table in `web/validate.js` needs
    checking against the current game manual.

### Phase 2: Cloudflare and GitHub Pages

This waits on the mentor action (Q6). Every step publishes something outside the
laptop, so each one gets confirmed first.

1. A mentor creates the Cloudflare account under the team email, then runs
   `npx wrangler login`.
2. Run `wrangler d1 create electrical-contract` and put the `database_id` in
   `worker/wrangler.toml`.
3. Run `wrangler d1 migrations apply DB --remote`, then `wrangler secret put` for
   `PASSCODE`, `TOKEN_SECRET` and `SNAPSHOT_KEY`, then `wrangler deploy`.
4. Create `Team488/electrical-contract-web`, which needs a Team488 org admin. Push
   the repo and turn on Pages from `web/`.
   - In `web/config.js`, set `API_BASE` to the workers.dev URL.
   - In the Worker config, set `ALLOWED_ORIGINS` to `https://team488.github.io`.
5. Import a fresh snapshot into the hosted database. Demo data is throwaway.
6. Verify by rerunning the phase 1 demo against the hosted URLs from a phone off the
   team Wi-Fi.

### Phase 3: automation

- **Snapshot on push.** A TeamXbot2026 GitHub Action runs on push to `main` or
  `ElectricalChecks` when `electrical_contract/**` or `electrical/report/**` changes.
  It runs the JSON exporter and POSTs the result to `/snapshot` using a
  `SNAPSHOT_KEY` repo secret.
- **Worker deploy.** An `electrical-contract-web` Action runs `wrangler deploy` and
  the migrations on push, using a `CLOUDFLARE_API_TOKEN` secret. Pages deploys
  itself.
- **Nightly backup.** An Action exports the robot's data as JSON and commits it to
  the repo, which gives a git history of the wiring. D1 Time Travel covers the last
  7 days.

### Phase 4: pilot and cutover

1. Pilot with the electrical subteam on the 2026 robot. The Gradle report keeps
   running as a cross-check.
2. Students move fully to the web. `./gradlew electricalReport` stays as a developer
   and CI tool.
3. Do the §3 cleanup: remove the report-only methods from `Contract2026`
   (`getCanBusConnectionOrder`, `getAdditionalPDHConnections`,
   `getAdditionalPowerBranches`, `getVrm1_*`). The PDH field in SeriouslyCommonLib
   stays (Q3).

### Later

- Photos, using R2.
- Cloudflare Access email login, if the passcode spreads too widely.
- More robots or seasons, which are new `robot` rows.

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
