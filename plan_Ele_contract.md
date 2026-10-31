# Electrical Contract Tool: Design

How the system works once all phases are done. Anything not built yet is marked
**(not built)** and listed in §11. The history of how the design got here is in
`plan_electrical_web.md`.

Live site: https://team488.github.io/electrical-contract-web/
Code: `Team488/electrical-contract-web` (web tool) and this repo (Java contract and exporter).

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
TeamXbot2026 (Java contract)
   │  ./gradlew electricalReport --args="--json"   → snapshot.json
   │  GitHub Action on push to main  (not built; done by hand today)
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
- **Snapshot import.** The importer matches code devices by `(bus, CAN ID)` or by
  DIO/PWM channel, never by name. It adds new devices and flags ones removed from the
  code.
- **First import only:** the electrical data is seeded from the contract's report-only
  methods (§8.1). After that, imports never overwrite web-owned data, and differences show up on the Checks tab.
- **Contract cleanup (not built):** once the web is the source, remove the report-only
  methods from the contract: `getCanBusConnectionOrder`,
  `getAdditionalPDHConnections`, `getAdditionalPowerBranches` and `getVrm1_*`. The PDH
  port field in SeriouslyCommonLib stays, and the robot code ignores it.

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
- Each robot or season is its own `ROBOT#<n>` partition. Only the 2026 robot exists
  today.

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

The live system holds the 2026 competition robot (`ROBOT#1`). To start a robot:

1. **Create the robot.**
   - The API does this automatically for robot 1 on its very first start. It creates
     the robot record (name `2026 Competition`), 24 empty PDH ports (`PDH00`–`PDH23`)
     and the 8 VRM1 outputs.
   - Another robot or season needs a robot record with a new number, `ROBOT#2` and so
     on, and a **controller profile** (§10). The profile decides which ports get
     created: PDH channels, VRM outputs if any, and the SmartIO and USB slots. **(not built:** today the name and ports are hard-coded in `api/src/table.ts`,
     and the page always shows robot 1 (`ROBOT_ID` in `web/config.js`).**)**
2. **Point the exporter at the right contract.** `electrical/report/Main.java`
   `createContract()` returns `Contract2026`; change it for a new season's contract.
   **(not built:** choosing the contract per robot.**)**
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
4. **The snapshot reaches the web.** Today Programming runs the export and import by
   hand (§12.3). **(not built:** a GitHub Action that does it on every push to
   `main`.**)**
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
  packages move to `org.wpilib`. The exporter is ported with it.
- The exporter stops guessing I/O types from names. Today it treats method names with
  "servo"/"pwm", device names ending in "DIO", or devices powered from the RIO as I/O
  devices.
- Instead, SeriouslyCommonLib's `DeviceInfo` gets an explicit **port type and mode**.
  That's a shared-library change to agree with the library's maintainers.
- `busName()` uses the bus's own name instead of only `Canivore` and `rio`.

**Known carry-over issue:** the 2026 hood servos (PWM 7 and 9) can't move to
SystemCore as they are. Servos aren't supported, and SmartIO has only ports 0–5. That's
a hardware choice for Electrical before the 2027 contract is written.

## 11. Not built yet

| Item | Owner | Phase | Notes |
| --- | --- | --- | --- |
| GitHub Action in TeamXbot2026 that posts the snapshot on every push to `main` | Programming | 3 | Today: run the exporter and import by hand (§12.3). Needs the `SNAPSHOT_KEY` repo secret |
| GitHub Action that runs `cdk deploy` on push to `electrical-contract-web` `main` | Programming | 3 | The OIDC deploy role `electrical-contract-github-deploy` already exists |
| Nightly JSON backup of the data, committed to git | Programming | 3 | DynamoDB point-in-time recovery (35 days) already covers restores |
| Copy wiring and chain position to the new device when a CAN ID or port changes (§9.1) | Programming | 3 | Today Electrical re-enters them by hand |
| Link a device newly added in Java to a matching web-only device by name (§9.1) | Programming | 3 | Today Electrical deletes the duplicate by hand |
| Close a proposal automatically when the snapshot shows the new value | Programming | 3 | Today Programming marks it done |
| Pilot on the 2026 robot, then full switch to the web | Electrical | 4 | |
| Contract cleanup (§9.2) | Programming | 4 | |
| Controller profiles, and starting another robot or season (§8.1, §10) | Programming | 2027 prep | Robot name, ports and `ROBOT_ID` are hard-coded for robot 1; the exporter is hard-coded to `Contract2026` |
| SystemCore I/O model: `smartio` kind with mode, SmartIO tab, CAN-FD bus names (§10) | Programming | 2027 prep | |
| SystemCore checks and report layout: ports 0–5, modes, 3.3 V rail and USB current (§10) | Programming | 2027 prep | |
| Current-draw field per device | Programming | 2027 prep | Electrical fills in the values |
| Explicit port type and mode in SeriouslyCommonLib `DeviceInfo`; port the exporter to the 2027 libraries (§10) | Programming | 2027 prep | Agree with the library maintainers |
| Replace the hood servos, which have no SystemCore support (§10) | Electrical | 2027 prep | Programming updates the 2027 contract to match |
| Check the SystemCore profile's port lists, connectors and pull direction against the final 2027 docs | Electrical | 2027 prep | Specs so far are from alpha hardware |
| Photos (S3), Cognito logins | Programming | Later | |
| Budget alert | Programming | Open | Left off because the account is shared with the scouting app |
| Update the account's CDK bootstrap stack (older than version 21) | Programming | Open | Shared with the scouting app; check with its owners first |
| Fix the wire-gauge table against the game manual | Electrical | Open | `web/validate.js` |
| Remove the stale CAN ID 59 from `getCanBusConnectionOrder` | Programming | Open | Java data fix |

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

Until the phase 3 Action exists, run these after merging contract changes:

```powershell
cd TeamXbot2026
./gradlew electricalReport --args="--json -f snapshot.json"
$env:SNAPSHOT_KEY = aws ssm get-parameter --with-decryption --name /electrical-contract/SNAPSHOT_KEY --query Parameter.Value --output text --region us-west-2
node ..\electrical-contract-web\scripts\import-snapshot.mjs snapshot.json https://iqzxovxrrtmrmj7luwdtc457c40lyehm.lambda-url.us-west-2.on.aws
```

`./gradlew electricalReport` with no options still writes `electrical-report.txt`.

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

Needs Node 22+, Python 3 and Java 17+ (the WPILib JDK is found automatically).

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
