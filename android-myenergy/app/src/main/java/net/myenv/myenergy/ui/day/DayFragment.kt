package net.myenv.myenergy.ui.day

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import net.myenv.myenergy.MainViewModel
import net.myenv.myenergy.R
import net.myenv.myenergy.databinding.TableRowBinding
import net.myenv.myenergy.model.Energy
import net.myenv.myenergy.model.PVProduction
import net.myenv.myenergy.model.Result
import net.myenv.myenergy.utils.extension.getDatefromTimestamp
import net.myenv.myenergy.utils.extension.show
import net.myenv.myenergy.utils.groupAveragePowerByHours
import net.myenv.myenergy.utils.groupProductionByHours
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*
import kotlin.math.round


class DayFragment : Fragment() {

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private lateinit var progressBar : View
    private lateinit var error : TextView
    private lateinit var statsTable : TableLayout
    private lateinit var infoWeather : LinearLayout
    private lateinit var infoSun : Group
    private lateinit var chart : CombinedChart
    private lateinit var weatherTemp : TextView
    private lateinit var iconWeather : ImageView
    private lateinit var textSunrise : TextView
    private lateinit var textSunset : TextView

    private var date = 0L // Real Time

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (arguments != null) {
            date = requireArguments().getLong("date")
        }
        if (date==0L) setHasOptionsMenu(true)
        if (date>0L) {
                val title = getDatefromTimestamp(date, "yyyy-MM-dd")
                (activity as AppCompatActivity?)!!.supportActionBar!!.title = title
        }

