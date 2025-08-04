# 🚀 Lesson 15 Workshop: File Handling & Uploads

## 🎯 Workshop Objective

Build a comprehensive file management system with secure uploads, multiple storage strategies, file validation, and efficient download streaming. You'll create a production-ready file handling service that can manage images, documents, and other media.

**⏱️ Estimated Time**: 40 minutes

---

## 🏗️ What You'll Build

### **Complete File Management System**
- **Secure File Uploads**: MultipartFile handling with comprehensive validation
- **Multiple Storage Options**: Local filesystem and cloud storage (S3) implementations
- **File Security**: Type validation, size limits, virus scanning, access control
- **Efficient Downloads**: Streaming support with range requests for large files
- **Metadata Management**: File tracking, search, tagging, and statistics
- **Automatic Cleanup**: Scheduled tasks for expired and temporary files

### **Real-World Features**
```kotlin
// Upload with validation and metadata
POST /api/files/upload
Content-Type: multipart/form-data
- file: product-image.jpg (2MB)
- title: "Red Running Shoes"
- tags: ["product", "shoes", "red"]
- isPublic: true
- category: "IMAGE"

// Secure download with access control
GET /api/files/download/abc123-unique-filename.jpg
Authorization: Bearer jwt-token

// Streaming for large files (videos, documents)
GET /api/files/stream/large-video.mp4
Range: bytes=1024-2048
```

**🎯 Success Metrics**: 
- Secure file uploads with 100% validation coverage
- Multi-storage support (local + cloud)
- Efficient streaming downloads with range request support
- Comprehensive file metadata and search capabilities

---

## 📁 Project Structure

```
class/workshop/lesson_15/
├── build.gradle.kts          # ✅ File handling dependencies
├── src/main/
│   ├── kotlin/com/learning/files/
│   │   ├── FilesApplication.kt
│   │   ├── config/
│   │   │   ├── FileStorageConfig.kt     # TODO: Configure storage strategies
│   │   │   └── SecurityConfig.kt        # TODO: File access security
│   │   ├── model/
│   │   │   ├── FileMetadata.kt
│   │   │   ├── FileCategory.kt
│   │   │   └── FileUploadRequest.kt
│   │   ├── repository/
│   │   │   └── FileMetadataRepository.kt
│   │   ├── service/
│   │   │   ├── FileStorageService.kt    # TODO: Storage interface
│   │   │   ├── LocalFileStorageService.kt # TODO: Local storage impl
│   │   │   ├── S3FileStorageService.kt  # TODO: Cloud storage impl
│   │   │   ├── FileValidationService.kt # TODO: File validation
│   │   │   ├── FileMetadataService.kt   # TODO: Metadata management
│   │   │   └── SecureFileService.kt     # TODO: Main file service
│   │   ├── controller/
│   │   │   ├── FileUploadController.kt  # TODO: Upload endpoints
│   │   │   ├── FileDownloadController.kt # TODO: Download endpoints
│   │   │   └── FileManagementController.kt # TODO: Admin endpoints
│   │   └── dto/
│   │       └── FileDTOs.kt
│   └── resources/
│       └── application.yml              # TODO: File storage configuration
└── src/test/
    └── kotlin/com/learning/files/
        └── FilesApplicationTests.kt     # TODO: File operation tests
```

---

## 🛠️ Step 1: Configure Dependencies & Storage

### **📝 TODO: Update build.gradle.kts**
```kotlin
dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // File handling
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Cloud storage (AWS S3)
    implementation("software.amazon.awssdk:s3:2.20.26")
    implementation("software.amazon.awssdk:auth:2.20.26")
    
    // Kotlin & Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // Database
    runtimeOnly("com.h2database:h2")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}
```

