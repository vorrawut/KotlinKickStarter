package com.learning.async.repository

import com.learning.async.model.DailyStatistics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DailyStatsRepository : JpaRepository<DailyStatistics, Long> {
    
    fun findByDate(date: LocalDate): DailyStatistics?
    
    @Query("SELECT d FROM DailyStatistics d WHERE d.date BETWEEN :startDate AND :endDate ORDER BY d.date DESC")
    fun findByDateBetween(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<DailyStatistics>
    
    @Query("SELECT d FROM DailyStatistics d ORDER BY d.date DESC")
    fun findAllOrderByDateDesc(): List<DailyStatistics>
    
    @Query("SELECT SUM(d.userRegistrations) FROM DailyStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    fun sumUserRegistrationsBetween(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Long?
}