package kollus.test.media.download

import com.kollus.sdk.media.content.KollusContent

class DownloadInfo(val folder: String?, val url: String) {
    val kollusContent: KollusContent

    init {
        kollusContent = KollusContent()
    }
}
