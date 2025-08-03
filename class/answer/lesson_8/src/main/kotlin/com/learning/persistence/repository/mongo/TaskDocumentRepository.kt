/**
 * Lesson 8 Complete Solution: MongoDB Repository - TaskDocumentRepository
 * 
 * Comprehensive MongoDB repository demonstrating NoSQL database capabilities
 */

package com.learning.persistence.repository.mongo

import com.learning.persistence.model.mongo.TaskDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TaskDocumentRepository : MongoRepository<TaskDocument, String> {
    
    // Method name queries - Spring Data MongoDB automatically implements these
    fun findByStatus(status: String): List<TaskDocument>
    
    fun findByTagsContaining(tag: String): List<TaskDocument>
    
    fun findByAssigneeUserId(userId: String): List<TaskDocument>
    
    fun findByPriorityGreaterThan(priority: Int): List<TaskDocument>
    
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<TaskDocument>
    
    fun findByProjectInfoProjectId(projectId: String): List<TaskDocument>
    
    fun findByStatusAndTagsIn(status: String, tags: List<String>): List<TaskDocument>
    
    fun findByAssigneeDepartment(department: String): List<TaskDocument>
    
    fun findByMetadataComplexity(complexity: String): List<TaskDocument>
    
    fun findByMetadataSourceAndStatus(source: String, status: String): List<TaskDocument>
    
    // JSON-based queries using @Query annotation
    @Query("{ 'priority': { '\$gte': ?0 } }")
    fun findByMinPriority(minPriority: Int): List<TaskDocument>
    
    @Query("{ 'assignee.department': ?0, 'status': { '\$ne': 'COMPLETED' } }")
    fun findActiveTasksByDepartment(department: String): List<TaskDocument>
    
    @Query("{ 'tags': { '\$in': ?0 } }")
    fun findByTagsIn(tags: List<String>): List<TaskDocument>
    
    @Query("{ 'metadata.customFields.?0': { '\$exists': true } }")
    fun findByCustomField(fieldName: String): List<TaskDocument>
    
    @Query("{ 'metadata.labels.?0': ?1 }")
    fun findByLabel(labelKey: String, labelValue: String): List<TaskDocument>
    
    @Query("{ 'assignee.userId': ?0, 'status': { '\$in': ['CREATED', 'IN_PROGRESS'] } }")
    fun findActiveTasksByAssignee(userId: String): List<TaskDocument>
    
    @Query("{ 'metadata.dueDate': { '\$lt': ?0 }, 'status': { '\$nin': ['COMPLETED', 'CANCELLED'] } }")
    fun findOverdueTasks(now: LocalDateTime): List<TaskDocument>
    
    @Query("{ 'metadata.attachments': { '\$ne': [] } }")
    fun findTasksWithAttachments(): List<TaskDocument>
    
    @Query("{ 'metadata.dependencies': { '\$ne': [] } }")
    fun findTasksWithDependencies(): List<TaskDocument>
    
    @Query("{ 'metadata.watchers': { '\$in': [?0] } }")
    fun findTasksWatchedByUser(userId: String): List<TaskDocument>
    
    // Complex queries with multiple conditions
    @Query("{ 'status': ?0, 'priority': { '\$gte': ?1 }, 'assignee.department': { '\$in': ?2 }, 'createdAt': { '\$gte': ?3 } }")
    fun findTasksByComplexCriteria(
        status: String,
        minPriority: Int,
        departments: List<String>,
        createdAfter: LocalDateTime
    ): List<TaskDocument>
    
    @Query("{ '\$or': [ { 'title': { '\$regex': ?0, '\$options': 'i' } }, { 'description': { '\$regex': ?0, '\$options': 'i' } }, { 'tags': { '\$regex': ?0, '\$options': 'i' } } ] }")
    fun findByTextInTitleDescriptionOrTags(searchText: String): List<TaskDocument>
    
    // Full-text search queries
    @Query("{ '\$text': { '\$search': ?0 } }")
    fun findByTextSearch(searchText: String): List<TaskDocument>
    
    @Query("{ '\$text': { '\$search': ?0 } }", sort = "{ score: { '\$meta': 'textScore' } }")
    fun findByTextSearchSortedByRelevance(searchText: String): List<TaskDocument>
    
    // Pagination queries
    fun findByStatus(status: String, pageable: Pageable): Page<TaskDocument>
    
    fun findByAssigneeUserId(userId: String, pageable: Pageable): Page<TaskDocument>
    
    @Query("{ 'priority': { '\$gte': ?0 } }")
    fun findByMinPriority(minPriority: Int, pageable: Pageable): Page<TaskDocument>
    
    // Aggregation pipeline queries using @Aggregation annotation
    @Aggregation(pipeline = [
        "{ '\$match': { 'status': { '\$ne': 'COMPLETED' } } }",
        "{ '\$group': { '_id': '\$assignee.department', 'count': { '\$sum': 1 } } }",
        "{ '\$sort': { 'count': -1 } }"
    ])
    fun getActiveTaskCountByDepartment(): List<DepartmentTaskCount>
    
    @Aggregation(pipeline = [
        "{ '\$match': { 'status': 'COMPLETED', 'metadata.actualHours': { '\$ne': null } } }",
        "{ '\$group': { '_id': '\$priority', 'avgHours': { '\$avg': '\$metadata.actualHours' } } }",
        "{ '\$sort': { '_id': 1 } }"
    ])
    fun getAverageCompletionTimeByPriority(): List<PriorityStats>
    
    @Aggregation(pipeline = [
        "{ '\$unwind': '\$tags' }",
        "{ '\$group': { '_id': '\$tags', 'count': { '\$sum': 1 } } }",
        "{ '\$sort': { 'count': -1 } }",
        "{ '\$limit': 10 }"
    ])
    fun getTopTags(): List<TagCount>
    
    @Aggregation(pipeline = [
        "{ '\$match': { 'createdAt': { '\$gte': ?0, '\$lt': ?1 } } }",
        "{ '\$group': { '_id': { 'year': { '\$year': '\$createdAt' }, 'month': { '\$month': '\$createdAt' } }, 'count': { '\$sum': 1 } } }",
        "{ '\$sort': { '_id.year': -1, '_id.month': -1 } }"
    ])
    fun getTaskCountByMonth(startDate: LocalDateTime, endDate: LocalDateTime): List<MonthlyCount>
    
    @Aggregation(pipeline = [
        "{ '\$match': { 'assignee.userId': { '\$ne': null } } }",
        "{ '\$group': { '_id': '\$assignee.userId', 'totalTasks': { '\$sum': 1 }, 'completedTasks': { '\$sum': { '\$cond': [{ '\$eq': ['\$status', 'COMPLETED'] }, 1, 0] } }, 'avgPriority': { '\$avg': '\$priority' } } }",
        "{ '\$addFields': { 'completionRate': { '\$divide': ['\$completedTasks', '\$totalTasks'] } } }",
        "{ '\$sort': { 'completionRate': -1, 'totalTasks': -1 } }"
    ])
    fun getAssigneePerformanceStats(): List<AssigneeStats>
    
    @Aggregation(pipeline = [
        "{ '\$match': { 'metadata.complexity': { '\$ne': null } } }",
        "{ '\$group': { '_id': '\$metadata.complexity', 'count': { '\$sum': 1 }, 'avgEstimatedHours': { '\$avg': '\$metadata.estimatedHours' }, 'avgActualHours': { '\$avg': '\$metadata.actualHours' } } }",
        "{ '\$addFields': { 'estimateAccuracy': { '\$cond': [{ '\$and': [{ '\$ne': ['\$avgEstimatedHours', null] }, { '\$ne': ['\$avgActualHours', null] }] }, { '\$divide': ['\$avgEstimatedHours', '\$avgActualHours'] }, null] } } }",
        "{ '\$sort': { 'count': -1 } }"
    ])
    fun getComplexityAnalysis(): List<ComplexityStats>
    
    @Aggregation(pipeline = [
        "{ '\$match': { 'projectInfo.projectId': { '\$ne': null } } }",
        "{ '\$group': { '_id': '\$projectInfo.projectId', 'projectName': { '\$first': '\$projectInfo.name' }, 'totalTasks': { '\$sum': 1 }, 'completedTasks': { '\$sum': { '\$cond': [{ '\$eq': ['\$status', 'COMPLETED'] }, 1, 0] } }, 'avgPriority': { '\$avg': '\$priority' } } }",
        "{ '\$addFields': { 'completionRate': { '\$divide': ['\$completedTasks', '\$totalTasks'] } } }",
        "{ '\$sort': { 'completionRate': -1 } }"
    ])
    fun getProjectProgress(): List<ProjectProgress>
    
    @Aggregation(pipeline = [
        "{ '\$match': { 'createdAt': { '\$gte': ?0 } } }",
        "{ '\$group': { '_id': { 'date': { '\$dateToString': { 'format': '%Y-%m-%d', 'date': '\$createdAt' } } }, 'created': { '\$sum': 1 }, 'completed': { '\$sum': { '\$cond': [{ '\$eq': ['\$status', 'COMPLETED'] }, 1, 0] } } } }",
        "{ '\$sort': { '_id.date': -1 } }",
        "{ '\$limit': 30 }"
    ])
    fun getDailyTaskTrends(fromDate: LocalDateTime): List<DailyTrend>
    
    @Aggregation(pipeline = [
        "{ '\$unwind': '\$metadata.labels' }",
        "{ '\$group': { '_id': { 'key': '\$metadata.labels.k', 'value': '\$metadata.labels.v' }, 'count': { '\$sum': 1 } } }",
        "{ '\$sort': { 'count': -1 } }",
        "{ '\$limit': 20 }"
    ])
    fun getMostUsedLabels(): List<LabelUsage>
    
    @Aggregation(pipeline = [
        "{ '\$match': { 'metadata.attachments': { '\$ne': [] } } }",
        "{ '\$unwind': '\$metadata.attachments' }",
        "{ '\$group': { '_id': '\$metadata.attachments.contentType', 'count': { '\$sum': 1 }, 'totalSize': { '\$sum': '\$metadata.attachments.fileSize' } } }",
        "{ '\$sort': { 'count': -1 } }"
    ])
    fun getAttachmentTypeStats(): List<AttachmentTypeStats>
}

