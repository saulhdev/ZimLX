package org.zimmob.zimlx.globalsearch.providers.web

import android.content.Context
import com.android.launcher3.R
import org.zimmob.zimlx.util.locale

class QwantWebSearchProvider(context: Context) : WebSearchProvider(context) {
    override val searchUrl = "https://www.qwant.com/?q=%s"
    override val suggestionsUrl = "https://api.qwant.com/api/suggest/?q=%s&client=opensearch&lang=${context.locale.language}"
    override val name = context.getString(R.string.web_search_qwant)

    override fun getIcon() = context.getDrawable(R.drawable.ic_qwant)!!
}