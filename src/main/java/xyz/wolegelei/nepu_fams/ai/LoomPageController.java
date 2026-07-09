package xyz.wolegelei.nepu_fams.ai;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoomPageController {

    @GetMapping("/spring/ai/loom")
    public String loomIndex() {
        return "redirect:/spring/ai/loom/index.html";
    }
}
