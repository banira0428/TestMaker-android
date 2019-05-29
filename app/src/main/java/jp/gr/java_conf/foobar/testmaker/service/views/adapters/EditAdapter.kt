package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton

/**
 * Created by keita on 2016/05/29.
 */
class EditAdapter(private val context: Context, private val realmController: RealmController, private val testId: Long) : androidx.recyclerview.widget.RecyclerView.Adapter<EditAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var filter: Boolean = false
    var searchWord: String = ""

    private var listener: EditAdapter.OnClickListener? = null

    interface OnClickListener {
        fun onClickEditQuestion(question: Quest)
        fun onClickDeleteQuestion(data: Quest)
    }

    fun setOnClickListener(listener: EditAdapter.OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.card_question, parent, false))
    }

    override fun getItemCount(): Int {

        return if (filter) realmController.getFilterQuestions(testId, searchWord).size
        else realmController.getQuestions(testId).size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = init(position)

        holder.itemView.setOnClickListener { }

        holder.itemView.setOnLongClickListener{ true }

        holder.order.text = (data.order + 1).toString()
        holder.problem.text = context.getString(R.string.question, data.problem)
        holder.answer.text = context.getString(R.string.answer, data.answer)

        holder.edit.setOnClickListener {
            if (listener != null) listener?.onClickEditQuestion(data)
        }

        holder.delete.setOnClickListener {
            if (listener != null) listener?.onClickDeleteQuestion(data)
        }

    }

    private fun init(position: Int): Quest {

        return if (filter) realmController.getFilterQuestions(testId, searchWord)[position]
        else realmController.getQuestions(testId)[position]

    }

    class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {

        val order: TextView = v.findViewById(R.id.order)
        val problem: TextView = v.findViewById(R.id.problem)
        val answer: TextView = v.findViewById(R.id.answer)
        val edit: ImageTextButton = v.findViewById(R.id.edit)
        val delete: ImageTextButton = v.findViewById(R.id.delete)

    }

}




