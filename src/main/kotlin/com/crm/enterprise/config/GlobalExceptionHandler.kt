package com.crm.enterprise.config

import com.crm.enterprise.dto.ErrorResponse
import com.crm.enterprise.service.DatabaseHandler
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*
import jakarta.validation.ConstraintViolationException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Validation error: ${ex.message}")
        
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage
            errors[fieldName] = errorMessage ?: "Invalid value"
        }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Request validation failed",
            path = request.getDescription(false).replace("uri=", ""),
            details = errors
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Constraint violation: ${ex.message}")
        
        val errors = mutableMapOf<String, String>()
        ex.constraintViolations.forEach { violation ->
            val fieldName = violation.propertyPath.toString()
            val errorMessage = violation.message
            errors[fieldName] = errorMessage
        }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Constraint Violation",
            message = "Request validation failed",
            path = request.getDescription(false).replace("uri=", ""),
            details = errors
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Malformed JSON request: ${ex.message}")
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Malformed JSON",
            message = "Request body is not valid JSON",
            path = request.getDescription(false).replace("uri=", ""),
            details = mapOf("error" to (ex.message ?: "Invalid JSON format"))
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Type mismatch error: ${ex.message}")
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Type Mismatch",
            message = "Invalid parameter type for '${ex.name}'",
            path = request.getDescription(false).replace("uri=", ""),
            details = mapOf(
                "parameter" to (ex.name ?: "unknown"),
                "expectedType" to (ex.requiredType?.simpleName ?: "Unknown"),
                "actualValue" to (ex.value?.toString() ?: "null")
            )
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(
        ex: DataAccessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Database access error: ${ex.message}", ex)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Database Error",
            message = "An error occurred while accessing the database",
            path = request.getDescription(false).replace("uri=", ""),
            details = mapOf("error" to "Database operation failed")
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(SQLException::class)
    fun handleSQLException(
        ex: SQLException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("SQL error: ${ex.message}", ex)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Database Error",
            message = "A database error occurred",
            path = request.getDescription(false).replace("uri=", ""),
            details = mapOf(
                "error" to "SQL operation failed",
                "sqlState" to (ex.sqlState ?: "Unknown"),
                "errorCode" to ex.errorCode.toString()
            )
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal argument: ${ex.message}")
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Argument",
            message = ex.message ?: "Invalid argument provided",
            path = request.getDescription(false).replace("uri=", ""),
            details = mapOf("error" to (ex.message ?: "Invalid argument"))
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.message}")
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Resource Not Found",
            message = ex.message ?: "The requested resource was not found",
            path = request.getDescription(false).replace("uri=", ""),
            details = mapOf("error" to (ex.message ?: "Resource not found"))
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(
        ex: SecurityException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Security error: ${ex.message}")
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "Access Denied",
            message = "You don't have permission to access this resource",
            path = request.getDescription(false).replace("uri=", ""),
            details = mapOf("error" to (ex.message ?: "Access denied"))
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    @ExceptionHandler(DatabaseHandler.DatabaseException::class)
    fun handleDatabaseException(
        ex: DatabaseHandler.DatabaseException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Database error: ${ex.message}", ex)
        val errorResponse = ex.toErrorResponse()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: ${ex.message}", ex)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.getDescription(false).replace("uri=", ""),
            details = mapOf("error" to "An unexpected error occurred")
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
