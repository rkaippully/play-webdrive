package controllers;

import play.*;
import play.data.validation.Valid;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
	User user = new User();
        render(user);
    }
    
    public static void save(@Valid User user) {
        render(user);
    }
}