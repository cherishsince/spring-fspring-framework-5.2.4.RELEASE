package example.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class EmailController {
    private static Pattern pattern;
    private Matcher matcher;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9]+[A-Za-z0-9]*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)$";

    public EmailController() {
        pattern = Pattern.compile(EMAIL_REGEX);
    }

    @GetMapping(value = "/form", name = "ss1")
    public ModelAndView showIndex() {
        return new ModelAndView("index");
    }

    @PostMapping(value = "/validate", name = "ss2")
    public ModelAndView validateEmail(@RequestParam String email) {
        ModelAndView modelAndView = new ModelAndView();
        boolean isValid = validate(email);
        if (!isValid) {
            modelAndView.setViewName("index");
            modelAndView.addObject("message", "Email is invalid");
        } else {
            modelAndView.setViewName("success");
            modelAndView.addObject("email", email);
        }
        return modelAndView;
    }

    private boolean validate(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
