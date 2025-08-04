# 🚀 Lesson 15: File Handling & Uploads - Concept Guide

## 🎯 Learning Objectives

By the end of this lesson, you will:
- **Handle file uploads** using Spring's MultipartFile with proper validation
- **Implement multiple storage strategies** including local filesystem and cloud storage
- **Validate file content** with type checking, size limits, and security scanning
- **Stream file downloads** efficiently for large files and better performance
- **Manage file metadata** with database integration and search capabilities
- **Secure file operations** with access control and virus scanning

---

## 🔍 Why File Handling Matters

### **Real-World Scenarios**
```kotlin
// E-commerce: Product images
POST /api/products/123/images
Content-Type: multipart/form-data
- File: product-image.jpg (2MB)
- Alt text: "Red running shoes"
- Category: "main"

// Social Media: Profile pictures
PUT /api/users/profile/avatar
Content-Type: multipart/form-data
- File: avatar.png (500KB)
- Crop coordinates: x=10, y=20, width=200, height=200

// Document Management: Report uploads
POST /api/documents
Content-Type: multipart/form-data
- File: quarterly-report.pdf (5MB)
- Tags: ["finance", "q4", "2024"]
- Access level: "restricted"
```

### **Business Benefits**
- **Enhanced User Experience**: Rich media content and document sharing
- **Reduced Server Load**: Efficient streaming and storage strategies
- **Security Compliance**: File validation and virus scanning
- **Scalable Storage**: Cloud integration for unlimited capacity

---

## 📁 MultipartFile Fundamentals

### **Basic File Upload Handling**
```kotlin
@RestController
@RequestMapping("/api/files")
class FileUploadController {
    
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("description", required = false) description: String?
    ): ResponseEntity<FileUploadResponse> {
        
        // Basic validation
        if (file.isEmpty) {
            return ResponseEntity.badRequest()
                .body(FileUploadResponse(success = false, message = "File is empty"))
        }
        
        // File information
        val originalName = file.originalFilename ?: "unknown"
        val contentType = file.contentType ?: "application/octet-stream"
        val size = file.size
        
        logger.info("Uploading file: $originalName, type: $contentType, size: $size bytes")
        
        return try {
            val savedFile = fileService.saveFile(file, description)
            ResponseEntity.ok(FileUploadResponse(
                success = true,
                fileId = savedFile.id,
                filename = savedFile.filename,
                url = "/api/files/${savedFile.id}/download"
            ))
        } catch (e: Exception) {
            logger.error("File upload failed", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FileUploadResponse(success = false, message = "Upload failed: ${e.message}"))
        }
    }
}

data class FileUploadResponse(
    val success: Boolean,
    val fileId: Long? = null,
    val filename: String? = null,
    val url: String? = null,
    val message: String? = null
)
```

### **Multiple File Upload**
```kotlin
@PostMapping("/upload-multiple")
fun uploadMultipleFiles(
    @RequestParam("files") files: Array<MultipartFile>,
    @RequestParam("category") category: String
): ResponseEntity<MultipleFileUploadResponse> {
    
    val results = mutableListOf<FileUploadResult>()
    var successCount = 0
    
    files.forEach { file ->
        try {
            if (!file.isEmpty) {
                val savedFile = fileService.saveFile(file, category)
                results.add(FileUploadResult(
                    filename = file.originalFilename ?: "unknown",
                    success = true,
                    fileId = savedFile.id,
                    url = "/api/files/${savedFile.id}/download"
                ))
                successCount++
            } else {
                results.add(FileUploadResult(
                    filename = file.originalFilename ?: "unknown",
                    success = false,
                    error = "File is empty"
                ))
            }
        } catch (e: Exception) {
            results.add(FileUploadResult(
                filename = file.originalFilename ?: "unknown",
                success = false,
                error = e.message
            ))
        }
    }
    
    return ResponseEntity.ok(MultipleFileUploadResponse(
        totalFiles = files.size,
        successfulUploads = successCount,
        results = results
    ))
}
```

