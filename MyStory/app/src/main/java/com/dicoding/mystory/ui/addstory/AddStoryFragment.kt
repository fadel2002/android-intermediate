package com.dicoding.mystory.ui.addstory

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.fragment.findNavController
import com.dicoding.mystory.R
import com.dicoding.mystory.data.local.datastore.UserPreference
import com.dicoding.mystory.data.remote.response.story.AddStoryResponse
import com.dicoding.mystory.data.remote.retrofit.ApiConfig
import com.dicoding.mystory.databinding.FragmentAddStoryBinding
import com.dicoding.mystory.helper.AuthViewModelFactory
import com.dicoding.mystory.helper.createCustomTempFile
import com.dicoding.mystory.helper.reduceFileImage
import com.dicoding.mystory.helper.uriToFile
import com.dicoding.mystory.ui.UserViewModel
import com.dicoding.mystory.views.CustomLoadingDialog
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddStoryFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var binding: FragmentAddStoryBinding
    private var getFile: File? = null
    private lateinit var currentPhotoPath: String
    private lateinit var loadingDialog: CustomLoadingDialog
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddStoryBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = CustomLoadingDialog(requireContext())
        if (!allPermissionsGranted()) {
            requestPermission()
        }

        configurationViewModel(requireContext())

        var latitude: Float? = null
        var longitude: Float? = null

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            val location =
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (location != null) {
                latitude = location.latitude.toFloat()
                longitude = location.longitude.toFloat()
            }
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener {
            uploadImage(latitude, longitude)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, requireContext())
                getFile = myFile
                binding.storyImage.setImageURI(uri)

            }
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            myFile.let { file ->
                getFile = file
                binding.storyImage.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, resources.getString(R.string.select_image))
        launcherIntentGallery.launch(chooser)
    }

    private fun startCamera() {
        val context = requireContext()
        val packageManager: PackageManager = context.packageManager
        val application: Application = context.applicationContext as Application
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                resources.getString(R.string.app_path),
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun requestPermission() {
        val permission = REQUIRED_PERMISSIONS
        permission.any {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it)
        }

        val requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permission ->
            val granted = permission.all { it.value }
            if (granted) {
                Toast.makeText(requireContext(), resources.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            } else {
                if (!permission.entries.all {
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            it.key
                        )
                    }
                ) {
                    Toast.makeText(requireContext(), resources.getString(R.string.permission_not_granted), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), resources.getString(R.string.permission_not_granted), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        requestPermission.launch(permission)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun uploadImage(latitude: Float?, longitude: Float?) {
        if (getFile != null) {
            val latitudeRequestBody = latitude?.toString()?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val longitudeRequestBody = longitude?.toString()?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            loadingDialog.showDialog()
            val file = reduceFileImage(getFile as File)

            val description =
                binding.description.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            userViewModel.getUser().observe(viewLifecycleOwner) { user ->
                if (user.isLogin) {
                    val client = ApiConfig.getApiService().addStory(
                        "Bearer ${user.token}",
                        imageMultipart,
                        description,
                        latitudeRequestBody,
                        longitudeRequestBody,
                    )
                    client.enqueue(object : Callback<AddStoryResponse> {
                        override fun onResponse(
                            call: Call<AddStoryResponse>,
                            response: Response<AddStoryResponse>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody != null && !responseBody.error) {
                                    findNavController().navigate(R.id.action_addStoryFragment_to_storyFragment)
                                    Toast.makeText(
                                        requireContext(),
                                        responseBody.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    response.message(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            loadingDialog.dismissDialog()
                        }

                        override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                            loadingDialog.dismissDialog()
                            Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                        }

                    })
                }
            }
        } else {
            Toast.makeText(requireContext(), resources.getString(R.string.an_image_needs_to_be_uploaded), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun configurationViewModel(context: Context) {
        val dataStore: DataStore<Preferences> = context.dataStore
        userViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(
                UserPreference.getInstance(dataStore)
            )
        )[UserViewModel::class.java]
    }
}