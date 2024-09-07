package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class EmulatorS3Application {
    static final XmlMapper XML_MAPPER = XmlMapper.builder()
            .findAndAddModules()
            .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
            .build();
    static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build();

    public static void main(String[] args) {
        SpringApplication.run(EmulatorS3Application.class, args);
    }

    @RestController
    static class S3Controller {
        @SneakyThrows
        @RequestMapping(
                method = {RequestMethod.GET, RequestMethod.HEAD},
                produces = MediaType.APPLICATION_XML_VALUE,
                path = "/**"
        )
        Object s3Endpoint(HttpServletRequest request,
                          @RequestParam MultiValueMap<String, String> queryString) {
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(request.getRequestURL().toString()).build();
            List<String> pathSegments = new ArrayList<>(uriComponents.getPathSegments());
            String bucket = pathSegments.removeFirst();

            String path = String.join("/", pathSegments);

            ListV2Params listV2Params = JSON_MAPPER.convertValue(queryString.toSingleValueMap(), ListV2Params.class);

            if (ListV2Params.LIST_TYPE_V2.equals(listV2Params.getListType())) {
                return XML_MAPPER.writeValueAsString(Map.of("ListBucketResult",
                        Map.of("IsTruncated", false,
                                "Contents", List.of(
                                        Map.of("ETag", "abc", "Size", 1),
                                        Map.of("ETag", "abc", "Size", 2)
                                ))
                ));
            }

            return null;
        }
    }


    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ListV2Params {
        static Integer LIST_TYPE_V2 = Integer.valueOf(2);

        @JsonProperty("list-type")
        Integer listType;

        String prefix;

        @JsonProperty("encoding-type")
        String encodingType;
    }

    @JacksonXmlRootElement(localName = "ListBucketResult")
    static class ListV2Response {
        @JacksonXmlProperty(localName = "IsTruncated")
        Boolean isTruncated;
    }
}
