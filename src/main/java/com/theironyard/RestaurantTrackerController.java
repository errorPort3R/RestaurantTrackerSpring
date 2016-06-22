package com.theironyard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

/**
 * Created by jeffryporter on 6/21/16.
 */
@Controller
public class RestaurantTrackerController
{
    @Autowired
    UserRepository users;

    @Autowired
    RestaurantRepository restaurants;

    @PostConstruct
    public void init()
    {
        if (users.count() == 0)
        {
            User user = new User("Jeff", "12345");
            users.save(user);
        }
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(HttpSession session, Model model, Integer rating, String location, String search)
    {
        String username = (String) session.getAttribute("username");
        Iterable<Restaurant> rests;
        if(username == null)
        {
            return "login";
        }
        else
        {
            User user = users.findByName(username);

            if (search != null)
            {
                rests = restaurants.searchLocation(search);
            }
            else
            {
                if (rating != null && location != null)
                {
                    rests = restaurants.findByRatingAndLocation(rating, location);
                } else if (location != null)
                {
                    rests = restaurants.findByLocation(location);
                } else if (rating != null)
                {
                    rests = restaurants.findByRatingGreaterThanEqual(rating);
                } else
                {
                    rests = restaurants.findByUser(user);
                }
            }

        }
        model.addAttribute("restaurants", rests);
        return "home";

    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, HttpSession session) throws Exception
    {
        User user = users.findByName(username);
        if (user == null)
        {
            user = new User(username,password);
            users.save(user);
        }
        else if(!user.password.equals(password))
        {
            throw new Exception("Wrong Password!");
        }
        session.setAttribute("username", username);
        return "redirect:/";
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public String logout(HttpSession session)
    {
        session.invalidate();
        return "redirect:/";
    }

    @RequestMapping(path = "/create-restaurant", method = RequestMethod.POST)
    public String create(HttpSession session,String name, String location, int rating, String comment)
    {
        String username = (String)session.getAttribute("username");
        User user = users.findByName(username);
        Restaurant r = new Restaurant(name, location, rating, comment, user);
        restaurants.save(r);
        return "redirect:/";
    }

    @RequestMapping(path = "/delete-restaurant", method = RequestMethod.POST)
    public String delete(int id)
    {
        restaurants.delete(id);
        return "redirect:/";
    }
}
