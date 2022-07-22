package kollus.test.media.utils

import android.content.Context
import android.media.AudioManager
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.gson.Gson
import kollus.test.media.download.DownloadService
import kollus.test.media.settings.AppConfig
import org.json.JSONObject
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import android.content.Intent
import android.net.Uri


class CommonUtil {
    private val TAG: String = CommonUtil::class.simpleName!!

    companion object {
        fun getDeviceWidth(context: Context): Int {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val mDisplayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(mDisplayMetrics)
            return mDisplayMetrics.widthPixels
        }

        fun getDeviceHeight(context: Context): Int {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val mDisplayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(mDisplayMetrics)
            return mDisplayMetrics.heightPixels
        }


        fun convertDownLoadStatus(what: Int): String {
            when (what) {
                DownloadService.ADD_HANDLER -> return "Download Handler init"
                DownloadService.DOWNLOAD_START -> return "Download start"
                DownloadService.DOWNLOAD_LOADED -> return "Download Loaded"
                DownloadService.DOWNLOAD_ALREADY_LOADED -> return "Download Already Loaded"
                DownloadService.DOWNLOAD_STARTED -> return "Download Started"
                DownloadService.DOWNLOAD_LOAD_ERROR -> return "Download Load Error"
                DownloadService.DOWNLOAD_CANCEL -> return "Download Cancel"
                DownloadService.DOWNLOAD_ERROR -> return "Download Error"
                DownloadService.DOWNLOAD_PROCESS -> return "Download Process"
                DownloadService.DOWNLOAD_COMPLETE -> return "Download Complete"
                DownloadService.DOWNLOAD_DRM -> return "Download DRM"
                DownloadService.DOWNLOAD_DRM_INFO -> return "Download DRM Info"
                else -> return ""
            }
        }


        fun setStreamVolume(context: Context?, isVolumeUp: Boolean) {
            val manager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (isVolumeUp) {
                manager?.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.RINGER_MODE_SILENT
                )
            } else {
                manager?.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.RINGER_MODE_SILENT
                )
            }
        }

        fun getStreamVolume(context: Context?): Int {
            val manager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return manager?.getStreamVolume(AudioManager.STREAM_MUSIC)
        }

        @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
        fun createUrl(cuid: String, mck: String, isSiType: Boolean): String? {
            var playUri: String? = null
            var strToken: String? = null

            val mcKeyMap = HashMap<String, String>()
            mcKeyMap["mckey"] = mck

            val mcKeyArray = ArrayList<Any>()
            mcKeyArray.add(mcKeyMap)

            val payLoadMap = HashMap<String, Any>()
            payLoadMap["cuid"] = cuid
            payLoadMap["expt"] = System.currentTimeMillis() + 3600
            payLoadMap["mc"] = mcKeyArray

            val jwtUtil = JwtUtil()
            strToken = jwtUtil.createJwt(Gson().toJson(payLoadMap), AppConfig.SECURITY_KEY)

            if (isSiType) {
                playUri = String.format(
                    "https://v.kr.kollus.com/si?jwt=%s&custom_key=%s",
                    strToken,
                    AppConfig.CUSTOM_KEY
                )
            } else {
                playUri = String.format(
                    "https://v.kr.kollus.com/s?jwt=%s&custom_key=%s",
                    strToken,
                    AppConfig.CUSTOM_KEY
                )
            }
            return playUri
        }

        fun getSimpleJson(key: String, value: String): JSONObject? {
            var jObject: JSONObject? = null
            try {
                jObject = JSONObject()
                jObject.put(key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                return jObject
            }
        }

        fun startKollusApp(context: Context) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage("com.kollus.media")
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun startKollusApp(context: Context?, url: String?) {
            try {
                val scheme = "kollus://path?url=$url"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context?.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                context?.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + "com.kollus.media")
                    )
                )
            }
        }
    }
}