### **File Upload with Additional Data**
```kotlin
@PostMapping("/upload-with-metadata")
fun uploadWithMetadata(
    @RequestParam("file") file: MultipartFile,
    @RequestParam("title") title: String,
    @RequestParam("tags") tags: List<String>,
    @RequestParam("isPublic") isPublic: Boolean,
    @RequestParam("expiresAt", required = false) 
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) expiresAt: LocalDateTime?
): ResponseEntity<FileUploadResponse> {
    
    val metadata = FileMetadata(
        title = title,
        tags = tags.toSet(),
        isPublic = isPublic,
        expiresAt = expiresAt
    )
    
    val savedFile = fileService.saveFileWithMetadata(file, metadata)
    
    return ResponseEntity.ok(FileUploadResponse(
        success = true,
        fileId = savedFile.id,
        filename = savedFile.filename,
        url = "/api/files/${savedFile.id}/download"
    ))
}
```

---

## ✅ File Validation Strategies

### **Comprehensive File Validation Service**
```kotlin
@Service
class FileValidationService {
    
    companion object {
        // File size limits (in bytes)
        const val MAX_IMAGE_SIZE = 10 * 1024 * 1024      // 10MB
        const val MAX_DOCUMENT_SIZE = 50 * 1024 * 1024   // 50MB
        const val MAX_VIDEO_SIZE = 500 * 1024 * 1024     // 500MB
        
        // Allowed MIME types
        val ALLOWED_IMAGE_TYPES = setOf(
            "image/jpeg", "image/png", "image/gif", "image/webp"
        )
        
        val ALLOWED_DOCUMENT_TYPES = setOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        )
        
        val DANGEROUS_EXTENSIONS = setOf(
            "exe", "bat", "cmd", "scr", "pif", "vbs", "js", "jar", "com"
        )
    }
    
    private val logger = LoggerFactory.getLogger(FileValidationService::class.java)
    
    fun validateFile(file: MultipartFile, category: FileCategory): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Check if file is empty
        if (file.isEmpty) {
            errors.add("File is empty")
            return ValidationResult(false, errors)
        }
        
        // Validate filename
        val filename = file.originalFilename ?: ""
        if (filename.isBlank()) {
            errors.add("Filename is required")
        }
        
        // Check for dangerous file extensions
        val extension = getFileExtension(filename).lowercase()
        if (extension in DANGEROUS_EXTENSIONS) {
            errors.add("File type '$extension' is not allowed for security reasons")
        }
        
        // Validate file size based on category
        val maxSize = when (category) {
            FileCategory.IMAGE -> MAX_IMAGE_SIZE
            FileCategory.DOCUMENT -> MAX_DOCUMENT_SIZE
            FileCategory.VIDEO -> MAX_VIDEO_SIZE
            FileCategory.OTHER -> MAX_DOCUMENT_SIZE
        }
        
        if (file.size > maxSize) {
            errors.add("File size (${formatFileSize(file.size)}) exceeds maximum allowed size (${formatFileSize(maxSize)})")
        }
        
        // Validate MIME type
        val contentType = file.contentType ?: ""
        val allowedTypes = when (category) {
            FileCategory.IMAGE -> ALLOWED_IMAGE_TYPES
            FileCategory.DOCUMENT -> ALLOWED_DOCUMENT_TYPES
            FileCategory.VIDEO -> setOf("video/mp4", "video/avi", "video/mov")
            FileCategory.OTHER -> ALLOWED_DOCUMENT_TYPES + ALLOWED_IMAGE_TYPES
        }
        
        if (contentType !in allowedTypes) {
            errors.add("File type '$contentType' is not allowed for category '$category'")
        }
        
        // Advanced validation: Check file signature (magic numbers)
        try {
            if (!validateFileSignature(file, contentType)) {
                errors.add("File signature does not match declared MIME type")
            }
        } catch (e: Exception) {
            logger.warn("Could not validate file signature", e)
            // Don't fail upload for signature validation errors
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    private fun validateFileSignature(file: MultipartFile, declaredType: String): Boolean {
        val bytes = file.inputStream.use { it.readNBytes(8) }
        
        return when (declaredType) {
            "image/jpeg" -> bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
            "image/png" -> bytes.size >= 8 && 
                bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && 
                bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
            "application/pdf" -> bytes.size >= 4 && 
                bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x44.toByte() && bytes[3] == 0x46.toByte()
            else -> true // Skip validation for unknown types
        }
    }
    
    private fun getFileExtension(filename: String): String {
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot > 0 && lastDot < filename.length - 1) {
            filename.substring(lastDot + 1)
        } else ""
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
            bytes >= 1024 -> "${bytes / 1024}KB"
            else -> "${bytes}B"
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

enum class FileCategory {
    IMAGE, DOCUMENT, VIDEO, OTHER
}
```

