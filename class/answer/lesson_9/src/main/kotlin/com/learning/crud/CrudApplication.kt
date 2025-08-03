/**
 * Lesson 9 Complete Solution: Advanced CRUD Operations & Transactions
 * 
 * Complete Spring Boot application demonstrating advanced CRUD operations,
 * transaction management, and audit trails in a blog management system
 */

package com.learning.crud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CrudApplication

fun main(args: Array<String>) {
    runApplication<CrudApplication>(*args)
}