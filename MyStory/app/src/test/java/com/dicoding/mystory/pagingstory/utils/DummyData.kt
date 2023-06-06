package com.dicoding.mystory.pagingstory.utils

import com.dicoding.mystory.data.model.Story

object DummyData {
    fun listStoryDummy(): List<Story> {
        val items = arrayListOf<Story>()
        for (i in 0 until 3) {
            val story = Story(
                id = "story-jUum3cHSSWBXYpwc",
                photoUrl = "https://img.freepik.com/premium-photo/image-colorful-galaxy-sky-generative-ai_791316-9864.jpg",
                createdAt = "2023-05-28T00:00:00.176Z",
                name = "Username",
                description = "Description",
                lat = (48.8566).toFloat(),
                lon = (2.3522).toFloat(),
            )
            items.add(story)
        }
        return items
    }

    fun emptyListStoryDummy(): List<Story> = emptyList()
}