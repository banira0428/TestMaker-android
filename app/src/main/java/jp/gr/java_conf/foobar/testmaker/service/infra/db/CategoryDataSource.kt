package jp.gr.java_conf.foobar.testmaker.service.infra.db

import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate
import jp.gr.java_conf.foobar.testmaker.service.domain.Category

class CategoryDataSource(private val realm: Realm){

    init {
        realm.executeTransaction {
            realm.where(Cate::class.java).findAll().forEachIndexed { index, it ->
                val category = Category()
                category.id = index.toLong()
                category.name = it.category
                category.color = it.color
                category.order = it.order
                it.deleteFromRealm()
            }
        }
    }

    fun create(category: Category) {
        realm.executeTransaction {
            it.copyToRealm(category)
        }
    }

    fun get(): List<Category> = realm.copyFromRealm(realm.where(Category::class.java).findAll())?.sortedBy { it.order }
            ?: listOf()

    fun delete(category: Category) {
        realm.executeTransaction {
            realm.where(Category::class.java).equalTo("category", category.name).findFirst()?.deleteFromRealm()
        }
    }

}