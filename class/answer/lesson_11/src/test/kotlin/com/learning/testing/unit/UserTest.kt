/**
 * Lesson 11 Complete Solution: User Unit Tests
 * 
 * Comprehensive unit tests demonstrating testing business logic
 */

package com.learning.testing.unit

import com.learning.testing.model.Post
import com.learning.testing.model.PostStatus
import com.learning.testing.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime

@DisplayName("User Entity Tests")
class UserTest {
    
    @Nested
    @DisplayName("When testing user creation")
    inner class WhenTestingUserCreation {
        
        @Test
        @DisplayName("Should create user with valid data")
        fun shouldCreateUserWithValidData() {
            // Given & When
            val user = User(
                username = "testuser",
                email = "test@example.com",
                firstName = "Test",
                lastName = "User"
            )
            
            // Then
            assertThat(user.username).isEqualTo("testuser")
            assertThat(user.email).isEqualTo("test@example.com")
            assertThat(user.firstName).isEqualTo("Test")
            assertThat(user.lastName).isEqualTo("User")
            assertThat(user.isActive).isTrue()
            assertThat(user.createdAt).isNotNull()
        }
    }
    
    @Nested
    @DisplayName("When testing business logic methods")
    inner class WhenTestingBusinessLogicMethods {
        
        @Test
        @DisplayName("Should return correct full name")
        fun shouldReturnCorrectFullName() {
            // Given
            val user = User(
                username = "testuser",
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe"
            )
            
            // When
            val fullName = user.getFullName()
            
            // Then
            assertThat(fullName).isEqualTo("John Doe")
        }
        
        @Test
        @DisplayName("Should return correct display name")
        fun shouldReturnCorrectDisplayName() {
            // Given
            val user = User(
                username = "johndoe",
                email = "john@example.com",
                firstName = "John",
                lastName = "Doe"
            )
            
            // When
            val displayName = user.getDisplayName()
            
            // Then
            assertThat(displayName).isEqualTo("John Doe (@johndoe)")
        }
        
        @ParameterizedTest
        @CsvSource(
            "John, Doe, JD",
            "Alice, Smith, AS",
            "Bob, Johnson, BJ"
        )
        @DisplayName("Should return correct initials")
        fun shouldReturnCorrectInitials(firstName: String, lastName: String, expectedInitials: String) {
            // Given
            val user = User(
                username = "testuser",
                email = "test@example.com",
                firstName = firstName,
                lastName = lastName
            )
            
            // When
            val initials = user.getInitials()
            
            // Then
            assertThat(initials).isEqualTo(expectedInitials)
        }
        
        @ParameterizedTest
        @ValueSource(strings = ["gmail.com", "example.org", "company.co.uk"])
        @DisplayName("Should extract email domain correctly")
        fun shouldExtractEmailDomainCorrectly(domain: String) {
            // Given
            val user = User(
                username = "testuser",
                email = "user@$domain",
                firstName = "Test",
                lastName = "User"
            )
            
            // When
            val extractedDomain = user.getEmailDomain()
            
            // Then
            assertThat(extractedDomain).isEqualTo(domain)
        }
    }
    
    @Nested
    @DisplayName("When testing user eligibility")
    inner class WhenTestingUserEligibility {
        
        @Test
        @DisplayName("Should allow active user to post")
        fun shouldAllowActiveUserToPost() {
            // Given
            val activeUser = User(
                username = "activeuser",
                email = "active@example.com",
                firstName = "Active",
                lastName = "User",
                isActive = true
            )
            
            // When & Then
            assertThat(activeUser.isEligibleForPosting()).isTrue()
        }
        
        @Test
        @DisplayName("Should not allow inactive user to post")
        fun shouldNotAllowInactiveUserToPost() {
            // Given
            val inactiveUser = User(
                username = "inactiveuser",
                email = "inactive@example.com",
                firstName = "Inactive",
                lastName = "User",
                isActive = false
            )
            
            // When & Then
            assertThat(inactiveUser.isEligibleForPosting()).isFalse()
        }
    }
    
