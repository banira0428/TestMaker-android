package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import kotlinx.android.synthetic.main.activity_my_page.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class FirebaseMyPageActivity : BaseActivity() {

    private val viewModel: FirebaseMyPageViewModel by viewModel()

    private lateinit var adapter: FirebaseMyPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        createAd(container)

        initToolBar()

        adapter = FirebaseMyPageAdapter(baseContext)
        adapter.download = { data: DocumentSnapshot ->
            viewModel.downloadTest(data.id)
        }
        adapter.showInfo = { data: FirebaseTest ->
            val dialogLayout = LayoutInflater.from(this@FirebaseMyPageActivity).inflate(R.layout.dialog_online_test_info, findViewById(R.id.layout_dialog_info))

            val textInfo = dialogLayout.findViewById<TextView>(R.id.text_info)
            textInfo.text = getString(R.string.info_firebase_test, data.userName, data.getDate(), data.overview)

            val builder = AlertDialog.Builder(this@FirebaseMyPageActivity, R.style.MyAlertDialogStyle)
            builder.setView(dialogLayout)
            builder.setTitle(data.name)
            builder.show()
        }
        adapter.delete = { data: DocumentSnapshot ->

            val builder = AlertDialog.Builder(this@FirebaseMyPageActivity, R.style.MyAlertDialogStyle)
            builder.setTitle(getString(R.string.delete_exam))
            builder.setMessage(getString(R.string.message_delete_exam, data.toObject(FirebaseTest::class.java)?.name))
            builder.setPositiveButton(android.R.string.ok) { _, _ ->

                viewModel.deleteTest(data.id)

            }
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.create().show()

        }


        viewModel.getMyTests().observeNonNull(this) {
            recycler_view.visibility = View.VISIBLE
            swipe_refresh.isRefreshing = false

            recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
            recycler_view.setHasFixedSize(true)
            recycler_view.adapter = this.adapter
            adapter.array = it
        }

        viewModel.getDownloadTest().observeNonNull(this) {

            viewModel.convert(it)
            val intent = Intent(this@FirebaseMyPageActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)

        }

        edit_profile.setOnClickListener {

            val user = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener

            val dialogLayout = LayoutInflater.from(this@FirebaseMyPageActivity).inflate(R.layout.dialog_edit_user_name, findViewById(R.id.layout_dialog_edit_user))
            val editUsername = dialogLayout.findViewById<EditText>(R.id.edit_user_name)
            editUsername.setText(user.displayName)
            val buttonSaveProfile = dialogLayout.findViewById<Button>(R.id.button_save_profile)
            val builder = AlertDialog.Builder(this@FirebaseMyPageActivity, R.style.MyAlertDialogStyle)
            builder.setView(dialogLayout)
            builder.setTitle(getString(R.string.message_edit_profile))
            val dialog = builder.show()

            buttonSaveProfile.setOnClickListener {

                if (editUsername.text.toString().isEmpty()) {
                    Toast.makeText(baseContext, getString(R.string.message_shortage), Toast.LENGTH_SHORT).show()

                } else {
                    viewModel.updateProfile(editUsername.text.toString()) { reloadUserProfile() }

                    dialog.dismiss()
                }
            }
        }

        swipe_refresh.setOnRefreshListener {
            viewModel.fetchMyTests()
        }

        reloadUserProfile()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_firebase_my_page, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val actionId = item.itemId

        when (actionId) {
            R.id.nav_logout -> {

                AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                        .setTitle(getString(R.string.logout))
                        .setMessage(getString(R.string.msg_logout))
                        .setPositiveButton(getString(R.string.ok)) { _, _ ->

                            FirebaseAuth.getInstance().signOut()
                            finish()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
            }
            android.R.id.home -> {

                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun reloadUserProfile() {

        val user = FirebaseAuth.getInstance().currentUser ?: return
        text_user_name.text = getString(R.string.creator_name, user.displayName)

    }
}