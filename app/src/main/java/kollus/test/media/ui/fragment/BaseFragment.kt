package kollus.test.media.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kollus.sdk.media.KollusStorage
import com.kollus.sdk.media.util.ErrorCodes
import com.kollus.sdk.media.util.Utils
import kollus.test.media.MainActivity
import kollus.test.media.settings.KollusConstants
import kollus.test.media.utils.LogUtil


open class BaseFragment : Fragment(), MainActivity.OnBackPressedListener {

    private val TAG: String = BaseFragment::class.simpleName!!
    //    private val TAG: String = "BaseFragment"
    protected var mStorage: KollusStorage? = null
    protected var mAppContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        mAppContext = activity?.applicationContext
        initStorage()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogUtil.d(TAG, "onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        LogUtil.d(TAG, "onResume")
        super.onResume()
    }

    override fun onDestroyView() {
        LogUtil.d(TAG, "onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        LogUtil.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onBackPressed() {
        activity?.finish()
    }

    private fun initStorage() {
        LogUtil.d(TAG, "initStorage")
        mStorage = KollusStorage.getInstance(mAppContext)
        mStorage?.let {
            var version = it.version
            if (!it.isReady) {
                val nRet = it.initialize(KollusConstants.KEY, KollusConstants.EXPIRE_DATE, mAppContext?.packageName)
                if (nRet != ErrorCodes.ERROR_OK) {
                    var msg = "App Key Invalid."
                    LogUtil.e(TAG, msg + nRet)
                }
                it.setDevice(
                    Utils.getStoragePath(mAppContext),
                    Utils.getPlayerId(mAppContext),
                    Utils.getPlayerIdMd5(mAppContext),
                    Utils.isTablet(mAppContext)
                )
            }
            it.setNetworkTimeout(KollusConstants.NETWORK_TIMEOUT_SEC, KollusConstants.NETWORK_RETRY_COUNT)
        }
    }
}