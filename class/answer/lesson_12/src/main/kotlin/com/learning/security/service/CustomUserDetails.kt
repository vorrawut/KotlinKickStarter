/**
 * Lesson 12 Complete Solution: Custom UserDetails
 * 
 * Custom UserDetails implementation for JWT claims and Spring Security integration
 */

package com.learning.security.service

import com.learning.security.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails as SpringUserDetails

class CustomUserDetails(
    private val user: User
) : SpringUserDetails {
    
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return user.getAuthorities()
    }
    
    override fun getPassword(): String = user.password
    
    override fun getUsername(): String = user.username
    
    override fun isAccountNonExpired(): Boolean = user.isAccountNonExpired()
    
    override fun isAccountNonLocked(): Boolean = user.isAccountNonLocked()
    
    override fun isCredentialsNonExpired(): Boolean = user.isCredentialsNonExpired()
    
    override fun isEnabled(): Boolean = user.isEnabled
    
    // Additional methods for JWT claims
    fun getId(): Long? = user.id
    
    fun getEmail(): String = user.email
    
    fun getUser(): User = user
    
    fun getFullName(): String = user.getFullName()
    
    fun hasRole(roleName: String): Boolean = user.hasRole(roleName)
    
    fun hasAnyRole(vararg roleNames: String): Boolean = user.hasAnyRole(*roleNames)
}