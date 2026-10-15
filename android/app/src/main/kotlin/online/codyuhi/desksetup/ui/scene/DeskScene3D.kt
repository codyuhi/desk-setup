package online.codyuhi.desksetup.ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import com.google.android.filament.Camera
import com.google.android.filament.MaterialInstance
import io.github.sceneview.FrameRatePolicy
import io.github.sceneview.SceneView
import io.github.sceneview.SceneScope
import io.github.sceneview.SurfaceType
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Size
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import online.codyuhi.desksetup.domain.Catalog
import online.codyuhi.desksetup.domain.DeskSetup
import online.codyuhi.desksetup.domain.Group
import online.codyuhi.desksetup.domain.Material
import online.codyuhi.desksetup.domain.ModelNode
import online.codyuhi.desksetup.domain.Models
import online.codyuhi.desksetup.domain.OrbitCamera
import online.codyuhi.desksetup.domain.Part
import online.codyuhi.desksetup.domain.Shape
import online.codyuhi.desksetup.domain.Vec3
import online.codyuhi.desksetup.ui.theme.SceneColors

/**
 * The 3D desk view (the native port of app/src/scene3d.js), rendered by Filament through
 * SceneView. Gestures are handled by the overlay in [SceneGestures]; this composable only draws.
 */
@Composable
fun DeskScene3D(
    setup: DeskSetup,
    selectedId: String?,
    camera: OrbitCamera,
    colors: SceneColors,
    modifier: Modifier = Modifier,
) {
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        // About 0.8 EV darker than SceneView's default so the web's material colours read true.
        setExposure(16f, 1f / 200f, 200f)
    }
    // Sun from the front-right and above, like the web's DirectionalLight at (3, 5, 3).
    val mainLight = rememberMainLightNode(engine) { lightDirection = Direction(-0.45f, -0.75f, -0.45f) }
    val materials = remember(materialLoader) { MaterialCache(materialLoader) }
    var aspect by remember { mutableStateOf(1.0) }

    SideEffect {
        cameraNode.setProjection(camera.fovYDegrees, aspect, 0.05, 100.0, Camera.Fov.VERTICAL)
        cameraNode.position = camera.eye.toPosition()
        cameraNode.lookAt(camera.target.toPosition())
    }

    Box(
        modifier
            .background(colors.background)
            .onSizeChanged { if (it.height > 0) aspect = it.width.toDouble() / it.height },
    ) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            surfaceType = SurfaceType.TextureSurface,
            engine = engine,
            materialLoader = materialLoader,
            environmentLoader = environmentLoader,
            isOpaque = false,
            frameRatePolicy = FrameRatePolicy.OnDemand(),
            autoCenterContent = false,
            cameraNode = cameraNode,
            mainLightNode = mainLight,
            cameraManipulator = null,
            onGestureListener = null,
        ) {
            Room(colors, materials)
            ModelContent(Models.desk(setup.deskWidth, setup.deskDepth, setup.deskHeight, setup.deskFinish), materials, null)
            val deskTop = setup.deskHeight * 0.01
            setup.placedItems.forEach { placed ->
                val cat = Catalog.item(placed.catalogId)?.takeUnless { it.isDeskBase } ?: return@forEach
                key(placed.id) {
                    val pos = Models.placement(cat, placed.x, placed.z, deskTop)
                    val tint = if (placed.id == selectedId) colors.select else null
                    Node(position = pos.toPosition(), rotation = threeEuler(Vec3(0.0, placed.rotation, 0.0))) {
                        ModelContent(remember(cat.id) { Models.item(cat) }, materials, tint)
                    }
                }
            }
        }
    }
}

/** Floor, back wall and the 6 m grid from the web scene. */
@Composable
private fun SceneScope.Room(colors: SceneColors, materials: MaterialCache) {
    CubeNode(size = Size(12f, 0.01f, 12f), center = Position(0f, -0.005f, 0f), materialInstance = materials.get(colors.floor, 0.1f, 0.6f))
    CubeNode(size = Size(12f, 8f, 0.01f), center = Position(0f, 4f, -2.5f), materialInstance = materials.get(colors.wall, 0f, 0.8f))
    val minor = materials.get(colors.gridMinor, 0f, 1f)
    val major = materials.get(colors.gridMajor, 0f, 1f)
    for (i in 0..30) {
        val p = -3f + i * 0.2f
        val m = if (i == 15) major else minor
        CubeNode(size = Size(0.004f, 0.001f, 6f), center = Position(p, 0.001f, 0f), materialInstance = m)
        CubeNode(size = Size(6f, 0.001f, 0.004f), center = Position(0f, 0.001f, p), materialInstance = m)
    }
}

@Composable
private fun SceneScope.ModelContent(node: ModelNode, materials: MaterialCache, tint: Color?) {
    when (node) {
        is Group -> Node(position = node.position.toPosition(), rotation = threeEuler(node.rotation)) {
            node.children.forEach { ModelContent(it, materials, tint) }
        }
        is Part -> {
            val mat = materials.get(node.material, tint)
            val pos = node.position.toPosition()
            val rot = threeEuler(node.rotation)
            val scale = Scale(node.scale.x.toFloat(), node.scale.y.toFloat(), node.scale.z.toFloat())
            when (val s = node.shape) {
                is Shape.Box -> CubeNode(size = Size(s.w.toFloat(), s.h.toFloat(), s.d.toFloat()), materialInstance = mat,
                    position = pos, rotation = rot, scale = scale)
                is Shape.Cylinder -> CylinderNode(radius = s.radius.toFloat(), height = s.h.toFloat(), materialInstance = mat,
                    position = pos, rotation = rot, scale = scale)
                is Shape.Sphere -> SphereNode(radius = s.r.toFloat(), materialInstance = mat, position = pos, rotation = rot, scale = scale)
                is Shape.Cone -> ConeNode(radius = s.r.toFloat(), height = s.h.toFloat(), materialInstance = mat,
                    position = pos, rotation = rot, scale = scale)
                is Shape.Torus -> TorusNode(majorRadius = s.major.toFloat(), minorRadius = s.minor.toFloat(), materialInstance = mat,
                    position = pos, rotation = rot, scale = scale)
            }
        }
    }
}

/** Shares one Filament material instance per colour/finish combination. */
private class MaterialCache(private val loader: MaterialLoader) {
    private val cache = HashMap<Triple<Long, Float, Float>, MaterialInstance>()

    fun get(color: Color, metallic: Float, roughness: Float): MaterialInstance {
        val k = Triple(color.value.toLong(), metallic, roughness)
        return cache.getOrPut(k) { loader.createColorInstance(color, metallic, roughness) }
    }

    /** Emissive parts are baked into a brighter base colour; the selected item is tinted towards [tint]. */
    fun get(m: Material, tint: Color?): MaterialInstance {
        var c = Color(m.color.toInt()).copy(alpha = m.opacity.toFloat())
        if (m.emissiveIntensity > 0) {
            c = lerp(c, Color(m.emissive.toInt()).copy(alpha = c.alpha), (m.emissiveIntensity / (1 + m.emissiveIntensity)).toFloat())
        }
        if (tint != null) c = lerp(c, tint.copy(alpha = c.alpha), 0.4f)
        return get(c, m.metalness.toFloat(), m.roughness.toFloat())
    }
}

private fun Vec3.toPosition() = Position(x.toFloat(), y.toFloat(), z.toFloat())
