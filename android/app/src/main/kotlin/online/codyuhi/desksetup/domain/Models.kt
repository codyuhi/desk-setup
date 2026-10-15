package online.codyuhi.desksetup.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Procedural 3D models, ported from app/src/models/proceduralModels.js. Units are metres, the
// origin sits on the desk surface (or the floor for the desk itself), and rotations are Three.js
// 'XYZ' Euler radians, so numbers can be copied straight from the web builders.

/** Primitive shapes, all centred on their local origin with Y as the main axis. */
sealed interface Shape {
    data class Box(val w: Double, val h: Double, val d: Double) : Shape

    /** Three's tapered cylinders become one radius (the mean) in the native renderer. */
    data class Cylinder(val radiusTop: Double, val radiusBottom: Double, val h: Double) : Shape {
        val radius get() = (radiusTop + radiusBottom) / 2
    }

    data class Sphere(val r: Double) : Shape

    /** Apex at +Y, like THREE.ConeGeometry. */
    data class Cone(val r: Double, val h: Double) : Shape

    /** Lies in the XZ plane (axis Y), unlike THREE.TorusGeometry, which lies in XY. */
    data class Torus(val major: Double, val minor: Double) : Shape
}

data class Material(
    val color: Long,
    val roughness: Double = 0.5,
    val metalness: Double = 0.0,
    val opacity: Double = 1.0,
    val emissive: Long = 0,
    val emissiveIntensity: Double = 0.0,
)

sealed interface ModelNode {
    val position: Vec3
    val rotation: Vec3
}

data class Part(
    val shape: Shape,
    val material: Material,
    override val position: Vec3 = Vec3.ZERO,
    override val rotation: Vec3 = Vec3.ZERO,
    val scale: Vec3 = Vec3.ONE,
) : ModelNode

data class Group(
    val children: List<ModelNode>,
    override val position: Vec3 = Vec3.ZERO,
    override val rotation: Vec3 = Vec3.ZERO,
) : ModelNode

private const val SCALE = 0.01 // cm → m

private fun v(x: Number, y: Number, z: Number) = Vec3(x.toDouble(), y.toDouble(), z.toDouble())
private fun box(w: Double, h: Double, d: Double, m: Material, pos: Vec3 = Vec3.ZERO, rot: Vec3 = Vec3.ZERO) =
    Part(Shape.Box(w, h, d), m, pos, rot)
private fun cyl(rt: Double, rb: Double, h: Double, m: Material, pos: Vec3 = Vec3.ZERO, rot: Vec3 = Vec3.ZERO) =
    Part(Shape.Cylinder(rt, rb, h), m, pos, rot)

/** THREE.PlaneGeometry is a zero-thickness quad facing +Z; a 2 mm slab renders the same from the front. */
private fun plane(w: Double, h: Double, m: Material, pos: Vec3 = Vec3.ZERO, rot: Vec3 = Vec3.ZERO) =
    Part(Shape.Box(w, h, 0.002), m, pos, rot)

object Models {
    fun deskMaterial(finishKey: String): Material = Catalog.finish(finishKey).let {
        Material(it.color, it.roughness.toDouble(), it.metalness.toDouble(), it.opacity.toDouble())
    }

    /** The standing desk (frame + top), built from the setup's size and finish. */
    fun desk(widthCm: Int, depthCm: Int, heightCm: Int, finishKey: String): Group {
        val w = widthCm * SCALE
        val d = depthCm * SCALE
        val h = heightCm * SCALE
        val t = 0.035
        val edge = Material(0xFF334155, roughness = 0.5)
        val metal = Material(0xFF334155, roughness = 0.3, metalness = 0.7)
        val legR = 0.035
        val legH = h - t
        return Group(
            listOf(
                box(w, t, d, deskMaterial(finishKey), v(0, h - t / 2, 0)),
                box(w + 0.004, t * 0.4, d + 0.004, edge, v(0, h - t / 2, 0)),
                box(legR * 2, legH, legR * 2.5, metal, v(-w * 0.35, legH / 2, 0)),
                box(legR * 2, legH, legR * 2.5, metal, v(w * 0.35, legH / 2, 0)),
                box(0.08, 0.025, d * 0.85, metal, v(-w * 0.35, 0.0125, 0)),
                box(0.08, 0.025, d * 0.85, metal, v(w * 0.35, 0.0125, 0)),
                box(w * 0.7, 0.03, 0.04, metal, v(0, h - t - 0.02, 0)),
            ),
        )
    }

