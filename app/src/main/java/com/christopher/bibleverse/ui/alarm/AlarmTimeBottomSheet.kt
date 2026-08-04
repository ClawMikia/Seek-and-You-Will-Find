package com.christopher.bibleverse.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.christopher.bibleverse.data.local.AlarmState
import com.christopher.bibleverse.databinding.BottomSheetAlarmTimeBinding
import com.christopher.bibleverse.ui.home.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AlarmTimeBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAlarmTimeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAlarmTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.timePicker.setIs24HourView(false)

        viewModel.alarmState.value?.let { applyState(it) }

        binding.btnSaveAlarm.setOnClickListener {
            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute
            viewModel.setAlarm(hour, minute)
            dismiss()
        }

        binding.btnRemoveAlarm.setOnClickListener {
            viewModel.clearAlarm()
            dismiss()
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        viewModel.alarmState.observe(viewLifecycleOwner) { state ->
            applyState(state)
        }
    }

    private fun applyState(state: AlarmState) {
        binding.timePicker.hour = state.hour
        binding.timePicker.minute = state.minute
        binding.btnRemoveAlarm.visibility = if (state.enabled) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AlarmTimeBottomSheet"
    }
}
