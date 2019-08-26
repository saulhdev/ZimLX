package org.zimmob.zimlx.globalsearch.providers.web

import android.content.Context
import com.android.launcher3.R

class DDGWebSearchProvider(context: Context) : WebSearchProvider(context) {
    override val searchUrl = "https://duckduckgo.com/?q=%s"
    override val suggestionsUrl = "https://ac.duckduckgo.com/ac/?q=%s&type=list"
    override val name = context.getString(R.string.web_search_ddg)

    override fun getIcon() = context.getDrawable(R.drawable.ic_ddg)!!
}