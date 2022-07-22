package kollus.test.media.download

import android.app.Service
import android.content.Intent
import android.os.*
import com.kollus.sdk.media.KollusPlayerDRMListener
import com.kollus.sdk.media.KollusStorage
import com.kollus.sdk.media.KollusStorage.OnKollusStorageListener
import com.kollus.sdk.media.content.KollusContent
import com.kollus.sdk.media.util.ErrorCodes
import com.kollus.sdk.media.util.Log
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DownloadService : Service() {

    private var mStorage: KollusStorage? = null
    private var mMessenger = Messenger(LocalHandler())
    private var mClientMessenger: Messenger? = null
    private lateinit var mHandlers: List<Handler>
    private var mExecutor: ExecutorService? = null
    private lateinit var mDownloadList: MutableList<DownloadInfo>

    private var mKollusStorageListener: OnKollusStorageListener = object : OnKollusStorageListener {
        override fun onComplete(content: KollusContent) {
            if (mClientMessenger != null) {
                try {
                    mClientMessenger!!.send(Message.obtain(null, DOWNLOAD_COMPLETE, 0, 0, content))
                    synchronized(mDownloadList) {
                        nextDownload()
                    }
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }
        }

        override fun onProgress(content: KollusContent) {
            if (mClientMessenger != null) {
                try {
                    mClientMessenger!!.send(Message.obtain(null, DOWNLOAD_PROCESS, 0, 0, content))
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }
        }

        override fun onError(content: KollusContent, errorCode: Int) {
            if (mClientMessenger != null) {
                try {
                    mClientMessenger!!.send(Message.obtain(null, DOWNLOAD_ERROR, 0, errorCode, content))
                    synchronized(mDownloadList) {
                        nextDownload()
                    }
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private var mKollusPlayerDRMListener: KollusPlayerDRMListener = object : KollusPlayerDRMListener {
        override fun onDRM(request: String, response: String) {
            if (mClientMessenger != null) {
                try {
                    mClientMessenger!!.send(Message.obtain(null, DOWNLOAD_DRM, DownloadDRM(request, response)))
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }
        }

        override fun onDRMInfo(content: KollusContent, nInfoCode: Int) {
            if (mClientMessenger != null) {
                try {
                    mClientMessenger!!.send(Message.obtain(null, DOWNLOAD_DRM_INFO, 0, nInfoCode, content))
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        mHandlers = ArrayList()
        mDownloadList = ArrayList()
        mStorage = KollusStorage.getInstance(applicationContext)

        mStorage!!.registerKollusStorageListener(mKollusStorageListener)
        mStorage!!.registerKollusPlayerDRMListener(mKollusPlayerDRMListener)
        mExecutor = Executors.newFixedThreadPool(1)
        Log.d(TAG, "onCreate")
    }

    override fun onBind(intent: Intent): IBinder? {
        return mMessenger.binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //		return super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        mStorage!!.unregisterKollusStorageListener(mKollusStorageListener)
        mStorage!!.unregisterKollusPlayerDRMListener(mKollusPlayerDRMListener)
        super.onDestroy()
    }

    private inner class LocalHandler : Handler() {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ADD_HANDLER -> {
                    mClientMessenger = Messenger(msg.obj as Handler)
                    try {
                        mClientMessenger!!.send(Message.obtain(null, ADD_HANDLER, "Registed messanger"))
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }

                }

                DOWNLOAD_START -> {
                    val info = msg.obj as DownloadInfo
                    val task = LoadTask(info)
                    mExecutor!!.execute(task)
                }

                DOWNLOAD_CANCEL -> {
                    val mediaContentKey = msg.obj as String
                    Log.d(TAG, "DOWNLOAD_CANCEL:$mediaContentKey")
                    try {
                        mStorage!!.unload(mediaContentKey)

                        synchronized(mDownloadList) {
                            if (mDownloadList[0].kollusContent.mediaContentKey == mediaContentKey)
                                nextDownload()
                            else
                                removeDownloadList(msg.arg1)
                        }

                        mClientMessenger!!.send(Message.obtain(null, DOWNLOAD_CANCELED, 0, 0, mediaContentKey))
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }

                }

                else -> {
                }
            }
            super.handleMessage(msg)
        }
    }

    private inner class LoadTask internal constructor(private val mInfo: DownloadInfo) : Runnable {

        override fun run() {
            try {
                synchronized(mDownloadList) {
                    for (info in mDownloadList) {
                        if (info.url == mInfo.url) {
                            Log.w(TAG, "Already exists in DownloadList")
                            mClientMessenger!!.send(Message.obtain(null, DOWNLOAD_ALREADY_LOADED))
                            return
                        }
                    }
                }

                val nErrorCode = mStorage!!.load(mInfo.url, "", mInfo.kollusContent)
                if (nErrorCode != ErrorCodes.ERROR_OK) {
                    mClientMessenger!!.send(Message.obtain(null, DOWNLOAD_LOAD_ERROR, 0, nErrorCode))
                } else {
                    mClientMessenger!!.send(Message.obtain(null, DOWNLOAD_LOADED, 0, 0, mInfo))
                    synchronized(mDownloadList) {
                        if (mDownloadList.isEmpty()) {
                            val nRet = mStorage!!.download(mInfo.kollusContent.mediaContentKey)
                            Log.d(TAG, "Send Message Start index " + mInfo.kollusContent.uriIndex + " return " + nRet)
                            if (nRet >= 0) {
                                mClientMessenger!!.send(
                                    Message.obtain(
                                        null,
                                        DOWNLOAD_STARTED,
                                        0,
                                        0,
                                        mInfo.kollusContent
                                    )
                                )
                            } else {
                                mClientMessenger!!.send(
                                    Message.obtain(
                                        null,
                                        DOWNLOAD_ERROR,
                                        0,
                                        nRet,
                                        mInfo.kollusContent
                                    )
                                )
                            }

                        }

                        mDownloadList.add(mInfo)
                    }
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

        }
    }

    @Throws(RemoteException::class)
    private fun nextDownload() {
        while (!mDownloadList.isEmpty()) {
            mDownloadList.removeAt(0)

            if (mDownloadList.isEmpty()) {
                break
            } else {
                val nRet = mStorage!!.download(mDownloadList[0].kollusContent.mediaContentKey)
                if (nRet >= 0) {
                    mClientMessenger!!.send(
                        Message.obtain(
                            null,
                            DOWNLOAD_STARTED,
                            0,
                            0,
                            mDownloadList[0].kollusContent
                        )
                    )
                    break
                } else {
                    mClientMessenger!!.send(
                        Message.obtain(
                            null,
                            DOWNLOAD_ERROR,
                            0,
                            nRet,
                            mDownloadList[0].kollusContent
                        )
                    )
                }
            }
        }
    }

    private fun removeDownloadList(index: Int) {
        for (info in mDownloadList) {
            if (info.kollusContent.uriIndex == index) {
                mDownloadList.remove(info)
                break
            }
        }
    }

    companion object {
        private val TAG = DownloadService::class.java.simpleName
        val ADD_HANDLER = 0
        val DOWNLOAD_START = 10
        val DOWNLOAD_LOADED = 11
        val DOWNLOAD_ALREADY_LOADED = 12
        val DOWNLOAD_STARTED = 13
        val DOWNLOAD_LOAD_ERROR = 14
        val DOWNLOAD_CANCEL = 20
        val DOWNLOAD_CANCELED = 21
        val DOWNLOAD_ERROR = 30
        val DOWNLOAD_PROCESS = 40
        val DOWNLOAD_COMPLETE = 50
        val DOWNLOAD_DRM = 60
        val DOWNLOAD_DRM_INFO = 61
    }
}

