/**
 * Lesson 11 Complete Solution: Post Unit Tests
 * 
 * Comprehensive unit tests for Post entity business logic
 */

package com.learning.testing.unit

import com.learning.testing.model.Comment
import com.learning.testing.model.CommentStatus
import com.learning.testing.model.Post
import com.learning.testing.model.PostStatus
import com.learning.testing.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDateTime

@DisplayName("Post Entity Tests")
class PostTest {
    
    @Nested
    @DisplayName("When testing post status methods")
    inner class WhenTestingPostStatusMethods {
        
        @Test
        @DisplayName("Should correctly identify published posts")
        fun shouldCorrectlyIdentifyPublishedPosts() {
            // Given
            val publishedPost = createPost(status = PostStatus.PUBLISHED)
            val draftPost = createPost(status = PostStatus.DRAFT)
            
            // When & Then
            assertThat(publishedPost.isPublished()).isTrue()
            assertThat(draftPost.isPublished()).isFalse()
        }
        
        @Test
        @DisplayName("Should correctly identify draft posts")
        fun shouldCorrectlyIdentifyDraftPosts() {
            // Given
            val draftPost = createPost(status = PostStatus.DRAFT)
            val publishedPost = createPost(status = PostStatus.PUBLISHED)
            
            // When & Then
            assertThat(draftPost.isDraft()).isTrue()
            assertThat(publishedPost.isDraft()).isFalse()
        }
        
        @Test
        @DisplayName("Should correctly identify archived posts")
        fun shouldCorrectlyIdentifyArchivedPosts() {
            // Given
            val archivedPost = createPost(status = PostStatus.ARCHIVED)
            val publishedPost = createPost(status = PostStatus.PUBLISHED)
            
            // When & Then
            assertThat(archivedPost.isArchived()).isTrue()
            assertThat(publishedPost.isArchived()).isFalse()
        }
    }
    
    @Nested
    @DisplayName("When testing authorization methods")
    inner class WhenTestingAuthorizationMethods {
        
        @Test
        @DisplayName("Should allow author to edit their own post")
        fun shouldAllowAuthorToEditTheirOwnPost() {
            // Given
            val author = createUser(id = 1L)
            val post = createPost(author = author)
            
            // When & Then
            assertThat(post.canBeEditedBy(1L)).isTrue()
        }
        
        @Test
        @DisplayName("Should not allow other users to edit post")
        fun shouldNotAllowOtherUsersToEditPost() {
            // Given
            val author = createUser(id = 1L)
            val post = createPost(author = author, status = PostStatus.PUBLISHED)
            
            // When & Then
            assertThat(post.canBeEditedBy(2L)).isFalse()
        }
        
        @Test
        @DisplayName("Should allow anyone to edit draft posts")
        fun shouldAllowAnyoneToEditDraftPosts() {
            // Given
            val author = createUser(id = 1L)
            val draftPost = createPost(author = author, status = PostStatus.DRAFT)
            
            // When & Then
            assertThat(draftPost.canBeEditedBy(2L)).isTrue()
        }
    }
    
    @Nested
    @DisplayName("When testing publication rules")
    inner class WhenTestingPublicationRules {
        
        @Test
        @DisplayName("Should allow draft post with valid content to be published")
        fun shouldAllowDraftPostWithValidContentToBePublished() {
            // Given
            val validDraftPost = createPost(
                title = "Valid Title",
                content = "Valid content that is not blank",
                status = PostStatus.DRAFT
            )
            
            // When & Then
            assertThat(validDraftPost.canBePublished()).isTrue()
        }
        
        @Test
        @DisplayName("Should not allow published post to be published again")
        fun shouldNotAllowPublishedPostToBePublishedAgain() {
            // Given
            val publishedPost = createPost(status = PostStatus.PUBLISHED)
            
            // When & Then
            assertThat(publishedPost.canBePublished()).isFalse()
        }
        
        @ParameterizedTest
        @CsvSource(
            "'', 'Valid content'",
            "' ', 'Valid content'",
            "'Valid title', ''",
            "'Valid title', ' '"
        )
        @DisplayName("Should not allow posts with blank title or content to be published")
        fun shouldNotAllowPostsWithBlankTitleOrContentToBePublished(title: String, content: String) {
            // Given
            val invalidPost = createPost(
                title = title,
                content = content,
                status = PostStatus.DRAFT
            )
            
            // When & Then
            assertThat(invalidPost.canBePublished()).isFalse()
        }
    }
    