### **📝 TODO: Configure application.yml**
```yaml
spring:
  application:
    name: files-workshop
  
  datasource:
    url: jdbc:h2:mem:files_db
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
      enabled: true
      file-size-threshold: 2KB
      location: ${java.io.tmpdir}

# TODO: Add file storage configuration
app:
  file:
    upload-dir: ./uploads
    storage-type: local  # local or s3
    max-size:
      image: 10485760    # 10MB
      document: 52428800 # 50MB  
      video: 524288000   # 500MB
    allowed-types:
      image: [image/jpeg, image/png, image/gif, image/webp]
      document: [application/pdf, text/plain, application/msword]
    cleanup:
      enabled: true
      expired-files-interval: 3600000  # 1 hour

# TODO: Add AWS S3 configuration (optional)
aws:
  s3:
    bucket-name: my-app-files
    region: us-east-1

logging:
  level:
    com.learning.files: DEBUG
```

---

## 🛠️ Step 2: Create File Storage Interface

### **📝 TODO: Implement FileStorageService.kt**
```kotlin
package com.learning.files.service

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface FileStorageService {
    fun store(file: MultipartFile, directory: String = ""): StoredFile
    fun load(filename: String, directory: String = ""): Resource
    fun delete(filename: String, directory: String = ""): Boolean
    fun exists(filename: String, directory: String = ""): Boolean
    fun getUrl(filename: String, directory: String = ""): String
}

data class StoredFile(
    val filename: String,
    val originalName: String,
    val size: Long,
    val contentType: String,
    val directory: String,
    val url: String
)
```

### **📝 TODO: Implement LocalFileStorageService.kt**
```kotlin
package com.learning.files.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.*
import java.util.*
import javax.annotation.PostConstruct

@Service
class LocalFileStorageService : FileStorageService {
    
    @Value("\${app.file.upload-dir:./uploads}")
    private lateinit var uploadDir: String
    
    private lateinit var rootLocation: Path
    private val logger = LoggerFactory.getLogger(LocalFileStorageService::class.java)
    
    @PostConstruct
    fun init() {
        // TODO: Initialize storage directory
        // HINT: Create rootLocation from uploadDir
        // HINT: Create directories if they don't exist
    }
    
    override fun store(file: MultipartFile, directory: String): StoredFile {
        // TODO: Implement file storage
        // HINT: Generate unique filename using UUID + timestamp
        // HINT: Create directory if it doesn't exist
        // HINT: Copy file to target location
        // HINT: Return StoredFile with file information
        
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("Filename cannot be null")
        
        // TODO: Generate unique filename
        val extension = getFileExtension(originalFilename)
        val uniqueFilename = "${UUID.randomUUID()}_${System.currentTimeMillis()}.$extension"
        
        // TODO: Determine target directory and file path
        
        // TODO: Store file using Files.copy()
        
        // TODO: Return StoredFile object
        
        throw NotImplementedError("TODO: Implement file storage")
    }
    
    override fun load(filename: String, directory: String): Resource {
        // TODO: Load file as Resource
        // HINT: Resolve file path in target directory
        // HINT: Create UrlResource from file path
        // HINT: Check if resource exists and is readable
        
        throw NotImplementedError("TODO: Implement file loading")
    }
    
    override fun delete(filename: String, directory: String): Boolean {
        return try {
            // TODO: Delete file from filesystem
            // HINT: Resolve file path and use Files.deleteIfExists()
            false
        } catch (e: IOException) {
            logger.error("Failed to delete file: $filename", e)
            false
        }
    }
    
    override fun exists(filename: String, directory: String): Boolean {
        // TODO: Check if file exists
        // HINT: Resolve file path and use Files.exists()
        return false
    }
    
    override fun getUrl(filename: String, directory: String): String {
        // TODO: Generate download URL
        // HINT: Return relative URL for download endpoint
        return "/api/files/download/$filename" + if (directory.isNotBlank()) "?dir=$directory" else ""
    }
    
    private fun getFileExtension(filename: String): String {
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot > 0 && lastDot < filename.length - 1) {
            filename.substring(lastDot + 1)
        } else ""
    }
}
```

---

## 🛠️ Step 3: Implement File Validation

