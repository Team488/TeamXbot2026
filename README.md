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


### For Library Developers (Advanced Setup)
If you're working on changes to SeriouslyCommonLib and want to test them in this robot code:

```bash
# Clone both repositories side-by-side
git clone <this-repo-url> TeamXbot2026
git clone <library-repo-url> SeriouslyCommonLib

# Build with local library (add the flag to use local instead of Maven)
cd TeamXbot2026
./gradlew build -DuseLocalCommonLib=true
```

All changes to the local SeriouslyCommonLib are rebuilt automatically when using the flag.