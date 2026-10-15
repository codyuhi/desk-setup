package online.codyuhi.desksetup.domain

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// Small, dependency-free vector math for model bounds and touch picking (unit-testable on the JVM).

data class Vec3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Double) = Vec3(x * s, y * s, z * s)
    fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z
    fun cross(o: Vec3) = Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    fun length() = sqrt(dot(this))
    fun normalized(): Vec3 = length().let { if (it == 0.0) this else this * (1 / it) }
    fun hadamard(o: Vec3) = Vec3(x * o.x, y * o.y, z * o.z)

    companion object {
        val ZERO = Vec3(0.0, 0.0, 0.0)
        val ONE = Vec3(1.0, 1.0, 1.0)
    }
}

/** Row-major 3×3 rotation matrix. */
data class Mat3(val m: DoubleArray) {
    operator fun times(v: Vec3) = Vec3(
        m[0] * v.x + m[1] * v.y + m[2] * v.z,
        m[3] * v.x + m[4] * v.y + m[5] * v.z,
        m[6] * v.x + m[7] * v.y + m[8] * v.z,
    )

    operator fun times(o: Mat3): Mat3 {
        val r = DoubleArray(9)
        for (i in 0..2) for (j in 0..2) r[i * 3 + j] = (0..2).sumOf { k -> m[i * 3 + k] * o.m[k * 3 + j] }
        return Mat3(r)
    }

    override fun equals(other: Any?) = other is Mat3 && m.contentEquals(other.m)
    override fun hashCode() = m.contentHashCode()

    companion object {
        val IDENTITY = Mat3(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0))

        fun rotX(a: Double) = Mat3(doubleArrayOf(1.0, 0.0, 0.0, 0.0, cos(a), -sin(a), 0.0, sin(a), cos(a)))
        fun rotY(a: Double) = Mat3(doubleArrayOf(cos(a), 0.0, sin(a), 0.0, 1.0, 0.0, -sin(a), 0.0, cos(a)))
        fun rotZ(a: Double) = Mat3(doubleArrayOf(cos(a), -sin(a), 0.0, sin(a), cos(a), 0.0, 0.0, 0.0, 1.0))

        /** Three.js Euler with the default 'XYZ' order: R = Rx · Ry · Rz. */
        fun euler(r: Vec3) = rotX(r.x) * rotY(r.y) * rotZ(r.z)
    }
}

/** Axis-aligned bounding box. */
data class Aabb(val min: Vec3, val max: Vec3) {
    val center get() = (min + max) * 0.5
    val size get() = max - min

    fun union(o: Aabb) = Aabb(
        Vec3(min(min.x, o.min.x), min(min.y, o.min.y), min(min.z, o.min.z)),
        Vec3(max(max.x, o.max.x), max(max.y, o.max.y), max(max.z, o.max.z)),
    )

    fun corners() = listOf(
        Vec3(min.x, min.y, min.z), Vec3(max.x, min.y, min.z), Vec3(min.x, max.y, min.z), Vec3(max.x, max.y, min.z),
        Vec3(min.x, min.y, max.z), Vec3(max.x, min.y, max.z), Vec3(min.x, max.y, max.z), Vec3(max.x, max.y, max.z),
    )

    /** Distance along [dir] where the ray first enters the box, or null (slab test). */
    fun intersect(origin: Vec3, dir: Vec3): Double? {
        var tMin = Double.NEGATIVE_INFINITY
        var tMax = Double.POSITIVE_INFINITY
        val o = doubleArrayOf(origin.x, origin.y, origin.z)
        val d = doubleArrayOf(dir.x, dir.y, dir.z)
        val lo = doubleArrayOf(min.x, min.y, min.z)
        val hi = doubleArrayOf(max.x, max.y, max.z)
        for (i in 0..2) {
            if (abs(d[i]) < 1e-12) {
                if (o[i] < lo[i] || o[i] > hi[i]) return null
            } else {
                var t1 = (lo[i] - o[i]) / d[i]
                var t2 = (hi[i] - o[i]) / d[i]
                if (t1 > t2) t1 = t2.also { t2 = t1 }
                tMin = max(tMin, t1)
                tMax = min(tMax, t2)
                if (tMin > tMax) return null
            }
        }
        if (tMax < 0) return null
        return max(tMin, 0.0)
    }

    companion object {
        fun of(points: List<Vec3>) = Aabb(
            Vec3(points.minOf { it.x }, points.minOf { it.y }, points.minOf { it.z }),
            Vec3(points.maxOf { it.x }, points.maxOf { it.y }, points.maxOf { it.z }),
        )
    }
}
