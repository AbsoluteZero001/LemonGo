package com.lemongo.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

public record PageResult<T>(List<T> records, long total, long current, long size) {

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
