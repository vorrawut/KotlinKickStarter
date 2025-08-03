/**
 * Lesson 11 Complete Solution: Testing Fundamentals
 * 
 * Complete Spring Boot application demonstrating comprehensive testing strategies
 * including unit tests, integration tests, repository tests, web tests, and performance tests
 */

package com.learning.testing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestingApplication

fun main(args: Array<String>) {
    runApplication<TestingApplication>(*args)
}