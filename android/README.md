# Desk Setup (DESKForge Studio) for Android

Native Android client for the Desk Setup web app (Kotlin, Jetpack Compose, Material 3 themed with Cedar). It is the same configurator as `app/`: same catalog, presets, prices, ergonomics rules and JSON config format, with a native 3D renderer, multi-touch gestures, auto-save and a saved-setups library. It works fully offline and requests **no** network permissions. See [`NATIVE_EVALUATION.md`](./NATIVE_EVALUATION.md).

## Architecture

| Layer | Contents |
| :--- | :--- |
| `domain/` | Pure Kotlin ports of the web app: `Catalog.kt` (catalog.js), `DeskSetup.kt` (state.js rules: clamps, budget, ergonomics), `SetupHistory.kt` (undo/redo), `Exports.kt` (JSON config + CSV), `Models.kt` (the 20 procedural models from proceduralModels.js, in metres with Three.js Euler angles), `OrbitCamera.kt` + `Picking.kt` (camera and touch hit-testing) |
| `data/` | Room (`desksetup.db`: the working setup with its undo stack, and saved setups) and DataStore settings (theme, 3D/2D view) |
| `ui/scene/` | `DeskScene3D` renders the models with SceneView 4.56 (Filament); `Scene3DCanvas` and `Blueprint2D` handle gestures through one shared gesture loop |
| `ui/studio/` | `StudioViewModel` (state, auto-save, camera), `StudioScreen` (adaptive shell), panels, share-sheet exports |

Gestures: tap selects; drag a **selected** item to move it along the desktop; long-press grabs any item; one finger elsewhere orbits; two fingers pinch-zoom and pan; a two-finger twist rotates the selected item in 15° steps with a haptic tick, or orbits when nothing is selected. In 2D, drag any item directly and drag empty space to pan. Every drag, twist or slider move is one undo step, and the setup auto-saves to Room (250 ms debounce, plus on `onStop`).

Parity with the web is tested: `GoldenParityTest` checks budget, ergonomics, presets and clamps against values produced by the web's own `app/src/state.js`, and `SceneMathTest` checks that SceneView orients every part exactly as Three.js did.

## Build and test

```bash
cd android
./gradlew lintDebug testDebugUnitTest            # lint (warnings are errors) + unit tests
./gradlew assembleDebug                          # local debug key
./gradlew assembleRelease -PappVersionName=1.0.0 # R8-minified; signed only when the ANDROID_KEYSTORE_* env vars are set
```

Requirements: JDK 21 and the Android SDK (`local.properties` with `sdk.dir`, git-ignored). The Gradle wrapper pins Gradle 9.8.1 with a checksum. Native code is built for `arm64-v8a` only (both phones and Apple-silicon emulators).

## Release and install

Releases are built and signed by the homelab Argo Workflows `android-build-apk` template and stored in Harbor as `harbor.minipc.local/apps/desk-setup-android:<version>`:

```bash
argo submit --from workflowtemplate/android-build-apk -n argo-workflows -p repo-name=desk-setup -p version=1.0.0 --watch
python3 ~/dev/repos/skills/skills/create-android-app/scripts/install_apk.py desk-setup 1.0.0
# local debug build instead:
python3 ~/dev/repos/skills/skills/create-android-app/scripts/install_apk.py --apk app/build/outputs/apk/debug/app-debug.apk
```

Debug and release builds share one application ID (`online.codyuhi.desksetup`), so the phone only ever has one Desk Setup. Switching between debug and release changes the signing key, which wipes the app's local data (saved setups included).

## Device notes

| Device | Screen | Window | Layout |
| :--- | :--- | :--- | :--- |
| Galaxy Z Fold 8 Ultra, folded (cover) | 1080×2520 px @ 480 dpi | 360×840 dp, compact | Top bar, full canvas, bottom bar opening sheets |
| Galaxy Z Fold 8 Ultra, unfolded | 2256×2504 px @ 480 dpi | 752×834 dp, medium | 72 dp icon rail; the canvas fills the rest; panels open as dismissible flyouts |
| Galaxy S26+ | not measured yet | compact | Same as the cover screen |

Portrait canvases widen the 3D field of view (45° → up to 60°) and pull the camera back so the whole desk fits; folding or unfolding keeps the setup, selection and a moved camera.

## Regenerating generated sources

- Icons: `LUCIDE_VERSION=1.27.0 node scripts/lucide2kt.mjs ../app/node_modules/lucide/dist/esm/icons <names…> > app/src/main/kotlin/online/codyuhi/desksetup/ui/components/Lucide.kt`.
- Parity fixture: `node scripts/golden-parity.mjs .. > app/src/test/resources/golden/web-parity.json` (runs the web's `state.js`; regenerate when web pricing or ergonomics rules change).

## Licences

Fonts (Source Serif 4, Inter, JetBrains Mono) are SIL OFL 1.1; licence texts are in `third_party/`. Icons are Lucide (ISC). SceneView and Filament are Apache 2.0.
