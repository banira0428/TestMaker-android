package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityPlayBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionsBuilder
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.setFontSize
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.result.ComposeResultActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayActivity : BaseActivity() {

    private val playViewModel: PlayViewModel by viewModel {
        parametersOf(
            test,
            QuestionsBuilder(test.questions)
                .retry(intent.hasExtra("isRetry"))
                .startPosition(test.startPosition)
                .mistakeOnly(sharedPreferenceManager.refine)
                .shuffle(sharedPreferenceManager.random)
                .limit(test.limit)
                .build()
        )
    }
    private val testViewModel: TestViewModel by viewModel()
    private val logger: TestMakerLogger by inject()

    private lateinit var test: Test

    private val startTime = System.currentTimeMillis()

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityPlayBinding>(this, R.layout.activity_play).apply {
            lifecycleOwner = this@PlayActivity
            viewModel = playViewModel
        }
    }

    private var soundCorrect: MediaPlayer? = null
    private var soundIncorrect: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundCorrect = MediaPlayer.create(this, R.raw.correct)
        soundIncorrect = MediaPlayer.create(this, R.raw.mistake)

        testViewModel.tests.find { it.id == intent.getLongExtra("id", -1L) }?.let {
            test = it
            supportActionBar?.setTitle(test.title)
        }

        createAd(binding.adView)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        playViewModel.state.observeNonNull(this) {
            if (it == State.FINISH) {
                ComposeResultActivity.startActivity(
                    this,
                    test.id,
                    System.currentTimeMillis() - startTime
                )
            }
        }

        playViewModel.judgeState.observeNonNull(this) {
            if (it == JudgeState.NONE) return@observeNonNull
            if (sharedPreferenceManager.audio && !playViewModel.isManual) {
                when (it) {
                    JudgeState.CORRECT -> soundCorrect?.start()
                    JudgeState.INCORRECT -> soundIncorrect?.start()
                    else -> {
                    }
                }
            }

            playViewModel.selectedQuestion.value?.let { question ->
                testViewModel.update(
                    question.copy(
                        isSolved = true,
                        isCorrect = it == JudgeState.CORRECT
                    )
                )
                logger.logAnswerQuestion(question)
            }
        }

        testViewModel.update(test.copy(
            questions = test.questions.map {
                it.copy(isSolved = false)
            }
        ))

        playViewModel.loadNextQuestion()

        binding.let {
            it.question.setFontSize(sharedPreferenceManager.playFontSize)
            it.textYourAnswer.setFontSize(sharedPreferenceManager.playFontSize)
            it.textAnswer.setFontSize(sharedPreferenceManager.playFontSize)
            it.textExplanation.setFontSize(sharedPreferenceManager.playFontSize)
        }
    }

    override fun onStart() {
        super.onStart()
        playViewModel.selectedQuestion.observeNonNull(this) {
            if (playViewModel.isManual) return@observeNonNull
            when (it.type) {
                Constants.WRITE -> binding.editAnswer.requestFocus()
                Constants.COMPLETE -> binding.editAnswersFirst.editAnswer.requestFocus()
                else -> {
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundCorrect?.release()
        soundCorrect = null
        soundIncorrect?.release()
        soundIncorrect = null
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onBackPressed() {
        ConfirmDangerDialogFragment(
            title = getString(R.string.play_dialog_confirm_interrupt),
            buttonText = getString(R.string.ok)
        ) {
            super.onBackPressed()
        }.show(supportFragmentManager, "TAG")
    }

    companion object {

        fun startActivity(activity: Activity, id: Long) {
            val intent = Intent(activity, PlayActivity::class.java).apply {
                putExtra("id", id)
            }
            activity.startActivity(intent)
        }

        fun startActivity(activity: Activity, id: Long, isRetry: Boolean) {
            val intent = Intent(activity, PlayActivity::class.java).apply {
                putExtra("id", id)
                putExtra("isRetry", isRetry)
            }
            activity.startActivity(intent)
        }
    }
}