    @Nested
    @DisplayName("When testing new user detection")
    inner class WhenTestingNewUserDetection {
        
        @Test
        @DisplayName("Should identify new user created within threshold")
        fun shouldIdentifyNewUserCreatedWithinThreshold() {
            // Given
            val recentUser = User(
                username = "newuser",
                email = "new@example.com",
                firstName = "New",
                lastName = "User",
                createdAt = LocalDateTime.now().minusDays(10)
            )
            
            // When & Then
            assertThat(recentUser.isNewUser(30)).isTrue()
            assertThat(recentUser.isNewUser(5)).isFalse()
        }
        
        @Test
        @DisplayName("Should identify old user created before threshold")
        fun shouldIdentifyOldUserCreatedBeforeThreshold() {
            // Given
            val oldUser = User(
                username = "olduser",
                email = "old@example.com",
                firstName = "Old",
                lastName = "User",
                createdAt = LocalDateTime.now().minusDays(60)
            )
            
            // When & Then
            assertThat(oldUser.isNewUser(30)).isFalse()
        }
    }
    
    @Nested
    @DisplayName("When testing post-related methods")
    inner class WhenTestingPostRelatedMethods {
        
        @Test
        @DisplayName("Should count posts correctly")
        fun shouldCountPostsCorrectly() {
            // Given
            val user = createUserWithPosts(3)
            
            // When & Then
            assertThat(user.getPostCount()).isEqualTo(3)
        }
        
        @Test
        @DisplayName("Should count published posts correctly")
        fun shouldCountPublishedPostsCorrectly() {
            // Given
            val user = createUserWithMixedPosts()
            
            // When & Then
            assertThat(user.getPublishedPostCount()).isEqualTo(2)
        }
        
        @Test
        @DisplayName("Should detect user with active posts")
        fun shouldDetectUserWithActivePosts() {
            // Given
            val userWithActivePosts = createUserWithMixedPosts()
            val userWithoutActivePosts = createUserWithDraftPosts()
            
            // When & Then
            assertThat(userWithActivePosts.hasActivePosts()).isTrue()
            assertThat(userWithoutActivePosts.hasActivePosts()).isFalse()
        }
        
        @Test
        @DisplayName("Should determine if user can be deactivated")
        fun shouldDetermineIfUserCanBeDeactivated() {
            // Given
            val userWithActivePosts = createUserWithMixedPosts()
            val userWithoutActivePosts = createUserWithDraftPosts()
            
            // When & Then
            assertThat(userWithActivePosts.canBeDeactivated()).isFalse()
            assertThat(userWithoutActivePosts.canBeDeactivated()).isTrue()
        }
    }
    
    // Helper methods for creating test data
    private fun createUserWithPosts(postCount: Int): User {
        val posts = (1..postCount).map { i ->
            Post(
                id = i.toLong(),
                title = "Post $i",
                content = "Content for post $i",
                author = User(
                    username = "testuser",
                    email = "test@example.com",
                    firstName = "Test",
                    lastName = "User"
                )
            )
        }
        
        return User(
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            posts = posts
        )
    }
    
    private fun createUserWithMixedPosts(): User {
        val author = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        
        val posts = listOf(
            Post(
                id = 1L,
                title = "Published Post 1",
                content = "Content 1",
                status = PostStatus.PUBLISHED,
                author = author
            ),
            Post(
                id = 2L,
                title = "Draft Post",
                content = "Content 2",
                status = PostStatus.DRAFT,
                author = author
            ),
            Post(
                id = 3L,
                title = "Published Post 2",
                content = "Content 3",
                status = PostStatus.PUBLISHED,
                author = author
            )
        )
        
        return author.copy(posts = posts)
    }
    
    private fun createUserWithDraftPosts(): User {
        val author = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        
        val posts = listOf(
            Post(
                id = 1L,
                title = "Draft Post 1",
                content = "Content 1",
                status = PostStatus.DRAFT,
                author = author
            ),
            Post(
                id = 2L,
                title = "Archived Post",
                content = "Content 2",
                status = PostStatus.ARCHIVED,
                author = author
            )
        )
        
        return author.copy(posts = posts)
    }
}