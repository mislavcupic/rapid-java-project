package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("logfleet/")
public class WelcomeScreenController {



    @GetMapping("/api/welcome")
    public String showWelcomeScreen()
    {
        return "WelcomeScreen";
    }
}
