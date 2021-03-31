package net.myenv.myenergy.ui.statistics


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_TEXT_END
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import net.myenv.myenergy.MainViewModel
import net.myenv.myenergy.R
import net.myenv.myenergy.databinding.FragmentStatisticsBinding
import net.myenv.myenergy.databinding.TableRowBinding
import net.myenv.myenergy.model.EnergyStats
import net.myenv.myenergy.model.Result
import net.myenv.myenergy.utils.extension.getDatefromTimestamp
import net.myenv.myenergy.utils.extension.show
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class StatisticsFragment : Fragment() {

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val progressBar by lazy {  binding.progress.root }
    private val error by lazy { binding.notFoundData }
    private val statsTable by lazy { binding.statsTable }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.getStats()
        initListener()

    }

    private fun initListener() {
        mainViewModel.statsEnergy.observe(viewLifecycleOwner, {
            when (it) {
                is Result.Loading -> {
                    progressBar.show(true)
                }
                is Result.Success -> {
                    error.show(false)
                    progressBar.show(false)
                    statsTable.show(true)
                    val stats = it.data as EnergyStats
                    showData(stats)
                }
                is Result.Failure -> {
                    error.show(false)
                    progressBar.show(true)
                }
            }
        })
    }

    private fun showData(stats: EnergyStats) {
        addRow(R.string.total_production, "${stats.pv_production} KW")
        addRow(R.string.max_power, stats.max_power.toString())
        addRow(R.string.max_power_date, getDatefromTimestamp(stats.max_power_date,"yyyy-MM-dd"))

    }


    private fun addRow(name: Int, value: String){
        val tableRow = TableRowBinding.inflate(this.layoutInflater)
        tableRow.value.textAlignment = TEXT_ALIGNMENT_TEXT_END
        tableRow.name.text = getString(name)
        tableRow.value.text = value
        statsTable.addView(tableRow.root)
    }



}
