package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class ShowTestsViewModel(private val repository: TestMakerRepository) : ViewModel() {

    fun getTest(testId: Long): Test = repository.getTest(testId)
    fun deleteTest(test: Test) = repository.deleteTest(test)
    fun updateHistory(test: Test) = repository.updateHistory(test)
    fun updateStart(test: Test, start: Int) = repository.updateStart(test, start)
    fun updateLimit(test: Test, limit: Int) = repository.updateLimit(test, limit)
}