### **📝 TODO: Create FileValidationService.kt**
```kotlin
package com.learning.files.service

import com.learning.files.model.FileCategory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileValidationService {
    
    companion object {
        // TODO: Define file size limits
        const val MAX_IMAGE_SIZE = 10 * 1024 * 1024      // 10MB
        const val MAX_DOCUMENT_SIZE = 50 * 1024 * 1024   // 50MB
        const val MAX_VIDEO_SIZE = 500 * 1024 * 1024     // 500MB
        
        // TODO: Define allowed MIME types
        val ALLOWED_IMAGE_TYPES = setOf(
            "image/jpeg", "image/png", "image/gif", "image/webp"
        )
        
        val ALLOWED_DOCUMENT_TYPES = setOf(
            "application/pdf", "text/plain", "application/msword"
        )
        
        // TODO: Define dangerous file extensions
        val DANGEROUS_EXTENSIONS = setOf(
            "exe", "bat", "cmd", "scr", "pif", "vbs", "js", "jar"
        )
    }
    
    private val logger = LoggerFactory.getLogger(FileValidationService::class.java)
    
    fun validateFile(file: MultipartFile, category: FileCategory): ValidationResult {
        val errors = mutableListOf<String>()
        
        // TODO: Implement comprehensive file validation
        
        // 1. Check if file is empty
        if (file.isEmpty) {
            errors.add("File is empty")
        }
        
        // 2. Validate filename
        val filename = file.originalFilename ?: ""
        if (filename.isBlank()) {
            errors.add("Filename is required")
        }
        
        // 3. TODO: Check for dangerous file extensions
        // HINT: Get file extension and check against DANGEROUS_EXTENSIONS
        
        // 4. TODO: Validate file size based on category
        // HINT: Use maxSize variable based on category
        
        // 5. TODO: Validate MIME type
        // HINT: Get contentType and check against allowed types for category
        
        // 6. TODO: Advanced validation - check file signature
        // HINT: Read first few bytes and validate against known file signatures
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    private fun validateFileSignature(file: MultipartFile, declaredType: String): Boolean {
        // TODO: Implement file signature validation
        // HINT: Read first 8 bytes of file
        // HINT: Check against known file signatures (magic numbers)
        
        return try {
            val bytes = file.inputStream.use { it.readNBytes(8) }
            
            when (declaredType) {
                "image/jpeg" -> bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
                "image/png" -> bytes.size >= 8 && 
                    bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && 
                    bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
                "application/pdf" -> bytes.size >= 4 && 
                    bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte() &&
                    bytes[2] == 0x44.toByte() && bytes[3] == 0x46.toByte()
                else -> true // Skip validation for unknown types
            }
        } catch (e: Exception) {
            logger.warn("Could not validate file signature", e)
            true // Don't fail upload for signature errors
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)
```

---

## 🛠️ Step 4: Create File Upload Controller

### **📝 TODO: Implement FileUploadController.kt**
```kotlin
package com.learning.files.controller

import com.learning.files.model.FileCategory
import com.learning.files.service.SecureFileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/files")
class FileUploadController(
    private val secureFileService: SecureFileService
) {
    
    // TODO: Basic file upload
    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("category") category: FileCategory,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("tags", required = false) tags: List<String>?,
        @RequestParam("isPublic", defaultValue = "false") isPublic: Boolean
    ): ResponseEntity<*> {
        
        // TODO: Create metadata object
        // TODO: Call secureFileService.uploadFile()
        // TODO: Return appropriate response based on result
        
        return ResponseEntity.ok("TODO: Implement file upload")
    }
    
    // TODO: Multiple file upload
    @PostMapping("/upload-multiple")
    fun uploadMultipleFiles(
        @RequestParam("files") files: Array<MultipartFile>,
        @RequestParam("category") category: FileCategory
    ): ResponseEntity<*> {
        
        // TODO: Process each file
        // TODO: Collect results
        // TODO: Return summary of upload results
        
        return ResponseEntity.ok("TODO: Implement multiple file upload")
    }
    
    // TODO: Upload with JSON metadata
    @PostMapping("/upload-with-metadata", consumes = ["multipart/form-data"])
    fun uploadWithMetadata(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("metadata") metadataJson: String
    ): ResponseEntity<*> {
        
        // TODO: Parse JSON metadata
        // TODO: Upload file with parsed metadata
        // TODO: Return upload result
        
        return ResponseEntity.ok("TODO: Implement upload with metadata")
    }
}
```

