package net.myenv.myenergy.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import net.myenv.myenergy.R
import net.myenv.myenergy.databinding.DialogDatePickerBinding
import net.myenv.myenergy.utils.extension.day
import net.myenv.myenergy.utils.extension.month
import net.myenv.myenergy.utils.extension.year
import java.util.*


class DialogSelectDates(private val pickerMode: Int,
                        private var startDate: Date,
                        private var endDate: Date,
                        private val listener: DialogSelectDatesListener? = null)
    : DialogFragment() {

    interface DialogSelectDatesListener {
        fun onDatesSet(startDate: Date, endDate: Date)
    }

    companion object{
        const val SHOW_DAY_PICKER = 0
        const val SHOW_MONTH_PICKER = 1
        const val SHOW_YEAR_PICKER = 2
        private const val MIN_DATE = 946684800000 // 01/01/2000 00:00:00 GMT
        private const val MIN_YEAR = 2000
    }

    private var _binding: DialogDatePickerBinding? = null
    private val binding get() = _binding!!

    private val calendar = Calendar.getInstance()

    private val pickerDayStart by lazy {  binding.pickerDayStart }
    private val pickerDayEnd by lazy {  binding.pickerDayEnd }
    private val pickerMonthStart by lazy { binding.pickerMonthStart }
    private val pickerMonthEnd by lazy { binding.pickerMonthEnd }
    private val pickerYearStart by lazy { binding.pickerYearStart }
    private val pickerYearEnd by lazy {  binding.pickerYearEnd }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogDatePickerBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when(pickerMode){
            SHOW_DAY_PICKER -> { showDayPicker() }
            SHOW_MONTH_PICKER -> { showMonthPicker() }
            SHOW_YEAR_PICKER -> { showYearPicker() }
        }

        binding.dialogDatesPickerDone.setOnClickListener{
            getDates()
            if (endDate.time >= startDate.time) {
                listener?.onDatesSet(startDate, endDate)
                dismiss()
            } else
                Toast.makeText(requireContext(), R.string.wrong_date_range, Toast.LENGTH_LONG).show()
        }
        binding.dialogDatesPickerCancel.setOnClickListener{
            dismiss()
        }
    }

    private fun showDayPicker() {
        binding.root.removeView(binding.dialogDatesPickerMonthAndYearStart)
        binding.root.removeView(binding.dialogDatesPickerMonthAndYearEnd)

        pickerDayStart.init( startDate.year(),  startDate.month(),  startDate.day(), null)
        pickerDayStart.apply {
            visibility = View.VISIBLE
            maxDate = Date().time
            minDate = MIN_DATE
        }

        pickerDayEnd.init( endDate.year(),  endDate.month(),  endDate.day(), null)
        pickerDayEnd.apply {
            visibility = View.VISIBLE
            maxDate = Date().time
            minDate = MIN_DATE
        }

    }

    private fun showMonthPicker() {
        pickerDayStart.visibility = View.GONE
        pickerDayEnd.visibility = View.GONE
        binding.dialogDatesPickerMonthAndYearStart.visibility = View.VISIBLE
        binding.dialogDatesPickerMonthAndYearEnd.visibility = View.VISIBLE

        pickerMonthStart.apply {
            minValue = 1
            maxValue = 12
            value = startDate.month() + 1
            wrapSelectorWheel = false
        }
        pickerMonthEnd.apply {
            minValue = 1
            maxValue = 12
            value = endDate.month() + 1
            wrapSelectorWheel = false
        }

        pickerYearStart.apply {
            minValue = MIN_YEAR
            maxValue = Date().year()
            value = startDate.year()
            wrapSelectorWheel = false
        }
        pickerYearEnd.apply {
            minValue = MIN_YEAR
            maxValue = Date().year()
            value = endDate.year()
            wrapSelectorWheel = false
        }
    }

    private fun showYearPicker() {
        binding.dialogDatesPickerMonthAndYearStart.visibility = View.VISIBLE
        binding.dialogDatesPickerMonthAndYearEnd.visibility = View.VISIBLE
        pickerDayStart.visibility = View.GONE
        pickerDayEnd.visibility = View.GONE
        pickerMonthStart.visibility = View.GONE
        pickerMonthEnd.visibility = View.GONE

        pickerYearStart.apply {
            minValue = MIN_YEAR
            maxValue = Date().year()
            value = startDate.year()
            wrapSelectorWheel = false
        }

        pickerYearEnd.apply {
            minValue = MIN_YEAR
            maxValue = Date().year()
            value = endDate.year()
            wrapSelectorWheel = false

        }

    }

    fun getDates(){
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.MONTH, 0)

        when(pickerMode) {
            SHOW_DAY_PICKER -> {
                calendar.set(Calendar.DAY_OF_MONTH, pickerDayStart.dayOfMonth )
                calendar.set(Calendar.MONTH, pickerDayStart.month )
                calendar.set(Calendar.YEAR, pickerDayStart.year )
                startDate = calendar.time
                calendar.set(Calendar.DAY_OF_MONTH, pickerDayEnd.dayOfMonth )
                calendar.set(Calendar.MONTH, pickerDayEnd.month )
                calendar.set(Calendar.YEAR, pickerDayEnd.year )
                endDate = calendar.time
            }
            SHOW_MONTH_PICKER -> {
                calendar.set(Calendar.MONTH, pickerMonthStart.value - 1)
                calendar.set(Calendar.YEAR, pickerYearStart.value )
                startDate = calendar.time
                calendar.set(Calendar.MONTH, pickerMonthEnd.value - 1 )
                calendar.set(Calendar.YEAR, pickerYearEnd.value )
                endDate = calendar.time
            }
            SHOW_YEAR_PICKER -> {
                calendar.set(Calendar.YEAR, pickerYearStart.value )
                startDate = calendar.time
                calendar.set(Calendar.YEAR,  pickerYearEnd.value )
                endDate = calendar.time
            }
        }
    }

}