    @Nested
    @DisplayName("When testing deletion rules")
    inner class WhenTestingDeletionRules {
        
        @ParameterizedTest
        @EnumSource(value = PostStatus::class, names = ["DRAFT", "ARCHIVED"])
        @DisplayName("Should allow deletion of draft and archived posts")
        fun shouldAllowDeletionOfDraftAndArchivedPosts(status: PostStatus) {
            // Given
            val post = createPost(status = status)
            
            // When & Then
            assertThat(post.canBeDeleted()).isTrue()
        }
        
        @Test
        @DisplayName("Should not allow deletion of published posts")
        fun shouldNotAllowDeletionOfPublishedPosts() {
            // Given
            val publishedPost = createPost(status = PostStatus.PUBLISHED)
            
            // When & Then
            assertThat(publishedPost.canBeDeleted()).isFalse()
        }
    }
    
    @Nested
    @DisplayName("When testing content analysis methods")
    inner class WhenTestingContentAnalysisMethods {
        
        @ParameterizedTest
        @CsvSource(
            "'Hello world', 2",
            "'This is a longer sentence with more words', 8",
            "'Single', 1",
            "'Multiple    spaces    between    words', 4"
        )
        @DisplayName("Should count words correctly")
        fun shouldCountWordsCorrectly(content: String, expectedWordCount: Int) {
            // Given
            val post = createPost(content = content)
            
            // When
            val wordCount = post.getWordCount()
            
            // Then
            assertThat(wordCount).isEqualTo(expectedWordCount)
        }
        
        @ParameterizedTest
        @CsvSource(
            "200, 1",
            "399, 1",
            "400, 2",
            "600, 3",
            "1000, 5"
        )
        @DisplayName("Should calculate reading time correctly")
        fun shouldCalculateReadingTimeCorrectly(wordCount: Int, expectedMinutes: Int) {
            // Given
            val content = "word ".repeat(wordCount).trim()
            val post = createPost(content = content)
            
            // When
            val readingTime = post.getReadingTimeMinutes()
            
            // Then
            assertThat(readingTime).isEqualTo(expectedMinutes)
        }
        
        @Test
        @DisplayName("Should identify popular posts")
        fun shouldIdentifyPopularPosts() {
            // Given
            val popularPost = createPost(viewCount = 150L)
            val unpopularPost = createPost(viewCount = 50L)
            
            // When & Then
            assertThat(popularPost.isPopular()).isTrue()
            assertThat(unpopularPost.isPopular()).isFalse()
        }
        
        @Test
        @DisplayName("Should identify recent posts")
        fun shouldIdentifyRecentPosts() {
            // Given
            val recentPost = createPost(createdAt = LocalDateTime.now().minusDays(3))
            val oldPost = createPost(createdAt = LocalDateTime.now().minusDays(10))
            
            // When & Then
            assertThat(recentPost.isRecent(7)).isTrue()
            assertThat(oldPost.isRecent(7)).isFalse()
        }
    }
    
    @Nested
    @DisplayName("When testing comment-related methods")
    inner class WhenTestingCommentRelatedMethods {
        
        @Test
        @DisplayName("Should count total comments correctly")
        fun shouldCountTotalCommentsCorrectly() {
            // Given
            val post = createPostWithComments(5)
            
            // When & Then
            assertThat(post.getCommentCount()).isEqualTo(5)
        }
        
        @Test
        @DisplayName("Should count approved comments correctly")
        fun shouldCountApprovedCommentsCorrectly() {
            // Given
            val post = createPostWithMixedComments()
            
            // When & Then
            assertThat(post.getApprovedCommentCount()).isEqualTo(3)
        }
        
        @Test
        @DisplayName("Should detect posts with comments")
        fun shouldDetectPostsWithComments() {
            // Given
            val postWithComments = createPostWithComments(2)
            val postWithoutComments = createPost()
            
            // When & Then
            assertThat(postWithComments.hasComments()).isTrue()
            assertThat(postWithoutComments.hasComments()).isFalse()
        }
    }
    