### **Custom Validation Annotations**
```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidFileValidator::class])
annotation class ValidFile(
    val message: String = "Invalid file",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val maxSize: Long = 10 * 1024 * 1024, // 10MB default
    val allowedTypes: Array<String> = ["image/jpeg", "image/png"]
)

class ValidFileValidator : ConstraintValidator<ValidFile, MultipartFile> {
    
    private var maxSize: Long = 0
    private var allowedTypes: Set<String> = emptySet()
    
    override fun initialize(annotation: ValidFile) {
        maxSize = annotation.maxSize
        allowedTypes = annotation.allowedTypes.toSet()
    }
    
    override fun isValid(file: MultipartFile?, context: ConstraintValidatorContext): Boolean {
        if (file == null || file.isEmpty) return false
        
        val errors = mutableListOf<String>()
        
        // Check file size
        if (file.size > maxSize) {
            errors.add("File size exceeds maximum allowed size")
        }
        
        // Check content type
        val contentType = file.contentType ?: ""
        if (contentType !in allowedTypes) {
            errors.add("File type '$contentType' is not allowed")
        }
        
        if (errors.isNotEmpty()) {
            context.disableDefaultConstraintViolation()
            errors.forEach { error ->
                context.buildConstraintViolationWithTemplate(error).addConstraintViolation()
            }
            return false
        }
        
        return true
    }
}

// Usage in controller
@PostMapping("/upload-validated")
fun uploadValidatedFile(
    @ValidFile(
        maxSize = 5 * 1024 * 1024, // 5MB
        allowedTypes = ["image/jpeg", "image/png"]
    )
    @RequestParam("file") file: MultipartFile
): ResponseEntity<*> {
    // File is automatically validated
    val savedFile = fileService.saveFile(file)
    return ResponseEntity.ok(savedFile)
}
```

---

## 💾 Storage Strategy Implementation

