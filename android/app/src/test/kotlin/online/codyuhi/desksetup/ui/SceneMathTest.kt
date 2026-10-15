package online.codyuhi.desksetup.ui

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion
import online.codyuhi.desksetup.domain.Mat3
import online.codyuhi.desksetup.domain.Vec3
import online.codyuhi.desksetup.ui.scene.threeEuler
import org.junit.Assert.assertEquals
import org.junit.Test

/** SceneView must orient parts exactly like Three.js did in the web builders. */
class SceneMathTest {
    @Test
    fun threeEulerMatchesThreeJsOrientation() {
        listOf(Vec3(0.8, -0.6, 0.5), Vec3(0.3, 0.0, -0.4), Vec3(-Math.PI / 2, 0.0, 0.0), Vec3(0.0, 1.2, 0.0)).forEach { r ->
            val q = Quaternion.fromEuler(threeEuler(r))
            listOf(Vec3(1.0, 0.0, 0.0), Vec3(0.0, 1.0, 0.0), Vec3(0.3, -0.2, 0.9)).forEach { v ->
                val expected = Mat3.euler(r) * v
                val actual = q * Float3(v.x.toFloat(), v.y.toFloat(), v.z.toFloat())
                assertEquals("$r x", expected.x, actual.x.toDouble(), 1e-4)
                assertEquals("$r y", expected.y, actual.y.toDouble(), 1e-4)
                assertEquals("$r z", expected.z, actual.z.toDouble(), 1e-4)
            }
        }
    }
}
