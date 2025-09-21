package com.crm.enterprise.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class FileUploadService {
    
    @Value("\${app.upload.dir:uploads}")
    private lateinit var uploadDir: String
    
    private val logger = LoggerFactory.getLogger(FileUploadService::class.java)
    
    private val allowedImageTypes = setOf(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    )
    
    private val maxFileSize = 10 * 1024 * 1024 // 10MB
    
    /**
     * Upload a file and return the file path
     */
    fun uploadFile(file: MultipartFile, companyId: Long, formId: Long): String {
        validateFile(file)
        
        val fileName = generateFileName(file.originalFilename ?: "file")
        val companyDir = Paths.get(uploadDir, "company_$companyId", "forms", "form_$formId")
        
        try {
            Files.createDirectories(companyDir)
            
            val filePath = companyDir.resolve(fileName)
            file.transferTo(filePath.toFile())
            
            val relativePath = "company_$companyId/forms/form_$formId/$fileName"
            logger.info("File uploaded successfully: $relativePath")
            
            return relativePath
        } catch (e: IOException) {
            logger.error("Error uploading file: ${file.originalFilename}", e)
            throw RuntimeException("Failed to upload file: ${e.message}")
        }
    }
    
    /**
     * Upload multiple files
     */
    fun uploadFiles(files: List<MultipartFile>, companyId: Long, formId: Long): List<String> {
        return files.map { uploadFile(it, companyId, formId) }
    }
    
    /**
     * Upload file from base64 data
     */
    fun uploadFileFromBase64(
        base64Data: String, 
        fileName: String, 
        mimeType: String, 
        companyId: Long, 
        formId: Long
    ): String {
        validateMimeType(mimeType)
        
        val fileExtension = getFileExtension(fileName)
        val generatedFileName = generateFileName("$fileName.$fileExtension")
        val companyDir = Paths.get(uploadDir, "company_$companyId", "forms", "form_$formId")
        
        try {
            Files.createDirectories(companyDir)
            
            val filePath = companyDir.resolve(generatedFileName)
            val fileBytes = Base64.getDecoder().decode(base64Data.split(",")[1])
            
            Files.write(filePath, fileBytes)
            
            val relativePath = "company_$companyId/forms/form_$formId/$generatedFileName"
            logger.info("File uploaded from base64: $relativePath")
            
            return relativePath
        } catch (e: Exception) {
            logger.error("Error uploading file from base64: $fileName", e)
            throw RuntimeException("Failed to upload file: ${e.message}")
        }
    }
    
    /**
     * Get file content
     */
    fun getFileContent(filePath: String): ByteArray {
        val fullPath = Paths.get(uploadDir, filePath)
        
        if (!Files.exists(fullPath)) {
            throw RuntimeException("File not found: $filePath")
        }
        
        return Files.readAllBytes(fullPath)
    }
    
    /**
     * Delete file
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val fullPath = Paths.get(uploadDir, filePath)
            Files.deleteIfExists(fullPath)
        } catch (e: Exception) {
            logger.error("Error deleting file: $filePath", e)
            false
        }
    }
    
    /**
     * Get file info
     */
    fun getFileInfo(filePath: String): Map<String, Any> {
        val fullPath = Paths.get(uploadDir, filePath)
        
        if (!Files.exists(fullPath)) {
            throw RuntimeException("File not found: $filePath")
        }
        
        val file = fullPath.toFile()
        return mapOf(
            "fileName" to file.name,
            "fileSize" to file.length(),
            "lastModified" to file.lastModified(),
            "exists" to file.exists()
        )
    }
    
    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw RuntimeException("File is empty")
        }
        
        if (file.size > maxFileSize) {
            throw RuntimeException("File size exceeds maximum allowed size of ${maxFileSize / (1024 * 1024)}MB")
        }
        
        val contentType = file.contentType
        if (contentType == null || !allowedImageTypes.contains(contentType)) {
            throw RuntimeException("File type not allowed. Allowed types: ${allowedImageTypes.joinToString(", ")}")
        }
    }
    
    private fun validateMimeType(mimeType: String) {
        if (!allowedImageTypes.contains(mimeType)) {
            throw RuntimeException("MIME type not allowed. Allowed types: ${allowedImageTypes.joinToString(", ")}")
        }
    }
    
    private fun generateFileName(originalFileName: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        val extension = getFileExtension(originalFileName)
        return "${timestamp}_${uuid}.$extension"
    }
    
    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast(".", "bin")
    }
}
