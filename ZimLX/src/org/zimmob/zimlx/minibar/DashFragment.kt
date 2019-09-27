package org.zimmob.zimlx.minibar

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.Utilities
import org.zimmob.zimlx.preferences.RecyclerViewFragment

class DashFragment : RecyclerViewFragment() {
    private val adapter by lazy { DashEditAdapter(requireContext()) }


    override fun onRecyclerViewCreated(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        adapter.itemTouchHelper = ItemTouchHelper(adapter.TouchHelperCallback()).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    override fun onResume() {
        super.onResume()
        Utilities.getZimPrefs(context).minibarItems = adapter.getDashItems()
    }

    override fun onPause() {
        super.onPause()

        Utilities.getZimPrefs(context).minibarItems = adapter.getDashItems()
    }
}