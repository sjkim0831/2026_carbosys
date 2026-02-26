package egovframework.com.uat.uia.web;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/saas/v2.0/kcert/v1.0")
public class EgovSimpleAuthController {

    @PostMapping("/token")
    public Map<String, Object> getToken(@RequestBody(required = false) Map<String, Object> requestParams) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "성공");
        response.put("access_token", UUID.randomUUID().toString());
        response.put("expires_in", 3600);
        response.put("token_type", "Bearer");
        return response;
    }

    @PostMapping("/prepare")
    public Map<String, Object> prepare(@RequestBody(required = false) Map<String, Object> requestParams) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "성공");
        response.put("tx_id", UUID.randomUUID().toString());
        response.put("one_time_token", UUID.randomUUID().toString());
        return response;
    }
}