---

## 🛠️ Step 5: Create Download Controller with Streaming

### **📝 TODO: Implement FileDownloadController.kt**
```kotlin
package com.learning.files.controller

import com.learning.files.service.SecureFileService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/files")
class FileDownloadController(
    private val secureFileService: SecureFileService
) {
    
    // TODO: Basic file download
    @GetMapping("/download/{fileId}")
    fun downloadFile(
        @PathVariable fileId: Long,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        
        // TODO: Get current user ID (from security context)
        val requestedBy = getCurrentUserId()
        
        // TODO: Download file using secureFileService
        // TODO: Return file as attachment with proper headers
        
        return ResponseEntity.notFound().build()
    }
    
    // TODO: Inline file viewing (for images, PDFs)
    @GetMapping("/view/{fileId}")
    fun viewFile(
        @PathVariable fileId: Long,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        
        // TODO: Similar to downloadFile but with inline disposition
        // TODO: Set Content-Disposition to "inline"
        
        return ResponseEntity.notFound().build()
    }
    
    // TODO: Streaming download for large files
    @GetMapping("/stream/{fileId}")
    fun streamFile(
        @PathVariable fileId: Long,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        // TODO: Implement streaming download
        // TODO: Handle Range requests for video streaming
        // TODO: Set appropriate headers for streaming
        
        try {
            val requestedBy = getCurrentUserId()
            
            // TODO: Get file download result
            // TODO: Check if successful
            // TODO: Set response headers
            // TODO: Handle range requests if present
            // TODO: Stream file content
            
        } catch (e: Exception) {
            response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        }
    }
    
    private fun getCurrentUserId(): Long? {
        // TODO: Extract user ID from security context
        // HINT: Use SecurityContextHolder.getContext().authentication
        return null // Placeholder
    }
    
    private fun handleRangeRequest(
        resource: Resource,
        rangeHeader: String,
        response: HttpServletResponse
    ) {
        // TODO: Parse Range header
        // TODO: Set partial content status and headers
        // TODO: Stream requested byte range
    }
}
```

---

## 🛠️ Step 6: Create File Metadata Management

### **📝 TODO: Implement FileMetadataService.kt**
```kotlin
package com.learning.files.service

import com.learning.files.model.FileMetadata
import com.learning.files.repository.FileMetadataRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class FileMetadataService(
    private val fileMetadataRepository: FileMetadataRepository
) {
    
    fun saveMetadata(
        storedFile: StoredFile,
        metadata: FileUploadMetadata? = null,
        uploadedBy: Long? = null
    ): FileMetadata {
        
        // TODO: Create FileMetadata entity
        // TODO: Set all properties from storedFile and metadata
        // TODO: Save to repository
        
        val fileMetadata = FileMetadata(
            filename = storedFile.filename,
            originalName = storedFile.originalName,
            contentType = storedFile.contentType,
            size = storedFile.size,
            // TODO: Set other properties
        )
        
        return fileMetadataRepository.save(fileMetadata)
    }
    
    fun getById(id: Long): FileMetadata? {
        // TODO: Find by ID
        return fileMetadataRepository.findById(id).orElse(null)
    }
    
    fun searchFiles(
        query: String? = null,
        tags: Set<String> = emptySet(),
        contentType: String? = null,
        uploadedBy: Long? = null,
        pageable: Pageable
    ): Page<FileMetadata> {
        
        // TODO: Implement search functionality
        // TODO: Use repository method with criteria
        
        return fileMetadataRepository.findAll(pageable)
    }
    
    fun incrementDownloadCount(fileId: Long) {
        // TODO: Update download count and last accessed time
        // HINT: Find by ID, update counters, save back
    }
    
    fun getFileStatistics(): FileStatistics {
        // TODO: Calculate file statistics
        // TODO: Total files, size, public/private counts
        
        val totalFiles = fileMetadataRepository.count()
        // TODO: Add more statistics
        
        return FileStatistics(
            totalFiles = totalFiles,
            totalSize = 0L,  // TODO: Calculate
            publicFiles = 0L, // TODO: Calculate
            privateFiles = 0L // TODO: Calculate
        )
    }
}

data class FileUploadMetadata(
    val title: String? = null,
    val description: String? = null,
    val tags: Set<String> = emptySet(),
    val isPublic: Boolean = false,
    val expiresAt: LocalDateTime? = null
)

data class FileStatistics(
    val totalFiles: Long,
    val totalSize: Long,
    val publicFiles: Long,
    val privateFiles: Long
)
```

