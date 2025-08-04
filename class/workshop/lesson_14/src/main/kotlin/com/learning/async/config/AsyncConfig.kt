package com.learning.async.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
// TODO: Add @EnableAsync annotation
@ConfigurationProperties(prefix = "async.thread-pools")
class AsyncConfig {
    
    // TODO: Add properties for thread pool configuration
    data class ThreadPoolProperties(
        var coreSize: Int = 5,
        var maxSize: Int = 20,
        var queueCapacity: Int = 100
    )
    
    var general = ThreadPoolProperties()
    var fast = ThreadPoolProperties(10, 50, 25)
    var heavy = ThreadPoolProperties(2, 8, 500)
    
    // TODO: Create general purpose thread pool
    @Bean("generalTaskExecutor")
    fun generalTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        // TODO: Configure thread pool with general properties
        // HINT: Set core/max pool size, queue capacity, thread name prefix
        // HINT: Set rejection policy and initialize
        executor.corePoolSize = general.coreSize
        executor.maxPoolSize = general.maxSize
        executor.queueCapacity = general.queueCapacity
        executor.setThreadNamePrefix("general-")
        executor.initialize()
        return executor
    }
    
    // TODO: Create fast task executor for high-priority operations
    @Bean("fastTaskExecutor") 
    fun fastTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        // TODO: Configure for low-latency operations
        // HINT: Use fast properties, smaller queue for responsiveness
        executor.corePoolSize = fast.coreSize
        executor.maxPoolSize = fast.maxSize
        executor.queueCapacity = fast.queueCapacity
        executor.setThreadNamePrefix("fast-")
        executor.initialize()
        return executor
    }
    
    // TODO: Create heavy task executor for long-running operations
    @Bean("heavyTaskExecutor")
    fun heavyTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        // TODO: Configure for CPU-intensive tasks
        // HINT: Use heavy properties, larger queue, CallerRunsPolicy
        executor.corePoolSize = heavy.coreSize
        executor.maxPoolSize = heavy.maxSize
        executor.queueCapacity = heavy.queueCapacity
        executor.setThreadNamePrefix("heavy-")
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        executor.initialize()
        return executor
    }
}