        val root = inflater.inflate(R.layout.fragment_day, container, false)
        initView(root)
        return root
    }

    private fun initView(root: View) {
        progressBar = root.findViewById(R.id.progress)
        error = root.findViewById(R.id.not_found_data)
        statsTable = root.findViewById(R.id.stats_table)
        chart = root.findViewById(R.id.day_chart)
        infoWeather = root.findViewById(R.id.info_weather)
        infoSun = root.findViewById(R.id.info_sun_group)
        weatherTemp = root.findViewById(R.id.weather_temp)
        iconWeather = root.findViewById(R.id.icon_weather)
        textSunrise = root.findViewById(R.id.text_sunrise)
        textSunset = root.findViewById(R.id.text_sunset)

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_refresh).isVisible = true
        super.onPrepareOptionsMenu(menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (date==0L)
            mainViewModel.getRealTimeProduction()
        else
            mainViewModel.getEnergy(date)
        initListener()
    }

    private fun initListener() {
        if (date==0L) {
            mainViewModel.pvProductionToday.observe(viewLifecycleOwner, {
                when (it) {
                    is Result.Loading -> {
                        progressBar.show(true)
                        error.show(false)
                        infoWeather.show(false)
                        infoSun.show(false)
                        statsTable.show(false)
                        chart.show(false)
                    }
                    is Result.Success -> {
                        error.show(false)
                        progressBar.show(false)
                        val item: Energy = it.data as Energy
                        showData(item)
                    }
                    is Result.Failure -> {
                        progressBar.show(false)
                        infoWeather.show(false)
                        infoSun.show(false)
                        chart.show(false)
                        statsTable.show(false)
                        error.show(true)
                    }
                }
            })
        } else {
            mainViewModel.energyDay.observe(viewLifecycleOwner, {
                when (it) {
                    is Result.Loading -> {
                        progressBar.show(true)
                        error.show(false)
                        infoWeather.show(false)
                        infoSun.show(false)
                        chart.show(false)
                    }
                    is Result.Success -> {
                        error.show(false)
                        progressBar.show(false)
                        val item: Energy = it.data as Energy
                        showData(item)
                    }
                    is Result.Failure -> {
                        progressBar.show(false)
                        infoWeather.show(false)
                        infoSun.show(false)
                        chart.show(false)
                        error.show(true)
                    }
                }
            })
        }
    }

    private fun showData(data: Energy) {

        if(data.weather_icon !=null && data.weather_temp !=null) {
            infoWeather.show(true)
            weatherTemp.text = String.format(" %s \u2103 ", data.weather_temp)
            iconWeather.setImageResource(data.weather_icon!!)
        }

        if(data.sunrise !=null && data.sunset !=null ) {
            infoSun.show(true)
            textSunrise.text =  getDatefromTimestamp(data.sunrise!!, "HH:mm")
            textSunset.text =  getDatefromTimestamp(data.sunset!!, "HH:mm")
        }

        statsTable.removeAllViews()
        statsTable.show(true)
        // Real time
        if (date==0L) {
            data.date?.let { addRow(R.string.last_updated, it) }
            data.pv_productionList?.get(0)?.date?.let { addRow(R.string.start_production, getDatefromTimestamp(it, "HH:mm")) }
            data.power?.let { addRow(R.string.power, "$it W") }
            data.avg_power?.let { addRow(R.string.avg_power, "$it W")}
            data.max_power?.let { addRow(R.string.max_power, "$it W")}
        }
        else {
            data.pv_productionList?.get(0)?.date?.let { addRow(R.string.start_production, getDatefromTimestamp(it, "HH:mm")) }
            data.avg_power?.let { addRow(R.string.avg_power, "$it W")}
            data.max_power?.let { addRow(R.string.max_power, "$it W")}
        }

        data.pv_production?.let { addRow(R.string.production, "$it KW")}
        data.pv_productionList?.let { drawChart(it) }

    }

    private fun addRow(name: Int, value: String?){
        val tableRow = TableRowBinding.inflate(this.layoutInflater)
        tableRow.name.text = getString(name)
        tableRow.value.text = value
        statsTable.addView(tableRow.root)
    }

    private fun drawChart(listPVProduction: List<PVProduction>){
        chart.show(true)

        configureCombinedChart()

        val lineDataSets = generatePowerData(listPVProduction)
        val barDataSets = generateProductionData(listPVProduction)

        chart.xAxis.axisMaximum = barDataSets.xMax + 0.25f
        chart.xAxis.axisMinimum = barDataSets.xMin - 0.25f;

        val data = CombinedData()
        data.setData(lineDataSets)
        data.setData(barDataSets)
        chart.data = data
        chart.invalidate()
    }

    private fun configureCombinedChart() {

        chart.description = null

        // apply styling
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(false)
        chart.extraBottomOffset = 20F

        val legend = chart.legend
        legend.isWordWrapEnabled = true
        legend.textColor = Color.WHITE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

        val xAxis: XAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        //set the horizontal distance of the bar
        xAxis.granularity = 1f // only intervals 1 hour
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val hour = value.toInt()
                return "$hour"
            }
        }


        val leftAxis: YAxis = chart.axisLeft
        leftAxis.spaceTop = 1f
        leftAxis.axisMinimum = 0f
        leftAxis.textColor = Color.WHITE
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${round(value * 100) / 100} KW/h"
            }
        }

        val rightAxis: YAxis = chart.axisRight
        //rightAxis.spaceTop = 1f
        rightAxis.axisMinimum = 0f
        rightAxis.textColor = Color.WHITE
        rightAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "$value W/h"
            }
        }


    }

    private fun generatePowerData(data: List<PVProduction>): LineData {

        val powerValues: ArrayList<Entry> = ArrayList()

        val list = groupAveragePowerByHours(data)
        list.forEach{
            val x: Float = it.key.toFloat()
            val y: Float = it.value
            powerValues.add(Entry(x, y))
        }

        val powerLineDataSet = LineDataSet(powerValues, "Avg Power")
        powerLineDataSet.apply {
            color = Color.RED
            lineWidth = 2.5F
            setDrawVerticalHighlightIndicator(false)
            setDrawHorizontalHighlightIndicator(false)
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawValues(true)
            setCircleColor(Color.rgb(240, 238, 70));
            circleRadius = 5f;
            //fillColor = Color.rgb(240, 238, 70);
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            valueTextSize = 10f
            //valueTextColor = Color.rgb(240, 238, 70);
            valueTextColor = Color.YELLOW
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}"
                }
            }
        }

        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(powerLineDataSet)
        return LineData(dataSets)
    }

    private fun generateProductionData(data: List<PVProduction>): BarData {

        val productionValues: ArrayList<BarEntry> = ArrayList()
        val list = groupProductionByHours(data)

        list.forEach{
            val x: Float = it.key
            val y: Float = it.value
            productionValues.add(BarEntry(x, y))
        }

        val productionBarDataSet = BarDataSet(productionValues, "Production")
        productionBarDataSet.apply {
            color = Color.GREEN
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "$value"
                }
            }
        }


        val dataBar = BarData(productionBarDataSet)
        dataBar.barWidth = 0.45f
        return dataBar
    }


}

