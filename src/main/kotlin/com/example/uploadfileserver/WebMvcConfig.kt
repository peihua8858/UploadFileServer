package com.example.uploadfileserver

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.*
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.nio.charset.StandardCharsets
import java.util.*


@Configuration
class WebMvcConfig : WebMvcConfigurer {
    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.setUseSuffixPatternMatch(false).setUseTrailingSlashMatch(true)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 将templates目录下的CSS、JS文件映射为静态资源，防止Spring把这些资源识别成thymeleaf模版
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/docs/**").addResourceLocations("classpath:/docs/")
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/")
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>?>) {
        //先清掉框架自带的jackson
        converters.clear()
        converters.add(StringHttpMessageConverter(StandardCharsets.UTF_8))
    }

    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("abc-")
        executor.keepAliveSeconds = 60
        executor.initialize()
        configurer.setTaskExecutor(executor)
        //因上传数据需要时间比较久
        configurer.setDefaultTimeout(30000)
    }

    /**
     * 开启跨域
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        // 设置允许跨域的路由
        registry.addMapping("/**") // 设置允许跨域请求的域名
            .allowedOriginPatterns("*") // 是否允许证书（cookies）
            .allowCredentials(true) // 设置允许的方法
            .allowedMethods("*") // 跨域允许时间
            .maxAge(3600)
    }

    @Bean
    fun localeResolver(): LocaleResolver {
        val slr = SessionLocaleResolver()
        slr.setDefaultLocale(Locale.CHINA)
        return slr
    }

    @Bean
    fun localeChangeInterceptor(): LocaleChangeInterceptor {
        val lci = LocaleChangeInterceptor()
        lci.paramName = "lang"
        return lci
    }

    /**
     * 配置servlet处理
     */
    override fun configureDefaultServletHandling(configurer: DefaultServletHandlerConfigurer) {
        //不可加上这句，否则全局异常无效
//        configurer.enable();
    }

//    @Bean
//    fun htmlViewResolver(): ViewResolver {
//        val resolver = ThymeleafViewResolver()
//        resolver.templateEngine = templateEngine(htmlTemplateResolver())
//        resolver.contentType = "text/html; charset=UTF-8"
//        resolver.characterEncoding = "UTF-8"
//        resolver.viewNames = arrayOf("*.html")
//        return resolver
//    }
//
//    private fun templateEngine(htmlTemplateResolver: ITemplateResolver?): ISpringTemplateEngine {
//        val templateEngine=SpringTemplateEngine()
//        templateEngine.setTemplateResolver(htmlTemplateResolver)
//        return templateEngine
//    }
//
//    private  fun htmlTemplateResolver(): ITemplateResolver {
//        val resolver = SpringResourceTemplateResolver()
//        resolver.characterEncoding = "UTF-8"
//        resolver.prefix = "/templates/"
//        resolver.isCacheable = false
//        resolver.templateMode = TemplateMode.HTML
//        return resolver
//    }
}