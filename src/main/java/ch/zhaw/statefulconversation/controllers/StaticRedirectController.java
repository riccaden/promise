package ch.zhaw.statefulconversation.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller fuer Weiterleitungen auf statische Frontend-Seiten.
 *
 * Leitet Anfragen ohne abschliessende Datei (z.B. /monitor, /realtime)
 * auf die jeweilige index.html weiter und uebertraegt dabei Query-Parameter.
 */
@Controller
public class StaticRedirectController {

    @GetMapping({ "/monitor", "/monitor/" })
    public String monitor(HttpServletRequest request) {
        return redirectWithQuery("/monitor/index.html", request);
    }

    @GetMapping({ "/realtime", "/realtime/" })
    public String realtime(HttpServletRequest request) {
        return redirectWithQuery("/realtime/index.html", request);
    }

    private String redirectWithQuery(String target, HttpServletRequest request) {
        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return "redirect:" + target;
        }
        return "redirect:" + target + "?" + query;
    }
}
