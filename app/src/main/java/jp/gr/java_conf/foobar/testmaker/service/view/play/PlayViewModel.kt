package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class PlayViewModel(private val repository: TestMakerRepository) : ViewModel() {

    var testId: Long = -1

    fun getTest(): RealmTest = repository.getTest(testId)
    fun resetSolving() = repository.resetSolving(testId)
    fun updateCorrect(quest: Quest, correct: Boolean) = repository.updateCorrect(quest, correct)
    fun updateSolving(quest: Quest, solving: Boolean) = repository.updateSolving(quest, solving)
    suspend fun loadImage(imagePath: String) = repository.loadImage(imagePath)


}