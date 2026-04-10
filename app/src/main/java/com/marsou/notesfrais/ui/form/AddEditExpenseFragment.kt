package com.marsou.notesfrais.ui.form

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.marsou.notesfrais.R
import com.marsou.notesfrais.data.model.ExpenseCategory
import com.marsou.notesfrais.databinding.FragmentAddEditExpenseBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditExpenseFragment : Fragment() {

    private var _binding: FragmentAddEditExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditExpenseViewModel by viewModels()
    private val args: AddEditExpenseFragmentArgs by navArgs()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    private var selectedDate: Date = Date()
    private var currentReceiptPath: String? = null
    private var currentOcrText: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryDropdown()
        setupDatePicker()
        setupSaveButton()
        setupCameraButton()
        setupObservers()
        viewModel.loadExpense(args.expenseId)
    }

    private fun setupCategoryDropdown() {
        val categories = ExpenseCategory.values().map { it.label }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, categories)
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setText(ExpenseCategory.DIVERS.label, false)
    }

    private fun setupDatePicker() {
        binding.etDate.setText(dateFormat.format(selectedDate))
        binding.etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.time = selectedDate
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    cal.set(year, month, day)
                    selectedDate = cal.time
                    binding.etDate.setText(dateFormat.format(selectedDate))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val selectedCategoryLabel = binding.actvCategory.text.toString()
            val category = ExpenseCategory.values()
                .firstOrNull { it.label == selectedCategoryLabel }
                ?: ExpenseCategory.DIVERS

            viewModel.saveExpense(
                title = binding.etTitle.text.toString(),
                amountStr = binding.etAmount.text.toString(),
                category = category,
                date = selectedDate,
                description = binding.etDescription.text.toString(),
                receiptImagePath = currentReceiptPath,
                ocrRawText = currentOcrText
            )
        }
    }

    private fun setupCameraButton() {
        binding.btnScanReceipt.setOnClickListener {
            val action = AddEditExpenseFragmentDirections
                .actionAddEditExpenseToCamera()
            findNavController().navigate(action)
        }
    }

    private fun setupObservers() {
        viewModel.expense.observe(viewLifecycleOwner) { expense ->
            expense ?: return@observe
            binding.etTitle.setText(expense.title)
            binding.etAmount.setText(expense.amount.toString())
            binding.actvCategory.setText(expense.category.label, false)
            selectedDate = expense.date
            binding.etDate.setText(dateFormat.format(expense.date))
            binding.etDescription.setText(expense.description)

            expense.receiptImagePath?.let { path ->
                currentReceiptPath = path
                showReceiptPreview(path)
            }
            currentOcrText = expense.ocrRawText

            requireActivity().title = getString(R.string.edit_expense)
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().popBackStack()
            } else {
                Snackbar.make(binding.root, R.string.fill_required_fields, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Listen for OCR result passed from CameraFragment via back-stack
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Bundle>("ocr_result")
            ?.observe(viewLifecycleOwner) { bundle ->
                val amount = bundle.getDouble("amount", -1.0)
                val title = bundle.getString("title")
                val rawText = bundle.getString("raw_text") ?: ""
                val imagePath = bundle.getString("image_path") ?: ""

                currentReceiptPath = imagePath
                currentOcrText = rawText

                if (amount > 0) binding.etAmount.setText(String.format(Locale.FRANCE, "%.2f", amount))
                if (!title.isNullOrBlank()) binding.etTitle.setText(title)
                if (imagePath.isNotEmpty()) showReceiptPreview(imagePath)

                if (rawText.isNotEmpty()) {
                    binding.cardOcrResult.visibility = View.VISIBLE
                    binding.tvOcrRawText.text = rawText
                }
            }
    }

    private fun showReceiptPreview(path: String) {
        val file = File(path)
        if (file.exists()) {
            binding.ivReceiptPreview.visibility = View.VISIBLE
            Glide.with(this).load(file).into(binding.ivReceiptPreview)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
