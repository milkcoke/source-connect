package offsetmanager.controller

import offsetmanager.service.OffsetManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(OffsetManagerController::class)
abstract class ControllerTestSupport(

) {
  @Autowired
  protected open lateinit var mockMvc: MockMvc

  @MockitoBean
  protected open lateinit var offsetManagerService: OffsetManagerService
}
