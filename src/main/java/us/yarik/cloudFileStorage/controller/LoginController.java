package us.yarik.cloudFileStorage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import us.yarik.cloudFileStorage.exception.ConflictException;
import us.yarik.cloudFileStorage.model.User;
import us.yarik.cloudFileStorage.service.UserService;

@Controller
public class LoginController {
    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginGet(Model model){
        model.addAttribute("user", new User());
        return "login";
    }


    @GetMapping("/registration")
    public String registrationGet(Model model){
        model.addAttribute("user", new User());
        return "registration";
    }
    //TODO Fix http status
    @PostMapping("/registration")
    public String registrationPost(@ModelAttribute("user") User user, Model model, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "registration";
        }
         try {
               userService.registerCheck(user);
         }catch(ConflictException e){
             model.addAttribute("error", e.getMessage());
             return "registration";
         }
        userService.save(user);
        return "redirect:/login";
    }

}
