package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController

/**
 * Created by keita on 2016/05/29.
 */
class ResultAdapter(context: Context, private val mRealmController: RealmController, private val testId: Long) : RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { return ViewHolder(layoutInflater.inflate(R.layout.card_result, parent, false)) }

    override fun getItemCount(): Int { return mRealmController.getQuestionsSolved(testId).size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = mRealmController.getQuestionsSolved(testId)[position]

        holder.number.text = (position + 1).toString()
        holder.problem.text = data.problem.toString()
        holder.answer.text = data.answer.toString()

        if (data.correct) {

            holder.mark.setImageResource(R.drawable.right)

        } else {

            holder.mark.setImageResource(R.drawable.mistake)

        }

    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var number: TextView = v.findViewById(R.id.number)
        var problem: TextView = v.findViewById(R.id.problem)
        var answer: TextView = v.findViewById(R.id.answer)
        var mark: ImageView = v.findViewById(R.id.mark)

    }

}




