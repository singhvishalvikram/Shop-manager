package com.shopmanager.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.shopmanager.R
import com.shopmanager.databinding.FragmentAddEditBinding
import com.shopmanager.utils.ImageUtils
import java.io.File

class AddEditFragment : Fragment() {

    private var _binding: FragmentAddEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private var editingItemId: Long? = null
    private var photoFile: File? = null
    private var capturedPhotoPath: String? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            val compressed = ImageUtils.compressImage(requireContext(), Uri.fromFile(photoFile))
            if (compressed != null) {
                capturedPhotoPath = compressed
                showPhotoPreview(compressed)
            } else {
                Snackbar.make(binding.root, R.string.photo_failed, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera() else
            Snackbar.make(binding.root, R.string.camera_permission_denied, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingItemId = arguments?.getLong("itemId", -1L)?.takeIf { it > 0 }

        if (editingItemId != null) {
            binding.formTitle.text = getString(R.string.edit_item)
            binding.btnSaveItem.text = getString(R.string.update_item)
            loadItemForEditing()
        }

        binding.btnTakePhoto.setOnClickListener { requestCamera() }
        binding.btnSaveItem.setOnClickListener { saveItem() }
        binding.btnCancelForm.setOnClickListener { findNavController().popBackStack() }
    }

    private fun loadItemForEditing() {
        viewModel.loadItem(editingItemId!!)
        viewModel.currentItem.observe(viewLifecycleOwner) { item ->
            if (item != null && item.id == editingItemId) {
                binding.inputName.setText(item.name)
                binding.inputType.setText(item.type)
                binding.inputDescription.setText(item.description)
                binding.inputPrice.setText(if (item.price > 0) item.price.toString() else "")
                binding.inputQuantity.setText(if (item.quantity > 0) item.quantity.toString() else "")
                binding.inputLocation.setText(item.location)
                capturedPhotoPath = item.imagePath
                if (item.imagePath.isNotEmpty()) showPhotoPreview(item.imagePath)
            }
        }
    }

    private fun requestCamera() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> launchCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.camera_permission_title)
                    .setMessage(R.string.camera_permission)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        photoFile = ImageUtils.createImageFile(requireContext())
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", photoFile!!
        )
        cameraLauncher.launch(uri)
    }

    private fun showPhotoPreview(path: String) {
        binding.itemPhotoPreview.visibility = View.VISIBLE
        Glide.with(this)
            .load(File(path))
            .centerCrop()
            .into(binding.itemPhotoPreview)
    }

    private fun saveItem() {
        val name = binding.inputName.text?.toString()?.trim() ?: ""
        if (name.isEmpty()) {
            binding.inputName.error = getString(R.string.name_required)
            return
        }

        val price = binding.inputPrice.text?.toString()?.toDoubleOrNull() ?: 0.0
        if (price < 0) {
            binding.inputPrice.error = getString(R.string.price_invalid)
            return
        }

        val quantity = binding.inputQuantity.text?.toString()?.toIntOrNull() ?: 0

        viewModel.saveItem(
            editingId = editingItemId,
            name = name,
            type = binding.inputType.text?.toString()?.trim() ?: "",
            description = binding.inputDescription.text?.toString()?.trim() ?: "",
            price = price,
            quantity = quantity,
            imagePath = capturedPhotoPath ?: "",
            location = binding.inputLocation.text?.toString()?.trim() ?: ""
        )

        Snackbar.make(binding.root, R.string.item_saved, Snackbar.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
