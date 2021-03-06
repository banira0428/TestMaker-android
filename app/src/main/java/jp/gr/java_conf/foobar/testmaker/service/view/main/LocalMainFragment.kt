package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.epoxy.stickyheader.StickyHeaderLinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.analytics.FirebaseAnalytics
import jp.gr.java_conf.foobar.testmaker.service.CardCategoryBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.ItemTestBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.LocalMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.DynamicLinksCreator
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditActivity
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditTestActivity
import jp.gr.java_conf.foobar.testmaker.service.view.online.UploadTestActivity
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.EditTextDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class LocalMainFragment : Fragment() {

    private val localMainViewModel: LocalMainViewModel by viewModel()

    private val mainController by lazy { MainController(requireContext()) }
    private val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val firebaseAnalytic: FirebaseAnalytics by inject()

    private var binding: LocalMainFragmentBinding? = null

    private val testViewModel: TestViewModel by sharedViewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()
    private val service: CloudFunctionsService by inject()
    private val dynamicLinksCreator: DynamicLinksCreator by inject()

    private var selectedTest: Test? = null //ログイン時に一度画面から離れるので選択中の値を保持

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mainController.setOnClickListener(object : MainController.OnClickListener {

            override fun onClickTest(test: Test) {
                ListDialogFragment(
                        test.title,
                        listOf(
                                DialogMenuItem(title = getString(R.string.play), iconRes = R.drawable.ic_play_arrow_white_24dp, action = { playTest(test) }),
                                DialogMenuItem(title = getString(R.string.edit), iconRes = R.drawable.ic_edit_white, action = { editTest(test) }),
                                DialogMenuItem(title = getString(R.string.delete), iconRes = R.drawable.ic_delete_white, action = { deleteTest(test) }),
                                DialogMenuItem(title = getString(R.string.share), iconRes = R.drawable.ic_share_white, action = { shareTest(test) })
                        )
                ).show(requireActivity().supportFragmentManager, "TAG")
            }

            override fun onClickCategoryMenu(category: Category) {

                ListDialogFragment(
                        category.name,
                        listOf(
                                DialogMenuItem(title = getString(R.string.edit_category_name), iconRes = R.drawable.ic_edit_white, action = { editCategory(category) }),
                                DialogMenuItem(title = getString(R.string.delete), iconRes = R.drawable.ic_delete_white, action = { deleteCategory(category) })
                        )
                ).show(requireActivity().supportFragmentManager, "TAG")

            }

        })

        categoryViewModel.categories.observeNonNull(this) {
            mainController.categories = it
        }

        testViewModel.testsLiveData.observeNonNull(this) {
            mainController.tests = it
        }

        return DataBindingUtil.inflate<LocalMainFragmentBinding>(inflater, R.layout.local_main_fragment, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner

            fab.setOnClickListener {
                EditTestActivity.startActivity(requireActivity())
            }

            recyclerView.layoutManager = StickyHeaderLinearLayoutManager(requireContext())
            recyclerView.adapter = mainController.adapter

            EpoxyTouchHelper
                    .initDragging(mainController)
                    .withRecyclerView(recyclerView)
                    .forVerticalList()
                    .withTargets(CardCategoryBindingModel_::class.java, ItemTestBindingModel_::class.java)
                    .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<EpoxyModel<*>>() {
                        override fun onModelMoved(fromPosition: Int, toPosition: Int, modelBeingMoved: EpoxyModel<*>?, itemView: View?) {
                            val from = mainController.adapter.getModelAtPosition(fromPosition)
                            val to = mainController.adapter.getModelAtPosition(toPosition)

                            if (from is ItemTestBindingModel_ && to is ItemTestBindingModel_) {
                                testViewModel.swap(from.test(), to.test())
                            } else if (from is CardCategoryBindingModel_ && to is CardCategoryBindingModel_) {
                                categoryViewModel.swap(from.category(), to.category())
                            }
                        }
                    })
        }.root
    }

    private fun playTest(test: Test) {
        firebaseAnalytic.logEvent("play", Bundle())

        if (test.questions.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.message_null_questions), Toast.LENGTH_SHORT).show()
        } else {
            initDialogPlayStart(test)
        }
    }

    private fun editTest(test: Test) {
        firebaseAnalytic.logEvent("edit", Bundle())
        EditActivity.startActivity(requireActivity(), test.id)
    }

    private fun deleteTest(test: Test) {
        firebaseAnalytic.logEvent("delete", Bundle())

        ConfirmDangerDialogFragment(getString(R.string.message_delete_exam, test.title)) {
            testViewModel.delete(test)
            categoryViewModel.refresh()
            requireContext().showToast(getString(R.string.msg_success_delete_test))
        }.show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun shareTest(test: Test) {
        ListDialogFragment(
                getString(R.string.title_dialog_share),
                listOf(
                        DialogMenuItem(title = getString(R.string.button_upload), iconRes = R.drawable.ic_baseline_cloud_upload_24, action = { uploadTest(test) }),
                        DialogMenuItem(title = getString(R.string.button_convert_to_csv), iconRes = R.drawable.ic_edit_white, action = { convertTestToCSV(test) })
                )
        ).show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun uploadTest(test: Test) {
        localMainViewModel.getUser()?.let {
            firebaseAnalytic.logEvent("upload_from_share_local", Bundle())
            UploadTestActivity.startActivityForResult(this, REQUEST_UPLOAD_TEST, test.id)
        } ?: run {
            login(test)
        }
    }

    private fun convertTestToCSV(test: Test) {

        requireActivity().executeJobWithDialog(
                title = getString(R.string.converting),
                task = {
                    withContext(Dispatchers.IO) {
                        service.testToText(test.escapedTest.copy(lang = if (Locale.getDefault().language == "ja") "ja" else "en"))
                    }
                },
                onSuccess = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, it.text)
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                },
                onFailure = {
                    requireContext().showErrorToast(it)
                }
        )
    }

    private fun initDialogPlayStart(test: Test) {

        if (!sharedPreferenceManager.isShowPlaySettingDialog) {
            startAnswer(test, (test.startPosition + 1), test.limit)
        } else {

            childFragmentManager.setFragmentResultListener(REQUEST_PLAY_CONFIG, viewLifecycleOwner) { requestKey, bundle ->
                if (requestKey != REQUEST_PLAY_CONFIG) return@setFragmentResultListener

                val position = bundle.getInt(PlayConfigDialogFragment.RESULT_START_POSITION)
                val limit = bundle.getInt(PlayConfigDialogFragment.RESULT_LIMIT)
                startAnswer(test, position, limit)
            }

            PlayConfigDialogFragment.newInstance(test, REQUEST_PLAY_CONFIG)
                    .show(childFragmentManager, "TAG")
        }
    }

    private fun startAnswer(test: Test, start: Int, limit: Int) {

        var incorrect = false

        for (element in test.questions) if (!(element.isCorrect)) incorrect = true

        if (!incorrect && sharedPreferenceManager.refine) {

            Toast.makeText(requireContext(), getString(R.string.message_null_wrongs), Toast.LENGTH_SHORT).show()

        } else {

            val result = test.copy(
                    limit = limit,
                    startPosition = start - 1,
                    history = Calendar.getInstance().timeInMillis)

            testViewModel.update(result)

            PlayActivity.startActivity(requireActivity(), result.id)

        }
    }

    private fun login(test: Test) {

        selectedTest = test

        AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
                .setTitle(getString(R.string.login))
                .setMessage(getString(R.string.msg_not_login))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    val providers = arrayListOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build())

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setTosAndPrivacyPolicyUrls(
                                            "https://testmaker-1cb29.firebaseapp.com/terms",
                                            "https://testmaker-1cb29.firebaseapp.com/privacy")
                                    .build(),
                            REQUEST_SIGN_IN_UPLOAD)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
    }

    private fun editCategory(category: Category) {

        EditTextDialogFragment(
                title = getString(R.string.title_dialog_edit_category),
                defaultText = category.name,
                hint = getString(R.string.hint_category_name))
        { text ->

            testViewModel.renameAllInCategory(category.name, text)
            categoryViewModel.update(category.copy(name = text))

        }.show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun deleteCategory(category: Category) {

        ConfirmDangerDialogFragment(getString(R.string.message_delete_category, category.name)) {
            testViewModel.deleteAllInCategory(category.name)
            categoryViewModel.delete(category)
        }.show(requireActivity().supportFragmentManager, "TAG")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SIGN_IN_UPLOAD && resultCode == Activity.RESULT_OK) {
            selectedTest?.let {
                uploadTest(it)
                selectedTest = null
            }
        } else if (requestCode == REQUEST_UPLOAD_TEST && resultCode == Activity.RESULT_OK) {

            val documentId = data?.getStringExtra(EXTRA_DOCUMENT_ID) ?: return
            val testName = data.getStringExtra(EXTRA_TEST_NAME) ?: return

            requireActivity().executeJobWithDialog(
                    title = getString(R.string.msg_creating_share_test_link),
                    task = {
                        dynamicLinksCreator.createShareTestDynamicLinks(documentId)
                    },
                    onSuccess = {
                        it.shortLink?.let {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share_test, testName, it))
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            startActivity(shareIntent)
                        }
                    },
                    onFailure = {
                        requireContext().showToast(getString(R.string.msg_failure_share_test))
                    }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        testViewModel.refresh()
        categoryViewModel.refresh()
    }

    companion object {
        const val REQUEST_SIGN_IN_UPLOAD = 54321
        const val REQUEST_UPLOAD_TEST = 54322
        const val REQUEST_PLAY_CONFIG = "request_play_config"

        const val EXTRA_DOCUMENT_ID = "documentId"
        const val EXTRA_TEST_NAME = "testName"
    }
}