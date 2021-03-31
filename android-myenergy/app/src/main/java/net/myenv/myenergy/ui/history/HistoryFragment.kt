package net.myenv.myenergy.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import net.myenv.myenergy.MainViewModel
import net.myenv.myenergy.R
import net.myenv.myenergy.model.Energy
import net.myenv.myenergy.model.Result
import net.myenv.myenergy.ui.history.DialogSelectDates.Companion.SHOW_DAY_PICKER
import net.myenv.myenergy.ui.history.DialogSelectDates.Companion.SHOW_MONTH_PICKER
import net.myenv.myenergy.ui.history.DialogSelectDates.Companion.SHOW_YEAR_PICKER
import net.myenv.myenergy.utils.extension.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


class HistoryFragment : Fragment() {

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private var selected_tab = 0
    private var dailyStartDate =  Date().minusDays(14)
    private var dailyEndDate = Date().startDay()
    private var monthlyStartDate =  Date().minusMonth(12)
    private var monthlyEndDate = Date().startDay()
    private var annuallyStartDate =  Date().minusYear(6)
    private var annuallyEndDate =  Date().startDay()

    private var firstDailyLoad = true
    private var firstMonthlyLoad = true
    private var firstAnnualyLoad = true

    private lateinit var historyEnergyList : RecyclerView
    private lateinit var tabs :TabLayout
    private lateinit var historyDates :Button
    private lateinit var notFoundData :TextView
    private lateinit var progress :View


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        historyEnergyList = root.findViewById(R.id.history_energy_list)
        tabs = root.findViewById(R.id.history_tab_layout)
        historyDates = root.findViewById(R.id.historyDates)
        notFoundData = root.findViewById(R.id.not_found_data)
        progress = root.findViewById(R.id.progress)
        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        firstDailyLoad = true
        firstMonthlyLoad = true
        firstAnnualyLoad = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initTabs()
        initListener()
        showDailyEnergy(true)
    }


    private fun initTabs() {
        val dayily = tabs.newTab().apply { text = getString(R.string.dayily) }
        val monthly = tabs.newTab().apply { text = getString(R.string.monthly) }
        val annually = tabs.newTab().apply { text = getString(R.string.annually) }
        tabs.addTab(dayily)
        tabs.addTab(monthly)
        tabs.addTab(annually)
        tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selected_tab = tab.position
                when (selected_tab) {
                    0 -> { showDailyEnergy(false) }
                    1 -> { showMonthlyEnergy(false) }
                    2 -> { showAnnuallyEnergy(false) }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }


    private fun initListener() {
        historyDates.setOnClickListener { showDialog() }
        val dailyListener = object : HistoryEnergyViewAdapter.HistoryEnergyAdapterListener{
            override fun onClick(date: Long) {
                val args = Bundle()
                args.putLong("date", date)
                Navigation.findNavController(view!!).navigate(R.id.day_Fragment, args)
            }
        }
        val monthlyListener = object : HistoryEnergyViewAdapter.HistoryEnergyAdapterListener{
            override fun onClick(date: Long) {
                tabs.getTabAt(0)?.select()
                dailyStartDate = Date(date)
                dailyEndDate = dailyStartDate.endMonth()
                showDailyEnergy(true)
            }
        }

        val annuallyListener = object : HistoryEnergyViewAdapter.HistoryEnergyAdapterListener{
            override fun onClick(date: Long) {
                tabs.getTabAt(1)?.select()
                monthlyStartDate = Date(date)
                monthlyEndDate =  monthlyStartDate.endYear()
                showMonthlyEnergy(true)
            }
        }

        mainViewModel.dailyEnergy.observe(viewLifecycleOwner, {
            if (selected_tab == 0)
                showList(it, true, dailyListener)
        })

        mainViewModel.monthlyEnergy.observe(viewLifecycleOwner, {
            if (selected_tab == 1)
                showList(it, false, monthlyListener)
        })

        mainViewModel.annuallyEnergy.observe(viewLifecycleOwner, {
            if (selected_tab == 2)
                showList(it, false, annuallyListener)
        })
    }

    private fun showDialog() {
        val listener = object : DialogSelectDates.DialogSelectDatesListener {
            override fun onDatesSet(startDate: Date, endDate: Date) {
                when(selected_tab){
                    0 -> {
                        if (dailyStartDate != startDate || dailyEndDate != endDate) {
                            dailyStartDate = startDate
                            dailyEndDate = endDate
                            showDailyEnergy(true)
                        }
                    }
                    1 -> {
                        if (monthlyStartDate != startDate || monthlyEndDate != endDate) {
                            monthlyStartDate = startDate
                            monthlyEndDate = endDate
                            showMonthlyEnergy(true)
                        }
                    }
                    2 -> {
                        if (annuallyStartDate != startDate || annuallyEndDate != endDate) {
                            annuallyStartDate = startDate
                            annuallyEndDate = endDate
                            showAnnuallyEnergy(true)
                        }
                    }
                }
            }
        }
        val pickerDialog = when(selected_tab){
            0 -> { DialogSelectDates(SHOW_DAY_PICKER, dailyStartDate, dailyEndDate, listener) }
            1 -> { DialogSelectDates(SHOW_MONTH_PICKER, monthlyStartDate, monthlyEndDate, listener) }
            2 -> { DialogSelectDates(SHOW_YEAR_PICKER, annuallyStartDate, annuallyEndDate, listener) }
            else -> { DialogSelectDates(selected_tab, dailyStartDate, dailyEndDate, listener) }
        }
        pickerDialog.show(requireActivity().supportFragmentManager, "DATES_PICKER")
    }

    private fun showDailyEnergy(reload: Boolean) {
        val sd = getDateAsString(dailyStartDate, "dd-MM-yyyy")
        val ed = getDateAsString(dailyEndDate, "dd-MM-yyyy")
        historyDates.text = String.format("%s - %s", sd, ed)

        if (reload || firstDailyLoad) {
            mainViewModel.getEnergyDaily(dailyStartDate.time, dailyEndDate.time, true)
            firstDailyLoad = false

        }
        else
            mainViewModel.getEnergyDaily(dailyStartDate.time, dailyEndDate.time, false)
    }

    private fun showMonthlyEnergy(reload: Boolean) {

        val sd = getDateAsString(monthlyStartDate, "MM-yyyy")
        val ed = getDateAsString(monthlyEndDate, "MM-yyyy")
        historyDates.text = String.format("%s - %s", sd, ed)

        if (reload || firstMonthlyLoad) {
            mainViewModel.getEnergyMonthly(monthlyStartDate.time, monthlyEndDate.time, true)
            firstMonthlyLoad = false
        }
        else
            mainViewModel.getEnergyMonthly(monthlyStartDate.time, monthlyStartDate.time, false)

    }

    private fun showAnnuallyEnergy(reload: Boolean) {
        val sd = getDateAsString(annuallyStartDate, "yyyy")
        val ed = getDateAsString(annuallyEndDate, "yyyy")
        historyDates.text = String.format("%s - %s", sd, ed)

        if (reload || firstAnnualyLoad) {
            mainViewModel.getEnergyAnnually(annuallyStartDate.time, annuallyEndDate.time, true)
            firstAnnualyLoad = false
        }
        else
            mainViewModel.getEnergyAnnually(annuallyStartDate.time, annuallyEndDate.time, false)

    }

    private fun showList(
            result: Result,
            showWeather: Boolean,
            listListener: HistoryEnergyViewAdapter.HistoryEnergyAdapterListener
    ) {
        when (result) {
            is Result.Loading -> {
                progress.show(true)
                historyEnergyList.show(false)
                notFoundData.show(false)
            }
            is Result.Success -> {
                progress.show(false)
                historyEnergyList.show(true)
                notFoundData.show(false)
                var item = result.data as List<Energy>
                historyEnergyList.adapter = null
                historyEnergyList.adapter = HistoryEnergyViewAdapter(
                        item,
                        showWeather,
                        listListener
                )
            }
            is Result.Failure -> {
                progress.show(false)
                historyEnergyList.show(false)
                notFoundData.show(true)
            }
        }
    }


}

