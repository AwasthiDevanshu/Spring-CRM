# Enterprise CRM API Documentation

## Overview

The Enterprise CRM API provides comprehensive endpoints for managing customer relationships, leads, contacts, deals, and analytics. This RESTful API is built with Spring Boot and follows industry best practices.

**Base URL:** `http://localhost:8080`  
**API Version:** 1.0.0  
**Documentation:** `http://localhost:8080/swagger-ui.html`

## Authentication

The API uses JWT (JSON Web Token) authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Login

**POST** `/api/auth/login`

Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "admin@acme.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "success": true,
  "message": null,
  "token": "jwt-token-1758044199996",
  "user": {
    "id": 1,
    "email": "admin@acme.com",
    "username": "admin",
    "firstName": "Admin",
    "lastName": "User",
    "fullName": "Admin User",
    "phone": "+1-555-0123",
    "isActive": true,
    "isSuperuser": true,
    "isCompanyAdmin": true,
    "companyId": 1,
    "lastLogin": "2025-09-14T04:43:10"
  }
}
```

### Get Current User

**GET** `/api/auth/me`

Get current authenticated user information.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "id": 1,
  "email": "admin@acme.com",
  "username": "admin",
  "firstName": "Admin",
  "lastName": "User",
  "fullName": "Admin User",
  "phone": "+1-555-0123",
  "isActive": true,
  "isSuperuser": true,
  "isCompanyAdmin": true,
  "companyId": 1,
  "lastLogin": "2025-09-14T04:43:10"
}
```

## Dashboard Analytics

### Get Dashboard Statistics

**GET** `/api/dashboard/stats`

Get key performance indicators and statistics.

**Headers:**
```
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "totalLeads": 156,
  "newLeads": 23,
  "qualifiedLeads": 45,
  "totalContacts": 89,
  "totalDeals": 34,
  "openDeals": 12,
  "wonDeals": 18,
  "lostDeals": 4,
  "totalRevenue": 125000.00,
  "monthlyRevenue": 25000.00,
  "conversionRate": 15.5,
  "averageDealSize": 3676.47,
  "topLeadSources": [
    {"source": "Website", "count": 45},
    {"source": "Referral", "count": 32},
    {"source": "Phone", "count": 28},
    {"source": "Email", "count": 21}
  ],
  "recentActivities": [
    {"description": "New lead: John Doe", "timeAgo": "2 hours ago"},
    {"description": "Deal closed: Enterprise License", "timeAgo": "4 hours ago"},
    {"description": "Contact updated: Jane Smith", "timeAgo": "6 hours ago"}
  ]
}
```

### Get Leads Chart Data

**GET** `/api/dashboard/leads/chart`

Get leads data for chart visualization.

**Headers:**
```
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Query Parameters:**
- `days` (optional): Number of days (default: 30)

**Response:**
```json
{
  "labels": ["Week 1", "Week 2", "Week 3", "Week 4"],
  "newLeads": [12, 19, 8, 15],
  "qualifiedLeads": [8, 12, 6, 10],
  "convertedLeads": [3, 5, 2, 4]
}
```

### Get Revenue Chart Data

**GET** `/api/dashboard/revenue/chart`

Get revenue data for chart visualization.

**Headers:**
```
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Query Parameters:**
- `months` (optional): Number of months (default: 12)

**Response:**
```json
{
  "labels": ["Jan", "Feb", "Mar", "Apr", "May", "Jun"],
  "revenue": [15000, 22000, 18000, 25000, 30000, 28000],
  "target": [20000, 20000, 20000, 20000, 20000, 20000]
}
```

## Leads Management

### Create Lead

**POST** `/api/leads`

Create a new lead in the CRM system.

**Headers:**
```
Content-Type: application/json
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-0123",
  "company": "Example Corp",
  "jobTitle": "CTO",
  "status": "NEW",
  "source": "WEBSITE",
  "score": 85,
  "notes": "High priority lead from website",
  "assignedUserId": 3
}
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-0123",
  "company": "Example Corp",
  "jobTitle": "CTO",
  "status": "NEW",
  "source": "WEBSITE",
  "score": 85,
  "notes": "High priority lead from website",
  "assignedUserId": 3,
  "companyId": 1,
  "createdAt": "2025-09-16T18:39:51",
  "updatedAt": "2025-09-16T18:39:51"
}
```

