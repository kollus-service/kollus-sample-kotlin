package kollus.test.media.hybrid

import android.os.Build
import kollus.test.media.utils.CommonUtil


class InterFace() {
    // forTest
    @Bridge("getOSVersion")
    fun getOSVersion(): String {
        return CommonUtil.getSimpleJson("version", Build.VERSION.SDK_INT.toString() + "").toString()
    }
}