    @Nested
    @DisplayName("When testing engagement calculations")
    inner class WhenTestingEngagementCalculations {
        
        @Test
        @DisplayName("Should calculate engagement score correctly")
        fun shouldCalculateEngagementScoreCorrectly() {
            // Given
            val post = createPost(viewCount = 100L)
                .copy(comments = createApprovedComments(4))
            
            // When
            val engagementScore = post.getEngagementScore()
            
            // Then
            assertThat(engagementScore).isEqualTo(120.0) // 100 views + (4 comments * 5)
        }
    }
    
    @Nested
    @DisplayName("When testing preview methods")
    inner class WhenTestingPreviewMethods {
        
        @Test
        @DisplayName("Should generate title preview for long titles")
        fun shouldGenerateTitlePreviewForLongTitles() {
            // Given
            val longTitle = "This is a very long title that exceeds the maximum length allowed for previews"
            val post = createPost(title = longTitle)
            
            // When
            val preview = post.getTitlePreview(20)
            
            // Then
            assertThat(preview).isEqualTo("This is a very long...")
            assertThat(preview.length).isEqualTo(23) // 20 + "..."
        }
        
        @Test
        @DisplayName("Should return full title for short titles")
        fun shouldReturnFullTitleForShortTitles() {
            // Given
            val shortTitle = "Short title"
            val post = createPost(title = shortTitle)
            
            // When
            val preview = post.getTitlePreview(50)
            
            // Then
            assertThat(preview).isEqualTo(shortTitle)
        }
    }
    
    // Helper methods for creating test data
    private fun createUser(
        id: Long = 1L,
        username: String = "testuser",
        email: String = "test@example.com"
    ): User {
        return User(
            id = id,
            username = username,
            email = email,
            firstName = "Test",
            lastName = "User"
        )
    }
    
    private fun createPost(
        title: String = "Test Post",
        content: String = "Test content for the post",
        status: PostStatus = PostStatus.DRAFT,
        viewCount: Long = 0L,
        author: User = createUser(),
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Post {
        return Post(
            title = title,
            content = content,
            status = status,
            viewCount = viewCount,
            author = author,
            createdAt = createdAt
        )
    }
    
    private fun createPostWithComments(commentCount: Int): Post {
        val author = createUser()
        val post = createPost(author = author)
        val comments = (1..commentCount).map { i ->
            Comment(
                id = i.toLong(),
                content = "Comment $i",
                author = author,
                post = post,
                status = CommentStatus.APPROVED
            )
        }
        return post.copy(comments = comments)
    }
    
    private fun createPostWithMixedComments(): Post {
        val author = createUser()
        val post = createPost(author = author)
        val comments = listOf(
            Comment(
                id = 1L,
                content = "Approved comment 1",
                author = author,
                post = post,
                status = CommentStatus.APPROVED
            ),
            Comment(
                id = 2L,
                content = "Pending comment",
                author = author,
                post = post,
                status = CommentStatus.PENDING
            ),
            Comment(
                id = 3L,
                content = "Approved comment 2",
                author = author,
                post = post,
                status = CommentStatus.APPROVED
            ),
            Comment(
                id = 4L,
                content = "Rejected comment",
                author = author,
                post = post,
                status = CommentStatus.REJECTED
            ),
            Comment(
                id = 5L,
                content = "Approved comment 3",
                author = author,
                post = post,
                status = CommentStatus.APPROVED
            )
        )
        return post.copy(comments = comments)
    }
    
    private fun createApprovedComments(count: Int): List<Comment> {
        val author = createUser()
        val post = createPost(author = author)
        return (1..count).map { i ->
            Comment(
                id = i.toLong(),
                content = "Approved comment $i",
                author = author,
                post = post,
                status = CommentStatus.APPROVED
            )
        }
    }
}