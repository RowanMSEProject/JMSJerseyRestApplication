/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.service;

import entities.Login;
import entities.Userroles;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.script.ScriptException;
import javax.swing.JOptionPane;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import producer.ProduceSender;

/**
 *
 * @author junxin
 */
@Stateless
@Path("entities.login")
public class LoginFacadeREST extends AbstractFacade<Login> {

    @PersistenceContext(unitName = "JMSJerseyRestApplicationPU")
    private EntityManager em;

    public LoginFacadeREST() {
        super(Login.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Login entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Integer id, Login entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Login find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<Login> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<Login> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @POST
    @Path("/create")
    @Consumes({"application/x-www-form-urlencoded", "application/xml", "application/json"})
    public void createUser(@FormParam("firstName") String firstName,
            @FormParam("lastName") String lastName,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("role") String role) {
        if (checkPassword(password)) {
            List<Login> users = em.createNamedQuery("Login.findAll").getResultList();
            int id = users.get(users.size() - 1).getUserid();
            id++;
            Login newUser = new Login(id, username, password, firstName, lastName);
            int roleID = Integer.parseInt(role);
            Userroles newRole = new Userroles(roleID);
            newUser.setRoleid(newRole);
            super.create(newUser);
            try {
                // Create a new JMS publisher
                ProduceSender.publish();
            } catch (JMSException ex) {
                Logger.getLogger(LoginFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NamingException ex) {
                Logger.getLogger(LoginFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LoginFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new WebApplicationException(Response.status(400).entity("Password must contain capital letter and number and be 8 characters long <a href=\"https://localhost:8100/JerseyRestDerby/createUserForm.html\">Create User</a>").build());
        }
    }

    /**
     * Update password of a user given userID and old password the new password
     * must return true when passed into checkPassword()
     *
     * @param userid
     * @param oldPswd
     * @param newPswd
     */
    @POST
    @Path("/updatePswd")
    @Consumes({"application/x-www-form-urlencoded", "application/xml", "application/json"})
    public void updatePswd(@FormParam("username") String userid,
            @FormParam("oldPassword") String oldPswd,
            @FormParam("newPassword") String newPswd) throws ScriptException, NoSuchMethodException {
        try {
            int id = Integer.parseInt(userid);
            List<Login> lone = em.createNamedQuery("Login.findByUserid").setParameter("userid", id).getResultList();
            if (lone.isEmpty() || !lone.get(0).getPassword().equals(oldPswd)) {
                JOptionPane.showMessageDialog(null, "Wrong Id or Password", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!checkPassword(newPswd)) {
                JOptionPane.showMessageDialog(null, "New Password must contain \ncapital letter \nlower case letter\nnumber \nand be 8 characters long", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                //String query = "UPDATE Login SET password='" + newPswd + "', WHERE userid=" + userid + " and "
                //        + "password='" + oldPswd + "'";
                lone.get(0).setPassword(newPswd);
                super.edit(lone.get(0));
//                super.executeQuery(query);
                try {
                    // Create a new JMS publisher
                    ProduceSender.publish();
                } catch (JMSException ex) {
                    Logger.getLogger(LoginFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NamingException ex) {
                    Logger.getLogger(LoginFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LoginFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "UserId must be number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean checkPassword(String password) {
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean isAtLeast8 = password.length() >= 8;//Checks for at least 8 characters
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.charAt(i))) {
                hasDigit = true;
                break;
            }
        }
        return hasUppercase && hasLowercase && isAtLeast8 && hasDigit;
    }

    @GET
    @Path("/users")
    @Consumes({"application/x-www-form-urlencoded", "application/xml", "application/json"})
    public String getUsers() {
        final String format = "%s\t\t%s\n";

        String answer = String.format(format, "Username", "Password");
        answer += "============================\n";

        List<Login> users = em.createNamedQuery("Login.findAll").getResultList();
        for (Login u : users) {
            answer += String.format(format, u.getUsername(), u.getPassword());
        }
        return answer;
    }

    @POST
    @Path("/rm")
    @Consumes("application/x-www-form-urlencoded")
    public void delete(@FormParam("userid") String userid) throws JMSException {
        //Old way we deleted users before new db added
        //String query = "DELETE FROM Skillsforusers sfu WHERE sfu.login.userid=" + userid;
        //super.executeQuery(query);
        //query = "DELETE FROM Login l WHERE l.userid=" + userid;
        //super.executeQuery(query);
        int id = Integer.parseInt(userid);
        super.remove(super.find(id));
        try {
            ProduceSender.publish();
        } catch (JMSException ex) {
            Logger.getLogger(LoginFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(LoginFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoginFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    @GET
    @Path("users/table")
    @Produces("text/html")
    public String getUsersTable() {
        if (em != null) {
            List<Login> results = em.createQuery("SELECT L FROM Login L").getResultList();
            String answer = "<h2> Users </h2>" + "<br><table border='1'> <tr>";
            answer = answer + "<th>USERID</th><th>FIRST NAME</th><th>LAST NAME</th><th>USERNAME</th><th>PASSWORD</th><th>ROLE</th></tr>";
            for (Login result : results) {
                answer = answer + "<tr><td>" + result.getUserid() + "</td>";
                answer = answer + "<td>" + result.getFirstname() + "</td>";
                answer = answer + "<td>" + result.getLastname() + "</td>";
                answer = answer + " <td>" + result.getUsername() + "</td>";
                answer = answer + " <td>" + result.getPassword() + "</td>";
                answer = answer + " <td>" + result.getRoleid().getDescription() + "</td></tr>";
            }
            answer = answer + "</table>";
            return answer;
        } else {
            throw new WebApplicationException(Response.status(400)
                    .entity("Null entity manager")
                    .build());
        }
    }
}
