package com.warehouse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("active", "home");
        return "index";
    }

    @GetMapping("/materials")
    public String materials(Model model) {
        model.addAttribute("active", "materials");
        return "materials";
    }

    @GetMapping("/arrivals")
    public String arrivals(Model model) {
        model.addAttribute("active", "arrivals");
        return "arrivals";
    }

    @GetMapping("/warehousing")
    public String warehousing(Model model) {
        model.addAttribute("active", "warehousing");
        return "warehousing";
    }

    @GetMapping("/export")
    public String export(Model model) {
        model.addAttribute("active", "export");
        return "export";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
