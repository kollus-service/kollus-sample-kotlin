package kollus.test.media.hybrid

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import java.util.*


class HybridWebView : WebView {

    private var javascriptInterfaceName = DEFAULT_JAVASCRIPT_INTERFACE_NAME
    private var mWebSettings: WebSettings? = null
    private var mBridgeProxy: BridgeProxy? = null
    protected var mContext: Context
    protected var mImeBackKeyEvent = false

    constructor(context: Context) : super(context) {
        mContext = context
        setup()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        setup()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        mContext = context
        setup()
    }

    protected fun setup() {
        mWebSettings = settings
        mWebSettings?.let {
            it.pluginState = WebSettings.PluginState.ON
            it.javaScriptEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                it.mediaPlaybackRequiresUserGesture = false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                it.allowFileAccessFromFileURLs = true
                it.allowUniversalAccessFromFileURLs = true
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                it.textZoom = 100
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            it.domStorageEnabled = true
            it.defaultTextEncodingName = "utf-8"
        }
    }

    @SuppressLint("JavascriptInterface")
    fun registerBridge(receiver: ArrayList<Any>) {
        val methodSet = HashMap<String, BridgeInfo>()

        for (obj in receiver) {
            val cls = obj.javaClass
            val methods = cls.methods
            for (method in methods) {
                val bridge = method.getAnnotation(Bridge::class.java)
                if (bridge != null) {
                    methodSet[bridge.value] = BridgeInfo(obj, method)
                }
            }
        }

        val handler = Handler(context.mainLooper)
        mBridgeProxy = BridgeProxy(this, handler)
        addJavascriptInterface(mBridgeProxy, javascriptInterfaceName)
        mBridgeProxy!!.registerBridgeReceiver(methodSet)
    }

    fun call(command: String, str: String) {
        mBridgeProxy!!.notifyToWeb(command, str)
    }

    fun setInterFaceName(inrerface: String) {
        javascriptInterfaceName = inrerface
    }

    /*
    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive && event.keyCode == KeyEvent.KEYCODE_BACK) {
            mImeBackKeyEvent = true
            return false
        }
        return super.dispatchKeyEvent(event)
    }
    */

    companion object {
        private val DEFAULT_JAVASCRIPT_INTERFACE_NAME = "Native"
        private val TAG = HybridWebView::class.simpleName
    }
}
