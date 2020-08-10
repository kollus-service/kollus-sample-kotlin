package kollus.test.media.ui.adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import com.kollus.sdk.media.content.KollusContent

import java.util.ArrayList

import kollus.test.media.R

class DownloadAdapter(private val mContext: Context, private val mContentsList: ArrayList<KollusContent>?) :
    ArrayAdapter<KollusContent>(mContext, R.layout.file_list, mContentsList) {

    private val mResources: Resources
    private val mInflater: LayoutInflater


    init {
        mResources = mContext.resources
        mInflater = LayoutInflater.from(mContext)
    }

    override fun getCount(): Int {
        return mContentsList?.size ?: 0

    }

    override fun getItem(position: Int): KollusContent? {
        return mContentsList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ContentsViewHolder

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.file_list, parent, false)

            holder = ContentsViewHolder()
            holder.icon = convertView!!.findViewById<View>(R.id.icon) as ImageView
            holder.txtPercent = convertView.findViewById(R.id.list_percent) as TextView
            holder.fileName = convertView.findViewById(R.id.file_name) as TextView
            holder.fileSize = convertView.findViewById(R.id.file_size) as TextView
            holder.timeBar = convertView.findViewById(R.id.download_progress) as ProgressBar
            holder.btnDelete = convertView.findViewById(R.id.download_cancel) as ImageView

            convertView.tag = holder
        } else {
            holder = convertView.tag as ContentsViewHolder
        }

        val content = mContentsList!![position]
        val thumbnail = content.thumbnailPath
        if (thumbnail != null && !thumbnail.startsWith("http://")) {
            val bm = BitmapFactory.decodeFile(thumbnail)
            if (bm != null)
                holder.icon!!.setImageBitmap(bm)
        }
        holder.btnDelete!!.tag = content

        val cource = content.course
        val subcource = content.subCourse
        val title: String?
        if (cource != null && cource.length > 0) {
            if (subcource != null && subcource.length > 0)
                title = "$cource($subcource)"
            else
                title = cource
        } else
            title = subcource
        holder.fileName!!.text = title
        if (content.downloadError) {
            holder.fileName!!.setTextColor(Color.RED)
            holder.fileName!!.paintFlags = holder.fileName!!.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.fileName!!.setTextColor(Color.BLACK)
            holder.fileName!!.paintFlags = holder.fileName!!.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        val strRecvFileSize = content.receivedSize.toString()
        val strFileSize = content.fileSize.toString()
        holder.fileSize!!.text = String.format("%s / %s", strRecvFileSize, strFileSize)
        holder.timeBar!!.progress = content.downloadPercent
        holder.btnDelete!!.setOnClickListener {
            //				view.setVisibility(View.GONE);
        }

        if (content.isCompleted) {
            holder.txtPercent!!.visibility = View.GONE
            holder.btnDelete!!.visibility = View.GONE
        } else {
            holder.txtPercent!!.visibility = View.VISIBLE
            holder.txtPercent!!.text = content.downloadPercent.toString() + "%"
            holder.btnDelete!!.visibility = View.VISIBLE
        }

        return convertView
    }

    inner class ContentsViewHolder {
        var check: CheckBox? = null
        var icon: ImageView? = null
        var folderName: TextView? = null
        var fileName: TextView? = null
        var fileSize: TextView? = null
        var playTime: TextView? = null
        var duration: TextView? = null
        var timeBar: ProgressBar? = null
        var btnDetail: ImageView? = null
        var icDrm: ImageView? = null
        var icHang: ImageView? = null
        var txtPercent: TextView? = null
        var btnDelete: ImageView? = null
    }

    companion object {
        private val TAG = DownloadAdapter::class.java.simpleName
    }
}