---

## 🛠️ Step 7: Integrate Everything in SecureFileService

### **📝 TODO: Implement SecureFileService.kt**
```kotlin
package com.learning.files.service

import com.learning.files.model.FileCategory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class SecureFileService(
    private val fileStorageService: FileStorageService,
    private val fileMetadataService: FileMetadataService,
    private val fileValidationService: FileValidationService
) {
    
    private val logger = LoggerFactory.getLogger(SecureFileService::class.java)
    
    @Transactional
    fun uploadFile(
        file: MultipartFile,
        category: FileCategory,
        metadata: FileUploadMetadata? = null,
        uploadedBy: Long? = null
    ): FileUploadResult {
        
        // TODO: Step 1 - Validate file
        // HINT: Use fileValidationService.validateFile()
        
        // TODO: Step 2 - Store file
        // HINT: Use fileStorageService.store()
        
        // TODO: Step 3 - Save metadata
        // HINT: Use fileMetadataService.saveMetadata()
        
        // TODO: Return success result
        
        return FileUploadResult(
            success = false,
            errors = listOf("TODO: Implement file upload")
        )
    }
    
    fun downloadFile(fileId: Long, requestedBy: Long?): FileDownloadResult {
        
        // TODO: Get file metadata
        // TODO: Check access permissions
        // TODO: Check if file has expired
        // TODO: Load file resource
        // TODO: Update download statistics
        
        return FileDownloadResult(
            success = false,
            error = "TODO: Implement file download"
        )
    }
    
    private fun hasAccessPermission(metadata: FileMetadata, requestedBy: Long?): Boolean {
        // TODO: Implement access control logic
        // TODO: Check if file is public
        // TODO: Check if user is owner
        // TODO: Check if user is admin
        
        return false
    }
}

data class FileUploadResult(
    val success: Boolean,
    val fileId: Long? = null,
    val filename: String? = null,
    val originalName: String? = null,
    val url: String? = null,
    val size: Long? = null,
    val errors: List<String> = emptyList()
)

data class FileDownloadResult(
    val success: Boolean,
    val resource: org.springframework.core.io.Resource? = null,
    val metadata: FileMetadata? = null,
    val error: String? = null
)
```

---

## 🛠️ Step 8: Test Your Implementation

