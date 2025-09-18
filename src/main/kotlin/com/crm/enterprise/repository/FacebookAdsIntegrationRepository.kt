// Temporarily disabled due to Facebook SDK compilation issues
/*
package com.crm.enterprise.repository

import com.crm.enterprise.entity.FacebookAdsIntegration
import com.crm.enterprise.entity.FacebookAdsLead
import org.springframework.data.repository.CrudRepository
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface FacebookAdsIntegrationRepository : CrudRepository<FacebookAdsIntegration, Long> {

    @Query("SELECT * FROM facebook_ads_integrations WHERE company_id = :companyId AND is_active = true")
    fun findByCompanyIdAndActive(companyId: Long): List<FacebookAdsIntegration>

    @Query("SELECT * FROM facebook_ads_integrations WHERE company_id = :companyId")
    fun findByCompanyId(companyId: Long): List<FacebookAdsIntegration>

    @Query("SELECT * FROM facebook_ads_integrations WHERE is_active = true")
    fun findAllActive(): List<FacebookAdsIntegration>

    @Query("SELECT * FROM facebook_ads_integrations WHERE is_active = true")
    fun getAllActiveIntegrations(): List<FacebookAdsIntegration>
}

@Repository
interface FacebookAdsLeadRepository : CrudRepository<FacebookAdsLead, Long> {

    @Query("SELECT * FROM facebook_ads_leads WHERE company_id = :companyId ORDER BY created_at DESC")
    fun findByCompanyId(companyId: Long): List<FacebookAdsLead>

    @Query("SELECT * FROM facebook_ads_leads WHERE company_id = :companyId AND status = :status ORDER BY created_at DESC")
    fun findByCompanyIdAndStatus(companyId: Long, status: String): List<FacebookAdsLead>

    @Query("SELECT * FROM facebook_ads_leads WHERE facebook_lead_id = :facebookLeadId")
    fun findByFacebookLeadId(facebookLeadId: String): FacebookAdsLead?

    @Query("SELECT * FROM facebook_ads_leads WHERE company_id = :companyId AND created_at >= :since ORDER BY created_at DESC")
    fun findByCompanyIdAndCreatedAtAfter(companyId: Long, since: LocalDateTime): List<FacebookAdsLead>

    @Query("SELECT * FROM facebook_ads_leads WHERE status = 'NEW' ORDER BY created_at ASC")
    fun findAllNewLeads(): List<FacebookAdsLead>

    @Query("SELECT * FROM facebook_ads_leads WHERE status = 'NEW' ORDER BY created_at ASC")
    fun processAllNewLeads(): List<FacebookAdsLead>
}
*/
