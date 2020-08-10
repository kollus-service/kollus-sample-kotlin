package kollus.test.media.ui.player

import android.content.Context
import android.text.TextUtils
import android.view.SurfaceView
import com.kollus.sdk.media.KollusPlayerLMSListener
import com.kollus.sdk.media.MediaPlayer
import com.kollus.sdk.media.MediaPlayer.*
import com.kollus.sdk.media.VideoWindowImpl
import kollus.test.media.utils.LogUtil

class CustomPlayer {
    private val TAG = CustomPlayer::class.simpleName!!
    private var mContext: Context? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mSurfaceVew: SurfaceView? = null
    private var mPlayType: Int = 0
    private var mSourceUrl: String? = null
    private var mPlayingRate = 1.0f

    constructor(context: Context, mediaPlayer: MediaPlayer, surfaceView: SurfaceView) {
        mContext = context
        mMediaPlayer = mediaPlayer
        mSurfaceVew = surfaceView

        initListener()
    }

    private fun initListener() {
        mMediaPlayer?.let {
            it.setOnPreparedListener(onPreparedListener)
            it.setOnCompletionListener(onCompletionListener)
            it.setOnErrorListener(onErrorListener)
            it.setOnVideoSizeChangedListener(onVideoSizeChangedListener)
            it.setKollusPlayerLMSListener(kollusPlayerLMSListener)
        }
    }

    fun start() {
        LogUtil.d(TAG, "start()")
        mMediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    fun prepareAsync() {
        LogUtil.d(TAG, "prepareAsync()")
        if (TextUtils.isEmpty(mSourceUrl)) {
            LogUtil.d(TAG, "mSourceUrl is empty")
            return
        }

        LogUtil.d(TAG, "mSourceUrl : $mSourceUrl")

        mMediaPlayer?.let {
            if (mPlayType == 0) {
                it.setDataSourceByUrl(mSourceUrl, "")
            } else {
                it.setDataSourceByKey(mSourceUrl, "")
            }
            it.stop()
            it.prepareAsync()
        }
    }

    fun pause() {
        LogUtil.d(TAG, "pause()")
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun setPlayingRate(mode: Int): Boolean {
        when (mode) {
            -1 -> mPlayingRate -= 0.1f
            1 -> mPlayingRate += 0.1f
            else -> mPlayingRate = 1f
        }

        if (mPlayingRate <= 0.5f) {
            mPlayingRate = 0.5f
        }

        if (mPlayingRate >= 2.0f) {
            mPlayingRate = 2.0f
        }

        LogUtil.d(TAG, "mPlayingRate : $mPlayingRate")

        return (mMediaPlayer?.setPlayingRate(mPlayingRate)) ?: false
    }

    fun setVolumeLevel(level: Int) {
        mMediaPlayer?.setVolumeLevel(level)
    }

    fun setMute(mute: Boolean) {
        mMediaPlayer?.setMute(mute)
    }

    fun setFF() {
        mMediaPlayer?.let {
            LogUtil.d(TAG, "setFF() : getCurrentPosition : " + it.currentPosition)
            val sec = it.currentPosition.plus(10000)
            it.seekToExact(sec)
        }
    }

    fun setRW() {
        mMediaPlayer?.let {
            LogUtil.d(TAG, "setRW() : getCurrentPosition : " + it.currentPosition)
            val sec = it.currentPosition?.minus(10000)
            it.seekToExact(sec)
        }
    }

    fun getPlayAt(): Int {
        mMediaPlayer?.let {
            LogUtil.d(TAG, "setRW() : getPlayAt : " + it?.playAt)
            return it.playAt
        }
        return -1
    }

    fun release() {
        LogUtil.d(TAG, "release()")
        mMediaPlayer?.release()
    }

    fun finish() {
        LogUtil.d(TAG, "finish()")

        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mMediaPlayer = null
    }

    fun getMediaPlayer(): MediaPlayer? {
        return mMediaPlayer
    }

    fun getSurfaceVew(): SurfaceView? {
        return mSurfaceVew
    }

    fun getSourceUrl(): String? {
        return mSourceUrl
    }

    fun getPlayingRate(): Float {
        return mPlayingRate
    }

    fun setDataSource(type: Int, url: String?) {
        mPlayType = type
        mSourceUrl = url
    }

    private fun setSizeSurfaceView(mediaPlayer: MediaPlayer) {
        mSurfaceVew?.let {
            it.holder?.setFixedSize(mediaPlayer.videoWidth, mediaPlayer.videoHeight)
            val mVideoWidth = mediaPlayer.videoWidth
            val mVideoHeight = mediaPlayer.videoHeight
            var displayWidth = it.width
            var displayHeight: Int = it.height

            var l = it.left
            var r = it.right
            var t = it.top
            var b = it.bottom

            if (mVideoWidth * displayHeight > displayWidth * mVideoHeight) {
                displayHeight = displayWidth * mVideoHeight / mVideoWidth

            } else if (mVideoWidth * displayHeight < displayWidth * mVideoHeight) {
                displayWidth = displayHeight * mVideoWidth / mVideoHeight
            }

            l = (r - l - displayWidth) / 2
            r = l + displayWidth
            t = (b - t - displayHeight) / 2
            b = t + displayHeight

            it.layout(l, t, r, b)
        }
    }

    private var onPreparedListener = OnPreparedListener {
        LogUtil.d(TAG, "onPrepared() - getPlayAt() : " + it?.playAt)
        setSizeSurfaceView(it)
        it?.start()
    }

    private var onCompletionListener = OnCompletionListener {
        LogUtil.d(TAG, "onCompletion()")
        it?.stop()
        it?.release()
    }

    private var onErrorListener = OnErrorListener { mediaPlayer, what, extra ->
        LogUtil.d(TAG, "onError() what : $what extra: $extra")
        LogUtil.d(TAG, "errorMsg : " + mediaPlayer?.getErrorString(extra))
        false
    }

    private var onVideoSizeChangedListener = OnVideoSizeChangedListener { mediaPlayer, width, height ->
        LogUtil.d(TAG, "onVideoSizeChanged() width : $width/ height : $height")
    }

    private val kollusPlayerLMSListener = KollusPlayerLMSListener { request, response ->
        LogUtil.d(TAG, "request : $request / response : $response")
    }
}