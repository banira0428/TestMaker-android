package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Quest

class CheckBoxQuestionAdapter(private val context: Context, private val array: Array<Quest>): androidx.recyclerview.widget.RecyclerView.Adapter<CheckBoxQuestionAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var checkBoxStates: Array<Boolean> = Array(array.size){false}

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return CheckBoxQuestionAdapter.ViewHolder(layoutInflater.inflate(R.layout.list_check_box_question, parent, false))
    }

    override fun getItemCount(): Int {
        return array.size
    }

    fun getItems(): Array<Quest>{
        return array
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val checkBox = holder.checkBox
        checkBox.isChecked = checkBoxStates[position]

        holder.problem.text = array[position].problem

        holder.answer.text = array[position].answer

        holder.itemView.setOnClickListener {

            checkBox.isChecked = !checkBox.isChecked
            checkBoxStates[position] = checkBox.isChecked

        }

        checkBox.setOnClickListener {
            checkBoxStates[position] = checkBox.isChecked
        }

    }

    class ViewHolder(v: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {

        val checkBox = v.findViewById<CheckBox>(R.id.check_move)
        val problem = v.findViewById<TextView>(R.id.problem)
        val answer = v.findViewById<TextView>(R.id.answer)
    }


}