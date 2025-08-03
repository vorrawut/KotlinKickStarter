/**
 * Lesson 7 Answer: Service Layer & Clean Architecture
 * 
 * Complete Spring Boot application demonstrating clean architecture principles
 */

package com.learning.architecture

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
class ArchitectureApplication

fun main(args: Array<String>) {
    runApplication<ArchitectureApplication>(*args)
}