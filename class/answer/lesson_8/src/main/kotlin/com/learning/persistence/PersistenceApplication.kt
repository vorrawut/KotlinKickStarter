/**
 * Lesson 8 Complete Solution: Persistence with Spring Data JPA & MongoDB
 * 
 * This application demonstrates dual database architecture using both SQL and NoSQL databases
 */

package com.learning.persistence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PersistenceApplication

fun main(args: Array<String>) {
    runApplication<PersistenceApplication>(*args)
}