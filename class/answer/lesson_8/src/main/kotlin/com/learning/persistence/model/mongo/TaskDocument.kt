/**
 * Lesson 8 Complete Solution: MongoDB Document - TaskDocument
 * 
 * Complete MongoDB document for flexible task data with rich embedded information
 */

package com.learning.persistence.model.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "tasks")
@CompoundIndex(def = "{'status': 1, 'priority': -1}")
@CompoundIndex(def = "{'assignee.userId': 1, 'status': 1}")
@CompoundIndex(def = "{'projectInfo.projectId': 1, 'status': 1}")
@CompoundIndex(def = "{'createdAt': -1}")
data class TaskDocument(
    @Id
    val id: String? = null,
    
    @Indexed
    @TextIndexed
    val title: String,
    
    @TextIndexed
    val description: String?,
    
    @Indexed
    val status: String,
    
    @Indexed
    val priority: Int,
    
    val assignee: AssigneeInfo?,
    
    val projectInfo: ProjectInfo?,
    
    val metadata: TaskMetadata,
    
    @Indexed
    val tags: List<String> = emptyList(),
    
    @Indexed
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    fun hasTag(tag: String): Boolean {
        return tags.any { it.equals(tag, ignoreCase = true) }
    }
    
    fun addTag(tag: String): TaskDocument {
        return if (!hasTag(tag)) {
            copy(tags = tags + tag, updatedAt = LocalDateTime.now())
        } else {
            this
        }
    }
    
    fun removeTag(tag: String): TaskDocument {
        return copy(
            tags = tags.filter { !it.equals(tag, ignoreCase = true) },
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun updateMetadata(key: String, value: Any): TaskDocument {
        val updatedCustomFields = metadata.customFields.toMutableMap()
        updatedCustomFields[key] = value
        
        return copy(
            metadata = metadata.copy(customFields = updatedCustomFields),
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun hasCustomField(key: String): Boolean {
        return metadata.customFields.containsKey(key)
    }
    
    fun getCustomField(key: String): Any? {
        return metadata.customFields[key]
    }
    
    fun isOverdue(): Boolean {
        return metadata.dueDate?.isBefore(LocalDateTime.now()) == true && 
               status !in listOf("COMPLETED", "CANCELLED")
    }
    
    fun getWorkloadScore(): Int {
        val priorityWeight = priority * 2
        val urgencyWeight = if (isOverdue()) 10 else 0
        val complexityWeight = when (metadata.complexity?.uppercase()) {
            "HIGH" -> 8
            "MEDIUM" -> 4
            "LOW" -> 2
            else -> 3
        }
        return priorityWeight + urgencyWeight + complexityWeight
    }
}

data class AssigneeInfo(
    val userId: String,
    val name: String,
    val email: String,
    val department: String,
    val role: String
) {
    fun getDisplayName(): String = "$name ($role)"
    
    fun isFromDepartment(dept: String): Boolean = 
        department.equals(dept, ignoreCase = true)
}

data class ProjectInfo(
    val projectId: String,
    val name: String,
    val description: String,
    val status: String
) {
    fun isActive(): Boolean = status.uppercase() in listOf("ACTIVE", "IN_PROGRESS")
    
    fun getDisplayInfo(): String = "$name (${status})"
}

data class TaskMetadata(
    val estimatedHours: Int? = null,
    val actualHours: Int? = null,
    val complexity: String? = null,
    val source: String? = null,
    val labels: Map<String, String> = emptyMap(),
    val attachments: List<AttachmentInfo> = emptyList(),
    val customFields: Map<String, Any> = emptyMap(),
    val dueDate: LocalDateTime? = null,
    val category: String? = null,
    val externalId: String? = null,
    val dependencies: List<String> = emptyList(),
    val watchers: List<String> = emptyList()
) {
    fun isEstimateAccurate(): Boolean {
        return if (estimatedHours != null && actualHours != null) {
            val variance = kotlin.math.abs(estimatedHours - actualHours)
            variance <= (estimatedHours * 0.2) // Within 20%
        } else false
    }
    
    fun getEfficiencyRatio(): Double? {
        return if (estimatedHours != null && actualHours != null && actualHours > 0) {
            estimatedHours.toDouble() / actualHours
        } else null
    }
    
    fun hasAttachments(): Boolean = attachments.isNotEmpty()
    
    fun hasDependencies(): Boolean = dependencies.isNotEmpty()
    
    fun hasWatchers(): Boolean = watchers.isNotEmpty()
    
    fun getLabel(key: String): String? = labels[key]
    
    fun hasLabel(key: String): Boolean = labels.containsKey(key)
}

data class AttachmentInfo(
    val fileName: String,
    val fileSize: Long,
    val contentType: String,
    val uploadedBy: String,
    val uploadedAt: LocalDateTime,
    val url: String? = null,
    val checksum: String? = null
) {
    fun isImage(): Boolean = contentType.startsWith("image/")
    
    fun isDocument(): Boolean = contentType in listOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )
    
    fun getFileSizeInMB(): Double = fileSize / (1024.0 * 1024.0)
    
    fun getDisplayName(): String = "$fileName (${String.format("%.2f", getFileSizeInMB())} MB)"
}