### **Storage Interface & Implementations**
```kotlin
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

// Local filesystem implementation
@Service
@Profile("!cloud")
class LocalFileStorageService : FileStorageService {
    
    @Value("\${app.file.upload-dir:./uploads}")
    private lateinit var uploadDir: String
    
    private lateinit var rootLocation: Path
    
    @PostConstruct
    fun init() {
        rootLocation = Paths.get(uploadDir)
        
        try {
            Files.createDirectories(rootLocation)
        } catch (e: IOException) {
            throw RuntimeException("Could not initialize storage location", e)
        }
    }
    
    override fun store(file: MultipartFile, directory: String): StoredFile {
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("Filename cannot be null")
        
        // Generate unique filename to avoid conflicts
        val extension = getFileExtension(originalFilename)
        val uniqueFilename = "${UUID.randomUUID()}_${System.currentTimeMillis()}.$extension"
        
        val targetDir = if (directory.isNotBlank()) {
            rootLocation.resolve(directory).also { Files.createDirectories(it) }
        } else {
            rootLocation
        }
        
        val targetFile = targetDir.resolve(uniqueFilename)
        
        try {
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING)
            }
            
            logger.info("Stored file: $uniqueFilename in directory: $directory")
            
            return StoredFile(
                filename = uniqueFilename,
                originalName = originalFilename,
                size = file.size,
                contentType = file.contentType ?: "application/octet-stream",
                directory = directory,
                url = "/api/files/download/$uniqueFilename" + if (directory.isNotBlank()) "?dir=$directory" else ""
            )
            
        } catch (e: IOException) {
            throw RuntimeException("Failed to store file $uniqueFilename", e)
        }
    }
    
    override fun load(filename: String, directory: String): Resource {
        val targetDir = if (directory.isNotBlank()) {
            rootLocation.resolve(directory)
        } else {
            rootLocation
        }
        
        val file = targetDir.resolve(filename)
        val resource = UrlResource(file.toUri())
        
        return if (resource.exists() && resource.isReadable) {
            resource
        } else {
            throw FileNotFoundException("Could not read file: $filename")
        }
    }
    
    override fun delete(filename: String, directory: String): Boolean {
        return try {
            val targetDir = if (directory.isNotBlank()) {
                rootLocation.resolve(directory)
            } else {
                rootLocation
            }
            
            val file = targetDir.resolve(filename)
            Files.deleteIfExists(file)
        } catch (e: IOException) {
            logger.error("Failed to delete file: $filename", e)
            false
        }
    }
    
    override fun exists(filename: String, directory: String): Boolean {
        val targetDir = if (directory.isNotBlank()) {
            rootLocation.resolve(directory)
        } else {
            rootLocation
        }
        
        return Files.exists(targetDir.resolve(filename))
    }
    
    override fun getUrl(filename: String, directory: String): String {
        return "/api/files/download/$filename" + if (directory.isNotBlank()) "?dir=$directory" else ""
    }
}

// Cloud storage implementation (AWS S3 example)
@Service
@Profile("cloud")
class S3FileStorageService : FileStorageService {
    
    @Value("\${aws.s3.bucket-name}")
    private lateinit var bucketName: String
    
    @Value("\${aws.s3.region:us-east-1}")
    private lateinit var region: String
    
    private lateinit var s3Client: S3Client
    
    @PostConstruct
    fun initializeS3Client() {
        s3Client = S3Client.builder()
            .region(Region.of(region))
            .build()
    }
    
    override fun store(file: MultipartFile, directory: String): StoredFile {
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("Filename cannot be null")
        val extension = getFileExtension(originalFilename)
        val uniqueFilename = "${UUID.randomUUID()}_${System.currentTimeMillis()}.$extension"
        
        val key = if (directory.isNotBlank()) "$directory/$uniqueFilename" else uniqueFilename
        
        try {
            val putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.contentType)
                .contentLength(file.size)
                .build()
            
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.inputStream, file.size))
            
            logger.info("Stored file in S3: $key")
            
            return StoredFile(
                filename = uniqueFilename,
                originalName = originalFilename,
                size = file.size,
                contentType = file.contentType ?: "application/octet-stream",
                directory = directory,
                url = "https://$bucketName.s3.$region.amazonaws.com/$key"
            )
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to store file in S3: $uniqueFilename", e)
        }
    }
    
    override fun load(filename: String, directory: String): Resource {
        val key = if (directory.isNotBlank()) "$directory/$filename" else filename
        
        try {
            val getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
            
            val response = s3Client.getObject(getRequest)
            return InputStreamResource(response)
            
        } catch (e: Exception) {
            throw FileNotFoundException("Could not read file from S3: $filename")
        }
    }
    
    override fun delete(filename: String, directory: String): Boolean {
        return try {
            val key = if (directory.isNotBlank()) "$directory/$filename" else filename
            
            val deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
            
            s3Client.deleteObject(deleteRequest)
            true
            
        } catch (e: Exception) {
            logger.error("Failed to delete file from S3: $filename", e)
            false
        }
    }
    
    override fun exists(filename: String, directory: String): Boolean {
        return try {
            val key = if (directory.isNotBlank()) "$directory/$filename" else filename
            
            val headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
            
            s3Client.headObject(headRequest)
            true
            
        } catch (e: NoSuchKeyException) {
            false
        } catch (e: Exception) {
            logger.error("Error checking file existence in S3: $filename", e)
            false
        }
    }
    
    override fun getUrl(filename: String, directory: String): String {
        val key = if (directory.isNotBlank()) "$directory/$filename" else filename
        return "https://$bucketName.s3.$region.amazonaws.com/$key"
    }
}
```

---

## ⬇️ File Download & Streaming

