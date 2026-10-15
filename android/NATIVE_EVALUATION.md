# Native Capability Evaluation: Desk Setup (DESKForge Studio)

- **Date**: 2026-10-15
- **Web app**: https://desk-setup.lan.codyuhi.online (frontend-only, static Vite + Three.js, no backend)
- **Primary users / devices**: Cody (Z Fold 8 Ultra), Cody's wife (S26+)
- **Ticket**: DESK-1

## How the app is used today
A desk-setup configurator. You pick a preset (Developer, Gamer, Minimalist) or start fresh, then size the desk (width × depth × height in cm), pick a finish, and add items from a 7-category catalog (monitors, PCs, peripherals, audio, lighting, decor). You can drag and rotate items in a 3D orbit view or a top-down 2D plan. A live budget and an ergonomic score update as you go. Results leave the app as a PNG snapshot, a JSON config (import and export) or a CSV shopping list. It's an occasional "plan my desk" tool, used on the couch more than at a desk.

## Web limitations that hurt
- **Nothing persists**: every reload resets to the Developer preset. Undo history and the current setup are lost.
- **Exports are downloads**: the PNG, JSON and CSV land in Downloads instead of going straight to Messages or Drive through the share sheet.
- **Touch is limited**: one-finger drag works, but there's no two-finger rotate for an item, and the gizmo handles are small on a phone.
- **Unfolded Fold layout is cramped**: the desktop layout gives the catalog and inspector sidebars too much width for a 752 dp screen.
- **No haptic feedback** when an item snaps, rotates or is deleted.

## Capability scores
| Capability | Value (0–3) | Cost (S/M/L) | Verdict | Notes |
| :--- | :---: | :---: | :--- | :--- |
| Offline-first (bundled catalog, zero network) | 3 | S | v1 | Catalog, presets and models compile into the APK, so no `INTERNET` permission is needed. |
| Auto-save current setup + saved-setups library (Room) | 3 | M | v1 | The biggest gain over the web app: reopen exactly where you left off, and keep named setups. |
| Foldable expanded canvas (72 dp rail, ≥85% canvas, flyout catalog/inspector) | 3 | M | v1 | Cover screen and S26+ use a bottom bar plus sheets, like the web mobile layout. |
| Multi-touch gestures: pinch zoom, two-finger orbit/pan, two-finger rotate of the selected item | 3 | M | v1 | This is the ticket's "3D touch gestures". It also applies to the 2D plan. |
| Camera orbit presets (perspective / iso / top / front) | 2 | S | v1 | Parity with the web app, plus animated transitions. |
| Snapshot PNG → Android Sharesheet | 3 | S | v1 | Rendered offscreen, shared through a `FileProvider`. No storage permission. |
| JSON config export/import (SAF) + share target / "Open with" for `.json` | 2 | S | v1 | Lets a setup move from the web app to the phone and back. |
| CSV shopping list → Sharesheet / SAF | 2 | S | v1 | Parity, with sharing added. |
| Haptics (snap, rotate 45°, delete, drag start) | 2 | S | v1 | No permission needed. |
| Budget + ergonomics parity test (vitest golden JSON) | 2 | S | v1 | Guarantees the Kotlin port prices and scores exactly like the web. |
| App shortcuts (New setup, Load preset) | 1 | S | later | Nice to have, low value for an occasional tool. |
| Flex Mode (tabletop: 3D above the hinge, inspector below) | 1 | M | later | Fun on the Fold, but the 3D view + rail already covers the inner screen. |
| Baseline profiles | 1 | S | later | Only worth it if cold start with the 3D engine feels slow. |
| S Pen precision drag on the 2D plan | 1 | S | no (free) | Works through the normal pointer input; no dedicated work. |
| Glance widget | 0 | M | no | Nothing time-sensitive to show. |
| Notifications | 0 | S | no | Nothing to notify about. |
| AR "place desk in my room" (ARCore) | 2 | L | no | Depends on Google Play Services for AR, which is Play-only. |
| Biometric lock | 0 | S | no | No sensitive data. |
| Photo Picker (room photo backdrop) | 1 | M | later | Could be a 2D-plan backdrop later. |

## Recommended v1 (beyond parity)
1. **Auto-save + saved setups library**: Room stores the working setup and its undo stack on every change, plus named saved setups. Adds a "My setups" list next to the presets. No permissions.
2. **Foldable expanded canvas**: on the inner screen the 3D/2D canvas gets about 680 of 752 dp, with a slim icon rail (Catalog, Desk, Stats, Menu) that opens flyouts. On compact screens a bottom bar opens sheets, matching the web mobile layout. No permissions.
3. **Multi-touch gestures**: two fingers orbit and pan the camera, pinch zooms, and with an item selected a two-finger twist rotates it (snapping to 15°). The same gestures work in the 2D plan. No permissions.
4. **Snapshot share**: Share opens the Android Sharesheet with the branded PNG (same title overlay as the web). No permissions.
5. **JSON import/export + "Open with Desk Setup"**: uses the Storage Access Framework, plus an intent filter for `application/json` shares. The format is identical to the web's, so files work in both directions. No permissions.
6. **CSV shopping list share**. No permissions.
7. **Haptics** on snap, rotate, delete and drag start. No permissions.
8. **Parity tests**: golden values from the web's `state.js` budget and ergonomics logic, dumped once with vitest and asserted in JUnit.

## Deferred (file tickets)
- App shortcuts (New setup / Load preset): low value (1).
- Flex Mode tabletop layout: low value (1); revisit after v1 is in use.
- Baseline profiles: only if cold start feels slow.
- Room photo backdrop for the 2D plan (Photo Picker): low value (1).

## Rejected
- AR placement: needs ARCore (Play Services for AR), which isn't allowed for sideloaded homelab apps.
- Widget, notifications, biometric lock: nothing to show, notify about or protect.

## Permissions requested in v1
- **None.** No `INTERNET`, no `ACCESS_LOCAL_NETWORK`, no storage permissions (Sharesheet, FileProvider and SAF need none).

## Open decisions
1. **3D renderer**: the web models are entirely procedural Three.js primitives (32 boxes, 17 cylinders, 12 planes, 3 spheres, 1 torus, 1 cone, roughly 680 lines across 20 model builders).
   - **A. Native Filament via SceneView (recommended)**: port each builder to Kotlin with primitive nodes. A truly native app, no JS runtime, best gesture integration. Cost L (about a day). The torus and cone need small custom meshes, and lighting will look close to the web but not pixel-identical.
   - **B. Bundled offline WebView running the existing `scene3d.js`**: perfect visual parity and low cost (M). It's a hybrid with two runtimes, harder to test, and doesn't fit the Kotlin-only and native gesture goals.
2. **Estimate**: DESK-1 is estimated at 2.5h. Full parity plus the v1 list with option A is realistically about 2–3 days. Re-estimate to 16h, or split it (scaffold + 2D plan + catalog first, 3D in a follow-up ticket)?
3. **Behavior change**: the app opens on the last auto-saved setup instead of always resetting to the Developer preset (the presets stay one tap away).
