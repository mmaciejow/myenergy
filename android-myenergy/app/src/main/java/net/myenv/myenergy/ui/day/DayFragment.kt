package net.myenv.myenergy.ui.day

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.card.MaterialCardView
import net.myenv.myenergy.MainViewModel
import net.myenv.myenergy.R
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

    private lateinit var dayCardView : MaterialCardView
    private lateinit var powerCardView : MaterialCardView
    private lateinit var productionCardView : MaterialCardView
    private lateinit var chartCardView : MaterialCardView

    private lateinit var progressBar : View
    private lateinit var error : TextView

    private lateinit var iconWeather : ImageView
    private lateinit var weatherTemp : TextView
    private lateinit var sunrise : TextView
    private lateinit var sunset : TextView

    private lateinit var power : TextView
    private lateinit var avgPower : TextView
    private lateinit var maxPower : TextView
    private lateinit var production : TextView
    private lateinit var startProduction : TextView
    private lateinit var chart : CombinedChart

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

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_refresh).isVisible = true
        super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_refresh -> {
            mainViewModel.getRealTimeProduction()
            true
        }
        else -> { super.onOptionsItemSelected(item) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initListener()
        if (date==0L) {
            mainViewModel.getRealTimeProduction()
        }
        else
            mainViewModel.getEnergy(date)

    }

    override fun onPause() {
        setSubtitleToolbar(null)
        super.onPause()
    }

    private fun setSubtitleToolbar(title: String?){
        (activity as AppCompatActivity?)!!.supportActionBar!!.subtitle = title
    }

    private fun initView(root: View) {
        progressBar = root.findViewById(R.id.progress)
        error = root.findViewById(R.id.not_found_data)

        // Cards
        dayCardView = root.findViewById(R.id.card_view_day_info)
        powerCardView = root.findViewById(R.id.card_view_power)
        productionCardView = root.findViewById(R.id.card_view_production)
        chartCardView = root.findViewById(R.id.card_view_chart)

        // Day Info
        iconWeather = root.findViewById(R.id.icon_weather)
        weatherTemp = root.findViewById(R.id.weather_temp)
        sunrise = root.findViewById(R.id.sunrise)
        sunset = root.findViewById(R.id.sunset)

        // Power Info
        power = root.findViewById(R.id.power)
        avgPower = root.findViewById(R.id.avg_power)
        maxPower = root.findViewById(R.id.max_power)

        // Production Info
        production = root.findViewById(R.id.production)
        startProduction = root.findViewById(R.id.start_production)

        // Chart
        chart = root.findViewById(R.id.day_chart)

    }

    private fun showCards(show: Boolean) {
        dayCardView.show(false)
        powerCardView.show(show)
        productionCardView.show(show)
        chartCardView.show(show)
    }

    private fun initListener() {
        if (date==0L) {
            mainViewModel.pvProductionToday.observe(viewLifecycleOwner) { checkResult(it) }
        } else {
            mainViewModel.energyDay.observe(viewLifecycleOwner) { checkResult(it) }
        }

    }

    private fun checkResult(result: Result){
        when (result) {
            is Result.Loading -> {
                showCards(false)
                progressBar.show(true)
                error.show(false)
                setSubtitleToolbar(null)
            }
            is Result.Success -> {
                error.show(false)
                progressBar.show(false)
                showCards(true)
                val item: Energy = result.data as Energy
                showData(item)
            }
            is Result.Failure -> {
                showCards(false)
                progressBar.show(false)
                error.show(true)
                setSubtitleToolbar(null)
            }
        }
    }

    private fun showData(data: Energy) {

        // Updated
        if (date==0L) {
            val title = resources.getString(R.string.updated, data.date)
            setSubtitleToolbar(title)
        }

        // Day Info
        if (data.weatherIcon != null && data.weatherTemp != null && data.sunrise != null && data.sunset != null) {
            dayCardView.show(true)
            weatherTemp.text = String.format(" %s° ", data.weatherTemp)
            iconWeather.setImageResource(data.weatherIcon!!)
            sunrise.text =  getDatefromTimestamp(data.sunrise!!, "HH:mm")
            sunset.text =  getDatefromTimestamp(data.sunset!!, "HH:mm")
        }

        // Power
        if (date==0L) {
            data.power?.let {
                power.show(true)
                power.text = it.toInt().toString()
            }
        }
        else {
            power.show(false)
        }

        data.avgPower?.let { avgPower.text = it.toString() }
        data.maxPower?.let { maxPower.text = it.toString() }

        // Production
        production.text = String.format(" %s KW ", data.pvProduction)
        data.pvProductionList?.get(0)?.date?.let {startProduction.text = getDatefromTimestamp(it, "HH:mm") }

        data.pvProductionList?.let { drawChart(it) }

    }

    private fun drawChart(listPVProduction: List<PVProduction>){
        chart.show(true)

        configureCombinedChart()

        val lineDataSets = generatePowerData(listPVProduction)
        val barDataSets = generateProductionData(listPVProduction)

        chart.xAxis.axisMaximum = barDataSets.xMax + 0.25f
        chart.xAxis.axisMinimum = barDataSets.xMin - 0.25f

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
            setCircleColor(Color.rgb(240, 238, 70))
            circleRadius = 5f
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

