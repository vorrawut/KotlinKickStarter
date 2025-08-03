/**
 * Lesson 8 Workshop: MongoDB Document - TaskDocument
 * 
 * TODO: Create a complete MongoDB document for flexible task data
 * This document stores additional metadata and flexible schema data
 */

package com.learning.persistence.model.mongo

// TODO: Import necessary MongoDB annotations
// TODO: Import LocalDateTime for date handling
import java.time.LocalDateTime

// TODO: Add @Document annotation with collection name "tasks"
// TODO: Add @CompoundIndex annotations for efficient queries
// Example: @CompoundIndex(def = "{'status': 1, 'priority': -1}")
data class TaskDocument(
    // TODO: Add @Id annotation for MongoDB primary key
    val id: String? = null,
    
    // TODO: Add @Indexed annotation for title field
    val title: String,
    
    // TODO: Add @TextIndexed annotation for full-text search on description
    val description: String?,
    
    val status: String,
    
    val priority: Int,
    
    // TODO: Embed assignee information instead of just ID
    val assignee: AssigneeInfo?,
    
    // TODO: Embed project information instead of just ID
    val projectInfo: ProjectInfo?,
    
    // TODO: Add flexible metadata object
    val metadata: TaskMetadata,
    
    // TODO: Add @Indexed annotation for tags array
    val tags: List<String> = emptyList(),
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // TODO: Add document-specific business logic
    fun hasTag(tag: String): Boolean {
        // TODO: Implement tag checking logic
        return false
    }
    
    fun addTag(tag: String): TaskDocument {
        // TODO: Implement immutable tag addition
        return this
    }
    
    fun updateMetadata(key: String, value: Any): TaskDocument {
        // TODO: Implement metadata update logic
        return this
    }
}

// TODO: Create embedded document for assignee information
data class AssigneeInfo(
    // TODO: Add assignee properties:
    // - userId: String
    // - name: String
    // - email: String
    // - department: String
    // - role: String
)

// TODO: Create embedded document for project information
data class ProjectInfo(
    // TODO: Add project properties:
    // - projectId: String
    // - name: String
    // - description: String
    // - status: String
)

// TODO: Create embedded document for flexible metadata
data class TaskMetadata(
    // TODO: Add metadata properties:
    // - estimatedHours: Int?
    // - actualHours: Int?
    // - complexity: String?
    // - source: String? (where the task came from)
    // - labels: Map<String, String> (flexible key-value pairs)
    // - attachments: List<AttachmentInfo>
    // - customFields: Map<String, Any> (completely flexible data)
)

// TODO: Create embedded document for attachment information
data class AttachmentInfo(
    // TODO: Add attachment properties:
    // - fileName: String
    // - fileSize: Long
    // - contentType: String
    // - uploadedBy: String
    // - uploadedAt: LocalDateTime
    // - url: String?
)