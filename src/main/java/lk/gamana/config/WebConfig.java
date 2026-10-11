package lk.gamana.config;

import lk.gamana.model.Role;
import lk.gamana.security.RoleInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry reg) {
        reg.addInterceptor(new RoleInterceptor(Role.ADMIN, "/admin/login"))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");

        reg.addInterceptor(new RoleInterceptor(Role.DRIVER, "/driver/login"))
                .addPathPatterns("/driver/**")
                .excludePathPatterns("/driver/login", "/driver/register");

        reg.addInterceptor(new RoleInterceptor(Role.PASSENGER, "/login"))
                .addPathPatterns("/passenger/**");
    }
}
