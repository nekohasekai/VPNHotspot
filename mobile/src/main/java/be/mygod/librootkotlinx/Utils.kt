@file:JvmName("Utils")

package be.mygod.librootkotlinx

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.*
import androidx.annotation.RequiresApi
import kotlinx.android.parcel.Parcelize
import java.io.File

class NoShellException(cause: Throwable) : Exception("Root missing", cause)

/**
 * Based on: https://android.googlesource.com/platform/bionic/+/aff9a34/linker/linker.cpp#3397
 */
@get:RequiresApi(29)
val genericLdConfigFilePath: String get() {
    "/system/etc/ld.config.${Build.VERSION.SDK_INT}.txt".let { if (File(it).isFile) return it }
    if (Build.VERSION.SDK_INT >= 30) "/linkerconfig/ld.config.txt".let {
        check(File(it).isFile) { "failed to find generated linker configuration from \"$it\"" }
        return it
    }
    val prop = Class.forName("android.os.SystemProperties")
    if (prop.getDeclaredMethod("getBoolean", String::class.java, Boolean::class.java).invoke(null,
                    "ro.vndk.lite", false) as Boolean) return "/system/etc/ld.config.vndk_lite.txt"
    when (val version = prop.getDeclaredMethod("get", String::class.java, String::class.java).invoke(null,
            "ro.vndk.version", "") as String) {
        "", "current" -> { }
        else -> "/system/etc/ld.config.$version.txt".let { if (File(it).isFile) return it }
    }
    return "/system/etc/ld.config.txt"
}

val systemContext by lazy {
    val classActivityThread = Class.forName("android.app.ActivityThread")
    val activityThread = classActivityThread.getMethod("systemMain").invoke(null)
    classActivityThread.getMethod("getSystemContext").invoke(activityThread) as Context
}

@Parcelize
data class ParcelableByte(val value: Byte) : Parcelable

@Parcelize
data class ParcelableShort(val value: Short) : Parcelable

@Parcelize
data class ParcelableInt(val value: Int) : Parcelable

@Parcelize
data class ParcelableLong(val value: Long) : Parcelable

@Parcelize
data class ParcelableFloat(val value: Float) : Parcelable

@Parcelize
data class ParcelableDouble(val value: Double) : Parcelable

@Parcelize
data class ParcelableBoolean(val value: Boolean) : Parcelable

@Parcelize
data class ParcelableString(val value: String) : Parcelable

@Parcelize
data class ParcelableByteArray(val value: ByteArray) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableByteArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Parcelize
data class ParcelableIntArray(val value: IntArray) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableIntArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Parcelize
data class ParcelableLongArray(val value: LongArray) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableLongArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Parcelize
data class ParcelableFloatArray(val value: FloatArray) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableFloatArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Parcelize
data class ParcelableDoubleArray(val value: DoubleArray) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableDoubleArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Parcelize
data class ParcelableBooleanArray(val value: BooleanArray) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableBooleanArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Parcelize
data class ParcelableStringArray(val value: Array<String>) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableStringArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Parcelize
data class ParcelableStringList(val value: List<String>) : Parcelable

@Parcelize
data class ParcelableSparseIntArray(val value: SparseIntArray) : Parcelable

@Parcelize
data class ParcelableSparseLongArray(val value: SparseLongArray) : Parcelable

@Parcelize
data class ParcelableSparseBooleanArray(val value: SparseBooleanArray) : Parcelable

@Parcelize
data class ParcelableCharSequence(val value: CharSequence) : Parcelable

@Parcelize
data class ParcelableSize(val value: Size) : Parcelable

@Parcelize
data class ParcelableSizeF(val value: SizeF) : Parcelable

@Parcelize
data class ParcelableArray(val value: Array<Parcelable?>) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableArray

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}

@Parcelize
data class ParcelableList(val value: List<Parcelable?>) : Parcelable

@SuppressLint("Recycle")
inline fun <T> useParcel(block: (Parcel) -> T) = Parcel.obtain().run {
    try {
        block(this)
    } finally {
        recycle()
    }
}

fun Parcelable?.toByteArray(parcelableFlags: Int = 0) = useParcel { p ->
    p.writeParcelable(this, parcelableFlags)
    p.marshall()
}
inline fun <reified T : Parcelable> ByteArray.toParcelable(classLoader: ClassLoader? = T::class.java.classLoader) =
    useParcel { p ->
        p.unmarshall(this, 0, size)
        p.setDataPosition(0)
        p.readParcelable<T>(classLoader)
    }
