package com.christopher.bibleverse.ui.filter

import android.graphics.Paint
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.christopher.bibleverse.R
import com.christopher.bibleverse.data.model.BibleBook
import com.christopher.bibleverse.data.model.BibleBooksProvider
import com.christopher.bibleverse.data.model.Testament
import com.christopher.bibleverse.data.model.VerseDetail
import com.christopher.bibleverse.databinding.BottomSheetVerseFilterBinding
import com.christopher.bibleverse.ui.home.HomeViewModel
import com.christopher.bibleverse.util.Resource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class VerseFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetVerseFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private var selectedTestament: Testament? = null
    private var selectedBook: BibleBook? = null
    private var selectedChapter: Int? = null
    private var selectedVerse: Int? = null
    private var pendingVerse: VerseDetail? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetVerseFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTestament.text = getString(R.string.testament_any)
        binding.tvBook.text = getString(R.string.filter_book_any)
        binding.tvChapter.text = getString(R.string.filter_chapter_any)
        binding.tvVerse.text = getString(R.string.filter_verse_any)
        setupPickers()
        showFilterState()

        binding.btnFetchVerse.setOnClickListener { fetch() }
        binding.btnCancel.setOnClickListener { dismiss() }
        fitButtonText(binding.btnCancel, 8f)
        fitButtonText(binding.btnFetchVerse, 8f)
        binding.btnTryAnother.setOnClickListener { fetch() }
        binding.btnSaveFavorite.setOnClickListener {
            pendingVerse?.let {
                viewModel.saveFavorite(it)
                viewModel.clearFetchResult()
                dismiss()
            }
        }

        viewModel.fetchResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> showLoadingState()
                is Resource.Success -> {
                    pendingVerse = result.data
                    showPreviewState(result.data)
                }
                is Resource.Error -> showErrorState()
                null -> showFilterState()
            }
        }
    }

    private fun fitButtonText(button: MaterialButton, minSp: Float) {
        button.post {
            if (button.width <= 0) return@post
            val iconWidth = if (button.icon != null) {
                (button.iconSize + button.iconPadding).toFloat()
            } else {
                0f
            }
            val target = (button.width - button.paddingLeft - button.paddingRight).toFloat() - iconWidth
            if (target <= 0f) return@post
            val transformed = button.transformationMethod
                ?.getTransformation(button.text, button) ?: button.text
            val scaledDensity = resources.displayMetrics.scaledDensity
            val min = minSp * scaledDensity
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = button.typeface }
            var size = button.textSize
            while (size > min) {
                paint.textSize = size
                if (paint.measureText(transformed.toString()) <= target) break
                size -= 0.5f * scaledDensity
            }
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        }
    }

    private fun fetch() {
        viewModel.fetchVerse(selectedTestament, selectedBook?.id, selectedChapter, selectedVerse)
    }

    private fun setupPickers() {
        val testamentOptions = listOf(
            getString(R.string.testament_any),
            getString(R.string.testament_old),
            getString(R.string.testament_new)
        )

        binding.tvTestament.setOnClickListener {
            val index = when (selectedTestament) {
                Testament.OLD -> 1
                Testament.NEW -> 2
                else -> 0
            }
            showPicker(getString(R.string.filter_testament_label), testamentOptions, index) { position ->
                selectedTestament = when (position) {
                    1 -> Testament.OLD
                    2 -> Testament.NEW
                    else -> null
                }
                binding.tvTestament.text = testamentOptions[position]
                selectedBook = null
                selectedChapter = null
                selectedVerse = null
                binding.tvBook.text = getString(R.string.filter_book_any)
                binding.tvChapter.text = getString(R.string.filter_chapter_any)
                binding.tvVerse.text = getString(R.string.filter_verse_any)
                updatePickerState()
            }
        }

        binding.tvBook.setOnClickListener {
            val books = BibleBooksProvider.booksFor(selectedTestament)
            val names = listOf(getString(R.string.filter_book_any)) + books.map { it.displayName }
            val index = selectedBook?.let { book ->
                (books.indexOfFirst { it.id == book.id } + 1).coerceAtLeast(0)
            } ?: 0
            showPicker(getString(R.string.filter_book_label), names, index) { position ->
                selectedBook = if (position == 0) null else books[position - 1]
                binding.tvBook.text = names[position]
                selectedChapter = null
                selectedVerse = null
                binding.tvChapter.text = getString(R.string.filter_chapter_any)
                binding.tvVerse.text = getString(R.string.filter_verse_any)
                updatePickerState()
            }
        }

        binding.tvChapter.setOnClickListener {
            val book = selectedBook ?: return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                val chapterCount = viewModel.chapterCount(book.id)
                if (_binding == null || chapterCount <= 0) return@launch
                val names = listOf(getString(R.string.filter_chapter_any)) +
                    (1..chapterCount).map { it.toString() }
                val index = selectedChapter ?: 0
                showPicker(getString(R.string.filter_chapter_label), names, index) { position ->
                    selectedChapter = if (position == 0) null else position
                    binding.tvChapter.text = names[position]
                    selectedVerse = null
                    binding.tvVerse.text = getString(R.string.filter_verse_any)
                    updatePickerState()
                }
            }
        }

        binding.tvVerse.setOnClickListener {
            val book = selectedBook ?: return@setOnClickListener
            val chapter = selectedChapter ?: return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                val verseCount = viewModel.verseCount(book.id, chapter)
                if (_binding == null || verseCount <= 0) return@launch
                val names = listOf(getString(R.string.filter_verse_any)) +
                    (1..verseCount).map { it.toString() }
                val index = selectedVerse ?: 0
                showPicker(getString(R.string.filter_verse_label), names, index) { position ->
                    selectedVerse = if (position == 0) null else position
                    binding.tvVerse.text = names[position]
                }
            }
        }

        updatePickerState()
    }

    private fun updatePickerState() {
        binding.tvChapter.isEnabled = selectedBook != null
        binding.tvVerse.isEnabled = selectedChapter != null
    }

    private fun showPicker(
        title: String,
        options: List<String>,
        selectedIndex: Int,
        onSelect: (Int) -> Unit
    ) {
        val themedContext = ContextThemeWrapper(
            requireContext(), R.style.ThemeOverlay_BibleVerse_Dialog
        )
        MaterialAlertDialogBuilder(themedContext)
            .setTitle(title)
            .setSingleChoiceItems(options.toTypedArray(), selectedIndex) { dialog, which ->
                onSelect(which)
                dialog.dismiss()
            }
            .show()
    }

    private fun showFilterState() {
        binding.groupFilterInputs.visibility = View.VISIBLE
        binding.groupPreview.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    private fun showLoadingState() {
        binding.groupFilterInputs.visibility = View.GONE
        binding.groupPreview.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showPreviewState(verse: VerseDetail) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.groupFilterInputs.visibility = View.GONE
        binding.groupPreview.visibility = View.VISIBLE

        binding.tvPreviewText.text = "\u201C${verse.text}\u201D"
        binding.tvPreviewReference.text = verse.reference
        binding.tvPreviewTranslation.text = verse.translationName
    }

    private fun showErrorState() {
        binding.progressBar.visibility = View.GONE
        binding.groupPreview.visibility = View.GONE
        binding.groupFilterInputs.visibility = View.VISIBLE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = getString(R.string.error_invalid_reference)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearFetchResult()
        _binding = null
    }

    companion object {
        const val TAG = "VerseFilterBottomSheet"
    }
}
