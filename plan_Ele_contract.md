# Electrical Contract Tool: Design

How the system works once all phases are done. Anything not built yet is marked
**(not built)** and listed in §11. The history of how the design got here is in
`plan_electrical_web.md`.

Live site: https://team488.github.io/electrical-contract-web/
Code: `Team488/electrical-contract-web` (web tool), plus the Java side:
- **2026:** this repo holds the contract, the exporter and the snapshot workflow.
- **2027 on:** the exporter and `@ReadyWhen` move to SeriouslyCommonLib. Each season
  repo gets its robot list and workflow from FRCRobotTemplate.

§13 has what changed and where each piece lives.

## 1. Purpose

Students view and edit the robot's wiring in a browser, with no Java, Git or Gradle
needed:

- what powers each device (PDH port, VRM output, or a converter)
- breaker sizes and wire gauges
- the physical CAN daisy-chain order
- devices the robot code doesn't know about (radio, switch, coprocessors, converters,
  fans)

The tool checks the wiring for conflicts and prints a pit report. It handles both the
roboRIO (2026 robot) and SystemCore, the controller FRC adopts for the 2027 season (§10).

## 2. Architecture

```
Season robot repo (TeamXbot2026 now; the 2027 repo, made from FRCRobotTemplate, next)
   │  merge to main → GitHub Action "Electrical snapshots" runs the exporter for every
   │  robot and publishes JSON to the electrical-snapshots branch
   │  (2027 on: the exporter itself comes from SeriouslyCommonLib, §13)
   ▼
raw.githubusercontent.com/Team488/<season repo>/electrical-snapshots/<branch>/
   │  read by the API on page load (at most every 2 min), imported when the commit is new
   ▼
API: AWS Lambda behind a Function URL  ◄──────►  DynamoDB (one table)
   ▲                                              SSM: passcode + keys
   │  HTTPS + JSON (polls every 10 s)
Browser page: GitHub Pages, served from web/ in electrical-contract-web
```

- **Front end:** static HTML and JS (Preact/htm, vendored, no build step). It runs the
  checks, the print view and the `.txt` export in the browser.
- **API:** TypeScript on Node 22. The same request handlers run in Lambda and in the
  local demo server.
- **Infrastructure:** defined in AWS CDK (`infra/`), in us-west-2, in the account
  shared with the scouting app.

## 3. Who owns which data

| Data | Owner | Where it lives | How it changes |
| --- | --- | --- | --- |
| Device names, CAN bus and ID, I/O port (DIO/PWM, or SmartIO port and mode), motor types | **Programming** | Java contract | Programming edits Java; the next snapshot updates the web (read-only there) |
| What powers each device, breakers, wire gauge, current draw, notes | **Electrical** | Web | Electrical edits in the browser |
| CAN daisy-chain order | **Electrical** | Web | Electrical reorders in the browser |
| Devices outside the code (radio, switch, coprocessors, converters, fans) | **Electrical** | Web | Electrical adds or removes them in the browser |
| Controller profile of each robot (roboRIO or SystemCore) | **Programming** | Web (robot record) | Set when the robot is created (§8.1) |

- **Proposing code changes (details in §9).** When Electrical wants a
  Programming-owned field changed (for example "move ShooterLeft to CAN 27"), they file
  a proposal. Programming makes the change in Java through normal code review, then
  marks the proposal done.
- **How Java reaches the web: automatically.** On every merge to `main` that touches
  the contracts or the exporter, the GitHub Action **Electrical snapshots**
  (`.github/workflows/electrical-snapshots.yml`) does the following:
  1. Runs the exporter for each robot. In 2026 that's one
     `electricalReport --contract X --json` per robot. From 2027 it's a single
     `electricalReport --args="--all -d out"` (§13).
  2. Commits the files to the `electrical-snapshots` branch, under a folder named
     after the source branch: `index.json` plus one `<Contract>.json` per robot.

  When a page loads, the API checks that folder at most every 2 minutes. If the commit
  differs from the last one it imported, it imports every robot's file. Only one API
  instance imports a given commit. A merge shows up on the web a few minutes later,
  with no secret stored in the robot repo and nobody running anything.
- **Snapshot import.** The importer matches code devices by `(bus, CAN ID)` or by
  DIO/PWM channel, never by name. It adds new devices and flags ones removed from the
  code.
- **First import only:** the electrical data is seeded from the contract's report-only
  methods (§8.1). After that, imports never overwrite web-owned data, and differences show up on the Checks tab.
- **Contract cleanup (not built):** once the web is the source, remove the report-only
  methods from the contract: `getCanBusConnectionOrder`,
  `getAdditionalPDHConnections`, `getAdditionalPowerBranches` and `getVrm1_*`. The PDH
  port field in SeriouslyCommonLib stays, and the robot code ignores it.

### 3.1 One web robot per repository and contract

