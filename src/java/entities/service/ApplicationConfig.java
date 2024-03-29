/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entities.service;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author junxin
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(entities.service.CertnamesFacadeREST.class);
        resources.add(entities.service.EmergencycontactsFacadeREST.class);
        resources.add(entities.service.EmergencycontactsforuserFacadeREST.class);
        resources.add(entities.service.LoginFacadeREST.class);
        resources.add(entities.service.SkillsFacadeREST.class);
        resources.add(entities.service.SkillsforusersFacadeREST.class);
        resources.add(entities.service.UserrolesFacadeREST.class);
    }
    
}
