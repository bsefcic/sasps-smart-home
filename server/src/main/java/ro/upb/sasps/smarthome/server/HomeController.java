package ro.upb.sasps.smarthome.server;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.upb.sasps.smarthome.contracts.HomeSnapshot;

@RestController
@RequestMapping("/api/v1")
public final class HomeController {
  private final HomeSession session;
  private final RequestMapper mapper;

  public HomeController(HomeSession session, RequestMapper mapper) {
    this.session = session;
    this.mapper = mapper;
  }

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "UP");
  }

  @GetMapping("/home")
  public HomeSnapshot home() {
    return session.snapshot();
  }

  @PostMapping(value = "/commands", consumes = "application/json")
  public ExecutionResponse execute(@RequestBody JsonNode body) {
    return session.execute(mapper.command(body));
  }

  @PostMapping(value = "/scenes/execute", consumes = "application/json")
  public ExecutionResponse executeScene(@RequestBody JsonNode body) {
    return session.executeScene(mapper.scene(body));
  }
}