The season's code runs on earlier physical robots by choosing a different contract at
startup: the `ContractToUse` preference picks `Competition` (2026), `2025`, `2023` or
`Robox` in `competition.Robot`. The web tool mirrors that. Each selectable contract of
each season repo is its own **web robot**, with its own snapshot, wiring, breakers, CAN
chain, proposals, history and checks.

- **Two pickers in the page header.** **Repository** (season repo, newest first), then
  **Robot** (that repo's contracts). Switching repository keeps the same contract name
  when the other repo has it. The browser remembers the choice. Each picker shows only
  when there's more than one choice.
- **Repo + contract is the key.** `Competition` in TeamXbot2026 and `Competition` in
  the 2027 repo are two robots, so season repos can reuse contract names freely.
- **The same physical robot appears once per repo.** For example, the 2025 robot is
  `2025` in TeamXbot2026 and will be another robot under the 2027 repo. Each copy has
  its own wiring, seeded from that repo's contract on its first import; edits to one
  don't reach the other. A robot from a repo that isn't the newest is tagged **older
  repository**. Edit the copy whose code is on the robot.
- Robots created before repos were tracked belong to `Team488/TeamXbot2026`.

- **Readiness.** A contract passes the `Hardware` mechanisms its robot has
  (`readinessSet`). Contract getters are tagged with the mechanism they belong to, and
  the exporter leaves out devices whose mechanism isn't ready. In 2026 the tag is
  `@ReadyWhen(Hardware.X)`; from 2027 it's `@ReadyWhen("X")`, from the library (§13). The swerve drive follows
  `isDriveReady()`, and the CANcoders follow `areCanCodersReady()`. Left-out devices
  show in a **Not on this robot** list (CAN tab, report).
- **Shared definitions.** The exporter records which class defines each device
  (`defined_in`). A device defined anywhere but the robot's own contract class (for
  example `GeneralContract`) is tagged **shared**. Proposing a change to it warns
  that other robots' contracts use the same definition, and that Programming can
  override it in this robot's class instead.
- **Report-only wiring lists** (CAN order, extra PDH loads, power branches) are used
  only when the robot's own contract class declares them, so Robox doesn't inherit
  the 2026 robot's wiring. The VRM getters in `GeneralContract` still seed every
  robot; check them during the first review (§8.1).
- **Verified on robot (not built).** An Electrical-owned checkbox per device, with
  name and date, plus a check for unverified devices. It confirms each physical
  device really is set to the ID its contract says before running this year's code
  on an older robot.

## 4. Data model (one DynamoDB table, `pk`/`sk`)

| Item          | `pk` / `sk`                  | Holds                                                                                                           |
| ------------- | ---------------------------- | --------------------------------------------------------------------------------------------------------------- |
| Robot         | `ROBOT#1` / `META`           | name, season, **controller profile** (`roborio` or `systemcore`, §10), `rev` (bumped on every change), id counter |
| Device        | `ROBOT#1` / `DEVICE#<id>`    | name, kind, code_key, in_code, **supply** (`PDH05`, `VRM1_12V_2A`, `RIO`, or `dev:<id>` for a converter), notes |
| Power port    | `ROBOT#1` / `PORT#PDH05`     | breaker (A), wire (AWG), notes                                                                                  |
| CAN chain     | `ROBOT#1` / `CHAIN#Canivore` | ordered list of devices                                                                                         |
| Code proposal | `ROBOT#1` / `PROPOSAL#<id>`  | field, from → to, note, status                                                                                  |
| Snapshot      | `SNAP#1` / `<time>`          | the Java snapshot as received                                                                                   |
| History       | `AUDIT#1` / `<time>#<seq>`   | who, what, before and after                                                                                     |

- Device kinds: `motor`, `can_sensor`, `dio`, `pwm` (roboRIO), `smartio` (SystemCore,
  with a `mode`), `vrm`, `converter`, `load`. Devices may carry an optional current draw
  (mA), used for the shared-rail checks in §10. **(not built:** `smartio`, mode and
  current draw.**)**
- One query loads everything the page needs.
- Each robot is its own `ROBOT#<n>` partition, found by its season repo and
  `ContractToUse` name (`repo` and `contract_name` on the robot record). Pull state is
  kept per repo (`SYNC` / `SNAPSHOTS#<repo>`). Devices also store `defined_in`, the Java
  class that defines them.

## 5. Editing together

- **Every editable item has a `version`.** A save is one DynamoDB transaction: write
  the item only if its version is still the one the student saw, add a history entry,
  and bump `rev`. If someone else saved first, nothing is written and the page shows
  their change with **Keep theirs / Overwrite**.
- **Live updates:** each page polls `rev` every 10 s and reloads when it changes.
- **Rule breaks are allowed.** The data may break the rules (two motors on one port)
  while a student is mid-rewire; the Checks tab shows the problem.