    /** Item model in desk-surface coordinates (origin on the desktop at the item's position). */
    fun item(item: CatalogItem): Group {
        val w = item.dimensions.width * SCALE
        val d = item.dimensions.depth * SCALE
        val h = item.dimensions.height * SCALE
        return Group(
            when (item.model) {
                ModelType.MonitorUltrawide -> monitor(w, h, 0.15, 0.03, 0.015, Material(0xFF27272A, 0.3, 0.5),
                    Material(0xFF1E293B, 0.1, 0.8, emissive = 0xFF0EA5E9, emissiveIntensity = 0.25)) + listOf(
                    cyl(0.015, 0.02, h * 0.8, METAL_LIGHT, v(0, h * 0.35, -0.04)),
                    box(w * 0.35, 0.01, 0.22, METAL_LIGHT, v(0, 0.005, 0)),
                )
                ModelType.MonitorDual27 -> dual27(w / 2, h)
                ModelType.MonitorVertical -> monitor(w, h, 0.1, 0.025, 0.012, Material(0xFF27272A, 0.4),
                    Material(0xFF1E293B, emissive = 0xFF0EA5E9, emissiveIntensity = 0.25)) + listOf(
                    cyl(0.016, 0.018, h * 0.7, METAL_LIGHT, v(0, h * 0.35, -0.04)),
                    box(0.2, 0.01, 0.18, METAL_LIGHT, v(0, 0.005, 0)),
                )
                ModelType.MonitorSuperUltrawide -> monitor(w, h, 0.15, 0.03, 0.02, Material(0xFF18181B, 0.3, 0.7),
                    Material(0xFF0F172A, emissive = 0xFF38BDF8, emissiveIntensity = 0.3)) + listOf(
                    box(0.04, h * 0.7, 0.04, METAL_LIGHT, v(0, h * 0.35, -0.05)),
                    box(w * 0.3, 0.012, 0.24, METAL_LIGHT, v(0, 0.006, 0)),
                )
                ModelType.PcTowerRgb -> pcTower(w, d, h)
                ModelType.MacStudio -> listOf(
                    box(w, h, w, Material(0xFFE2E8F0, 0.2, 0.85), v(0, h / 2, 0)),
                    cyl(w * 0.45, w * 0.45, 0.008, Material(0xFF334155, 0.5), v(0, 0.004, 0)),
                )
                ModelType.LaptopOnStand -> laptop(w, d)
                ModelType.KeyboardMech -> keyboard(w, d, h)
                ModelType.DeskPad -> listOf(box(w, h, d, Material(0xFF475569, 0.8), v(0, h / 2, 0)))
                ModelType.MouseErgo -> listOf(
                    box(w, h, d, Material(0xFF27272A, 0.3, 0.3), v(0, h / 2, 0)),
                    cyl(0.004, 0.004, 0.012, Material(0xFFCBD5E1, 0.2, 0.9), v(0, h + 0.002, -d * 0.25), v(0, 0, PI / 2)),
                )
                ModelType.SpeakersPair -> speakers(item.dimensions.width / 2 * SCALE, d, h)
                ModelType.HeadphonesStand -> headphones(h)
                ModelType.MonitorLightBar -> listOf(
                    cyl(0.008, 0.008, w, Material(0xFF334155, 0.3, 0.8), v(0, 0.48, -0.02), v(0, 0, PI / 2)),
                )
                ModelType.ArchitectLamp -> lamp()
                // The strip is only a coloured light in the web scene; a thin glowing bar stands in for it here.
                ModelType.RgbLedStrip -> listOf(
                    box(w, 0.008, 0.01, Material(0xFFC084FC, emissive = 0xFFC084FC, emissiveIntensity = 1.5), v(0, 0.004, 0)),
                )
                ModelType.PottedPlant -> plant(item.dimensions.width / 2 * SCALE, item.dimensions.height * 0.4 * SCALE)
                ModelType.PegboardWall -> listOf(
                    box(w, h, 0.015, Material(0xFFF8FAFC, 0.6), v(0, h / 2 + 0.5, -0.42)),
                    box(w * 0.5, 0.015, 0.08, Material(0xFFE2E8F0, 0.4), v(0, h * 0.4 + 0.5, -0.38)),
                )
                ModelType.CoffeeMug -> {
                    val r = item.dimensions.width / 2 * SCALE
                    listOf(
                        cyl(r, r * 0.85, h, Material(0xFF334155, 0.4), v(0, h / 2, 0)),
                        cyl(r * 0.88, r * 0.88, 0.005, Material(0xFF451A03, 0.1), v(0, h * 0.85, 0)),
                    )
                }
                ModelType.DeskBaseSetter -> listOf(box(w, h, d, Material(0xFF64748B, 0.5), v(0, h / 2, 0)))
            },
        )
    }

