/**
 * Lesson 8 Workshop: MongoDB Repository - TaskDocumentRepository
 * 
 * TODO: Create comprehensive MongoDB repository for TaskDocument
 * Demonstrate MongoDB-specific queries and aggregation features
 */

package com.learning.persistence.repository.mongo

// TODO: Import necessary Spring Data MongoDB classes
// TODO: Import MongoRepository
// TODO: Import @Query and @Aggregation annotations
// TODO: Import your TaskDocument and related classes
import com.learning.persistence.model.mongo.TaskDocument
import java.time.LocalDateTime

// TODO: Create interface extending MongoRepository<TaskDocument, String>
interface TaskDocumentRepository {
    
    // TODO: Add method name queries (Spring Data MongoDB will implement automatically)
    // Examples:
    // - findByStatus(status: String): List<TaskDocument>
    // - findByTagsContaining(tag: String): List<TaskDocument>
    // - findByAssigneeUserId(userId: String): List<TaskDocument>
    // - findByPriorityGreaterThan(priority: Int): List<TaskDocument>
    // - findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<TaskDocument>
    // - findByProjectInfoProjectId(projectId: String): List<TaskDocument>
    
    // TODO: Add JSON-based queries using @Query annotation
    // Examples:
    // @Query("{ 'priority': { '\$gte': ?0 } }")
    // fun findByMinPriority(minPriority: Int): List<TaskDocument>
    
    // @Query("{ 'assignee.department': ?0, 'status': { '\$ne': 'COMPLETED' } }")
    // fun findActiveTasksByDepartment(department: String): List<TaskDocument>
    
    // @Query("{ 'tags': { '\$in': ?0 } }")
    // fun findByTagsIn(tags: List<String>): List<TaskDocument>
    
    // @Query("{ 'metadata.customFields.?0': { '\$exists': true } }")
    // fun findByCustomField(fieldName: String): List<TaskDocument>
    
    // TODO: Add full-text search queries
    // @Query("{ '\$text': { '\$search': ?0 } }")
    // fun findByTextSearch(searchText: String): List<TaskDocument>
    
    // TODO: Add aggregation pipeline queries using @Aggregation annotation
    // Examples:
    // @Aggregation(pipeline = [
    //     "{ '\$match': { 'status': { '\$ne': 'COMPLETED' } } }",
    //     "{ '\$group': { '_id': '\$assignee.department', 'count': { '\$sum': 1 } } }",
    //     "{ '\$sort': { 'count': -1 } }"
    // ])
    // fun getActiveTaskCountByDepartment(): List<DepartmentTaskCount>
    
    // @Aggregation(pipeline = [
    //     "{ '\$match': { 'status': 'COMPLETED' } }",
    //     "{ '\$group': { '_id': '\$priority', 'avgHours': { '\$avg': '\$metadata.actualHours' } } }"
    // ])
    // fun getAverageCompletionTimeByPriority(): List<PriorityStats>
    
    // @Aggregation(pipeline = [
    //     "{ '\$unwind': '\$tags' }",
    //     "{ '\$group': { '_id': '\$tags', 'count': { '\$sum': 1 } } }",
    //     "{ '\$sort': { 'count': -1 } }",
    //     "{ '\$limit': 10 }"
    // ])
    // fun getTopTags(): List<TagCount>
    
    // TODO: Add geographic or date-based aggregations
    // @Aggregation(pipeline = [
    //     "{ '\$match': { 'createdAt': { '\$gte': ?0, '\$lt': ?1 } } }",
    //     "{ '\$group': { '_id': { 'year': { '\$year': '\$createdAt' }, 'month': { '\$month': '\$createdAt' } }, 'count': { '\$sum': 1 } } }"
    // ])
    // fun getTaskCountByMonth(startDate: LocalDateTime, endDate: LocalDateTime): List<MonthlyCount>
}

// TODO: Create data classes for aggregation results
// data class DepartmentTaskCount(val department: String, val count: Int)
// data class PriorityStats(val priority: Int, val avgHours: Double)
// data class TagCount(val tag: String, val count: Int)
// data class MonthlyCount(val year: Int, val month: Int, val count: Int)