- **History:** lists every change with who made it. **Undo** writes the old values
  back as a new change.

## 6. Access

- **Viewing:** public; anyone with the link can view.
- **Editing:** needs the team passcode plus a display name. Login returns a signed
  token valid for 90 days.
- **Passcode:** stored in SSM (`/electrical-contract/PASSCODE`). Mentors change it
  each season or after a leak.
- **Personal logins (not built):** if the passcode spreads too widely, the upgrade
  path is per-student logins through Amazon Cognito.

## 7. Checks and reports

The Checks tab and the printed report run these checks:

- more than one motor on a PDH port
- a motor and a non-motor sharing a PDH port
- more than one device on a VRM output
- a breaker above the PDH channel limit (40 A on ports 0–19, 15 A on 20–23)
- wire too thin for its breaker. The gauge table in `web/validate.js` still needs
  checking against the current game manual.
- duplicate CAN IDs
- CAN devices missing from the chain, and chain entries that aren't in the code
- devices with no power source
- converters that point at a deleted device
- devices removed from the Java contract
- PDH ports where the Java contract and the wiring disagree
- open code proposals

**Print / PDF** gives the full report in the `electrical-report.txt` layout, plus the
check results, a timestamp and the revision. Save it before an event, since venues
often have no internet. **Download .txt** gives the same text.

## 8. Startup

### 8.1 Starting a robot or season

1. **Create the robot.** It's created automatically by its first snapshot import:
   `POST /api/repos/<owner>/<repo>/contracts/<ContractToUse name>/snapshot` finds the
   robot with that repo and contract name, or creates the next one. (The older
   `POST /api/contracts/<name>/snapshot` means TeamXbot2026.)
   - The robot record holds its repo, name, season, contract name and class, and
     controller profile.
   - It starts with empty power ports for its profile: 24 PDH ports (`PDH00`–`PDH23`)
     and the 8 VRM1 outputs for roboRIO.
   - Robot 1 (2026 Competition) also gets created on the API's very first start.
   - **(not built:** SystemCore profiles (§10); every robot is roboRIO today.**)**
2. **Register the contract with the exporter.** `CONTRACTS` in
   `electrical/report/Main.java` maps each `ContractToUse` name to its class and a
   display name. Use the same names as the switch in `competition.Robot`. A new
   season's contract is one new line there. From 2027 the file comes from
   FRCRobotTemplate and is only that list plus a call to the library (§13.3).
3. **Import the first snapshot** (§12.3). On a robot's first import, the importer:
   1. Creates a device for every code device: CAN devices keyed `can:<bus>:<id>`, DIO
      and PWM devices keyed `dio:<ch>` and `pwm:<ch>`. Each motor's power source starts
      as the PDH port given in Java.
   2. Reads the contract's report-only methods:
      - `getAdditionalPDHConnections()`: each named load becomes a device on that PDH
        port. Names ending `_Pwr` become converters, `VRM1` becomes a VRM, anything
        else a load. `No_Connect` is skipped.
      - `getVrm1_*()`: the named devices go on those VRM outputs.
      - `getAdditionalPowerBranches()`: each child device is powered from its
        converter.
      - `getCanBusConnectionOrder()`: becomes the CAN chain, sorted by position.
   3. Marks the robot as seeded. Later imports only update code-owned fields.
4. **Review on the Checks tab and clean up.** The seeding matches the report-only
   lists to code devices **by name**, so a load spelled differently from its code
   device becomes a second device. For example, the PDH list's `Pigeon` and the code's
   `Pigeon2.0`. Fix those by hand:
   1. Set the code device's power source to match.
   2. Delete the extra web device.
   3. Fill in breakers and wire gauges.
5. **Set the season passcode** (§12.2) and share the link.

### 8.2 What happens when someone opens the page

1. GitHub Pages serves the static files. `config.js` picks the API: the local one
   when the page comes from localhost or the LAN, otherwise the Lambda Function URL.
2. The page loads the robot in one request. If a login token from an earlier visit is
   saved in the browser, editing turns on.
3. Every 10 s while the tab is visible, the page asks for `rev` and reloads when it
   has changed.

### 8.3 What happens when the API starts (Lambda cold start)

AWS starts a new API instance after a quiet period or after a deploy. That adds about
1–2 s to the first request. On start the API:

1. Reads `PASSCODE`, `TOKEN_SECRET` and `SNAPSHOT_KEY` from SSM
   (`/electrical-contract/`).
2. Checks that robot 1 exists, and creates it on the first ever start.
3. Keeps the secrets in memory for the life of the instance. That's why a passcode
   change needs an API restart (§12.2).

At most 5 instances run at once (reserved concurrency).

## 9. Getting changes back into Java

There are two kinds of change, and they go back differently.

### 9.1 Programming-owned fields: proposal → Java → snapshot

