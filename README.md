# Team 488 - 2026 Robot Project

[![Build Status](https://dev.azure.com/Team488/Team%20488%20Builds/_apis/build/status%2FTeam488.TeamXbot2026?branchName=main)](https://dev.azure.com/Team488/Team%20488%20Builds/_build/latest?definitionId=12&branchName=main)

![Robot Picture](./logo.jpg)

## Getting Started

### For Most Students (Simple Setup)
```bash
git clone <this-repo-url>
cd TeamXbot2026
./gradlew build
```

That's it! SeriouslyCommonLib will be automatically fetched from Maven.

### For Library Developers (Advanced Setup)
If you're working on changes to SeriouslyCommonLib and want to test them in this robot code:

```bash
# Clone both repositories side-by-side
git clone <this-repo-url> TeamXbot2026
git clone <library-repo-url> SeriouslyCommonLib

# Build - automatically uses local SeriouslyCommonLib
cd TeamXbot2026
./gradlew build
```

Gradle will automatically detect the local SeriouslyCommonLib and use it instead of Maven. All changes to the library are rebuilt automatically.

**To force Maven dependency** (even if local directory exists):
```bash
./gradlew build -DuseLocalCommonLib=false
```