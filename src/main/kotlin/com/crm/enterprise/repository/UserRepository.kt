package com.crm.enterprise.repository

import com.crm.enterprise.entity.User
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<User, Long> {
    
    @Query("SELECT * FROM users WHERE email = :email")
    fun findByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE username = :username")
    fun findByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE company_id = :companyId")
    fun findByCompanyId(companyId: Long): List<User>
    
    @Query("SELECT * FROM users WHERE company_id = :companyId AND is_active = true")
    fun findActiveByCompanyId(companyId: Long): List<User>
    
    @Query("SELECT * FROM users WHERE is_superuser = true")
    fun findSuperUsers(): List<User>
    
    @Query("SELECT * FROM users WHERE company_id = :companyId AND is_company_admin = true")
    fun findCompanyAdmins(companyId: Long): List<User>
}