    /**
     * Vertical lift applied when placing an item: the pegboard hangs on the wall in the web scene
     * (y + 0.35 m, z fixed at -0.42 m), everything else sits on the desktop.
     */
    fun placement(item: CatalogItem, xCm: Double, zCm: Double, deskTopM: Double): Vec3 =
        if (item.model == ModelType.PegboardWall) Vec3(xCm * SCALE, deskTopM + 0.35, -0.42)
        else Vec3(xCm * SCALE, deskTopM, zCm * SCALE)

    /** Bounds of a model in its own space (approximating round shapes by their boxes). */
    fun bounds(group: Group): Aabb = boundsOf(group, Mat3.IDENTITY, Vec3.ZERO)

    private fun boundsOf(node: ModelNode, parentRot: Mat3, parentPos: Vec3): Aabb {
        val rot = parentRot * Mat3.euler(node.rotation)
        val pos = parentPos + parentRot * node.position
        return when (node) {
            is Group -> node.children.map { boundsOf(it, rot, pos) }.reduce(Aabb::union)
            is Part -> Aabb.of(localBox(node.shape).corners().map { pos + rot * it.hadamard(node.scale) })
        }
    }

    private fun localBox(s: Shape): Aabb {
        val half = when (s) {
            is Shape.Box -> Vec3(s.w / 2, s.h / 2, s.d / 2)
            is Shape.Cylinder -> Vec3(maxOf(s.radiusTop, s.radiusBottom), s.h / 2, maxOf(s.radiusTop, s.radiusBottom))
            is Shape.Sphere -> Vec3(s.r, s.r, s.r)
            is Shape.Cone -> Vec3(s.r, s.h / 2, s.r)
            is Shape.Torus -> Vec3(s.major + s.minor, s.minor, s.major + s.minor)
        }
        return Aabb(half * -1.0, half)
    }

    private val METAL_LIGHT = Material(0xFF64748B, 0.2, 0.9)

    private fun monitor(w: Double, h: Double, lift: Double, caseD: Double, bezel: Double, case: Material, screen: Material) = listOf(
        box(w, h, caseD, case, v(0, h / 2 + lift, 0)),
        plane(w - bezel, h - bezel, screen, v(0, h / 2 + lift, caseD / 2 + 0.001)),
    )

    private fun dual27(w: Double, h: Double): List<ModelNode> {
        val plastic = Material(0xFF27272A, 0.4)
        val screen = Material(0xFF1E293B, emissive = 0xFF0284C7, emissiveIntensity = 0.3)
        fun side(sign: Int) = Group(
            listOf(box(w, h, 0.025, plastic), plane(w - 0.01, h - 0.01, screen, v(0, 0, 0.013))),
            position = v(sign * w * 0.52, h / 2 + 0.15, 0),
            rotation = v(0, -sign * 0.15, 0),
        )
        return listOf(
            side(-1),
            side(1),
            cyl(0.02, 0.02, h * 0.9, Material(0xFF475569, 0.3, 0.8), v(0, h * 0.45, -0.06)),
        )
    }

    private fun pcTower(w: Double, d: Double, h: Double): List<ModelNode> {
        val rgb = Material(0xFF00E5FF, emissive = 0xFF00E5FF, emissiveIntensity = 2.5)
        return listOf(
            box(w, h, d, Material(0xFF27272A, 0.3, 0.7), v(0, h / 2, 0)),
            plane(d - 0.04, h - 0.04, Material(0xFF38BDF8, 0.05, 0.9, opacity = 0.35), v(-w / 2 - 0.002, h / 2, 0), v(0, -PI / 2, 0)),
        ) + (0 until 3).map { i ->
            // Fan rings face the front (+Z): rotate the XZ-plane torus up into XY.
            Part(Shape.Torus(0.05, 0.006), rgb, v(0, h * 0.25 + i * 0.12, d / 2 + 0.001), v(PI / 2, 0, 0))
        }
    }

    private fun laptop(w: Double, d: Double): List<ModelNode> {
        val alum = Material(0xFFCBD5E1, 0.2, 0.85)
        val stand = Material(0xFF64748B, 0.2, 0.9)
        val screenH = d * 0.8
        val lid = Group(
            listOf(
                box(w, screenH, 0.006, alum, v(0, screenH / 2, 0)),
                plane(w - 0.02, screenH - 0.02, Material(0xFF0F172A, emissive = 0xFF38BDF8, emissiveIntensity = 0.3), v(0, screenH / 2, 0.004)),
            ),
            position = v(0, 0.012, -d / 2),
            rotation = v(-0.3, 0, 0),
        )
        return listOf(
            Group(
                listOf(
                    box(0.02, 0.09, d * 0.8, stand, v(-w * 0.35, -0.045, 0)),
                    box(0.02, 0.09, d * 0.8, stand, v(w * 0.35, -0.045, 0)),
                    box(w, 0.012, d, alum, v(0, 0.006, 0)),
                    plane(w * 0.88, d * 0.52, Material(0xFF334155, 0.6), v(0, 0.013, -d * 0.12), v(-PI / 2, 0, 0)),
                    plane(w * 0.32, d * 0.28, alum, v(0, 0.013, d * 0.28), v(-PI / 2, 0, 0)),
                    lid,
                ),
                position = v(0, 0.09, 0),
                rotation = v(0.2, 0, 0),
            ),
        )
    }

