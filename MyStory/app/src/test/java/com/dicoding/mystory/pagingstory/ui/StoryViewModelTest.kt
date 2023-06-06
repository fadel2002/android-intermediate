package com.dicoding.mystory.pagingstory.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.mystory.data.model.Story
import com.dicoding.mystory.data.repository.StoryRepository
import com.dicoding.mystory.pagingstory.utils.CoroutinesTestRule
import com.dicoding.mystory.pagingstory.utils.DummyData
import com.dicoding.mystory.pagingstory.utils.PagingDataTest
import com.dicoding.mystory.pagingstory.utils.getOrAwaitValue
import com.dicoding.mystory.ui.story.StoryPagingAdapter
import com.dicoding.mystory.ui.story.StoryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var storyViewModel: StoryViewModel
    private var token: String = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLTVFbFZqSVRYNjhHLXg1N3EiLCJpYXQiOjE2ODU2NzcwNDR9.MpZJ9_R5gXCc93yOz1QmpOvsTJD24vAJOp5oT7bRp9g"

    @Before
    fun setup() {
        storyRepository = Mockito.mock(StoryRepository::class.java)
        storyViewModel = StoryViewModel(storyRepository)
    }

    @get:Rule
    var mainCoroutineRule = CoroutinesTestRule()

    @Test
    fun `Stories Paging Should Not Null`() = runTest {
        val dataDummyStories = DummyData.listStoryDummy()
        val data = PagingDataTest.snapshot(dataDummyStories)

        val dataStories = MutableLiveData<PagingData<Story>>()
        dataStories.value = data

        Mockito.`when`(storyRepository.getPagingStories(token)).thenReturn(dataStories)

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryPagingAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            mainDispatcher = mainCoroutineRule.dispatcher,
            workerDispatcher = mainCoroutineRule.dispatcher
        )

        val result = storyViewModel.getPagingStory(token).getOrAwaitValue()

        differ.submitData(result)
        differ.itemCount < 1
        advanceUntilIdle()

        Assert.assertEquals(dataDummyStories.size, differ.snapshot().size)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dataDummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dataDummyStories[0], differ.snapshot()[0])
    }


    @Test
    fun `Stories length should be the same`() = runTest {
        val dataDummyStories = DummyData.listStoryDummy()
        val data = PagingDataTest.snapshot(dataDummyStories)

        val dataStories = MutableLiveData<PagingData<Story>>()
        dataStories.value = data

        Mockito.`when`(storyRepository.getPagingStories(token)).thenReturn(dataStories)

        val result = storyViewModel.getPagingStory(token).getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryPagingAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            mainDispatcher = mainCoroutineRule.dispatcher,
            workerDispatcher = mainCoroutineRule.dispatcher
        )
        differ.submitData(result)
        advanceUntilIdle()

        Assert.assertEquals(dataDummyStories.size, differ.snapshot().size)
    }

    @Test
    fun `First Story item should be the same`() = runTest {
        val dataDummyStories = DummyData.listStoryDummy()
        val data = PagingDataTest.snapshot(dataDummyStories)

        val dataStories = MutableLiveData<PagingData<Story>>()
        dataStories.value = data

        Mockito.`when`(storyRepository.getPagingStories(token)).thenReturn(dataStories)

        val result = storyViewModel.getPagingStory(token).getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryPagingAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            mainDispatcher = mainCoroutineRule.dispatcher,
            workerDispatcher = mainCoroutineRule.dispatcher
        )
        differ.submitData(result)
        advanceUntilIdle()

        Assert.assertEquals(dataDummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `If Story empty should return true`() = runTest {
        val dataDummyStories = DummyData.emptyListStoryDummy()
        val data = PagingDataTest.snapshot(dataDummyStories)

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryPagingAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            mainDispatcher = mainCoroutineRule.dispatcher,
            workerDispatcher = mainCoroutineRule.dispatcher
        )
        differ.submitData(data)
        advanceUntilIdle()

        Assert.assertEquals(differ.snapshot().isEmpty(), true)
    }

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}