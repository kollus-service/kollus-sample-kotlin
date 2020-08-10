package kollus.test.media.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kollus.sdk.media.content.FileManager
import com.kollus.sdk.media.content.KollusContent
import com.kollus.sdk.media.util.KollusUri
import com.kollus.sdk.media.util.Log
import kollus.test.media.R
import kollus.test.media.download.DownloadInfo
import kollus.test.media.download.DownloadService
import kollus.test.media.settings.AppConfig.Companion.MODE_MAKE_JWT
import kollus.test.media.utils.CommonUtil
import kollus.test.media.utils.LogUtil
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException


class DownLoadFragment : BaseFragment() {

    private val mFileManager: FileManager? = null
    private var mMessenger: Messenger? = null
    private var mBounded: Boolean = false
    private val mLogContenTitle: TextView? = null
    private var mLogView: TextView? = null
    private var contentTitle: TextView? = null
    private var contentProgress: TextView? = null


    var jwtUrl ="";
        
    internal var mConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
            LogUtil.d(TAG, "onServiceDisconnected")
            mBounded = false
            mMessenger = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            LogUtil.d(TAG, "onServiceConnected")

            mMessenger = Messenger(service)
            mBounded = true

            try {
                mMessenger!!.send(Message.obtain(null, DownloadService.ADD_HANDLER, ClientHandler()))
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DATA_DOWNLOAD -> startDownload(msg.obj as String)
            }//                case DATA_DOWNLOAD_CANCEL:
            //                    alertCancelDownload(msg.arg1 == 1);
            //                    break;
            //                case CHECK_EXIT:
            //                    mExit = false;
            //                    break;
            super.handleMessage(msg)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogUtil.d(TAG, "onCreateView")

        val root = inflater.inflate(R.layout.fragment_download, container, false)
        mLogView = root.findViewById(R.id.log_tv)
        contentProgress = root.findViewById(R.id.log_progress)
        contentTitle = root.findViewById(R.id.log_title)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)

        if (!mBounded) {
            val intent = Intent(activity, DownloadService::class.java)
            context!!.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        LogUtil.d(TAG, "onResume")
        super.onResume()
    }

    override fun onDestroy() {
        LogUtil.d(TAG, "onDestroy")
        super.onDestroy()
        if (mBounded) {
            context!!.unbindService(mConnection)
        }
    }

    inner class ClientHandler : Handler() {

        override fun handleMessage(msg: Message) {
            LogUtil.d(TAG, "handleMessage msg.what : " + msg.what)
            mLogView!!.text = mLogView!!.text.toString() + "\n" + CommonUtil.convertDownLoadStatus(msg.what)
            when (msg.what) {
                DownloadService.ADD_HANDLER -> {
                    if (MODE_MAKE_JWT) {
                        val mckey = "MEDIA_CONTENT_KEY"
                        try {
                            jwtUrl = CommonUtil.createUrl("CLIENT_USER_ID", mckey, true)!!
                        } catch (e: NoSuchAlgorithmException) {
                            e.printStackTrace()
                        } catch (e: InvalidKeyException) {
                            e.printStackTrace()
                        }

                    }
                    startDownload(jwtUrl)
                }
                DownloadService.DOWNLOAD_LOADED -> {
                }
                DownloadService.DOWNLOAD_ALREADY_LOADED -> {
                }
                DownloadService.DOWNLOAD_STARTED -> {
                }
                DownloadService.DOWNLOAD_CANCELED -> {
                }
                DownloadService.DOWNLOAD_LOAD_ERROR -> {
                }
                DownloadService.DOWNLOAD_ERROR -> {
                    val errorCode = msg.arg2
                    mLogView!!.text = mLogView!!.text.toString() + " " + errorCode
                }
                DownloadService.DOWNLOAD_PROCESS -> {
                    val content = msg.obj as KollusContent
                    if (content != null) {
                        val percent = (content.receivingSize * 100 / content.fileSize).toInt()
                        contentTitle!!.text = "Title : " + content.subCourse + " (" + content.mediaContentKey + ")"
                        contentProgress!!.text = "Process: " + content.downloadPercent + "%"
                        content.downloadPercent = percent
                    }
                }
                DownloadService.DOWNLOAD_COMPLETE -> {
                }
                DownloadService.DOWNLOAD_DRM -> {
                }
                DownloadService.DOWNLOAD_DRM_INFO -> {
                }
                else -> {
                }
            }

            super.handleMessage(msg)
        }
    }

    private fun startDownload(url: String) {
        val uri = KollusUri.parse(url)
        var location: String? = null
        var path: String
        val queryIndex = url.indexOf('?')
        if (queryIndex > 0) {
            path = url.substring(0, queryIndex)
        } else {
            path = url
        }

        val keySet = uri.queryParameterNames
        var first = true
        for (key in keySet) {
            Log.d(TAG, String.format("startDownload '%s' ==> '%s'", key, uri.getQueryParameter(key)))
            if (key.equals("folder", ignoreCase = true)) {
                location = Uri.decode(uri.getQueryParameter(key))
            } else {
                if (first)
                    path += "?"
                else
                    path += "&"

                path += key
                path += "="
                path += uri.getQueryParameter(key)

                first = false
            }
        }

        var downloadLocation = mFileManager
        if (location != null) {
            val items = location.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (item in items) {
                downloadLocation = downloadLocation!!.addDirectory(item)
            }
        }

        LogUtil.d(TAG, "startDownload downStart --> folder [$location] url [$path]")
        val info = DownloadInfo(location, path)
        try {
            mMessenger!!.send(Message.obtain(null, DownloadService.DOWNLOAD_START, info))
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    companion object {

        private val TAG = DownLoadFragment::class.java.simpleName

        private val DATA_DOWNLOAD = 101
        private val DATA_DOWNLOAD_CANCEL = 102
        private val CHECK_EXIT = 103

        fun newInstance(): DownLoadFragment {
            return DownLoadFragment()
        }
    }
}