### **📝 TODO: Create comprehensive tests**
```kotlin
package com.learning.files

import com.learning.files.model.FileCategory
import com.learning.files.service.FileValidationService
import com.learning.files.service.LocalFileStorageService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile

@SpringBootTest
class FilesApplicationTests {
    
    @Test
    fun contextLoads() {
        // Basic Spring Boot test
    }
    
    // TODO: Test file validation
    @Test
    fun `should validate file correctly`() {
        val validationService = FileValidationService()
        
        // Create mock file
        val mockFile = MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test content".toByteArray()
        )
        
        // TODO: Test validation for different categories
        // TODO: Test with valid and invalid files
        // TODO: Verify error messages
    }
    
    // TODO: Test file storage
    @Test
    fun `should store and retrieve file`() {
        // TODO: Test local file storage
        // TODO: Store a mock file
        // TODO: Verify file exists
        // TODO: Load file and verify content
        // TODO: Clean up test file
    }
    
    // TODO: Test file upload end-to-end
    @Test
    fun `should upload file through controller`() {
        // TODO: Create mock HTTP request with file
        // TODO: Call upload endpoint
        // TODO: Verify response
        // TODO: Check file was stored
        // TODO: Check metadata was saved
    }
}
```

---

## 🚀 Step 9: Test Your File Management System

### **1. Run the Application**
```bash
cd class/workshop/lesson_15
./gradlew bootRun
```

### **2. Test File Upload**
```bash
# Upload an image file
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@test-image.jpg" \
  -F "category=IMAGE" \
  -F "title=Test Image" \
  -F "tags=test,demo" \
  -F "isPublic=true"

# Upload a document
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@document.pdf" \
  -F "category=DOCUMENT" \
  -F "title=Important Document" \
  -F "description=This is a test document"
```

### **3. Test File Download**
```bash
# Download file by ID
curl -O http://localhost:8080/api/files/download/1

# View file inline (for images/PDFs)
curl http://localhost:8080/api/files/view/1

# Test streaming (for large files)
curl -H "Range: bytes=0-1023" http://localhost:8080/api/files/stream/1
```

### **4. Test File Management**
```bash
# Search files
curl "http://localhost:8080/api/files/search?query=test&tags=demo"

# Get file statistics
curl http://localhost:8080/api/files/statistics

# List user's files
curl http://localhost:8080/api/files/my-files
```

---

## 🎯 Expected Results

### **Successful Upload Response**
```json
{
  "success": true,
  "fileId": 1,
  "filename": "abc123-uuid_timestamp.jpg",
  "originalName": "test-image.jpg",
  "url": "/api/files/download/abc123-uuid_timestamp.jpg",
  "size": 2048576
}
```

### **File Statistics**
```json
{
  "totalFiles": 15,
  "totalSize": 52428800,
  "publicFiles": 8,
  "privateFiles": 7,
  "recentUploads": 3,
  "averageFileSize": 3495253
}
```

### **Directory Structure After Upload**
```
uploads/
├── image/
│   ├── abc123-uuid_12345.jpg
│   └── def456-uuid_67890.png
├── document/
│   ├── ghi789-uuid_11111.pdf
│   └── jkl012-uuid_22222.txt
└── video/
    └── mno345-uuid_33333.mp4
```

---

## 🏆 Challenge Extensions

### **🔥 Bonus Challenge 1: Cloud Storage Integration**
Implement AWS S3 storage service with proper authentication and bucket management.

### **🔥 Bonus Challenge 2: Image Processing**
Add image resizing, thumbnail generation, and format conversion capabilities.

### **🔥 Bonus Challenge 3: File Versioning**
Implement file versioning system with history tracking and rollback capabilities.

### **🔥 Bonus Challenge 4: Virus Scanning**
Integrate with ClamAV or similar service for real-time virus scanning.

### **🔥 Bonus Challenge 5: CDN Integration**
Add CloudFront or similar CDN integration for global file distribution.

---

## 🎓 Learning Outcomes

Upon completion, you'll have:

✅ **Implemented secure file uploads** with comprehensive validation and error handling  
✅ **Created multiple storage strategies** supporting both local and cloud storage  
✅ **Built efficient file downloads** with streaming support and range requests  
✅ **Designed file metadata system** with search, tagging, and statistics  
✅ **Implemented access controls** with user-based permissions and security  
✅ **Added automatic cleanup** with scheduled tasks for file lifecycle management

**🚀 Next Lesson**: Logging & Observability - monitoring and debugging production applications!