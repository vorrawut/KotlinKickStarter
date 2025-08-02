/**
 * Lesson 4 Complete Solution: Spring Boot Application
 * 
 * This demonstrates:
 * - @SpringBootApplication annotation combining @Configuration, @EnableAutoConfiguration, @ComponentScan
 * - Main application entry point with runApplication
 * - Spring Boot auto-configuration in action
 */

package com.learning.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaymentApplication

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}