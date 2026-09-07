package com.lemongo.service;

import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.TestErrorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestErrorService {

    private final TestErrorMapper testErrorMapper;

    public void badRequest() {
        throw new BusinessException(400, "模拟参数错误：商品 id 必须是正整数");
    }

    public void notFound() {
        throw new BusinessException(404, "模拟资源不存在：购物车记录已被移除");
    }

    public void serverError() {
        throw new IllegalStateException("模拟 Service 内部异常");
    }

    public void databaseError() {
        testErrorMapper.simulateDatabaseFailure();
    }

    public void serviceError() {
        throw new UnsupportedOperationException("模拟业务模块未实现操作");
    }
}
