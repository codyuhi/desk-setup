package online.codyuhi.desksetup.ui.scene

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion
import dev.romainguy.kotlin.math.eulerAngles
import io.github.sceneview.math.Rotation
import online.codyuhi.desksetup.domain.Vec3
import kotlin.math.PI

/**
 * Converts a Three.js 'XYZ' Euler (radians) into SceneView's rotation (degrees, applied through
 * `Quaternion.fromEuler` in ZYX order) so both describe the same orientation.
 */
fun threeEuler(r: Vec3): Rotation {
    if (r.x == 0.0 && r.z == 0.0) return Rotation(0f, (r.y * 180 / PI).toFloat(), 0f)
    val deg = { a: Double -> (a * 180 / PI).toFloat() }
    val q = Quaternion.fromAxisAngle(Float3(1f, 0f, 0f), deg(r.x)) *
        Quaternion.fromAxisAngle(Float3(0f, 1f, 0f), deg(r.y)) *
        Quaternion.fromAxisAngle(Float3(0f, 0f, 1f), deg(r.z))
    return eulerAngles(q)
}