### **Efficient File Download Controller**
```kotlin
@RestController
@RequestMapping("/api/files")
class FileDownloadController {
    
    @Autowired
    private lateinit var fileStorageService: FileStorageService
    
    @Autowired
    private lateinit var fileMetadataService: FileMetadataService
    
    // Basic file download
    @GetMapping("/download/{filename}")
    fun downloadFile(
        @PathVariable filename: String,
        @RequestParam(required = false) directory: String?,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        
        return try {
            val resource = fileStorageService.load(filename, directory ?: "")
            val metadata = fileMetadataService.getByFilename(filename)
            
            // Determine content type
            val contentType = metadata?.contentType 
                ?: request.servletContext.getMimeType(resource.file.absolutePath)
                ?: "application/octet-stream"
            
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${metadata?.originalName ?: filename}\"")
                .body(resource)
                
        } catch (e: FileNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Error downloading file: $filename", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    // Inline file serving (for images, PDFs)
    @GetMapping("/view/{filename}")
    fun viewFile(
        @PathVariable filename: String,
        @RequestParam(required = false) directory: String?,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        
        return try {
            val resource = fileStorageService.load(filename, directory ?: "")
            val metadata = fileMetadataService.getByFilename(filename)
            
            val contentType = metadata?.contentType 
                ?: request.servletContext.getMimeType(resource.file.absolutePath)
                ?: "application/octet-stream"
            
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${metadata?.originalName ?: filename}\"")
                .body(resource)
                
        } catch (e: FileNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
    
    // Streaming download for large files
    @GetMapping("/stream/{filename}")
    fun streamFile(
        @PathVariable filename: String,
        @RequestParam(required = false) directory: String?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        try {
            val resource = fileStorageService.load(filename, directory ?: "")
            val metadata = fileMetadataService.getByFilename(filename)
            
            val contentType = metadata?.contentType ?: "application/octet-stream"
            val fileSize = resource.contentLength()
            
            response.contentType = contentType
            response.setHeader("Content-Length", fileSize.toString())
            response.setHeader("Content-Disposition", "attachment; filename=\"${metadata?.originalName ?: filename}\"")
            response.setHeader("Accept-Ranges", "bytes")
            
            // Handle range requests for video streaming
            val rangeHeader = request.getHeader("Range")
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                handleRangeRequest(resource, rangeHeader, response)
            } else {
                // Stream entire file
                resource.inputStream.use { inputStream ->
                    response.outputStream.use { outputStream ->
                        inputStream.copyTo(outputStream, 8192)
                    }
                }
            }
            
        } catch (e: FileNotFoundException) {
            response.status = HttpStatus.NOT_FOUND.value()
        } catch (e: Exception) {
            logger.error("Error streaming file: $filename", e)
            response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        }
    }
    
    private fun handleRangeRequest(
        resource: Resource,
        rangeHeader: String,
        response: HttpServletResponse
    ) {
        val fileSize = resource.contentLength()
        val ranges = parseRangeHeader(rangeHeader, fileSize)
        
        if (ranges.isEmpty()) {
            response.status = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value()
            response.setHeader("Content-Range", "bytes */$fileSize")
            return
        }
        
        val range = ranges.first()
        val start = range.first
        val end = range.second
        val contentLength = end - start + 1
        
        response.status = HttpStatus.PARTIAL_CONTENT.value()
        response.setHeader("Content-Range", "bytes $start-$end/$fileSize")
        response.setHeader("Content-Length", contentLength.toString())
        
        resource.inputStream.use { inputStream ->
            inputStream.skip(start)
            response.outputStream.use { outputStream ->
                val buffer = ByteArray(8192)
                var remaining = contentLength
                
                while (remaining > 0) {
                    val bytesToRead = minOf(buffer.size.toLong(), remaining).toInt()
                    val bytesRead = inputStream.read(buffer, 0, bytesToRead)
                    
                    if (bytesRead == -1) break
                    
                    outputStream.write(buffer, 0, bytesRead)
                    remaining -= bytesRead
                }
            }
        }
    }
    
    private fun parseRangeHeader(rangeHeader: String, fileSize: Long): List<Pair<Long, Long>> {
        val ranges = mutableListOf<Pair<Long, Long>>()
        
        try {
            val rangeSpec = rangeHeader.substring(6) // Remove "bytes="
            val rangeParts = rangeSpec.split(",")
            
            rangeParts.forEach { part ->
                val trimmed = part.trim()
                when {
                    trimmed.startsWith("-") -> {
                        // Suffix range: -500 (last 500 bytes)
                        val suffixLength = trimmed.substring(1).toLong()
                        val start = maxOf(0, fileSize - suffixLength)
                        ranges.add(Pair(start, fileSize - 1))
                    }
                    trimmed.endsWith("-") -> {
                        // Prefix range: 500- (from byte 500 to end)
                        val start = trimmed.substring(0, trimmed.length - 1).toLong()
                        if (start < fileSize) {
                            ranges.add(Pair(start, fileSize - 1))
                        }
                    }
                    else -> {
                        // Full range: 500-999
                        val dashIndex = trimmed.indexOf('-')
                        if (dashIndex > 0) {
                            val start = trimmed.substring(0, dashIndex).toLong()
                            val end = trimmed.substring(dashIndex + 1).toLong()
                            if (start <= end && start < fileSize) {
                                ranges.add(Pair(start, minOf(end, fileSize - 1)))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn("Invalid range header: $rangeHeader", e)
        }
        
        return ranges
    }
}
```

