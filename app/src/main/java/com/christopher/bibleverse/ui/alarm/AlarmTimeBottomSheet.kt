package com.christopher.bibleverse.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.christopher.bibleverse.R
import com.christopher.bibleverse.databinding.BottomSheetAlarmTimeBinding
import com.christopher.bibleverse.ui.home.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AlarmTimeBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAlarmTimeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private val reminderId: Long
        get() = arguments?.getLong(ARG_REMINDER_ID, NO_REMINDER) ?: NO_REMINDER

    private val isEditMode: Boolean
        get() = reminderId >= 0

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

        if (isEditMode) {
            binding.tvSheetTitle.text = getString(R.string.alarm_title_edit)
            binding.btnSaveReminder.text = getString(R.string.btn_save_reminder)
            binding.btnRemoveReminder.visibility = View.VISIBLE
            viewModel.reminders.value?.find { it.id == reminderId }?.let { reminder ->
                binding.timePicker.hour = reminder.hour
                binding.timePicker.minute = reminder.minute
            }
        }

        binding.btnSaveReminder.setOnClickListener {
            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute
            if (isEditMode) {
                viewModel.editReminder(reminderId, hour, minute)
            } else {
                viewModel.addReminder(hour, minute)
            }
            dismiss()
        }

        binding.btnRemoveReminder.setOnClickListener {
            if (isEditMode) {
                viewModel.removeReminder(reminderId)
            }
            dismiss()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AlarmTimeBottomSheet"
        private const val ARG_REMINDER_ID = "arg_reminder_id"
        private const val NO_REMINDER = -1L

        fun newInstance(reminderId: Long): AlarmTimeBottomSheet =
            AlarmTimeBottomSheet().apply {
                arguments = Bundle().apply { putLong(ARG_REMINDER_ID, reminderId) }
            }
    }
}
