package online.codyuhi.desksetup.domain

/** Hit-testing for the 3D view (the native stand-in for the web's THREE.Raycaster). */
object Picking {
    private val boundsCache = HashMap<String, Aabb>()

    /** Model bounds, padded so small items (mouse, mug) are at least [MIN_TARGET_M] across for fingers. */
    fun itemBounds(item: CatalogItem): Aabb = boundsCache.getOrPut(item.id) {
        val b = Models.bounds(Models.item(item))
        fun pad(size: Double) = ((MIN_TARGET_M - size) / 2).coerceAtLeast(0.0)
        val p = Vec3(pad(b.size.x), pad(b.size.y), pad(b.size.z))
        Aabb(b.min - p, b.max + p)
    }

    private const val MIN_TARGET_M = 0.08

    /** Nearest placed item hit by the ray, or null. */
    fun pick(setup: DeskSetup, origin: Vec3, dir: Vec3): String? {
        val deskTop = setup.deskHeight * 0.01
        return setup.placedItems.mapNotNull { placed ->
            val cat = Catalog.item(placed.catalogId)?.takeUnless { it.isDeskBase } ?: return@mapNotNull null
            val pos = Models.placement(cat, placed.x, placed.z, deskTop)
            // Into item space: undo the translation, then the Y rotation.
            val inv = Mat3.rotY(-placed.rotation)
            val t = itemBounds(cat).intersect(inv * (origin - pos), inv * dir) ?: return@mapNotNull null
            placed.id to t
        }.minByOrNull { it.second }?.first
    }
}
