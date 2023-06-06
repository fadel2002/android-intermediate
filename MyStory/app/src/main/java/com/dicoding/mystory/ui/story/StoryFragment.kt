package com.dicoding.mystory.ui.story

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.mystory.R
import com.dicoding.mystory.databinding.FragmentStoryBinding
import com.dicoding.mystory.helper.StoryViewModelFactory
import com.dicoding.mystory.data.local.datastore.UserPreference
import com.dicoding.mystory.helper.AuthViewModelFactory
import com.dicoding.mystory.ui.UserViewModel

class   StoryFragment : Fragment() {
    private lateinit var binding: FragmentStoryBinding
    private lateinit var userViewModel: UserViewModel
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "config")
    private val storyViewModel by activityViewModels<StoryViewModel>{
        StoryViewModelFactory.getInstance(requireActivity())
    }

    private val storyPagingAdapter: StoryPagingAdapter by lazy {
        StoryPagingAdapter {
            findNavController().navigate(StoryFragmentDirections.actionStoryFragmentToDetailFragment(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        if(activity is AppCompatActivity){
            val mainActivity = (activity as? AppCompatActivity)
            mainActivity?.setSupportActionBar(binding.toolbar)
            mainActivity?.supportActionBar?.title = "Story"
        }

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        binding.addStory.setOnClickListener {
            findNavController().navigate(R.id.action_storyFragment_to_addStoryFragment)
        }

        binding.rvStory.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = storyPagingAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    storyPagingAdapter.retry()
                }
            )
        }

        setupViewModel(requireContext())

        userViewModel.getUser().observe(viewLifecycleOwner) { user ->
            if (user.isLogin) {
                storyViewModel.getPagingStory("Bearer ${user.token}").observe(viewLifecycleOwner) { result ->
                    storyPagingAdapter.submitData(lifecycle, result)
                }
            }
        }

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                requireActivity().finishAffinity()
                return@setOnKeyListener true
            }
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.story_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                logout()
                true
            }
            R.id.map -> {
                findNavController().navigate(R.id.action_storyFragment_to_mapsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        try {
            userViewModel.logout()
            Toast.makeText(requireContext(), resources.getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_storyFragment_to_loginFragment)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), resources.getString(R.string.logout_failed), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
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