### Get Leads

**GET** `/api/leads`

Get leads with optional filtering.

**Headers:**
```
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Query Parameters:**
- `status` (optional): Lead status (NEW, CONTACTED, QUALIFIED, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST)
- `assignedUserId` (optional): Assigned user ID
- `search` (optional): Search term

**Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123",
    "company": "Example Corp",
    "jobTitle": "CTO",
    "status": "NEW",
    "source": "WEBSITE",
    "score": 85,
    "notes": "High priority lead from website",
    "assignedUserId": 3,
    "companyId": 1,
    "createdAt": "2025-09-16T18:39:51",
    "updatedAt": "2025-09-16T18:39:51"
  }
]
```

### Get Lead by ID

**GET** `/api/leads/{id}`

Get a specific lead by its ID.

**Headers:**
```
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-0123",
  "company": "Example Corp",
  "jobTitle": "CTO",
  "status": "NEW",
  "source": "WEBSITE",
  "score": 85,
  "notes": "High priority lead from website",
  "assignedUserId": 3,
  "companyId": 1,
  "createdAt": "2025-09-16T18:39:51",
  "updatedAt": "2025-09-16T18:39:51"
}
```

### Update Lead

**PUT** `/api/leads/{id}`

Update an existing lead.

**Headers:**
```
Content-Type: application/json
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-0123",
  "company": "Example Corp",
  "jobTitle": "CTO",
  "status": "CONTACTED",
  "source": "WEBSITE",
  "score": 90,
  "notes": "Updated notes",
  "assignedUserId": 3
}
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-0123",
  "company": "Example Corp",
  "jobTitle": "CTO",
  "status": "CONTACTED",
  "source": "WEBSITE",
  "score": 90,
  "notes": "Updated notes",
  "assignedUserId": 3,
  "companyId": 1,
  "createdAt": "2025-09-16T18:39:51",
  "updatedAt": "2025-09-16T18:40:15"
}
```

### Delete Lead

**DELETE** `/api/leads/{id}`

Delete a lead from the system.

**Headers:**
```
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Response:** `204 No Content`

## Contacts Management

### Create Contact

**POST** `/api/contacts`

Create a new contact in the CRM system.

**Headers:**
```
Content-Type: application/json
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "phone": "+1-555-0124",
  "jobTitle": "Marketing Director",
  "department": "Marketing",
  "leadId": 1,
  "notes": "Converted from lead",
  "assignedUserId": 2
}
```

**Response:**
```json
{
  "id": 1,
  "firstName": "Jane",
  "lastName": "Smith",
  "fullName": "Jane Smith",
  "email": "jane.smith@example.com",
  "phone": "+1-555-0124",
  "jobTitle": "Marketing Director",
  "department": "Marketing",
  "companyId": 1,
  "leadId": 1,
  "isActive": true,
  "notes": "Converted from lead",
  "assignedUserId": 2,
  "createdAt": "2025-09-16T18:39:51",
  "updatedAt": "2025-09-16T18:39:51"
}
```

### Get Contacts

**GET** `/api/contacts`

Get contacts with optional filtering.

**Headers:**
```
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Query Parameters:**
- `assignedUserId` (optional): Assigned user ID
- `search` (optional): Search term

**Response:**
```json
[
  {
    "id": 1,
    "firstName": "Jane",
    "lastName": "Smith",
    "fullName": "Jane Smith",
    "email": "jane.smith@example.com",
    "phone": "+1-555-0124",
    "jobTitle": "Marketing Director",
    "department": "Marketing",
    "companyId": 1,
    "leadId": 1,
    "isActive": true,
    "notes": "Converted from lead",
    "assignedUserId": 2,
    "createdAt": "2025-09-16T18:39:51",
    "updatedAt": "2025-09-16T18:39:51"
  }
]
```