// Data classes for aggregation results
data class DepartmentTaskCount(
    val _id: String, // department name
    val count: Int
)

data class PriorityStats(
    val _id: Int, // priority level
    val avgHours: Double?
)

data class TagCount(
    val _id: String, // tag name
    val count: Int
)

data class MonthlyCount(
    val _id: MonthYear,
    val count: Int
)

data class MonthYear(
    val year: Int,
    val month: Int
)

data class AssigneeStats(
    val _id: String, // userId
    val totalTasks: Int,
    val completedTasks: Int,
    val avgPriority: Double,
    val completionRate: Double
)

data class ComplexityStats(
    val _id: String, // complexity level
    val count: Int,
    val avgEstimatedHours: Double?,
    val avgActualHours: Double?,
    val estimateAccuracy: Double?
)

data class ProjectProgress(
    val _id: String, // projectId
    val projectName: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val avgPriority: Double,
    val completionRate: Double
)

data class DailyTrend(
    val _id: DailyTrendId,
    val created: Int,
    val completed: Int
)

data class DailyTrendId(
    val date: String
)

data class LabelUsage(
    val _id: LabelKey,
    val count: Int
)

data class LabelKey(
    val key: String,
    val value: String
)

data class AttachmentTypeStats(
    val _id: String, // contentType
    val count: Int,
    val totalSize: Long
)