package com.dicoding.mystory.helper

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ExecutorService {
    val diskIO: Executor = Executors.newSingleThreadExecutor()
}