## Deals Management

### Create Deal

**POST** `/api/deals`

Create a new deal in the CRM system.

**Headers:**
```
Content-Type: application/json
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "name": "Enterprise Software License",
  "description": "Annual software license for 500 users",
  "value": 50000.00,
  "currency": "USD",
  "status": "OPEN",
  "probability": 75,
  "expectedCloseDate": "2024-03-31T00:00:00",
  "contactId": 1,
  "pipelineId": 1,
  "stageId": 4,
  "assignedUserId": 3
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Enterprise Software License",
  "description": "Annual software license for 500 users",
  "value": 50000.00,
  "currency": "USD",
  "status": "OPEN",
  "probability": 75,
  "expectedCloseDate": "2024-03-31T00:00:00",
  "actualCloseDate": null,
  "contactId": 1,
  "pipelineId": 1,
  "stageId": 4,
  "assignedUserId": 3,
  "companyId": 1,
  "createdAt": "2025-09-16T18:39:51",
  "updatedAt": "2025-09-16T18:39:51"
}
```

### Get Deals

**GET** `/api/deals`

Get deals with optional filtering.

**Headers:**
```
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Query Parameters:**
- `status` (optional): Deal status (OPEN, WON, LOST, CANCELLED)
- `assignedUserId` (optional): Assigned user ID
- `pipelineId` (optional): Pipeline ID
- `stageId` (optional): Stage ID

**Response:**
```json
[
  {
    "id": 1,
    "name": "Enterprise Software License",
    "description": "Annual software license for 500 users",
    "value": 50000.00,
    "currency": "USD",
    "status": "OPEN",
    "probability": 75,
    "expectedCloseDate": "2024-03-31T00:00:00",
    "actualCloseDate": null,
    "contactId": 1,
    "pipelineId": 1,
    "stageId": 4,
    "assignedUserId": 3,
    "companyId": 1,
    "createdAt": "2025-09-16T18:39:51",
    "updatedAt": "2025-09-16T18:39:51"
  }
]
```

### Get Deal Analytics

**GET** `/api/deals/analytics/summary`

Get summary statistics for deals.

**Headers:**
```
X-Company-Id: 1
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "totalDeals": 34,
  "openDeals": 12,
  "wonDeals": 18,
  "lostDeals": 4,
  "totalValue": 125000.00,
  "wonValue": 75000.00,
  "averageDealSize": 3676.47,
  "conversionRate": 52.94
}
```

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "success": false,
  "message": "Invalid request data"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Invalid credentials"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Internal server error"
}
```

## Data Models

### Lead Status
- `NEW`: New lead
- `CONTACTED`: Lead has been contacted
- `QUALIFIED`: Lead has been qualified
- `PROPOSAL`: Proposal sent
- `NEGOTIATION`: In negotiation
- `CLOSED_WON`: Deal closed successfully
- `CLOSED_LOST`: Deal lost

### Lead Source
- `WEBSITE`: Website
- `PHONE`: Phone call
- `EMAIL`: Email
- `REFERRAL`: Referral
- `SOCIAL_MEDIA`: Social media
- `ADVERTISEMENT`: Advertisement
- `TRADE_SHOW`: Trade show
- `OTHER`: Other

### Deal Status
- `OPEN`: Open deal
- `WON`: Won deal
- `LOST`: Lost deal
- `CANCELLED`: Cancelled deal

## Rate Limiting

The API implements rate limiting to ensure fair usage:
- 1000 requests per hour per IP address
- 100 requests per minute per authenticated user

## Pagination

List endpoints support pagination:
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `sort`: Sort field and direction (e.g., "createdAt,desc")

## Webhooks

The API supports webhooks for real-time notifications:
- Lead created/updated
- Deal status changed
- Contact created/updated

Configure webhooks in the settings section of the application.

## SDKs and Libraries

Official SDKs are available for:
- JavaScript/TypeScript
- Python
- Java
- C#

## Support

For API support and questions:
- Email: support@crm.com
- Documentation: https://crm.com/docs
- Status Page: https://status.crm.com
