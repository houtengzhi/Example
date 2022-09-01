package com.yechy.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 *
 * Created by cloud on 2022/8/31.
 */
class ExampleAdapter: RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder>() {

    private val dataList = ExampleData.getData()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adaptive_list_view_item, parent, false)
        return ExampleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        val example = dataList[position]
        holder.titleTextView.text = example.title
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ExampleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView

        init {
            titleTextView = itemView.findViewById(R.id.text)
        }
    }

    class ExampleData(val title: String?) {

        companion object {
            fun getData(): List<ExampleData> {
                val dataList = mutableListOf<ExampleData>()
                for (index in 0..10) {
                    dataList.add(ExampleData("${index}: title"))
                }
                return dataList
            }
        }
    }
}