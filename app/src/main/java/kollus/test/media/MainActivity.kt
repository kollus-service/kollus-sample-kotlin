package kollus.test.media

import android.os.Bundle
import androidx.fragment.app.Fragment;
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kollus.test.media.ui.fragment.ContentsListFragment
import kollus.test.media.ui.fragment.DownLoadFragment
import kollus.test.media.ui.fragment.PlayVideoFragment
import kollus.test.media.ui.fragment.WebViewPlayFragment
import kollus.test.media.utils.LogUtil

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.simpleName!!

    private var currentPos = 1

    private var FRAGMENT_PLAY_VIDEO_TEST = 0
    private var FRAGMENT_DOWNLOAD_TEST = 1
    private var FRAGMENT_CONTENTS_LIST_TEST = 2
    private var FRAGMENT_WEBVIEW_PLAY_TEST = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val btn_first = findViewById<View>(R.id.btn_first) as Button
        val btn_second = findViewById<View>(R.id.btn_second) as Button
        val btn_third = findViewById<View>(R.id.btn_third) as Button
        val btn_fourth = findViewById<View>(R.id.btn_fourth) as Button

        btn_first.setOnClickListener(movePageListener)
        btn_first.tag = FRAGMENT_PLAY_VIDEO_TEST
        btn_second.setOnClickListener(movePageListener)
        btn_second.tag = FRAGMENT_DOWNLOAD_TEST
        btn_third.setOnClickListener(movePageListener)
        btn_third.tag = FRAGMENT_CONTENTS_LIST_TEST
        btn_fourth.setOnClickListener(movePageListener)
        btn_fourth.tag = FRAGMENT_WEBVIEW_PLAY_TEST

        startFragment(FRAGMENT_PLAY_VIDEO_TEST)
    }

    private var movePageListener: View.OnClickListener = View.OnClickListener { view ->
        val tag = view.tag as Int
        LogUtil.d(TAG, "onClick() : pos : $tag")
        startFragment(tag)
    }

    private fun startFragment(tagNo: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        if (currentPos == tagNo) {
            return
        }
        when (tagNo) {
            FRAGMENT_PLAY_VIDEO_TEST -> {
                transaction.replace(R.id.container, PlayVideoFragment())
                transaction.commit()
                currentPos = FRAGMENT_PLAY_VIDEO_TEST
            }

            FRAGMENT_DOWNLOAD_TEST -> {
                transaction.replace(R.id.container, DownLoadFragment())
                transaction.commit()
                currentPos = FRAGMENT_DOWNLOAD_TEST
            }

            FRAGMENT_CONTENTS_LIST_TEST -> {
                transaction.replace(R.id.container, ContentsListFragment())
                transaction.commit()
                currentPos = FRAGMENT_CONTENTS_LIST_TEST
            }

            FRAGMENT_WEBVIEW_PLAY_TEST -> {
                transaction.replace(R.id.container, WebViewPlayFragment())
                transaction.commit()
                currentPos = FRAGMENT_WEBVIEW_PLAY_TEST
            }

            else -> {
            }
        }
    }

    fun setCurrentPos(currentPos: Int) {
        this.currentPos = currentPos
    }

    fun replaceFragment(fragment: Fragment, type: Int, urlOrMcKey: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val bundle = Bundle()
        bundle.putInt("playType", type)
        bundle.putString("urlOrMcKey", urlOrMcKey)
        fragment.arguments = bundle

        fragmentTransaction.replace(R.id.container, fragment).commit()
    }

    override fun onResume() {
        LogUtil.d(TAG, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        LogUtil.d(TAG, "onPause()")
        super.onPause()
    }

    override fun onDestroy() {
        LogUtil.d(TAG, "onDestroy()")
        super.onDestroy()
    }

    override fun onBackPressed() {
        val fragmentList = supportFragmentManager.fragments
        if (fragmentList != null) {
            for (fragment in fragmentList) {
                if (fragment is OnBackPressedListener) {
                    (fragment as OnBackPressedListener).onBackPressed()
                }
            }
        }
    }

    interface OnBackPressedListener {
        fun onBackPressed()
    }

}
