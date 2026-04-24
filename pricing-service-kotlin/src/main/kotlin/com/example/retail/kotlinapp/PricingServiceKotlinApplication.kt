package com.example.retail.kotlinapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class PricingServiceKotlinApplication

fun main(args: Array<String>) {
    runApplication<PricingServiceKotlinApplication>(*args)
}
