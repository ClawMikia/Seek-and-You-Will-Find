package com.christopher.bibleverse.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
        setupTestamentSpinner()
        updateBookSpinner(null)
        resetChapterSpinner()
        resetVerseSpinner()
        showFilterState()

        binding.btnFetchVerse.setOnClickListener { fetch() }
        binding.btnCancel.setOnClickListener { dismiss() }
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

    private fun fetch() {
        viewModel.fetchVerse(selectedTestament, selectedBook?.id, selectedChapter, selectedVerse)
    }

    private fun setupTestamentSpinner() {
        val options = listOf(getString(R.string.testament_any)) +
            listOf(getString(R.string.testament_old), getString(R.string.testament_new))
        binding.spinnerTestament.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, options
        )
        binding.spinnerTestament.setSelection(0)
        binding.spinnerTestament.onItemSelectedListener = simpleSelectionListener { position ->
            selectedTestament = when (position) {
                1 -> Testament.OLD
                2 -> Testament.NEW
                else -> null
            }
            selectedBook = null
            updateBookSpinner(selectedTestament)
            resetChapterSpinner()
            resetVerseSpinner()
        }
    }

    private fun updateBookSpinner(testament: Testament?) {
        val books = BibleBooksProvider.booksFor(testament)
        val names = listOf(getString(R.string.filter_book_any)) + books.map { it.displayName }
        binding.spinnerBook.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, names
        )
        binding.spinnerBook.setSelection(0)
        binding.spinnerBook.onItemSelectedListener = simpleSelectionListener { position ->
            selectedBook = if (position == 0) null else books[position - 1]
            selectedChapter = null
            selectedVerse = null
            resetVerseSpinner()
            val book = selectedBook
            if (book == null) {
                resetChapterSpinner()
            } else {
                loadChapterSpinner(book)
            }
        }
    }

    private fun loadChapterSpinner(book: BibleBook) {
        viewLifecycleOwner.lifecycleScope.launch {
            val chapterCount = viewModel.chapterCount(book.id)
            if (_binding == null) return@launch
            val names = listOf(getString(R.string.filter_chapter_any)) +
                (1..chapterCount).map { it.toString() }
            binding.spinnerChapter.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, names
            )
            binding.spinnerChapter.isEnabled = chapterCount > 0
            binding.spinnerChapter.setSelection(0)
            binding.spinnerChapter.onItemSelectedListener = simpleSelectionListener { position ->
                selectedChapter = if (position == 0) null else position
                selectedVerse = null
                val chapter = selectedChapter
                if (chapter == null) {
                    resetVerseSpinner()
                } else {
                    loadVerseSpinner(book, chapter)
                }
            }
        }
    }

    private fun loadVerseSpinner(book: BibleBook, chapter: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val verseCount = viewModel.verseCount(book.id, chapter)
            if (_binding == null) return@launch
            val names = listOf(getString(R.string.filter_verse_any)) +
                (1..verseCount).map { it.toString() }
            binding.spinnerVerse.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, names
            )
            binding.spinnerVerse.isEnabled = verseCount > 0
            binding.spinnerVerse.setSelection(0)
            binding.spinnerVerse.onItemSelectedListener = simpleSelectionListener { position ->
                selectedVerse = if (position == 0) null else position
            }
        }
    }

    private fun resetChapterSpinner() {
        selectedChapter = null
        binding.spinnerChapter.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.filter_chapter_any))
        )
        binding.spinnerChapter.isEnabled = false
    }

    private fun resetVerseSpinner() {
        selectedVerse = null
        binding.spinnerVerse.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.filter_verse_any))
        )
        binding.spinnerVerse.isEnabled = false
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

    private fun simpleSelectionListener(onSelected: (Int) -> Unit) =
        object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long
            ) = onSelected(position)

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

    companion object {
        const val TAG = "VerseFilterBottomSheet"
    }
}