    private fun keyboard(w: Double, d: Double, h: Double): List<ModelNode> {
        val key = Material(0xFFF8FAFC, 0.4)
        val accent = Material(0xFFF97316, 0.4)
        val rows = 5
        val cols = 14
        val kw = w * 0.9 / cols
        val kd = d * 0.85 / rows
        val keys = (0 until rows).flatMap { r ->
            (0 until cols).map { c ->
                val isAccent = (r == 0 && c == 0) || (r == 2 && c == cols - 1)
                box(kw * 0.88, 0.008, kd * 0.88, if (isAccent) accent else key, v(-w * 0.42 + c * kw, h + 0.004, -d * 0.38 + r * kd))
            }
        }
        return listOf(box(w, h, d, Material(0xFF3F3F46, 0.3, 0.5), v(0, h / 2, 0))) + keys
    }

    private fun speakers(halfSpread: Double, d: Double, h: Double): List<ModelNode> {
        val spk = 0.16
        val cabinet = Material(0xFF27272A, 0.5)
        val cone = Material(0xFFEAB308, 0.3, 0.6)
        return listOf(-1, 1).map { sign ->
            Group(
                listOf(
                    box(spk, h, d, cabinet, v(0, h / 2, 0)),
                    cyl(spk * 0.35, spk * 0.35, 0.01, cone, v(0, h * 0.45, d / 2 + 0.001), v(PI / 2, 0, 0)),
                ),
                position = v(sign * halfSpread, 0, 0),
            )
        }
    }

    private fun headphones(h: Double): List<ModelNode> {
        val pad = Material(0xFF334155, 0.8)
        return listOf(
            cyl(0.06, 0.07, h, Material(0xFF78350F, 0.5), v(0, h / 2, 0)),
            Group(
                listOf(
                    cyl(0.04, 0.04, 0.03, pad, v(-0.07, 0, 0), v(0, 0, PI / 2)),
                    cyl(0.04, 0.04, 0.03, pad, v(0.07, 0, 0), v(0, 0, PI / 2)),
                ),
                position = v(0, h * 0.75, 0),
            ),
        )
    }

    private fun lamp(): List<ModelNode> {
        val metal = Material(0xFF384252, 0.3, 0.8)
        return listOf(
            cyl(0.08, 0.08, 0.015, metal, v(0, 0.0075, 0)),
            cyl(0.006, 0.006, 0.28, metal, v(0.06, 0.13, 0.06), v(0.3, 0, -0.4)),
            Part(Shape.Sphere(0.014), metal, v(0.12, 0.25, 0.12)),
            cyl(0.006, 0.006, 0.26, metal, v(0.18, 0.35, 0.18), v(0.4, 0, -0.6)),
            Group(
                listOf(
                    Part(Shape.Cone(0.08, 0.12), Material(0xFF1E293B, 0.4), v(0, -0.06, 0)),
                    Part(Shape.Sphere(0.02), Material(0xFFFFF4D6, emissive = 0xFFFFF4D6, emissiveIntensity = 1.0), v(0, -0.04, 0)),
                ),
                position = v(0.24, 0.42, 0.24),
                rotation = v(0.8, -0.6, 0.5),
            ),
        )
    }

    private fun plant(potR: Double, potH: Double): List<ModelNode> {
        val leaf = Material(0xFF16A34A, 0.5)
        return listOf(
            cyl(potR, potR * 0.75, potH, Material(0xFFF8FAFC, 0.3), v(0, potH / 2, 0)),
            cyl(potR * 0.9, potR * 0.9, 0.01, Material(0xFF331F10, 0.9), v(0, potH, 0)),
        ) + (0 until 5).map { i ->
            val angle = i / 5.0 * PI * 2
            Part(Shape.Sphere(0.08), leaf, v(cos(angle) * 0.07, potH + 0.12, sin(angle) * 0.07), v(0.3, angle, 0.4), v(1.2, 0.1, 1.5))
        }
    }
}
