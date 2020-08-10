package kollus.test.media.hybrid

import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kollus.test.media.utils.LogUtil
import java.lang.reflect.InvocationTargetException
import java.util.*

class BridgeProxy(webView: WebView, handler: Handler) {

    private var mHandler: Handler? = null
    private var mWebView: WebView? = null
    //    private Object mBridgeReceiver
    private var mBridgeMethodSet: Map<String, BridgeInfo> = HashMap()

    init {
        this.mWebView = webView
        this.mHandler = handler
    }

    fun registerBridgeReceiver(bridgeMethodSet: Map<String, BridgeInfo>) {
        //        this.mBridgeReceiver = bridgeReceiver
        this.mBridgeMethodSet = bridgeMethodSet
    }

    fun setBridgeHandler(handler: Handler) {
        mHandler = handler
    }

    @JavascriptInterface
    fun Execute(command: String?, param: String?) {
        if (mHandler == null || command == null && param == null) {
            return
        }
        mHandler!!.post { invokeMethod(command, param) }
    }

    // web --> native
    private fun invokeMethod(command: String?, param: String?) {
        try {
            var result: Any? = null
            val bridge = mBridgeMethodSet[command]

            if (bridge != null) {
                val method = bridge.methodName
                if (method == null) {
                }

                if (param == null || param == "undefined") {
                    result = method.invoke(bridge.objectName)
                } else {
                    result = method.invoke(bridge.objectName, param)
                }
                if (!shouldWaitResultCommands?.contains(command)!!) {
                    notifyToWeb(command, result?.toString())
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    // native --> web
    fun notifyToWeb(command: String?, obj: String?) {
        if (mHandler != null && mWebView != null) {
            LogUtil.d(
                TAG,
                "[native --> web] command : $command, obj : $obj, [to : ${mWebView?.javaClass?.simpleName}]"
            )
            mHandler?.post { mWebView?.loadUrl(makeJavaScript(WEB_FUNC_NAME, command, obj)) }
        }
    }

    fun makeJavaScript(func: String, command: String?, obj: String?): String {
        val builder = StringBuilder()
        builder.append("javascript:")
        builder.append(func)
        builder.append("('$command'")
        builder.append(", ")
        builder.append("'$obj')")

        return builder.toString()
    }

    companion object {
        private val TAG = BridgeProxy::class.simpleName!!

        private val WEB_FUNC_NAME = "window.OnNativeEvent"

        internal var shouldWaitResultCommands: Set<*>? = null
    }
}