---

## 🗄️ File Metadata Management

### **File Metadata Entity & Service**
```kotlin
@Entity
@Table(name = "file_metadata")
data class FileMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val filename: String,
    
    @Column(nullable = false)
    val originalName: String,
    
    @Column(nullable = false)
    val contentType: String,
    
    @Column(nullable = false)
    val size: Long,
    
    @Column
    val directory: String? = null,
    
    @Column
    val title: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @ElementCollection
    @CollectionTable(name = "file_tags", joinColumns = [JoinColumn(name = "file_id")])
    @Column(name = "tag")
    val tags: Set<String> = emptySet(),
    
    @Column
    val uploadedBy: Long? = null,
    
    @Column(nullable = false)
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val expiresAt: LocalDateTime? = null,
    
    @Column(nullable = false)
    val isPublic: Boolean = false,
    
    @Column
    val downloadCount: Long = 0,
    
    @Column
    val lastAccessedAt: LocalDateTime? = null
)

@Service
class FileMetadataService(
    private val fileMetadataRepository: FileMetadataRepository
) {
    
    private val logger = LoggerFactory.getLogger(FileMetadataService::class.java)
    
    fun saveMetadata(
        storedFile: StoredFile,
        metadata: FileUploadMetadata? = null,
        uploadedBy: Long? = null
    ): FileMetadata {
        val fileMetadata = FileMetadata(
            filename = storedFile.filename,
            originalName = storedFile.originalName,
            contentType = storedFile.contentType,
            size = storedFile.size,
            directory = storedFile.directory.takeIf { it.isNotBlank() },
            title = metadata?.title,
            description = metadata?.description,
            tags = metadata?.tags ?: emptySet(),
            uploadedBy = uploadedBy,
            isPublic = metadata?.isPublic ?: false,
            expiresAt = metadata?.expiresAt
        )
        
        return fileMetadataRepository.save(fileMetadata)
    }
    
    fun getByFilename(filename: String): FileMetadata? {
        return fileMetadataRepository.findByFilename(filename)
    }
    
    fun getById(id: Long): FileMetadata? {
        return fileMetadataRepository.findById(id).orElse(null)
    }
    
    fun searchFiles(
        query: String? = null,
        tags: Set<String> = emptySet(),
        contentType: String? = null,
        uploadedBy: Long? = null,
        isPublic: Boolean? = null,
        pageable: Pageable = PageRequest.of(0, 20)
    ): Page<FileMetadata> {
        return fileMetadataRepository.searchFiles(
            query, tags, contentType, uploadedBy, isPublic, pageable
        )
    }
    
    fun incrementDownloadCount(fileId: Long) {
        fileMetadataRepository.findById(fileId).ifPresent { file ->
            val updated = file.copy(
                downloadCount = file.downloadCount + 1,
                lastAccessedAt = LocalDateTime.now()
            )
            fileMetadataRepository.save(updated)
        }
    }
    
    fun deleteExpiredFiles(): Int {
        val expiredFiles = fileMetadataRepository.findExpiredFiles(LocalDateTime.now())
        var deletedCount = 0
        
        expiredFiles.forEach { file ->
            try {
                // Delete physical file
                fileStorageService.delete(file.filename, file.directory ?: "")
                
                // Delete metadata
                fileMetadataRepository.delete(file)
                deletedCount++
                
                logger.info("Deleted expired file: ${file.filename}")
                
            } catch (e: Exception) {
                logger.error("Failed to delete expired file: ${file.filename}", e)
            }
        }
        
        return deletedCount
    }
    
    fun getFileStatistics(): FileStatistics {
        val totalFiles = fileMetadataRepository.count()
        val totalSize = fileMetadataRepository.sumFileSize() ?: 0L
        val publicFiles = fileMetadataRepository.countByIsPublic(true)
        val recentFiles = fileMetadataRepository.countRecentFiles(LocalDateTime.now().minusDays(7))
        
        return FileStatistics(
            totalFiles = totalFiles,
            totalSize = totalSize,
            publicFiles = publicFiles,
            privateFiles = totalFiles - publicFiles,
            recentUploads = recentFiles,
            averageFileSize = if (totalFiles > 0) totalSize / totalFiles else 0L
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
    val privateFiles: Long,
    val recentUploads: Long,
    val averageFileSize: Long
)
```

