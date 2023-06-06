package com.dicoding.mystory.ui.maps

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.dicoding.mystory.R
import com.dicoding.mystory.databinding.FragmentMapBinding
import com.dicoding.mystory.helper.StoryViewModelFactory
import com.dicoding.mystory.data.Result
import com.dicoding.mystory.data.local.datastore.UserPreference
import com.dicoding.mystory.helper.AuthViewModelFactory
import com.dicoding.mystory.ui.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {

    private lateinit var binding: FragmentMapBinding
    private lateinit var userViewModel: UserViewModel
    private val listLocations = ArrayList<LatLng>()
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "config")
    private val mapsViewModel by activityViewModels<MapsViewModel>{
        StoryViewModelFactory.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->
            setMapStyle(googleMap)
            getMyLocation(googleMap)

            userViewModel.getUser().observe(viewLifecycleOwner) { user ->
                if (user.isLogin) {
                    mapsViewModel.getAllStory("Bearer ${user.token}").observe(viewLifecycleOwner) { response ->
                        when (response) {
                            is Result.Success -> {
                                listLocations.clear()
                                for (story in response.data) {
                                    if (story.lat != null && story.lon != null) {
                                        val location = LatLng(story.lat.toDouble(), story.lon.toDouble())
                                        listLocations.add(location)
                                        val markerOptions = MarkerOptions()
                                            .position(location).title(story.name).snippet(story.description)
                                        googleMap.addMarker(markerOptions)
                                    }
                                }

                                if (listLocations.isNotEmpty()) {
                                    val boundsBuilder = LatLngBounds.builder()
                                    for (location in listLocations) {
                                        boundsBuilder.include(location)
                                    }
                                    val bounds = boundsBuilder.build()
                                    val padding = 500
                                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                                    googleMap.animateCamera(cameraUpdate)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            googleMap.uiSettings.apply {
                isZoomControlsEnabled = true
                isIndoorLevelPickerEnabled = true
                isCompassEnabled = true
                isMapToolbarEnabled = true
            }
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                val mapFragment =
                    childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment?.getMapAsync { googleMap ->
                    getMyLocation(googleMap)
                }
            }
        }

    private fun getMyLocation(googleMap: GoogleMap) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setMapStyle(googleMap: GoogleMap) {
        try {
            val success =
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                    )
                )
            if (!success) {
                Log.d("Fragment","Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.d("Fragment","Can't find style. Error:")
        }
    }
    private fun setupViewModel(context: Context) {
        val dataStore: DataStore<Preferences> = context.dataStore
        userViewModel = ViewModelProvider(
            this, AuthViewModelFactory(
                UserPreference.getInstance(dataStore)
            )
        )[UserViewModel::class.java]
    }
}