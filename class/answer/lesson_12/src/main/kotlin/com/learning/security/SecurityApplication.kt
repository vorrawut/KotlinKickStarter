/**
 * Lesson 12 Complete Solution: Authentication & JWT Security
 * 
 * Complete Spring Boot application demonstrating enterprise-grade security
 * with JWT authentication, role-based access control, and comprehensive testing
 */

package com.learning.security

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SecurityApplication

fun main(args: Array<String>) {
    runApplication<SecurityApplication>(*args)
}