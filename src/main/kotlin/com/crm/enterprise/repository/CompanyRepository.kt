package com.crm.enterprise.repository

import com.crm.enterprise.entity.Company
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : CrudRepository<Company, Long> {
    
    @Query("SELECT * FROM companies WHERE name = :name")
    fun findByName(name: String): Company?
    
    @Query("SELECT * FROM companies WHERE is_active = true")
    fun findActiveCompanies(): List<Company>
    
    @Query("SELECT * FROM companies WHERE name LIKE :namePattern")
    fun findByNameContaining(namePattern: String): List<Company>
}

