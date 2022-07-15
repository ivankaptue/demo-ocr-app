package com.klid.demo_ocr_app

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.klid.demo_ocr_app.databinding.FragmentFirstBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

const val REQUEST_CAMERA_CODE = 100

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var bitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestCameraPermission()

        binding.buttonCapture.setOnClickListener {
            handleCapture()
        }

        binding.buttonCopy.setOnClickListener {
            handleCopyToClipboard()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val result = CropImage.getActivityResult(data)
                    bitmap =
                        MediaStore.Images.Media.getBitmap(
                            requireContext().contentResolver,
                            result.uri
                        )
                    extractTextFromImage(bitmap!!)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleCopyToClipboard() {
        copyToClipboard(binding.textData.text.toString())
    }

    private fun copyToClipboard(text: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clip = ClipData.newPlainText("Copied data", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied", Toast.LENGTH_LONG).show()
    }

    private fun extractTextFromImage(bitmap: Bitmap) {
        val recognizer = TextRecognizer.Builder(requireContext()).build()
        if (!recognizer.isOperational) {
            Toast.makeText(requireContext(), "No OCR operational", Toast.LENGTH_LONG).show()
            return
        }

        val frame = Frame.Builder().setBitmap(bitmap).build()
        val array: SparseArray<TextBlock> = recognizer.detect(frame)
        val stringBuilder = StringBuilder()
        array.forEach { key, textBlock ->
            stringBuilder.append(textBlock.value).append("\n")
        }
        binding.textData.text = stringBuilder.toString()
        binding.buttonCapture.text = "Retake"
        binding.buttonCopy.visibility = View.VISIBLE
    }

    private fun handleCapture() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(requireContext(), this)
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CAMERA_CODE
            )
        }
    }
}
