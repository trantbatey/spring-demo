package com.example.springdemo.controllers;

import com.example.springdemo.models.Ad;
import com.example.springdemo.models.User;
import com.example.springdemo.repositories.AdRepository;
import com.example.springdemo.repositories.UserRepository;
import com.example.springdemo.services.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdController {
    private final AdRepository adRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    public AdController(AdRepository adRepo, UserRepository userRepo, EmailService emailService) {
        this.adRepo = adRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    @GetMapping("/ads")
    public String index(Model model) {
        model.addAttribute("ads", adRepo.findAll());
        return "ads/index";
    }

    @GetMapping("/ads/{id}")
    public String showAd(@PathVariable long id, Model model) {
        Ad ad = adRepo.getAdById(id);
        model.addAttribute("ad", ad);
        return "ads/show";
    }

    @GetMapping("/ads/create")
    public String showCreateView(Model model) {
        model.addAttribute("ad", new Ad());
        return "ads/create";
    }

    @PostMapping("/ads/create")
    public String createAd(@ModelAttribute Ad ad) {
        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (obj == null || !(obj instanceof UserDetails)) {
            return "redirect:/login";
        }
        User user = (User) obj;
        ad.setOwner(user);
        adRepo.save(ad);
        emailService.prepareAndSendAd(ad, "Created Ad: " + ad.getTitle(),
                ad.getTitle() +"\n\n" +
                        ad.getDescription());
        return "redirect:/ads/" + ad.getId();
    }

    @GetMapping("/ads/delete/{id}")
    public String deleteAd(@PathVariable long id, Model model) {
        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (obj == null || !(obj instanceof UserDetails)) {
            return "redirect:/login";
        }
        User currentUser = (User) obj;
        Ad ad = adRepo.getAdById(id);
        if (ad.getOwner() == null) {
            ad.setOwner(currentUser);
        }
        adRepo.delete(ad);
        emailService.prepareAndSendAd(ad, "Deleted Ad: " + ad.getTitle(),
                ad.getTitle() +"\n\n" +
                        ad.getDescription());
        return "redirect:/ads";
    }

    @GetMapping("/ads/edit/{id}")
    public String editAd(@PathVariable long id, Model model) {
        Ad ad = adRepo.getAdById(id);
        model.addAttribute("ad", ad);
        return "ads/edit";
    }

    @PostMapping("/ads/edit")
    public String updateAd(@RequestParam(name = "id") long id,
                           @RequestParam(name = "title") String title,
                           @RequestParam(name = "description") String description) {
        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (obj == null || !(obj instanceof UserDetails)) {
            return "redirect:/login";
        }
        User currentUser = (User) obj;
        Ad ad = adRepo.getAdById(id);
        ad.setTitle(title);
        ad.setDescription(description);
        if (ad.getOwner() == null) {
            ad.setOwner(currentUser);
        }
        adRepo.save(ad);
        emailService.prepareAndSendAd(ad, "Edited Ad: " + ad.getTitle(),
                ad.getTitle() +"\n\n" +
                        ad.getDescription());
        return "redirect:/ads/" + ad.getId();
    }
}
