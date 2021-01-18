package jp.gr.java_conf.foobar.testmaker.service.view.share

import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.itemMenu

class ListDialogController : EpoxyController() {

    var menus = emptyList<MenuItem>()
        set(value) {
            field = value
            requestModelBuild()
        }

    override fun buildModels() {

        menus.forEachIndexed { index, it ->

            itemMenu {
                id(index)
                text(it.title)
                iconRes(it.iconRes)
                onClick { _, _, _, _ ->
                    it.action()
                }
            }
        }
    }
}