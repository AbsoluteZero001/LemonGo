package com.lemongo.vo;

import com.lemongo.entity.ErrorLog;
import com.lemongo.entity.RequestLog;
import java.util.List;

public record RequestDetailVo(
        RequestLog requestLog,
        List<TraceLayerVo> layers,
        ErrorLog errorLog) {

    public record TraceLayerVo(
            String layerType,
            String layerName,
            String layerMethod,
            String description) {
    }
}
