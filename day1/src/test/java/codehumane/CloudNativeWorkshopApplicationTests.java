package codehumane;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CloudNativeWorkshopApplication.class)
@WebAppConfiguration
public class CloudNativeWorkshopApplicationTests {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Before
	public void before() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
				.build();
	}

	@Test
	public void contextLoads() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/reservations"))
				.andExpect(MockMvcResultMatchers.content().contentType(
				        MediaType.parseMediaType("application/hal+json;charset=UTF-8")))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

}
