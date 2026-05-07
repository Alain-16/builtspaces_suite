package com.builtspaces.elanet;

import com.builtspaces.elanet.common.TenantEntity;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import jakarta.persistence.Entity;
import com.tngtech.archunit.core.domain.JavaClass;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;

@AnalyzeClasses(packages= "com.builtspaces.elanet")
public class ArchitectureTest {
	
    @ArchTest                                                                                                                                                                        
    static final ArchRule property_module_is_standalone =                                                                                                                          
        noClasses().that().resideInAPackage("..property..")                                                                                                                          
            .should().dependOnClassesThat()
            .resideInAnyPackage("..contact..", "..deal..", "..marketing..", "..activity..")                                                                                          
            .because("property is a foundation module — nothing depends on it except deal and marketing, never the reverse");                                                        
                                                                                                                                                                                     
    @ArchTest                                                                                                                                                                        
    static final ArchRule contact_module_is_standalone =                                                                                                                             
        noClasses().that().resideInAPackage("..contact..")                                                                                                                           
            .should().dependOnClassesThat()
            .resideInAnyPackage("..property..", "..deal..", "..marketing..", "..activity..")                                                                                         
            .because("contact is a foundation module — same rule as property");                                                                                                      
 
    @ArchTest                                                                                                                                                                        
    static final ArchRule activity_module_is_standalone =                                                                                                                          
        noClasses().that().resideInAPackage("..activity..")                                                                                                                          
            .should().dependOnClassesThat()
            .resideInAnyPackage("..property..", "..contact..", "..deal..", "..marketing..")                                                                                          
            .because("activity only receives events — it never initiates calls into other modules");                                                                                 
                                                                                                                                                                                     
    // -------------------------------------------------------------------------                                                                                                     
    // Cross-module access rules — deal and marketing may only call into                                                                                                             
    // property and contact via service interfaces and DTOs, never repositories                                                                                                      
    // or entities directly. This keeps module internals private.
    // -------------------------------------------------------------------------                                                                                                     
                                                                                                                                                                                   
    @ArchTest                                                                                                                                                                        
    static final ArchRule deal_does_not_access_cross_module_repositories =                                                                                                         
        noClasses().that().resideInAPackage("..deal..")                                                                                                                              
            .should().dependOnClassesThat(simpleNameEndingWith("Repository").and(resideInAnyPackage("..property..","..contact..")))                                                                                                                
            .because("deal must use PropertyService/ContactService — direct repository access bypasses tenant scoping and service logic");
                                                                                                                                                                                     
    @ArchTest                                                                                                                                                                      
    static final ArchRule marketing_does_not_access_cross_module_repositories =                                                                                                      
        noClasses().that().resideInAPackage("..marketing..")                                                                                                                       
            .should().dependOnClassesThat(simpleNameEndingWith("Repository").and(resideInAnyPackage("..property..","..contact..")))
            .because("marketing must use PropertyService — same reasoning as deal");                                                                                                 
                                                                                                                                                                                     
    // -------------------------------------------------------------------------
    // Controller layer rule — controllers coordinate requests, services own                                                                                                         
    // business logic and data access. A controller that calls a repository                                                                                                          
    // directly is skipping validation, authorization, and tenant scoping.                                                                                                           
    // -------------------------------------------------------------------------                                                                                                     
                                                                                                                                                                                     
    @ArchTest                                                                                                                                                                        
    static final ArchRule controllers_do_not_access_repositories =                                                                                                                 
        noClasses().that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .because("controllers must go through services — repositories are an implementation detail of the service layer");                                                       

    // -------------------------------------------------------------------------
    // Multi-tenancy enforcement — every @Entity in a business module must
    // extend TenantEntity. Organisation and User are excluded because they
    // ARE the tenant boundary, not scoped within one.
    // -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule business_entities_must_extend_tenant_entity =
        classes().that().areAnnotatedWith(Entity.class)
            .and().resideInAnyPackage(
                "..property..", "..contact..", "..deal..",
                "..activity..", "..marketing.."
            )
            .should().beAssignableTo(TenantEntity.class)
            .because("every business entity must carry org_id — extending TenantEntity is how that is enforced");

}