The CAN ID, CAN bus, DIO/PWM channel, device name and motor type are read by the robot
code, so they only change in Java, through code review.

1. **Electrical proposes.** On the CAN or DIO/PWM tab, **Propose change** records the
   device, the field, the current and new values, and why. It appears on the **Code
   changes** tab as *Open*.
2. **Programming makes the change** in `Contract2026` or `GeneralContract`, then opens
   a PR, gets it reviewed and merges it to `main`.
3. **Physical change.** If it's a CAN ID, someone sets the new ID on the device itself
   (Phoenix Tuner X for CTRE devices) when the code is deployed. The web tool doesn't
   track this step.
4. **The snapshot reaches the web by itself.** The merge runs the Electrical snapshots
   Action, and the API picks up the new files on the next page load, a few minutes
   later (§3). The page header shows which Java commit each robot is on.
5. **Programming marks the proposal** *Done in Java* on the Code changes tab. **(not
   built:** closing it automatically when the snapshot shows the new value.**)**

What the import does depends on the kind of change. Devices are matched by their
code key, never by name.

| Change in Java | What the import does | Follow-up on the web |
|---|---|---|
| Rename a device, or change its motor type | Updates the same device; its wiring, notes and chain position stay | None |
| Change a CAN ID or bus, or a DIO/PWM channel | Treated as **removing** the old device and **adding** a new one. The old one is flagged "removed from the Java contract" and keeps its wiring. The new one gets the PDH port from Java and is missing from the CAN chain | Electrical copies the wiring to the new device, put it in the chain where the old one was, then delete the old one. **(not built:** carrying wiring and chain position over automatically.**)** |
| Add a device | Adds it, powered from the PDH port in Java, and flags it as missing from the chain | Add it to the chain. If Electrical had already added it as a web-only device, copy that device's wiring over and delete it. **(not built:** linking to the web device by name.**)** |
| Remove a device | Flags it "removed from the Java contract" and keeps it on the web | Delete it on the Devices tab once it's physically gone |

### 9.2 Electrical-owned fields: stay on the web

What powers each device, breakers, wire gauge, notes, devices outside the code, and
the CAN chain order **don't go back into Java**, and don't need to: the robot code
never reads them. The web is their only home once a robot is seeded.

- **Until the contract cleanup (phase 4):** the same facts still sit in Java, in the
  report-only methods and the PDH port arguments of the motor constructors. They go
  stale as Electrical edits, which is expected.
  - The Checks tab notes each motor where Java and the wiring disagree ("Java says
    PDH05, wiring says PDH07"). Treat that as information, not an error.
  - Programming doesn't need to update Java to match.
  - `./gradlew electricalReport` shows the stale Java view. For the real wiring, use
    the web's **Print / PDF**.
- **After the cleanup:** the report-only methods are gone. The motor constructors keep
  their PDH port argument because SeriouslyCommonLib requires it, but the robot code
  ignores it. The Java report covers Programming-owned fields only.
- **(not planned):** writing web wiring back into Java source. Keeping one home per
  fact is the point of the split.

## 10. Controller profiles: roboRIO and SystemCore

FRC moves from the roboRIO to **SystemCore** for the 2027 season (kickoff January 9,
2027). Each robot record carries a **controller profile**, and the profile decides the
robot's ports, I/O model, checks and report layout. The 2026 robot uses the roboRIO
profile and doesn't change. The 2027 robot starts as a new robot (§8.1) with the
SystemCore profile. **(not built:** profiles; today the roboRIO layout is hard-coded.**)**

The profiles are data (a table of port lists and limits), not code, because the
SystemCore specs are from alpha hardware and still changing: connectors, and the
digital-input pull direction.

| | roboRIO profile | SystemCore profile |
|---|---|---|
| CAN buses | `rio`, plus CANivore if fitted | `can_s0`–`can_s4` (5 CAN-FD buses, 120 Ω termination built in), plus CANivore if still used |
| I/O | Separate DIO 0–9 and PWM 0–9 numbering (`dio:<ch>`, `pwm:<ch>`) | 6 **SmartIO** ports, each with a runtime mode: digital in/out, PWM in/out, analog in (12-bit, 0–3.3 V), addressable LED, or quadrature (`io:<port>` + `mode`) |
| Other connections | — | I2C ×2, USB 3.0 ×4, M.2 slot |
| Removed vs. roboRIO | — | Relays, analog out, SPI, MXP; **PWM servos aren't supported** |
| Controller power | "RoboRio" load on a PDH port | "SystemCore" load on a PDH port, fed through its Bridge Port (MicroFit) |
| Low-voltage sources for devices | `RIO` | `CTRL_3V3` (shared SmartIO/I2C 3.3 V rail), `USB0`–`USB3` |
| VRM | VRM1 outputs pre-created | Created only if the robot has a VRM |
| Built-in IMU | No (Pigeon on CAN) | Yes. A Pigeon is just another CAN device if the robot still has one |

**SystemCore checks** (in addition to the ones in §7):
- a SmartIO port outside 0–5, or two devices on one port
- a device whose mode the port can't do. For example, a servo: SystemCore drops PWM
  servo support.
- the 3.3 V rail over **1 A total**, summing the current draw entered for SmartIO and
  I2C devices
- USB over **2 A shared** across the 4 ports
- a CAN bus name the profile doesn't have

**Report:** the SystemCore print layout lists the 5 CAN buses, a **SmartIO** section
(port, mode, device) instead of DIO/PWM, and rail-current totals.

**Exporter and contract:**
- The 2027 contract is written against the 2027 SeriouslyCommonLib and WPILib, where
  packages move to `org.wpilib`. The exporter has been ported to them and moved into
  the library (§13).
- The exporter stops guessing I/O types from names. Today it treats method names with
  "servo"/"pwm", device names ending in "DIO", or devices powered from the RIO as I/O
  devices.
- Instead, SeriouslyCommonLib's `DeviceInfo` gets an explicit **port type and mode**.
  That's a shared-library change to agree with the library's maintainers.
- `busName()` uses the bus's own name instead of only `Canivore` and `rio`. (Done in
  the library exporter: buses come from the devices, `can_s0`–`can_s4` and Canivore.)

