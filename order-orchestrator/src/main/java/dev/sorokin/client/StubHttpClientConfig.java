package dev.sorokin.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;

@Configuration
public class StubHttpClientConfig {

    @Bean
    public StubHttpClient stubHttpClient(
            RestClient.Builder builder,
            StubClientProperties props,
            ClientHttpRequestFactory jdkRequestFactory
    ) {
        RestClient restClient = builder
                .baseUrl(props.getBaseUrl())
                .requestFactory(jdkRequestFactory)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(StubHttpClient.class);
    }

    @Bean
    public ClientHttpRequestFactory jdkRequestFactory(
            StubClientProperties properties
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return requestFactory;
    }

}
