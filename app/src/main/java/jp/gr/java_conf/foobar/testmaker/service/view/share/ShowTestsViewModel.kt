package jp.gr.java_conf.foobar.testmaker.service.view.share

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.CategoryRepository
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class ShowTestsViewModel(private val repository: TestMakerRepository, private val auth: Auth, private val categoryRepository: CategoryRepository) : ViewModel() {

    fun updateStart(test: RealmTest, start: Int) = repository.updateStart(test, start)
    fun updateLimit(test: RealmTest, limit: Int) = repository.updateLimit(test, limit)
    suspend fun uploadTest(test: RealmTest, documentId: String): String = repository.createTest(test, "", documentId)

    fun getUser(): FirebaseUser? = auth.getUser()

}