---

## 🔒 File Security & Access Control

### **Security-First File Service**
```kotlin
@Service
class SecureFileService(
    private val fileStorageService: FileStorageService,
    private val fileMetadataService: FileMetadataService,
    private val fileValidationService: FileValidationService,
    private val virusScannerService: VirusScannerService
) {
    
    private val logger = LoggerFactory.getLogger(SecureFileService::class.java)
    
    @Transactional
    fun uploadFile(
        file: MultipartFile,
        category: FileCategory,
        metadata: FileUploadMetadata? = null,
        uploadedBy: Long? = null
    ): FileUploadResult {
        
        // Step 1: Validate file
        val validationResult = fileValidationService.validateFile(file, category)
        if (!validationResult.isValid) {
            return FileUploadResult(
                success = false,
                errors = validationResult.errors
            )
        }
        
        // Step 2: Virus scanning
        val scanResult = virusScannerService.scanFile(file)
        if (!scanResult.isClean) {
            logger.warn("Virus detected in uploaded file: ${file.originalFilename}")
            return FileUploadResult(
                success = false,
                errors = listOf("File contains malicious content")
            )
        }
        
        // Step 3: Store file
        val storedFile = try {
            fileStorageService.store(file, category.name.lowercase())
        } catch (e: Exception) {
            logger.error("Failed to store file", e)
            return FileUploadResult(
                success = false,
                errors = listOf("Storage failed: ${e.message}")
            )
        }
        
        // Step 4: Save metadata
        val savedMetadata = fileMetadataService.saveMetadata(storedFile, metadata, uploadedBy)
        
        logger.info("File uploaded successfully: ${storedFile.filename} by user $uploadedBy")
        
        return FileUploadResult(
            success = true,
            fileId = savedMetadata.id!!,
            filename = storedFile.filename,
            originalName = storedFile.originalName,
            url = storedFile.url,
            size = storedFile.size
        )
    }
    
    fun downloadFile(
        fileId: Long,
        requestedBy: Long?
    ): FileDownloadResult {
        
        val metadata = fileMetadataService.getById(fileId)
            ?: return FileDownloadResult(success = false, error = "File not found")
        
        // Check access permissions
        if (!hasAccessPermission(metadata, requestedBy)) {
            return FileDownloadResult(success = false, error = "Access denied")
        }
        
        // Check if file has expired
        if (metadata.expiresAt != null && metadata.expiresAt.isBefore(LocalDateTime.now())) {
            return FileDownloadResult(success = false, error = "File has expired")
        }
        
        return try {
            val resource = fileStorageService.load(metadata.filename, metadata.directory ?: "")
            
            // Update download statistics
            fileMetadataService.incrementDownloadCount(fileId)
            
            FileDownloadResult(
                success = true,
                resource = resource,
                metadata = metadata
            )
            
        } catch (e: FileNotFoundException) {
            logger.error("File not found in storage: ${metadata.filename}")
            FileDownloadResult(success = false, error = "File not found in storage")
        } catch (e: Exception) {
            logger.error("Error downloading file: ${metadata.filename}", e)
            FileDownloadResult(success = false, error = "Download failed")
        }
    }
    
    private fun hasAccessPermission(metadata: FileMetadata, requestedBy: Long?): Boolean {
        return when {
            // Public files are accessible by anyone
            metadata.isPublic -> true
            
            // Owner can always access their files
            metadata.uploadedBy == requestedBy -> true
            
            // Admin users can access any file (implement admin check)
            requestedBy != null && isAdminUser(requestedBy) -> true
            
            // Deny access by default
            else -> false
        }
    }
    
    private fun isAdminUser(userId: Long): Boolean {
        // Implement admin user check
        // This could query a user service or check roles
        return false // Placeholder
    }
    
    @Scheduled(fixedDelay = 3600000) // Run every hour
    fun cleanupExpiredFiles() {
        try {
            val deletedCount = fileMetadataService.deleteExpiredFiles()
            if (deletedCount > 0) {
                logger.info("Cleaned up $deletedCount expired files")
            }
        } catch (e: Exception) {
            logger.error("Error during file cleanup", e)
        }
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
    val resource: Resource? = null,
    val metadata: FileMetadata? = null,
    val error: String? = null
)

// Simple virus scanner interface
interface VirusScannerService {
    fun scanFile(file: MultipartFile): ScanResult
}

data class ScanResult(
    val isClean: Boolean,
    val threatName: String? = null,
    val scanDuration: Long = 0
)

@Service
class MockVirusScannerService : VirusScannerService {
    
    override fun scanFile(file: MultipartFile): ScanResult {
        // Mock implementation - always returns clean
        // In production, integrate with ClamAV, VirusTotal, etc.
        
        val startTime = System.currentTimeMillis()
        
        // Simulate scanning time
        Thread.sleep(100 + Random.nextInt(200))
        
        val scanDuration = System.currentTimeMillis() - startTime
        
        return ScanResult(
            isClean = true,
            scanDuration = scanDuration
        )
    }
}
```

