package online.codyuhi.desksetup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrbitCameraTest {
    @Test
    fun lookingAtReproducesTheWebEyeInLandscape() {
        val cam = OrbitCamera.lookingAt(Vec3(0.0, 1.8, 2.5), Vec3(0.0, 0.75, 0.0), aspect = 2.0)
        assertEquals(0.0, cam.eye.x, 1e-9)
        assertEquals(1.8, cam.eye.y, 1e-9)
        assertEquals(2.5, cam.eye.z, 1e-9)
        assertEquals(45.0, cam.fovYDegrees, 0.0)
    }

    @Test
    fun portraitWidensFovAndPullsBack() {
        val cam = OrbitCamera.initial(aspect = 0.5)
        assertEquals(60.0, cam.fovYDegrees, 1e-9)
        assertTrue(cam.distance > OrbitCamera.initial(aspect = 2.0).distance)
    }

    @Test
    fun centreRayLooksAtTarget() {
        val cam = OrbitCamera.initial(aspect = 1.5)
        val (origin, dir) = cam.ray(150.0, 50.0, 300.0, 100.0)
        val toTarget = (cam.target - origin).normalized()
        assertEquals(1.0, dir.dot(toTarget), 1e-9)
    }

    @Test
    fun elevationAndDistanceStayInBounds() {
        val cam = OrbitCamera.initial().orbit(0.0, 10.0).zoom(100.0)
        assertEquals(OrbitCamera.MAX_ELEVATION, cam.elevation, 1e-12)
        assertEquals(OrbitCamera.MIN_DISTANCE, cam.distance, 1e-12)
        val low = cam.orbit(0.0, -10.0).zoom(0.001)
        assertEquals(OrbitCamera.MIN_ELEVATION, low.elevation, 1e-12)
        assertEquals(OrbitCamera.MAX_DISTANCE, low.distance, 1e-12)
    }

    @Test
    fun rayHitsTheDeskPlane() {
        val cam = OrbitCamera.preset(CameraView.Top, 74, aspect = 1.0)
        val (o, d) = cam.ray(50.0, 50.0, 100.0, 100.0)
        val hit = rayPlaneY(o, d, 0.74)!!
        assertEquals(0.0, hit.x, 0.02)
        assertEquals(0.0, hit.z, 0.02)
    }
}
