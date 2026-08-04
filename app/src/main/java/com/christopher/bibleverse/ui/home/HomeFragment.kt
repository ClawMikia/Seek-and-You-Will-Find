package com.christopher.bibleverse.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.christopher.bibleverse.R
import com.christopher.bibleverse.data.local.Reminder
import com.christopher.bibleverse.data.model.VerseDetail
import com.christopher.bibleverse.databinding.FragmentHomeBinding
import com.christopher.bibleverse.databinding.ItemReminderBinding
import com.christopher.bibleverse.ui.alarm.AlarmTimeBottomSheet
import com.christopher.bibleverse.ui.filter.VerseFilterBottomSheet
import com.christopher.bibleverse.util.DateTimeUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGetVerse.setOnClickListener { openFilterSheet() }
        binding.btnChangeVerse.setOnClickListener { openFilterSheet() }
        binding.btnAddReminder.setOnClickListener { openAlarmSheet() }

        viewModel.favoriteVerse.observe(viewLifecycleOwner) { verse ->
            renderVerse(verse)
        }

        viewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            renderReminders(reminders)
        }
    }

    private fun renderReminders(reminders: List<Reminder>) {
        binding.alarmListContainer.removeAllViews()
        if (reminders.isEmpty()) {
            binding.tvAlarmStatus.visibility = View.VISIBLE
            binding.alarmListContainer.visibility = View.GONE
            return
        }
        binding.tvAlarmStatus.visibility = View.GONE
        binding.alarmListContainer.visibility = View.VISIBLE
        reminders.forEach { reminder ->
            val row = ItemReminderBinding.inflate(layoutInflater, binding.alarmListContainer, false)
            row.tvReminderTime.text = DateTimeUtils.formatHourMinute(reminder.hour, reminder.minute)
            row.root.setOnClickListener {
                AlarmTimeBottomSheet.newInstance(reminder.id)
                    .show(childFragmentManager, AlarmTimeBottomSheet.TAG)
            }
            row.btnRemoveReminder.setOnClickListener {
                viewModel.removeReminder(reminder.id)
            }
            binding.alarmListContainer.addView(row.root)
        }
    }

    private fun renderVerse(verse: VerseDetail?) {
        if (verse == null) {
            binding.groupEmptyState.visibility = View.VISIBLE
            binding.groupVerseDetail.visibility = View.GONE
            return
        }
        binding.groupEmptyState.visibility = View.GONE
        binding.groupVerseDetail.visibility = View.VISIBLE

        binding.tvVerseText.text = "\u201C${verse.text}\u201D"
        binding.tvReferenceValue.text = verse.reference
        binding.tvBookValue.text = verse.bookName
        binding.tvTestamentValue.text = if (verse.testament.name == "OLD") {
            getString(R.string.testament_old)
        } else {
            getString(R.string.testament_new)
        }
        binding.tvChapterValue.text = verse.chapter.toString()
        binding.tvVerseNumberValue.text = verse.verseNumber.toString()
        binding.tvTranslationValue.text = "${verse.translationName} (${verse.translationId.uppercase()})"
        binding.tvSavedOnValue.text = DateTimeUtils.formatSavedDate(verse.savedAtEpochMillis)
    }

    private fun openFilterSheet() {
        VerseFilterBottomSheet().show(childFragmentManager, VerseFilterBottomSheet.TAG)
    }

    private fun openAlarmSheet() {
        ensureNotificationPermission()
        AlarmTimeBottomSheet().show(childFragmentManager, AlarmTimeBottomSheet.TAG)
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
