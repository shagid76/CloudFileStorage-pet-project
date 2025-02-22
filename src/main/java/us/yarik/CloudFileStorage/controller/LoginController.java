package us.yarik.CloudFileStorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import us.yarik.CloudFileStorage.advice.ConflictException;
import us.yarik.CloudFileStorage.model.User;
import us.yarik.CloudFileStorage.service.UserService;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;

    @GetMapping("/login")
    public String loginGet(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/registration")
    public String registrationGet(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String registrationPost(@ModelAttribute("user")  @Valid User user, BindingResult bindingResult , Model model) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        try {
            userService.registerCheck(user);
        } catch (ConflictException e) {
            model.addAttribute("error", e.getMessage());
            return "registration";
        }
        userService.save(user);
        return "redirect:/login";
    }
}