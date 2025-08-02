package com.learning.kickstart

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinKickStarterApplication

fun main(args: Array<String>) {
	runApplication<KotlinKickStarterApplication>(*args)
}
