package ro.upb.sasps.smarthome.server;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "smart-home.variant=baseline")
class BaselineHttpTest extends HttpContractTest {}
