package org.zimmob.zimlx.backup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import org.zimmob.zimlx.settings.ui.SettingsBaseActivity

class BackupListActivity : SettingsBaseActivity(), BackupListAdapter.Callbacks {

    private val permissionRequestReadExternalStorage = 0

    private val bottomSheet by lazy { BottomSheetDialog(this) }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerView) }
    private val adapter by lazy { BackupListAdapter(this) }

    private val restoreBackup by lazy { bottomSheetView.findViewById<View>(R.id.action_restore_backup) }
    private val shareBackup by lazy { bottomSheetView.findViewById<View>(R.id.action_share_backup) }
    private val removeBackup by lazy { bottomSheetView.findViewById<View>(R.id.action_remove_backup_from_list) }
    private val divider by lazy { bottomSheetView.findViewById<View>(R.id.divider) }

    private val bottomSheetView by lazy {
        layoutInflater.inflate(R.layout.backup_bottom_sheet,
                findViewById(android.R.id.content), false)
    }

    private var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_list)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        restoreBackup.setOnClickListener {
            bottomSheet.dismiss()
            openRestore(currentPosition)
        }
        shareBackup.setOnClickListener {
            bottomSheet.dismiss()
            shareBackup(currentPosition)
        }
        removeBackup.setOnClickListener {
            bottomSheet.dismiss()
            removeItem(currentPosition)
        }
        bottomSheet.setContentView(bottomSheetView)

        adapter.callbacks = this
        loadLocalBackups()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        Utilities.checkRestoreSuccess(this)
    }

    private fun loadLocalBackups() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(findViewById(android.R.id.content), R.string.read_external_storage_required,
                        Snackbar.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    permissionRequestReadExternalStorage)
        } else {
            adapter.setData(ZimBackup.listLocalBackups(this))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            permissionRequestReadExternalStorage -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    adapter.setData(ZimBackup.listLocalBackups(this))
                }
            }
            else -> {

            }
        }
    }

    override fun openBackup() {
        startActivityForResult(Intent(this, NewBackupActivity::class.java), 1)
    }

    override fun openRestore() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = ZimBackup.MIME_TYPE
        intent.putExtra(Intent.EXTRA_MIME_TYPES, ZimBackup.EXTRA_MIME_TYPES)
        startActivityForResult(intent, 2)
    }

    override fun openRestore(position: Int) {
        startActivity(Intent(this, RestoreBackupActivity::class.java).apply {
            putExtra(RestoreBackupActivity.EXTRA_URI, adapter[position].uri.toString())
        })
    }

    override fun openEdit(position: Int) {
        currentPosition = position
        val visibility = if (adapter[position].meta != null) View.VISIBLE else View.GONE
        restoreBackup.visibility = visibility
        shareBackup.visibility = visibility
        divider.visibility = visibility
        bottomSheetView.findViewById<TextView>(android.R.id.title).text =
                adapter[position].meta?.name ?: getString(R.string.backup_invalid)
        bottomSheet.show()
    }

    private fun removeItem(position: Int) {
        adapter.removeItem(position)
        saveChanges()
    }

    private fun shareBackup(position: Int) {
        val shareTitle = getString(R.string.backup_share_title)
        val shareText = getString(R.string.backup_share_text)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = ZimBackup.MIME_TYPE
        shareIntent.putExtra(Intent.EXTRA_STREAM, adapter[position].uri)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(shareIntent, shareTitle))
    }

    private fun saveChanges() {
        Utilities.getZimPrefs(this).blockingEdit {
            recentBackups.replaceWith(adapter.toUriList())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                adapter.addItem(ZimBackup(this, resultData.data))
                saveChanges()
            }
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                val takeFlags = intent.flags and
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                contentResolver.takePersistableUriPermission(resultData.data, takeFlags)
                val uri = resultData.data
                if (!Utilities.getZimPrefs(this).recentBackups.contains(uri)) {
                    adapter.addItem(ZimBackup(this, uri))
                    saveChanges()
                }
                openRestore(0)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, resultData)
        }
    }
}
