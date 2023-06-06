package com.dicoding.mystory.ui.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.mystory.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private var storyName: String? = null
    private var storyDescription: String? = null
    private var storyPhotoUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(activity is AppCompatActivity){
            val mainActivity = (activity as? AppCompatActivity)
            mainActivity?.setSupportActionBar(binding.toolbar)
            mainActivity?.supportActionBar?.title = "Story Detail"
            mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        val data = DetailFragmentArgs.fromBundle(arguments as Bundle).story

        if (data != null) {
            storyName = data.name
            storyDescription = data.description
            storyPhotoUrl = data.photoUrl

            binding.nameUser.text = storyName
            binding.tvDescription.text = storyDescription
            Glide.with(requireContext())
                .load(storyPhotoUrl)
                .into(binding.ivStoryImg)
        }
    }
}