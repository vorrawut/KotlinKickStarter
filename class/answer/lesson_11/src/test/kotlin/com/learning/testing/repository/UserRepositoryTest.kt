/**
 * Lesson 11 Complete Solution: User Repository Tests
 * 
 * Comprehensive repository tests demonstrating @DataJpaTest and testing strategies
 */

package com.learning.testing.repository

import com.learning.testing.model.Post
import com.learning.testing.model.PostStatus
import com.learning.testing.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @BeforeEach
    fun setUp() {
        // Clean up before each test
        testEntityManager.clear()
    }
    
    @Nested
    @DisplayName("When testing basic CRUD operations")
    inner class WhenTestingBasicCrudOperations {
        
        @Test
        @DisplayName("Should save and find user by ID")
        fun shouldSaveAndFindUserById() {
            // Given
            val user = createTestUser("testuser", "test@example.com")
            
            // When
            val savedUser = testEntityManager.persistAndFlush(user)
            val foundUser = userRepository.findById(savedUser.id!!)
            
            // Then
            assertThat(foundUser).isPresent
            assertThat(foundUser.get().username).isEqualTo("testuser")
            assertThat(foundUser.get().email).isEqualTo("test@example.com")
        }
        
        @Test
        @DisplayName("Should delete user successfully")
        fun shouldDeleteUserSuccessfully() {
            // Given
            val user = createTestUser("testuser", "test@example.com")
            val savedUser = testEntityManager.persistAndFlush(user)
            
            // When
            userRepository.deleteById(savedUser.id!!)
            testEntityManager.flush()
            
            // Then
            val foundUser = userRepository.findById(savedUser.id!!)
            assertThat(foundUser).isNotPresent
        }
    }
    
    @Nested
    @DisplayName("When testing method name queries")
    inner class WhenTestingMethodNameQueries {
        
        @Test
        @DisplayName("Should find user by username")
        fun shouldFindUserByUsername() {
            // Given
            val user = createTestUser("uniqueuser", "unique@example.com")
            testEntityManager.persistAndFlush(user)
            
            // When
            val foundUser = userRepository.findByUsername("uniqueuser")
            
            // Then
            assertThat(foundUser).isNotNull
            assertThat(foundUser!!.email).isEqualTo("unique@example.com")
        }
        
        @Test
        @DisplayName("Should return null for non-existent username")
        fun shouldReturnNullForNonExistentUsername() {
            // When
            val foundUser = userRepository.findByUsername("nonexistent")
            
            // Then
            assertThat(foundUser).isNull()
        }
        
        @Test
        @DisplayName("Should find user by email")
        fun shouldFindUserByEmail() {
            // Given
            val user = createTestUser("emailuser", "emailtest@example.com")
            testEntityManager.persistAndFlush(user)
            
            // When
            val foundUser = userRepository.findByEmail("emailtest@example.com")
            
            // Then
            assertThat(foundUser).isNotNull
            assertThat(foundUser!!.username).isEqualTo("emailuser")
        }
        
        @Test
        @DisplayName("Should find active users only")
        fun shouldFindActiveUsersOnly() {
            // Given
            createAndPersistUser("active1", "active1@example.com", true)
            createAndPersistUser("active2", "active2@example.com", true)
            createAndPersistUser("inactive1", "inactive1@example.com", false)
            
            // When
            val activeUsers = userRepository.findByIsActiveTrue()
            
            // Then
            assertThat(activeUsers).hasSize(2)
            assertThat(activeUsers).allMatch { it.isActive }
        }
        
        @Test
        @DisplayName("Should find users by partial username")
        fun shouldFindUsersByPartialUsername() {
            // Given
            createAndPersistUser("john_doe", "john@example.com")
            createAndPersistUser("john_smith", "johnsmith@example.com")
            createAndPersistUser("jane_doe", "jane@example.com")
            
            // When
            val johnUsers = userRepository.findByUsernameContainingIgnoreCase("john")
            
            // Then
            assertThat(johnUsers).hasSize(2)
            assertThat(johnUsers).allMatch { it.username.contains("john", ignoreCase = true) }
        }
        
        @Test
        @DisplayName("Should check if username exists")
        fun shouldCheckIfUsernameExists() {
            // Given
            createAndPersistUser("existinguser", "existing@example.com")
            
            // When & Then
            assertThat(userRepository.existsByUsername("existinguser")).isTrue()
            assertThat(userRepository.existsByUsername("nonexistentuser")).isFalse()
        }
        
        @Test
        @DisplayName("Should count active users")
        fun shouldCountActiveUsers() {
            // Given
            createAndPersistUser("active1", "active1@example.com", true)
            createAndPersistUser("active2", "active2@example.com", true)
            createAndPersistUser("inactive1", "inactive1@example.com", false)
            
            // When
            val activeCount = userRepository.countByIsActiveTrue()
            
            // Then
            assertThat(activeCount).isEqualTo(2)
        }
    }
    
    @Nested
    @DisplayName("When testing custom JPQL queries")
    inner class WhenTestingCustomJpqlQueries {
        
        @Test
        @DisplayName("Should find users created after specific date")
        fun shouldFindUsersCreatedAfterSpecificDate() {
            // Given
            val cutoffDate = LocalDateTime.now().minusDays(10)
            val oldUser = createTestUser("olduser", "old@example.com")
                .copy(createdAt = LocalDateTime.now().minusDays(20))
            val newUser = createTestUser("newuser", "new@example.com")
                .copy(createdAt = LocalDateTime.now().minusDays(5))
            
            testEntityManager.persistAndFlush(oldUser)
            testEntityManager.persistAndFlush(newUser)
            
            // When
            val recentUsers = userRepository.findUsersCreatedAfter(cutoffDate)
            
            // Then
            assertThat(recentUsers).hasSize(1)
            assertThat(recentUsers[0].username).isEqualTo("newuser")
        }
        
        @Test
        @DisplayName("Should find users created between dates")
        fun shouldFindUsersCreatedBetweenDates() {
            // Given
            val start = LocalDateTime.now().minusDays(20)
            val end = LocalDateTime.now().minusDays(10)
            
            val beforeUser = createTestUser("before", "before@example.com")
                .copy(createdAt = LocalDateTime.now().minusDays(25))
            val betweenUser = createTestUser("between", "between@example.com")
                .copy(createdAt = LocalDateTime.now().minusDays(15))
            val afterUser = createTestUser("after", "after@example.com")
                .copy(createdAt = LocalDateTime.now().minusDays(5))
            
            testEntityManager.persistAndFlush(beforeUser)
            testEntityManager.persistAndFlush(betweenUser)
            testEntityManager.persistAndFlush(afterUser)
            
            // When
            val usersInRange = userRepository.findUsersCreatedBetween(start, end)
            
            // Then
            assertThat(usersInRange).hasSize(1)
            assertThat(usersInRange[0].username).isEqualTo("between")
        }
        
        @Test
        @DisplayName("Should find users with no posts")
        fun shouldFindUsersWithNoPosts() {
            // Given
            val userWithPosts = createAndPersistUser("withposts", "withposts@example.com")
            val userWithoutPosts = createAndPersistUser("withoutposts", "withoutposts@example.com")
            
            // Create a post for the first user
            val post = Post(
                title = "Test Post",
                content = "Test content",
                author = userWithPosts,
                status = PostStatus.PUBLISHED
            )
            testEntityManager.persistAndFlush(post)
            
            // When
            val usersWithNoPosts = userRepository.findUsersWithNoPosts()
            
            // Then
            assertThat(usersWithNoPosts).hasSize(1)
            assertThat(usersWithNoPosts[0].username).isEqualTo("withoutposts")
        }
    }
    
    @Nested
    @DisplayName("When testing update queries")
    inner class WhenTestingUpdateQueries {
        
        @Test
        @DisplayName("Should update user status")
        fun shouldUpdateUserStatus() {
            // Given
            val user = createAndPersistUser("updateuser", "update@example.com", true)
            
            // When
            val updatedCount = userRepository.updateUserStatus(user.id!!, false)
            testEntityManager.clear() // Clear persistence context to force reload
            
            // Then
            assertThat(updatedCount).isEqualTo(1)
            val updatedUser = testEntityManager.find(User::class.java, user.id)
            assertThat(updatedUser.isActive).isFalse()
        }
        
        @Test
        @DisplayName("Should deactivate users created before date")
        fun shouldDeactivateUsersCreatedBeforeDate() {
            // Given
            val cutoffDate = LocalDateTime.now().minusDays(30)
            val oldUser1 = createTestUser("old1", "old1@example.com")
                .copy(createdAt = LocalDateTime.now().minusDays(40))
            val oldUser2 = createTestUser("old2", "old2@example.com")
                .copy(createdAt = LocalDateTime.now().minusDays(35))
            val newUser = createTestUser("new", "new@example.com")
                .copy(createdAt = LocalDateTime.now().minusDays(10))
            
            testEntityManager.persistAndFlush(oldUser1)
            testEntityManager.persistAndFlush(oldUser2)
            testEntityManager.persistAndFlush(newUser)
            
            // When
            val deactivatedCount = userRepository.deactivateUsersCreatedBefore(cutoffDate)
            testEntityManager.clear()
            
            // Then
            assertThat(deactivatedCount).isEqualTo(2)
            
            val activeUsers = userRepository.findByIsActiveTrue()
            assertThat(activeUsers).hasSize(1)
            assertThat(activeUsers[0].username).isEqualTo("new")
        }
    }
    
    @Nested
    @DisplayName("When testing pagination queries")
    inner class WhenTestingPaginationQueries {
        
        @Test
        @DisplayName("Should paginate users correctly")
        fun shouldPaginateUsersCorrectly() {
            // Given
            repeat(25) { i ->
                createAndPersistUser("user$i", "user$i@example.com")
            }
            
            // When
            val pageRequest = PageRequest.of(0, 10, Sort.by("username"))
            val page = userRepository.findAllByOrderByUsernameAsc(pageRequest)
            
            // Then
            assertThat(page.content).hasSize(10)
            assertThat(page.totalElements).isEqualTo(25)
            assertThat(page.totalPages).isEqualTo(3)
            assertThat(page.isFirst).isTrue()
            assertThat(page.hasNext()).isTrue()
            
            // Verify sorting
            val usernames = page.content.map { it.username }
            assertThat(usernames).isSorted()
        }
        
        @Test
        @DisplayName("Should handle empty page results")
        fun shouldHandleEmptyPageResults() {
            // When
            val pageRequest = PageRequest.of(0, 10)
            val page = userRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageRequest)
            
            // Then
            assertThat(page.content).isEmpty()
            assertThat(page.totalElements).isEqualTo(0)
            assertThat(page.totalPages).isEqualTo(0)
            assertThat(page.isEmpty).isTrue()
        }
    }
    
    @Nested
    @DisplayName("When testing native queries")
    inner class WhenTestingNativeQueries {
        
        @Test
        @DisplayName("Should find users by email domain using native query")
        fun shouldFindUsersByEmailDomainUsingNativeQuery() {
            // Given
            createAndPersistUser("user1", "user1@company.com")
            createAndPersistUser("user2", "user2@company.com")
            createAndPersistUser("user3", "user3@other.org")
            
            // When
            val companyUsers = userRepository.findByEmailDomainNative("company.com")
            
            // Then
            assertThat(companyUsers).hasSize(2)
            assertThat(companyUsers).allMatch { it.email.contains("company.com") }
        }
    }
    
    @Nested
    @DisplayName("When testing complex relationship queries")
    inner class WhenTestingComplexRelationshipQueries {
        
        @Test
        @DisplayName("Should find active users with published posts")
        fun shouldFindActiveUsersWithPublishedPosts() {
            // Given
            val activeUserWithPosts = createAndPersistUser("activewithposts", "activewithposts@example.com", true)
            val activeUserWithoutPosts = createAndPersistUser("activewithoutposts", "activewithoutposts@example.com", true)
            val inactiveUserWithPosts = createAndPersistUser("inactivewithposts", "inactivewithposts@example.com", false)
            
            // Create published post for active user
            val publishedPost = Post(
                title = "Published Post",
                content = "Published content",
                author = activeUserWithPosts,
                status = PostStatus.PUBLISHED,
                publishedAt = LocalDateTime.now()
            )
            testEntityManager.persistAndFlush(publishedPost)
            
            // Create published post for inactive user
            val inactiveUserPost = Post(
                title = "Inactive User Post",
                content = "Content from inactive user",
                author = inactiveUserWithPosts,
                status = PostStatus.PUBLISHED,
                publishedAt = LocalDateTime.now()
            )
            testEntityManager.persistAndFlush(inactiveUserPost)
            
            // When
            val result = userRepository.findActiveUsersWithPublishedPosts()
            
            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].username).isEqualTo("activewithposts")
            assertThat(result[0].isActive).isTrue()
        }
    }
    
    // Helper methods for creating test data
    private fun createTestUser(
        username: String,
        email: String,
        isActive: Boolean = true
    ): User {
        return User(
            username = username,
            email = email,
            firstName = "Test",
            lastName = "User",
            isActive = isActive,
            createdAt = LocalDateTime.now()
        )
    }
    
    private fun createAndPersistUser(
        username: String,
        email: String,
        isActive: Boolean = true
    ): User {
        val user = createTestUser(username, email, isActive)
        return testEntityManager.persistAndFlush(user)
    }
}