**Known carry-over issue:** the 2026 hood servos (PWM 7 and 9) can't move to
SystemCore as they are. Servos aren't supported, and SmartIO has only ports 0–5. That's
a hardware choice for Electrical before the 2027 contract is written.

## 11. Not built yet

| Item | Owner | Phase | Notes |
| --- | --- | --- | --- |
| Point `snapshotSources` at the `main` folder once `ElectricalWeb` is merged (§12.3) | Programming | 3 | One line in `infra/cdk.json`, then `cdk deploy` |
| Copy a robot's wiring from an older repo's copy when it first appears in a new repo (§3.1) | Programming | 2027 prep | Today the new copy is seeded from that repo's Java, and Electrical re-checks it |
| GitHub Action that runs `cdk deploy` on push to `electrical-contract-web` `main` | Programming | 3 | The OIDC deploy role `electrical-contract-github-deploy` already exists |
| Nightly JSON backup of the data, committed to git | Programming | 3 | DynamoDB point-in-time recovery (35 days) already covers restores |
| Copy wiring and chain position to the new device when a CAN ID or port changes (§9.1) | Programming | 3 | Today Electrical re-enters them by hand |
| Link a device newly added in Java to a matching web-only device by name (§9.1) | Programming | 3 | Today Electrical deletes the duplicate by hand |
| Close a proposal automatically when the snapshot shows the new value | Programming | 3 | Today Programming marks it done |
| Pilot on the 2026 robot, then full switch to the web | Electrical | 4 | |
| Contract cleanup (§9.2) | Programming | 4 | |
| Controller profiles (§10) | Programming | 2027 prep | Robots and contracts are per robot already (§3.1); every robot uses the roboRIO port layout |
| SystemCore I/O model: `smartio` kind with mode, SmartIO tab, CAN-FD bus names (§10) | Programming | 2027 prep | |
| SystemCore checks and report layout: ports 0–5, modes, 3.3 V rail and USB current (§10) | Programming | 2027 prep | |
| Current-draw field per device | Programming | 2027 prep | Electrical fills in the values |
| Explicit port type and mode in SeriouslyCommonLib `DeviceInfo` (§10) | Programming | 2027 prep | Agree with the library maintainers. Porting the exporter to the 2027 libraries is done (SeriouslyCommonLib #692) |
| Land the exporter move, in order (§13.5): merge and release SeriouslyCommonLib #692; fix FRCRobotTemplate `main` for SeriouslyCommonLib #688; bump the template's library version; merge FRCRobotTemplate #96 | Programming | 2027 prep | |
| Create the 2027 repo from the template; tag its contract getters with `@ReadyWhen`; add its folder to `snapshotSources` (§13.6) | Programming | 2027 prep | |
| Replace the hood servos, which have no SystemCore support (§10) | Electrical | 2027 prep | Programming updates the 2027 contract to match |
| Check the SystemCore profile's port lists, connectors and pull direction against the final 2027 docs | Electrical | 2027 prep | Specs so far are from alpha hardware |
| "Verified on robot" checkbox per device, with a check for unverified devices (§3.1) | Electrical | Later | Programming builds it; Electrical owns the data |
| Photos (S3), Cognito logins | Programming | Later | |
| Budget alert | Programming | Open | Left off because the account is shared with the scouting app |
| Update the account's CDK bootstrap stack (older than version 21) | Programming | Open | Shared with the scouting app; check with its owners first |
| Fix the wire-gauge table against the game manual | Electrical | Open | `web/validate.js` |
| Remove the stale CAN ID 59 from `getCanBusConnectionOrder` | Programming | Open | Java data fix |
| `Contract2023` gives its steering motors the drive motors' names (`…/Drive` twice per corner) | Programming | Open | Same bug `main` already fixed in `Contract2026` |
| `Contract2025` turns off the drive and every mechanism, so the 2025 robot shows no code devices | Programming | Open | Check whether that's intended before running 2026 code on it |
| Review each older robot's seeded wiring, especially the VRM layout inherited from `GeneralContract` (§3.1) | Electrical | Open | |

## 12. How to run it

### 12.1 Students and mentors: use it

1. Open https://team488.github.io/electrical-contract-web/ on any device.
2. To edit, click **Log in to edit** and enter your name and the team passcode.
3. Before an event, use **Print / PDF** to save the report.
4. If the page looks stale, press Ctrl+Shift+R. The first request after a quiet
   period takes 1–2 s while AWS starts the API.

### 12.2 Mentors: passcode and access

Needs the AWS CLI, signed in with `aws login --region us-west-2`. In a window opened
before the install, use the full path, `& "C:\Program Files\Amazon\AWSCLIV2\aws.exe"`.

```powershell
# Read the passcode
aws ssm get-parameter --with-decryption --name /electrical-contract/PASSCODE --query Parameter.Value --output text --region us-west-2

# Change it
aws ssm put-parameter --overwrite --type SecureString --name /electrical-contract/PASSCODE --value <new> --region us-west-2
```

The API reads the passcode at startup. After changing it, restart the API with
`cdk deploy` (§12.4), or by changing the Lambda's description in the AWS console.
Students already logged in stay logged in until their 90-day token expires. To log
everyone out, also replace `/electrical-contract/TOKEN_SECRET`.

### 12.3 Programmers: send a contract change to the web

Merge it to `main`. That's all:
1. The **Electrical snapshots** Action runs, in about a minute; it shows in the repo's
   Actions tab.
2. The web picks up the new files on its next page load, at most a couple of minutes
   later.
3. Check the header: "Java <commit>" should show the merge.

- **Source branch:** the API reads the folders listed in `snapshotSources`
  (comma-separated, one per season repo) in `electrical-contract-web/infra/cdk.json`.
  Until the `ElectricalWeb` branch is merged, TeamXbot2026's entry is the
  `ElectricalWeb` folder. After the merge, change it to `.../main` and run
  `cdk deploy`.
- **Re-run without a code change:** Actions → Electrical snapshots → **Run workflow**.
  This works once the workflow is on `main`.
- **Manual import** (local testing, or if GitHub is down):
  `scripts/sync-contracts.ps1 -Api <url> [-Repo owner/name]` with `$env:SNAPSHOT_KEY`
  set. The key is in
  SSM at `/electrical-contract/SNAPSHOT_KEY`. For one robot, run
  `./gradlew electricalReport --args="--contract 2025 --json -f snapshot.json"`, then
  `node ..\electrical-contract-web\scripts\import-snapshot.mjs snapshot.json <api> --repo Team488/TeamXbot2026`.

Without `--json`, `./gradlew electricalReport [--args="--contract NAME"]` still writes
`electrical-report.txt`, now including a "Not on this robot" section.

From 2027 the same commands work, and `--args="--all -d out"` writes every robot at
once, as the workflow does (§13.3). Building needs JDK 25.

### 12.4 Programmers: change and deploy the web tool

Work in `electrical-contract-web`.

- **Front end (`web/`):** push to `main`, and the Pages workflow publishes it within
  about a minute.

- **API or infrastructure (`api/`, `infra/`):**
  
  ```powershell
  cd infra; npm install
  aws configure export-credentials --format powershell | Invoke-Expression   # CDK can't read `aws login` credentials directly
  npx cdk deploy -c reservedConcurrency=5
  ```
  
  Settings are in `infra/cdk.json`. The table is kept even if the stack is destroyed.

### 12.5 Programmers: local development (no AWS)

Needs Node 22+, Python 3 and Java 17+ (the WPILib JDK is found automatically). The
2027 repos (SeriouslyCommonLib `main`, FRCRobotTemplate) need JDK 25
(`winget install EclipseAdoptium.Temurin.25.JDK`).

```powershell
cd electrical-contract-web\api; npm install; cd ..
./scripts/dev.ps1 -Snapshot          # DynamoDB Local + API on :8787 + page on :8000
```

- Open http://localhost:8000. The page uses the local API whenever it's opened from
  localhost or the LAN.
- `-Reset` wipes the local data. `-Lan` lets phones on the Wi-Fi connect (Windows
  Firewall must allow ports 8000 and 8787).
- Tests: `cd api; npm test` (API, against an in-memory DynamoDB Local) and
  `cd infra; npm test` (CDK template).

## 13. Java code organization: 2026 and from 2027

The Java side of the tool reads a robot's electrical contract and turns it into a text
report and JSON snapshots. In 2026 it was built inside TeamXbot2026. For 2027 it's
split so that every new season repo gets it without copying code:

- **SeriouslyCommonLib** holds what doesn't change from season to season: the
  `@ReadyWhen` tag, the report interface and the exporter.
- **FRCRobotTemplate** holds what each season repo owns: the robot list and the
  workflow. A new season repo made from the template has the whole pipeline on day one.

### 13.1 What changed

| Piece | 2026 (TeamXbot2026) | From 2027 |
| --- | --- | --- |
| Readiness tag | `@ReadyWhen(Hardware.X)`, enum-typed, in `competition.electrical_contract` | `@ReadyWhen("X")`, a string, in SeriouslyCommonLib `xbot.common.injection.electrical_contract` |
| Readiness check | Exporter calls `isReady(Hardware)` directly | Contract implements `XElectricalReportContract.isReady(String)` and `getHardwareNames()` |
| Report-only wiring lists (CAN order, extra PDH loads, power branches) | Methods on `ElectricalContract`; `CanBusOrderEntry` record nested in `Contract2026` | Default methods and the `CanBusOrderEntry` record on `XElectricalReportContract`. A contract overrides only what it has |
| Exporter | `src/main/java/electrical/report/Main.java`, about 1,000 lines, with the robot list inside | `xbot.common.electrical.report.ElectricalReport` in the library. The season repo's `Main.java` is about 40 lines, mostly comments: the robot list and `ElectricalReport.run(args, CONTRACTS)` |
| CAN bus names | Hard-coded: `Canivore` and `rio` | Taken from each device's `CANBusId`: `can_s0`–`can_s4` (SystemCore) and `Canivore` |
| Writing every robot | Workflow loops over a list of robots, then builds `index.json` with Python | `--all -d DIR` writes every robot plus `index.json` |
| `PDHPort` with PDH00–23 | Local copy in this repo (the 2026 library build stops at PDH19) | In the library since SeriouslyCommonLib #653; no copy needed |
| Workflow | `.github/workflows/electrical-snapshots.yml` here, Windows runner, also runs on `ElectricalWeb` | Same file in FRCRobotTemplate, Ubuntu runner, JDK 25, `main` only |
| Tests | None | 8 unit tests in the library (`ElectricalReportTest`) |

**JSON output is unchanged.** The web API imports 2026 and 2027 snapshots the same
way. The template's output was checked against the API's test fixtures.

**2026 doesn't change.** TeamXbot2026 keeps its own exporter, enum `@ReadyWhen` and
`PDHPort` copy, because its library build (20260214.33) predates all of this. The
`PDHPort` copy has a comment saying so.

### 13.2 SeriouslyCommonLib (PR #692)

- **`xbot.common.injection.electrical_contract.ReadyWhen`:** annotation on a
  contract getter, `@ReadyWhen("Shooter")`. It's kept at run time so the exporter can
  read it. The exporter also finds it on the abstract getter in `ElectricalContract`,
  so one tag there covers every contract's override.
- **`xbot.common.injection.electrical_contract.XElectricalReportContract`:** the
  interface the exporter reads. All its methods have defaults, so a contract only
  overrides what it uses:

  | Method | Default | Purpose |
  | --- | --- | --- |
  | `isReady(String name)` | `true` | Is that mechanism on this robot? |
  | `getHardwareNames()` | `null` (no check) | All valid names. A `@ReadyWhen` name not in this set stops the export with an error, so a typo can't silently hide a device |
  | `getCanBusConnectionOrder()` | empty | `List<CanBusOrderEntry(busId, canId, deviceName, busPosition)>`: physical chain order |
  | `getAdditionalPDHConnections()` | empty | `Map<PDHPort, List<String>>`: loads on PDH ports that aren't in the code |
  | `getAdditionalPowerBranches()` | empty | `Map<String, List<String>>`: devices fed from a converter |

- **`xbot.common.electrical.report.ElectricalReport`:** the exporter.
  - `RobotContract(String robotName, Supplier<? extends XElectricalReportContract> create)`
    is one robot.
  - `run(String[] args, Map<String, RobotContract> contracts)` is the command line.
    Map keys are the `ContractToUse` names, and the first entry is the default.
  - `writeAll(Path dir, Map<String, RobotContract> contracts)` writes every robot.
  - How it finds devices:
    - It calls every no-argument getter that returns `CANMotorControllerInfo`,
      `DeviceInfo` and similar types.
    - For a swerve contract (`XSwerveDriveElectricalContract`), it also calls the
      per-module getters.
    - Getters whose `@ReadyWhen` mechanism isn't ready go into the "Not on this robot"
      list.
  - For each device it records the class that defines it (`definedIn`). It uses the
    report-only wiring lists only when the robot's own class declares them.
  - It writes its own JSON, so the library gets no new dependency.
  - Plain `DeviceInfo` devices are still sorted into DIO and PWM by naming
    convention. The fix is the explicit port type in §10, marked `TODO(SystemCore)`
    in the code.
- **Tests (`ElectricalReportTest`):**
  - leaving out devices that aren't ready
  - rejecting a misspelled name
  - `definedIn`
  - wiring lists taken only from the robot's own class
  - sorting DIO and PWM devices
  - JSON and text output
  - `--all` with `index.json`
  - JSON escaping

### 13.3 FRCRobotTemplate (draft PR #96)

| File | Change |
| --- | --- |
| `competition/electrical_contract/ElectricalContract.java` | Implements `XElectricalReportContract`. `isReady(String)` and `getHardwareNames()` map to the `Hardware` enum. The class comment shows the `@ReadyWhen` pattern |
| `competition/electrical_contract/Hardware.java` | Comment: `@ReadyWhen` names must match these constants. The enum starts empty; each season adds its mechanisms |
| `competition/electrical_contract/Contract2026.java` | Uses the library's `CanBusOrderEntry`; its nested copy is removed |
| `electrical/report/Main.java` (new) | `CONTRACTS`: `Competition`→`Contract2026`, `2025`, `2023`, `Robox`, matching the `ContractToUse` switch in `competition.Robot`. `main` calls `ElectricalReport.run` |
| `.github/workflows/electrical-snapshots.yml` (new) | Runs on a push to `main` that touches the contracts, the exporter, `build.gradle` or `vendordeps`, or by hand (`workflow_dispatch`). Runs `./gradlew electricalReport --args="--all -d out"` and commits `out/` to the `electrical-snapshots` branch under `<branch>/`. Uses only the built-in `GITHUB_TOKEN`; no secrets |
| `README.md` | "Electrical report and web tool" section |

`build.gradle` already had the `electricalReport` task, so it doesn't change.

Commands, from a template-based repo:

```
./gradlew electricalReport                                        text report, first robot
./gradlew electricalReport --args="--contract 2025"               text report, another robot
./gradlew electricalReport --args="--contract 2025 --json -f x.json"
./gradlew electricalReport --args="--all -d out"                  every robot as JSON + index.json
```

### 13.4 How it was checked

Checked on 2026-11-03, with JDK 25.
- **SeriouslyCommonLib `electrical-report`:** `./gradlew build` passes, including all
  tests and checkstyle.
- **FRCRobotTemplate `electrical-report`:** built with `-DuseLocalCommonLib=true`
  against the library at #684 (the template's pinned 20261010.5) plus #692's files.
  - `./gradlew build` passes.
  - `--all` wrote 4 robots plus `index.json`: Competition with 39 CAN devices, 2023
    with 12, Robox with 14, and 2025 with 0, because `Contract2025` turns everything
    off (§11).

### 13.5 Landing order

1. **Merge SeriouslyCommonLib #692 and publish a release.**
2. **Fix FRCRobotTemplate `main` for SeriouslyCommonLib #688.** #688 removed
   `CANBusId.RIO` and the `DeviceInfo(name, id, inverted, simScaling)` constructor.
   The template's `main` already fails to compile against the library's `main`, so this
   fix is needed whatever happens to #96.
3. **Bump `SeriouslyCommonLibVersion` in the template** (currently `20261010.5`), and
   take #96 out of draft. CI then compiles it, and it can merge.

### 13.6 Starting the 2027 season repo

1. Create the repo from FRCRobotTemplate after step 3 above.
2. Add the season's mechanisms to `Hardware`, and tag each contract getter with
   `@ReadyWhen("<Mechanism>")`. Untagged getters are always reported.
3. Keep `CONTRACTS` in `electrical/report/Main.java` in step with the `ContractToUse`
   switch.
4. Merge to `main`; the workflow publishes the first snapshots.
5. In `electrical-contract-web/infra/cdk.json`, add
   `https://raw.githubusercontent.com/Team488/<2027 repo>/electrical-snapshots/main` to
   `snapshotSources` (comma-separated after TeamXbot2026's entry), then run `cdk deploy`.
   - The 2027 repo appears in the **Repository** picker on the next page load, with
     its robots under it. Their contract names come from that repo's `CONTRACTS`, and
     may repeat the 2026 names: robots are keyed by repo and contract (§3.1).
   - The 2026 repo's robots stay as they are, tagged **older repository**.
   - Each 2027 robot starts with wiring seeded from the 2027 repo's Java. Electrical
     reviews it (§8.1), or copies it over from the older repo's copy of the same
     robot.
   - Until profiles exist (§10), every robot uses the roboRIO port layout.
