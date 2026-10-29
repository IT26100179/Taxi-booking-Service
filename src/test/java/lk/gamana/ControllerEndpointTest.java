package lk.gamana;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControllerEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLogoutRedirectsToHome() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testPublicPages() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/landing")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
        mockMvc.perform(get("/driver/login")).andExpect(status().isOk());
        mockMvc.perform(get("/driver/register")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/login")).andExpect(status().isOk());
    }

    @Test
    void testAdminPages() throws Exception {
        mockMvc.perform(get("/admin")
                .sessionAttr("userId", "u-1001")
                .sessionAttr("role", "ADMIN")
                .sessionAttr("userName", "Super Admin"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/drivers")
                .sessionAttr("userId", "u-1001")
                .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/rides")
                .sessionAttr("userId", "u-1001")
                .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/rates")
                .sessionAttr("userId", "u-1001")
                .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/promos")
                .sessionAttr("userId", "u-1001")
                .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/api/status")
                .sessionAttr("adminUserId", "u-1001"))
                .andExpect(status().isOk());
    }

    @Test
    void testPassengerPages() throws Exception {
        mockMvc.perform(get("/passenger")
                .sessionAttr("userId", "u-1002")
                .sessionAttr("role", "PASSENGER")
                .sessionAttr("userName", "Passenger One"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/passenger/history")
                .sessionAttr("userId", "u-1002")
                .sessionAttr("role", "PASSENGER")
                .sessionAttr("userName", "Passenger One"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/passenger/api/status")
                .sessionAttr("passengerUserId", "u-1002"))
                .andExpect(status().isOk());
    }

    @Test
    void testDriverPages() throws Exception {
        mockMvc.perform(get("/driver")
                .sessionAttr("userId", "u-1003")
                .sessionAttr("role", "DRIVER")
                .sessionAttr("userName", "Kasun"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/driver/api/status")
                .sessionAttr("driverUserId", "u-1003"))
                .andExpect(status().isOk());
    }
}
