package us.yarik.CloudFileStorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import us.yarik.CloudFileStorage.advice.ConflictException;
import us.yarik.CloudFileStorage.model.CreateUserRequest;
import us.yarik.CloudFileStorage.service.UserService;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;

    @GetMapping("/login")
    public String loginGet(Model model) {
        model.addAttribute("user", new CreateUserRequest());
        return "login";
    }

    @GetMapping("/registration")
    public String registrationGet(Model model) {
        model.addAttribute("user", new CreateUserRequest());
        return "registration";
    }

    @PostMapping("/registration")
    public String registrationPost(@ModelAttribute("user") @Valid CreateUserRequest createUserRequest,
                                   BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        try {
            userService.registerCheck(createUserRequest);
        } catch (ConflictException e) {
            model.addAttribute("error", "Email already exists!");
            return "registration";
        }
        userService.createUser(createUserRequest);
        return "redirect:/login";
    }
}