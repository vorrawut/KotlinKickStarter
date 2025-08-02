# 🎯 Lesson 2: Collections & Functional Programming

**Duration**: 25 minutes  
**Prerequisites**: Lesson 1 (Kotlin Basics)  
**Difficulty**: Intermediate  

## 📋 What You'll Learn

- Kotlin's powerful collection operations (filter, map, groupBy, etc.)
- Functional programming patterns and method chaining
- Extension functions for domain-specific operations
- Real-world data processing and analytics patterns
- Performance considerations with sequences

## 🚀 Quick Start

1. **Workshop** (Start here): [workshop_2.md](workshop_2.md)
2. **Concepts** (Theory): [concept.md](concept.md)
3. **Practice**: Complete the TODOs in `class/workshop/lesson_2/`
4. **Verify**: Compare with solutions in `class/answer/lesson_2/`

## 🎯 Learning Objectives

By the end of this lesson, you will be able to:

- ✅ Use functional operations to transform and filter data
- ✅ Chain collection operations for complex data processing
- ✅ Create extension functions for domain-specific operations
- ✅ Build analytics and reporting systems
- ✅ Handle performance considerations with large datasets

## 🔨 What You'll Build

A **User Analytics System** that demonstrates:

```kotlin
// Functional data processing
val topEngineers = users
    .getActiveUsers()
    .getUsersByDepartment("Engineering")
    .getHighPerformers()
    .getTopPerformers(5)

// Extension functions for domain logic
fun List<User>.departmentCounts(): Map<String, Int> = 
    groupBy { it.department }.mapValues { it.value.size }

// Complex analytics
val insights = users
    .filter { it.isActive }
    .groupBy { it.department }
    .mapValues { (_, deptUsers) -> 
        DepartmentStats.fromUsers(deptUsers) 
    }
```

## 📚 Key Concepts Covered

### 1. **Collection Operations**
- Filtering: `filter`, `find`, `firstOrNull`
- Transformation: `map`, `flatMap`, `associate`
- Aggregation: `sumOf`, `average`, `maxBy`, `groupBy`
- Sorting: `sortedBy`, `sortedByDescending`

### 2. **Method Chaining**
- Fluent API design with collection operations
- Combining multiple transformations
- Readable data processing pipelines

### 3. **Extension Functions**
- Adding domain-specific operations to collections
- Creating reusable, expressive APIs
- Encapsulating business logic

### 4. **Performance Optimization**
- When to use sequences for lazy evaluation
- Collection vs sequence performance characteristics
- Memory efficiency considerations

## 🧪 Testing Your Knowledge

The workshop includes:
- Real data generation with 1000+ user records
- Complex analytics across multiple departments
- Performance comparisons between collections and sequences
- Advanced filtering with multiple criteria

## 🎯 Success Criteria

Your implementation should:
- ✅ Process 1000+ user records efficiently
- ✅ Generate comprehensive department analytics
- ✅ Demonstrate advanced collection chaining
- ✅ Use extension functions for clean APIs
- ✅ Show performance optimization techniques

## 🔄 Run & Test

```bash
# Navigate to workshop
cd class/workshop/lesson_2

# Build and run
./gradlew run

# Expected output: Complete analytics dashboard
# with department stats, top performers, insights
```

## 📈 What's Next?

- **Lesson 3**: OOP + Kotlin Features (Sealed Classes, Interfaces)
- **Lesson 4**: Spring Boot Setup & Dependency Injection
- **Lesson 5**: REST Controllers & DTOs

## 💡 Pro Tips

1. **Start with simple operations** - filter, map, then build complexity
2. **Use meaningful extension names** - `getActiveUsers()` vs `filter { it.isActive }`
3. **Chain operations fluently** - readable left-to-right processing
4. **Consider sequences for large data** - lazy evaluation can improve performance
5. **Test with realistic data** - use the data generator for varied scenarios

## 🎮 Challenge Exercises

Once you complete the basic workshop:

1. **Add time-series analysis** - track performance trends over time
2. **Implement caching** - cache expensive analytics calculations
3. **Create custom aggregations** - percentiles, standard deviation
4. **Build comparison tools** - department vs department analytics
5. **Add data validation** - ensure data quality in processing

## 🤔 Common Patterns

### Data Transformation Pipeline
```kotlin
users.filter { criteria }
     .map { transformation }
     .groupBy { categorization }
     .mapValues { aggregation }
```

### Multi-step Analytics
```kotlin
val analytics = users
    .groupBy { it.department }
    .mapValues { (dept, deptUsers) ->
        AnalyticsService.analyze(dept, deptUsers)
    }
```

### Extension Function Design
```kotlin
// Good: Domain-specific, reusable
fun List<User>.getHighPerformers() = filter { it.score >= 85 }

// Avoid: Too generic or complex
fun List<User>.complexAnalysis() = /* too much logic */
```

## 🐛 Troubleshooting

**Common Issues:**

1. **Empty collection errors** - Use safe operations: `firstOrNull()`, check `isEmpty()`
2. **Performance with large datasets** - Consider `asSequence()` for chained operations
3. **Memory issues** - Avoid creating unnecessary intermediate collections
4. **Type inference problems** - Be explicit with generic types when needed

**Best Practices:**
- Handle empty collections gracefully
- Use appropriate collection types (List vs Set vs Map)
- Prefer immutable collections and transformations
- Chain operations for readability

## 🎉 Congratulations!

After completing this lesson, you'll have mastered Kotlin's collections API and functional programming patterns. These skills are essential for:

- **Data processing** in backend applications
- **Analytics and reporting** systems
- **Business logic implementation** with clean, readable code
- **Performance optimization** in data-intensive applications

The collection operations and functional patterns you learn here are used extensively in real-world Kotlin development and form the foundation for more advanced topics in upcoming lessons.

**Ready for advanced OOP features?** Continue to [Lesson 3: OOP + Kotlin Features](../lesson_3/)