---

## 🎯 Best Practices

### **Do's**
- ✅ **Validate all uploaded files** for type, size, and content
- ✅ **Use unique filenames** to prevent conflicts and security issues
- ✅ **Implement access controls** to protect sensitive files
- ✅ **Scan for malware** before storing files
- ✅ **Set file size limits** appropriate for your use case
- ✅ **Use streaming** for large file downloads
- ✅ **Track file metadata** for search and management

### **Don'ts**
- ❌ **Don't trust file extensions** - validate MIME types and file signatures
- ❌ **Don't store files in web-accessible directories** without security
- ❌ **Don't ignore file size limits** - prevent DOS attacks
- ❌ **Don't serve files without access control** checks
- ❌ **Don't keep expired files** - implement cleanup routines
- ❌ **Don't expose internal file paths** in URLs
- ❌ **Don't skip virus scanning** for untrusted uploads

### **Security Considerations**
- 🔒 **File Upload Vulnerabilities**: Validate file types and scan for malware
- 🔒 **Directory Traversal**: Use secure path handling and validation
- 🔒 **Access Control**: Implement proper authentication and authorization
- 🔒 **Storage Security**: Encrypt sensitive files and use secure storage
- 🔒 **Rate Limiting**: Prevent abuse with upload rate limits

---

## 🚀 Configuration Best Practices

### **Application Configuration**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
      enabled: true
      file-size-threshold: 2KB
      location: ${java.io.tmpdir}

app:
  file:
    upload-dir: ./uploads
    max-size:
      image: 10485760    # 10MB
      document: 52428800 # 50MB
      video: 524288000   # 500MB
    allowed-types:
      image: [image/jpeg, image/png, image/gif, image/webp]
      document: [application/pdf, text/plain, application/msword]
    virus-scan:
      enabled: true
      timeout: 30000
    cleanup:
      enabled: true
      expired-files-interval: 3600000  # 1 hour
      temp-files-retention: 86400000   # 24 hours

# Cloud storage configuration
aws:
  s3:
    bucket-name: ${AWS_S3_BUCKET:my-app-files}
    region: ${AWS_REGION:us-east-1}
    access-key: ${AWS_ACCESS_KEY:}
    secret-key: ${AWS_SECRET_KEY:}
```

---

## 🎓 Summary

File Handling & Uploads provides:

- **📁 Comprehensive Upload Handling**: MultipartFile processing with validation
- **💾 Flexible Storage**: Local filesystem and cloud storage implementations
- **🔒 Security**: File validation, virus scanning, and access control
- **⬇️ Efficient Downloads**: Streaming support with range requests
- **🗄️ Metadata Management**: File tracking, search, and statistics
- **♻️ Lifecycle Management**: Automatic cleanup and expiration handling

**Key Takeaways**:
1. **Security First**: Always validate and scan uploaded files
2. **Storage Strategy**: Choose appropriate storage based on requirements
3. **Performance**: Use streaming for large files and efficient downloads
4. **Metadata**: Track file information for management and search
5. **Access Control**: Implement proper permissions and authentication

Next lesson: **Logging & Observability** for production monitoring and debugging!