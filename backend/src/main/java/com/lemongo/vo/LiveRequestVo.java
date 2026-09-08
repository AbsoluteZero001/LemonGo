package com.lemongo.vo;

import com.lemongo.entity.RequestLog;
import java.util.List;

public record LiveRequestVo(
        RequestLog requestLog,
        List<RequestDetailVo.TraceLayerVo> layers) {
}
