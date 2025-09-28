package localoffsetmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import localoffsetmanager.service.RemoteOffsetService;


@WebMvcTest(controllers = {
  RemoteOffsetController.class,
})
public abstract class ControllerTestSupport {
  @Autowired
  protected MockMvc mockMvc;

  protected ObjectMapper mapper = new ObjectMapper();

  @MockitoBean
  protected RemoteOffsetService remoteOffsetService;
}
