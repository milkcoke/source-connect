package offsetmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import offsetmanager.service.OffsetManagerService;


@WebMvcTest(controllers = {
  OffsetManagerController.class,
})
public abstract class ControllerTestSupport {
  @Autowired
  protected MockMvc mockMvc;

  protected ObjectMapper mapper = new ObjectMapper();

  @MockitoBean
  protected OffsetManagerService offsetManagerService;
}
