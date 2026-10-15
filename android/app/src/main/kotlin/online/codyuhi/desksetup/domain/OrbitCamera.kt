package online.codyuhi.desksetup.domain

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Orbit camera equivalent to the web's OrbitControls setup: 45° vertical field of view, distance
 * 0.8–6 m, never below the floor (maxPolarAngle = π/2 − 0.02). Angles are radians; azimuth 0
 * looks from +Z (the user's side of the desk) towards the wall.
 */
data class OrbitCamera(
    val target: Vec3 = Vec3(0.0, 0.75, 0.0),
    val azimuth: Double = 0.0,
    val elevation: Double = 0.0,
    val distance: Double = 3.0,
    val fovYDegrees: Double = FOV_Y_DEGREES,
) {
    private val fovY get() = fovYDegrees * PI / 180

    val eye: Vec3
        get() = target + Vec3(cos(elevation) * sin(azimuth), sin(elevation), cos(elevation) * cos(azimuth)) * distance

    fun orbit(dAzimuth: Double, dElevation: Double) =
        copy(azimuth = azimuth + dAzimuth, elevation = (elevation + dElevation).coerceIn(MIN_ELEVATION, MAX_ELEVATION))

    fun zoom(factor: Double) = copy(distance = (distance / factor).coerceIn(MIN_DISTANCE, MAX_DISTANCE))

    /** Pan in the camera's screen plane; [dx]/[dy] are fractions of the viewport height. */
    fun pan(dx: Double, dy: Double): OrbitCamera {
        val (right, up) = basis().let { it.first to it.second }
        val scale = 2 * distance * tan(fovY / 2)
        val moved = target - right * (dx * scale) + up * (dy * scale)
        return copy(target = Vec3(moved.x.coerceIn(-3.0, 3.0), moved.y.coerceIn(0.0, 2.5), moved.z.coerceIn(-3.0, 3.0)))
    }

    /** Right, up and forward unit vectors. */
    fun basis(): Triple<Vec3, Vec3, Vec3> {
        val forward = (target - eye).normalized()
        val right = forward.cross(Vec3(0.0, 1.0, 0.0)).normalized()
        val up = right.cross(forward)
        return Triple(right, up, forward)
    }

    /** World-space ray through pixel ([px], [py]) of a [width]×[height] viewport. */
    fun ray(px: Double, py: Double, width: Double, height: Double): Pair<Vec3, Vec3> {
        val (right, up, forward) = basis()
        val ndcX = 2 * px / width - 1
        val ndcY = 1 - 2 * py / height
        val t = tan(fovY / 2)
        val dir = (forward + right * (ndcX * t * width / height) + up * (ndcY * t)).normalized()
        return eye to dir
    }

    companion object {
        const val FOV_Y_DEGREES = 45.0
        const val MIN_DISTANCE = 0.8
        const val MAX_DISTANCE = 6.0
        const val MIN_ELEVATION = 0.02
        val MAX_ELEVATION = PI / 2 - 0.003

        /**
         * Portrait canvases (phones, the Fold cover screen) widen the vertical field of view from 45°
         * up to 60° so the desk isn't cropped at the sides; landscape keeps the web's 45°.
         */
        fun fovFor(aspect: Double): Double =
            if (aspect >= 1.0) FOV_Y_DEGREES else FOV_Y_DEGREES + (60 - FOV_Y_DEGREES) * ((1 - aspect) / 0.5).coerceIn(0.0, 1.0)

        /** Distance at which [halfWidth] metres either side of the target fill the view horizontally. */
        fun fitDistance(aspect: Double, fovYDegrees: Double, halfWidth: Double = 0.95): Double =
            halfWidth / (tan(fovYDegrees * PI / 360) * aspect)

        /** Builds the camera that sits at [eye] looking at [target], like `camera.position.set` in the web. */
        fun lookingAt(eye: Vec3, target: Vec3, aspect: Double = 16.0 / 9, halfWidth: Double = 0.95): OrbitCamera {
            val off = eye - target
            val fov = fovFor(aspect)
            val dist = maxOf(off.length(), fitDistance(aspect, fov, halfWidth))
            return OrbitCamera(
                target = target,
                azimuth = atan2(off.x, off.z),
                elevation = asin((off.y / off.length()).coerceIn(-1.0, 1.0)).coerceIn(MIN_ELEVATION, MAX_ELEVATION),
                distance = dist.coerceIn(MIN_DISTANCE, MAX_DISTANCE),
                fovYDegrees = fov,
            )
        }

        /** The web's four camera buttons (scene3d.js setCameraView), pulled back on narrow screens. */
        fun preset(view: CameraView, deskHeightCm: Int, aspect: Double = 16.0 / 9, deskWidthCm: Int = 160): OrbitCamera {
            val ty = deskHeightCm * 0.01 / 2 + 0.4
            val target = Vec3(0.0, ty, 0.0)
            val eye = when (view) {
                CameraView.Top -> Vec3(0.0, 3.2, 0.01)
                CameraView.Front -> Vec3(0.0, ty + 0.1, 2.2)
                CameraView.Isometric -> Vec3(2.2, 2.0, 2.2)
                CameraView.Perspective -> Vec3(0.0, 1.8, 2.5)
            }
            return lookingAt(eye, target, aspect, halfWidthFor(deskWidthCm))
        }

        /** Initial view: the web starts at (0, 1.8, 2.5) looking at (0, 0.75, 0). */
        fun initial(aspect: Double = 16.0 / 9, deskWidthCm: Int = 160): OrbitCamera =
            lookingAt(Vec3(0.0, 1.8, 2.5), Vec3(0.0, 0.75, 0.0), aspect, halfWidthFor(deskWidthCm))

        /** Half the desk plus 15 cm of margin either side (0.95 m for the standard 160 cm desk). */
        fun halfWidthFor(deskWidthCm: Int) = deskWidthCm / 200.0 + 0.15
    }
}

enum class CameraView { Perspective, Isometric, Top, Front }

/** Intersects a ray with the horizontal plane y = [planeY]; null when parallel or behind. */
fun rayPlaneY(origin: Vec3, dir: Vec3, planeY: Double): Vec3? {
    if (kotlin.math.abs(dir.y) < 1e-9) return null
    val t = (planeY - origin.y) / dir.y
    return if (t < 0) null else origin + dir * t
}
