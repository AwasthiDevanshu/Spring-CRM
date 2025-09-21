# Database Connection Pool and Global Exception Handling

This document describes the enhanced database connection management and global exception handling implemented in the Spring CRM application.

## Database Connection Pool (HikariCP)

### Configuration

The application uses HikariCP as the connection pool implementation with the following optimized settings:

#### Connection Pool Settings
- **Maximum Pool Size**: 20 connections
- **Minimum Idle**: 5 connections
- **Idle Timeout**: 5 minutes (300,000 ms)
- **Connection Timeout**: 20 seconds
- **Max Lifetime**: 20 minutes (1,200,000 ms)
- **Leak Detection Threshold**: 1 minute

#### MySQL Optimizations
- **Prepared Statement Caching**: Enabled with 250 statements cached
- **Server-side Prepared Statements**: Enabled
- **Batch Statement Rewriting**: Enabled for better performance
- **Connection Validation**: Automatic validation with "SELECT 1" query
- **Auto-reconnection**: Enabled with 3 retry attempts

### Connection Management Features

#### 1. Automatic Connection Cleanup
- **Idle Connection Cleanup**: Every 5 minutes
- **Connection Validation**: Every 10 minutes
- **Stale Connection Detection**: Automatic eviction of invalid connections

#### 2. Connection Monitoring
- **Pool Statistics Logging**: Every 5 minutes
- **Health Check Endpoints**: Available at `/api/health/database/`
- **Connection Leak Detection**: Automatic detection and logging

#### 3. Error Handling
- **Connection Error Recovery**: Automatic handling of connection failures
- **Transaction Rollback**: Automatic rollback on connection errors
- **Connection Cleanup**: Proper cleanup of failed connections

## Global Exception Handling

### ErrorResponse Structure

All exceptions are handled centrally and return a consistent `ErrorResponse` structure:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/leads",
  "details": {
    "fieldName": "Error message"
  }
}
```

### Exception Types Handled

#### 1. Validation Exceptions
- **MethodArgumentNotValidException**: Bean validation errors
- **ConstraintViolationException**: Constraint validation errors
- **HttpMessageNotReadableException**: Malformed JSON requests
- **MethodArgumentTypeMismatchException**: Type conversion errors

#### 2. Database Exceptions
- **DataAccessException**: General database access errors
- **SQLException**: SQL-specific errors with detailed error codes

#### 3. Business Logic Exceptions
- **IllegalArgumentException**: Invalid arguments
- **NoSuchElementException**: Resource not found (404)
- **SecurityException**: Access denied (403)

#### 4. Generic Exception Handling
- **Exception**: Catch-all for unexpected errors (500)

### Benefits

#### 1. Consistent Error Responses
- All API endpoints return consistent error format
- Proper HTTP status codes
- Detailed error information for debugging

#### 2. Centralized Logging
- All exceptions are logged centrally
- Different log levels for different exception types
- Structured logging for monitoring

#### 3. Clean Controller Code
- Controllers no longer need try-catch blocks
- Focus on business logic rather than error handling
- Automatic error response generation

## Monitoring and Health Checks

### Database Health Endpoints

#### 1. Connection Pool Status
```
GET /api/health/database/status
```
Returns detailed connection pool statistics including:
- Active connections
- Idle connections
- Total connections
- Threads awaiting connection
- Pool configuration

#### 2. Connection Test
```
GET /api/health/database/test
```
Tests database connectivity and returns health status.

### Logging

#### Connection Pool Statistics (Every 5 minutes)
```
=== HikariCP Pool Statistics ===
Active Connections: 3
Idle Connections: 2
Total Connections: 5
Threads Awaiting Connection: 0
Pool Name: CRM-HikariCP
===============================
```

#### Connection Validation (Every 10 minutes)
```
Database connection validation successful
```

## Configuration Files

### application.yml
Enhanced HikariCP configuration with MySQL optimizations and connection management settings.

### DatabaseConfig.kt
- HikariCP DataSource configuration
- Connection pool monitoring
- Connection validation scheduling

### ConnectionCleanupService.kt
- Idle connection cleanup
- Connection error handling
- Pool statistics collection

### GlobalExceptionHandler.kt
- Centralized exception handling
- Consistent error response format
- Comprehensive logging

## Best Practices

### 1. Connection Management
- Always close connections properly
- Use try-with-resources when possible
- Monitor connection pool statistics

### 2. Exception Handling
- Let the global handler manage exceptions
- Use specific exception types for business logic
- Provide meaningful error messages

### 3. Monitoring
- Monitor connection pool health
- Set up alerts for connection issues
- Regular review of error logs

## Troubleshooting

### Common Issues

#### 1. Connection Pool Exhaustion
- Check for connection leaks
- Review connection pool size settings
- Monitor active connection count

#### 2. Database Connection Errors
- Verify database connectivity
- Check connection pool configuration
- Review error logs for specific SQL errors

#### 3. Performance Issues
- Monitor connection pool statistics
- Review MySQL-specific optimizations
- Check for long-running queries

### Health Check Commands

```bash
# Check connection pool status
curl http://localhost:8080/crm/api/health/database/status

# Test database connectivity
curl http://localhost:8080/crm/api/health/database/test

# Check application health
curl http://localhost:8080/crm/api/health
```

## Future Enhancements

1. **Connection Pool Metrics**: Integration with Micrometer for metrics collection
2. **Circuit Breaker**: Implement circuit breaker pattern for database operations
3. **Connection Pool Scaling**: Dynamic pool size adjustment based on load
4. **Advanced Monitoring**: Integration with APM tools like New Relic or DataDog
5. **Database Sharding**: Support for multiple database instances
