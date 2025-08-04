package com.learning.caching.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "blog_posts")
data class BlogPost(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val title: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,
    
    @Column(nullable = false)
    val authorId: Long,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId", insertable = false, updatable = false)
    val author: User? = null,
    
    @ElementCollection
    @CollectionTable(name = "blog_post_tags", joinColumns = [JoinColumn(name = "post_id")])
    @Column(name = "tag")
    val tags: Set<String> = emptySet(),
    
    @Column(nullable = false)
    val viewCount: Long = 0,
    
    @Column(nullable = false)
    val published: Boolean = false,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun getWordCount(): Int = content.split("\\s+".toRegex()).size
    
    fun getReadingTimeMinutes(): Int = (getWordCount() / 200).coerceAtLeast(1)
    
    fun getPreview(length: Int = 150): String {
        return if (content.length <= length) content
        else content.substring(0, length) + "..."
    }
}