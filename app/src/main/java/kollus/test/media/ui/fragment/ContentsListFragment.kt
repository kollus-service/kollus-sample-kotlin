package kollus.test.media.ui.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView

import com.kollus.sdk.media.KollusStorage
import com.kollus.sdk.media.content.KollusContent

import java.util.ArrayList

import kollus.test.media.MainActivity
import kollus.test.media.R
import kollus.test.media.ui.adapter.DownloadAdapter
import kollus.test.media.utils.LogUtil


class ContentsListFragment : BaseFragment(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    //private var mStorage: KollusStorage? = null
    private var mListView: ListView? = null
    private var mAdapter: DownloadAdapter? = null
    private var mDownLoadList: ArrayList<KollusContent> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogUtil.d(TAG, "onCreateView")

        val root = inflater.inflate(R.layout.fragment_contentlist, container, false)

        mStorage = KollusStorage.getInstance(context)
        mDownLoadList = mStorage!!.downloadContentList

        mListView = root.findViewById(R.id.contents_list)

        mAdapter = DownloadAdapter(context!!, mDownLoadList)
        mListView!!.adapter = mAdapter
        mListView!!.onItemClickListener = this
        mListView!!.onItemLongClickListener = this

        return root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroy() {
        LogUtil.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        LogUtil.d(TAG, "onItemClick() position : $position")

        val content = mAdapter!!.getItem(position)
        if (content != null) {
            (activity as MainActivity).replaceFragment(PlayVideoFragment.newInstance(), 1, content.mediaContentKey)
        }
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        LogUtil.d(TAG, "onItemLongClick() position : $position")

        val content = mAdapter!!.getItem(position)
        if (content != null) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirm").setMessage("Delete download content?")
            builder.setPositiveButton("Ok") { dialog, which ->
                val errorCode = mStorage!!.remove(content.mediaContentKey)
                LogUtil.d(TAG, "errorCode : $errorCode")
                mDownLoadList.remove(content)
                mAdapter!!.notifyDataSetChanged()
            }

            builder.setNegativeButton("Cancel") { dialog, which -> }

            val alertDialog = builder.create()
            alertDialog.show()

            return true
        }
        return false
    }

    companion object {

        private val TAG = ContentsListFragment::class.java.simpleName


        fun newInstance(): ContentsListFragment {
            return ContentsListFragment()
        }
    }
}
