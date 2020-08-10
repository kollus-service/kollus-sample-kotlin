package kollus.test.media.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import com.kollus.sdk.media.MediaPlayer
import kollus.test.media.R
import kollus.test.media.settings.AppConfig
import kollus.test.media.ui.player.CustomPlayer
import kollus.test.media.utils.CommonUtil
import kollus.test.media.utils.LogUtil
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException

class PlayVideoFragment : BaseFragment(), View.OnClickListener {

    private val TAG: String = PlayVideoFragment::class.simpleName!!
    private var mMediaPlayer: MediaPlayer? = null
    private var mSurfaceView: SurfaceView? = null
    private var mPlayer: CustomPlayer? = null
    private var mLogTextView: TextView? = null
    private var jwtUrl: String? = null

    companion object {
        fun newInstance() = PlayVideoFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_playvideo, container, false)

        mSurfaceView = root.findViewById(R.id.surface_view)
        mSurfaceView?.holder?.addCallback(surfaceCallback)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mSurfaceView?.setSecure(true)
        }

        mMediaPlayer = MediaPlayer(context, mStorage, 1234)
        mPlayer = CustomPlayer(context!!, mMediaPlayer!!, mSurfaceView!!)

        if (AppConfig.MODE_MAKE_JWT) {
            var mckey = "MEDIA_CONTENT_KEY"
            var cuid = "CLIENT_USER_ID"
            try {
                jwtUrl = CommonUtil.createUrl(cuid, mckey, true)!!
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            }
        }

        mLogTextView = root.findViewById<View>(R.id.control_log) as TextView

        root.findViewById<View>(R.id.play).setOnClickListener(this)
        root.findViewById<View>(R.id.pause).setOnClickListener(this)
        root.findViewById<View>(R.id.rate_up).setOnClickListener(this)
        root.findViewById<View>(R.id.rate_down).setOnClickListener(this)
        root.findViewById<View>(R.id.volume_up).setOnClickListener(this)
        root.findViewById<View>(R.id.volume_down).setOnClickListener(this)
        root.findViewById<View>(R.id.mute).setOnClickListener(this)
        root.findViewById<View>(R.id.un_mute).setOnClickListener(this)
        root.findViewById<View>(R.id.ff).setOnClickListener(this)
        root.findViewById<View>(R.id.rw).setOnClickListener(this)
        root.findViewById<View>(R.id.restart).setOnClickListener(this)
        root.findViewById<View>(R.id.callApp).setOnClickListener(this)

        return root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        LogUtil.d(TAG, "onDestroyView")
        mPlayer?.finish()
        super.onDestroyView()
    }

    override fun onDestroy() {
        LogUtil.d(TAG, "onDestroy")
        mPlayer?.finish()
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.play -> {
                setLogText("play")
                mPlayer?.start()
            }
            R.id.pause -> {
                setLogText("pause")
                mPlayer?.pause()
            }
            R.id.rate_up -> {
                mPlayer?.setPlayingRate(1)
                setLogText("Rate Up : " + String.format("%.1f", mPlayer?.getPlayingRate()))
            }
            R.id.rate_down -> {
                mPlayer?.setPlayingRate(-1)
                setLogText("Rate Down : " + String.format("%.1f", mPlayer?.getPlayingRate()))
            }
            R.id.volume_up -> {
                CommonUtil.setStreamVolume(mAppContext, true)
                mPlayer?.setVolumeLevel(CommonUtil.getStreamVolume(mAppContext))
                setLogText("Volume up : " + CommonUtil.getStreamVolume(mAppContext))
            }
            R.id.volume_down -> {
                CommonUtil.setStreamVolume(mAppContext, false)
                mPlayer?.setVolumeLevel(CommonUtil.getStreamVolume(mAppContext))
                setLogText("Volume down : " + CommonUtil.getStreamVolume(mAppContext))
            }
            R.id.ff -> {
                mPlayer?.setFF()
                setLogText("setFF(10) : " + mPlayer?.getMediaPlayer()?.currentPosition + "ms")
            }
            R.id.rw -> {
                mPlayer?.setRW()
                setLogText("setRW(10) : " + mPlayer?.getMediaPlayer()?.currentPosition + "ms")
            }
            R.id.mute -> {
                mPlayer?.setMute(true)
                setLogText("set Mute")
            }
            R.id.un_mute -> {
                mPlayer?.setMute(false)
                setLogText("set unMute")
            }
            R.id.restart -> {
                mPlayer?.prepareAsync()
                setLogText("re start")
            }
            R.id.callApp -> {
                setLogText("re callApp")
                //CommonUtil.startKollusApp(context, jwtUrl)
            }
            else -> {
            }
        }
    }

    fun setLogText(log: String?) {
        mLogTextView?.text = log
    }

    private var surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder?) {
            LogUtil.d(TAG, "surfaceCreated()")

            mPlayer?.getMediaPlayer()?.setDisplay(holder)
            mPlayer?.setDataSource(0, jwtUrl)
            mPlayer?.prepareAsync()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            LogUtil.d(TAG, "surfaceChanged()")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            LogUtil.d(TAG, "surfaceDestroyed()")
        }
    }

}