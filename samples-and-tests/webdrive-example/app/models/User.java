package models;

import java.util.List;
import java.util.Set;

import play.*;
import play.data.validation.Email;
import play.data.validation.IPv4Address;
import play.data.validation.IPv6Address;
import play.data.validation.Match;
import play.data.validation.Max;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Phone;
import play.data.validation.Range;
import play.data.validation.Required;
import play.data.validation.URL;
import play.data.validation.Valid;
import play.db.jpa.Model;

import javax.persistence.*;

@Entity
public class User extends Model {
    
    @Required
    public Boolean enabled;
    
    @Required 
    public String username;
    
    @Required
    public String name;
    
    public String givenName;
    
    @Required @Email 
    public String email;
     
    public String role;
    
    public String details;
    
}
