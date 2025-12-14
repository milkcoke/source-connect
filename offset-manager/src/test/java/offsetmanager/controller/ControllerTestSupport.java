package offsetmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import offsetmanager.service.OffsetManagerService;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OffsetManagerController.class)
public abstract class ControllerTestSupport {
  @Autowired
  protected MockMvc mockMvc;

  @MockitoBean
  protected OffsetManagerService offsetManagerService;
}
