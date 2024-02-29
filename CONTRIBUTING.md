RUN & CONTRIBUTING
---

#### Run the Project

The application performs operations related to media files through a [submodule](https://github.com/emreesen27/media-store).

Clone the project:
```
git clone
```

Run to fetch and update the submodule:
```
git submodule update --init
```

#### Contribute

At the commit stage, I run commit-msg and lint checks. Committing automatically triggers the lint task. If you encounter any issues, manually trigger it.

```
./gradlew ktlintFormat
```
Ensure your commit message follows this format:
```
[DEV]||[FIX] [commit message]
```
