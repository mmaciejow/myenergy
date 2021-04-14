package net.myenv.myenergy.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.myenv.myenergy.databinding.HistoryTableListHeaderBinding
import net.myenv.myenergy.databinding.HistoryTableListItemBinding
import net.myenv.myenergy.model.Energy


class HistoryEnergyViewAdapter(private val energyList: List<Energy>,
                               private val showWeather: Boolean = true,
                               private val listener: HistoryEnergyAdapterListener? = null)
    : RecyclerView.Adapter<HistoryEnergyViewAdapter.HistoryViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    fun onClick(id: Int){
        val date = energyList[id].id
        date?.let { listener?.onClick(it) }
    }

    interface HistoryEnergyAdapterListener{
        fun onClick(date: Long)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if  (viewType == TYPE_HEADER) {
            val binding = HistoryTableListHeaderBinding.inflate(inflater, parent, false)
            return HistoryViewHolder(binding)
        } else {
            val binding = HistoryTableListItemBinding.inflate(inflater, parent, false)
            val viewHolder = HistoryViewHolder(binding)
            if (listener != null)
                binding.root.setOnClickListener {  onClick( viewHolder.adapterPosition - 1) }
            return viewHolder
        }

    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val rowPos = holder.adapterPosition
        if (getItemViewType(position) == TYPE_ITEM ) {
            val item = energyList[rowPos - 1]
            holder.bindContent(item)
        }

    }

    override fun getItemCount(): Int {
        return energyList.size + 1 // one more to add header row
    }

    override fun getItemViewType(position: Int): Int = when(position) {
        0 -> TYPE_HEADER
        else -> TYPE_ITEM
    }

    inner class HistoryViewHolder : RecyclerView.ViewHolder {
        private var headerBinding: HistoryTableListHeaderBinding? = null
        private var itemBinding: HistoryTableListItemBinding? = null

        constructor(binding: HistoryTableListHeaderBinding) : super(binding.root) {
            headerBinding = binding
            if (showWeather){
                headerBinding!!.iconWeather.visibility = View.VISIBLE
            }
        }

        constructor(binding: HistoryTableListItemBinding) : super(binding.root) {
            itemBinding = binding
            if (showWeather)
                itemBinding!!.iconWeather.visibility = View.VISIBLE
        }


        fun bindContent(energy: Energy){
            itemBinding?.let { view ->
                view.date.text = energy.date
                view.production.text = energy.pvProduction.toString()
                if (showWeather){

                    energy.weatherIcon?.let { view.iconWeather.setImageResource(it) }
                }


            }

        }
    }

}

