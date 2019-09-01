package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.util.Log
import android.widget.Toast
import io.realm.*
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager
import java.util.*

/**
 * Created by keita on 2017/02/08.
 */

class RealmController(context: Context, config: RealmConfiguration) {
    private val realm: Realm = Realm.getInstance(config)

    fun copyToRealm(it: Test) {
        realm.beginTransaction()

        if(it.id == 0L){
            it.id = realm.where(Test::class.java).max("id")?.toLong()?.plus(1) ?: -1L
        }

        realm.copyToRealmOrUpdate(it)
        realm.commitTransaction()

    }

    val maxQuestionId: Long
        get() {
            return realm.where(Quest::class.java).max("id")?.toLong() ?: 1
        }

}
