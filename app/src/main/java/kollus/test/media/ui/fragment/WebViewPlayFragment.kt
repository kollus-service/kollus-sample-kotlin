package kollus.test.media.ui.fragment

import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import kollus.test.media.MainActivity
import kollus.test.media.R
import kollus.test.media.hybrid.FullscreenableChromeClient
import kollus.test.media.hybrid.HybridWebView
import kollus.test.media.settings.AppConfig.Companion.MODE_MAKE_JWT
import kollus.test.media.utils.CommonUtil
import kollus.test.media.utils.LogUtil
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException


class WebViewPlayFragment : BaseFragment(), MainActivity.OnBackPressedListener {

    private var mWebView: HybridWebView? = null
    private var url: String? = null
    private val receiver = ArrayList<Any>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val root = inflater.inflate(R.layout.fragment_webview, container, false)

        mWebView = root.findViewById(R.id.webView) as HybridWebView

        mWebView?.let {
            it.registerBridge(receiver)
            it.setWebViewClient(CustomWebViewClient())
            //        mWebView.setWebChromeClient(new CustomWebChromeClient())
            it.setWebChromeClient(FullscreenableChromeClient(activity!!))
            it.setVerticalScrollbarOverlay(true)

            if (MODE_MAKE_JWT) {
                val mckey = "MEDIA_CONTENT_KEY"
                try {
                    url = CommonUtil.createUrl("CLIENT_USER_ID", mckey, false)
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                } catch (e: InvalidKeyException) {
                    e.printStackTrace()
                }
            }
            //it.loadUrl(url)
            //it.loadUrl("file:///android_asset/index.html");
            it.loadUrl("https://v.kr.kollus.com/SrTBm4vo?");
        }
        return root
    }

    override fun onBackPressed() {
        mWebView?.let {
            if (it.canGoBack()) {
                it.goBack()
            } else {
                activity?.finish()
            }
        } ?: activity?.finish()
    }

    internal inner class CustomWebViewClient : WebViewClient() {

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {}

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return super.shouldOverrideUrlLoading(view, url)
        }
    }

    internal inner class CustomWebChromeClient : WebChromeClient() {

        override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
            LogUtil.d(
                TAG, cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId()
            )
            return true
        }

        override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
            LogUtil.d(
                TAG, message + " -- From line "
                        + lineNumber + " of "
                        + sourceID
            )
        }

        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            return super.onJsAlert(view, url, message, result)
        }
    }

    companion object {

        private val TAG = WebViewPlayFragment::class.java.simpleName
    }
}
