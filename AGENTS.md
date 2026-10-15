# Desk Setup: agent notes

- `app/`: the web app (Vite + Three.js, frontend-only), built by the root `Dockerfile` and deployed to the minipc cluster.
- `archive/`: the old HTML and React versions; not built.

## Android client

- Lives in `android/` (Kotlin + Compose + SceneView). Build, test and install instructions: [`android/README.md`](android/README.md); feature decisions: [`android/NATIVE_EVALUATION.md`](android/NATIVE_EVALUATION.md).
- Fully offline: no API, no network permissions. Catalog, presets, prices and ergonomics rules are ported from `app/src/catalog.js` and `app/src/state.js`. **Change both sides together**, then regenerate the parity fixture (`node android/scripts/golden-parity.mjs . > android/app/src/test/resources/golden/web-parity.json`) and run `./gradlew testDebugUnitTest`.
- The JSON config format is shared: the app reads web exports and adds `deskWidth`/`deskDepth`, which the web importer ignores.
- `android/` is excluded from the web Docker build context (`.dockerignore`).
