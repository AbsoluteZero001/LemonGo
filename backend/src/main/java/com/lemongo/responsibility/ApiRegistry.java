package com.lemongo.responsibility;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemongo.entity.SysApi;
import com.lemongo.entity.SysDeveloper;
import com.lemongo.entity.SysModule;
import com.lemongo.mapper.SysApiMapper;
import com.lemongo.mapper.SysDeveloperMapper;
import com.lemongo.mapper.SysModuleMapper;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Loads the API -> module -> developer ownership graph and resolves each request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiRegistry {

    private final SysApiMapper apiMapper;
    private final SysModuleMapper moduleMapper;
    private final SysDeveloperMapper developerMapper;
    private final Map<String, Responsibility> responsibilityMap = new HashMap<>();

    @PostConstruct
    public void refresh() {
        List<SysApi> apis = apiMapper.selectList(
                new LambdaQueryWrapper<SysApi>().eq(SysApi::getStatus, 1));
        Map<Long, SysModule> modules = new HashMap<>();
        moduleMapper.selectList(null)
                .forEach(module -> modules.put(module.getId(), module));
        Map<Long, SysDeveloper> developers = new HashMap<>();
        developerMapper.selectList(null)
                .forEach(developer -> developers.put(developer.getId(), developer));

        responsibilityMap.clear();
        for (SysApi api : apis) {
            SysModule module = modules.get(api.getModuleId());
            SysDeveloper developer = developers.get(api.getDeveloperId());
            if (module == null || developer == null) {
                log.warn("API {} has missing module or developer", api.getApiPath());
                continue;
            }
            responsibilityMap.put(key(api.getHttpMethod(), api.getApiPath()),
                    new Responsibility(api, module, developer));
        }
        log.info("Loaded {} API ownership entries", responsibilityMap.size());
    }

    public Responsibility resolve(String httpMethod, String requestUri) {
        Responsibility exact = responsibilityMap.get(key(httpMethod, requestUri));
        if (exact != null) {
            return exact;
        }
        String templateUri = requestUri.replaceAll("(/[0-9]+)", "/{id}");
        Responsibility template = responsibilityMap.get(key(httpMethod, templateUri));
        if (template != null) {
            return template;
        }
        if (requestUri.endsWith("/")) {
            return resolve(httpMethod, requestUri.substring(0, requestUri.length() - 1));
        }
        return null;
    }

    private String key(String httpMethod, String path) {
        return httpMethod.toUpperCase() + " " + path;
    }

    public record Responsibility(
            SysApi api,
            SysModule module,
            SysDeveloper developer) {
    }
}
