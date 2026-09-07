package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemongo.common.api.ResultCode;
import com.lemongo.dto.ProductSaveRequest;
import com.lemongo.entity.Product;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.ProductMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public Page<Product> list(long page, long size, String category, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(Product::getDeleted, 0)
                .like(StringUtils.hasText(category), Product::getCategory, category)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(Product::getProductName, keyword)
                        .or()
                        .like(Product::getDetailText, keyword))
                .orderByDesc(Product::getSales);
        return productMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Product detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || !Integer.valueOf(1).equals(product.getStatus())
                || !Integer.valueOf(0).equals(product.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在或已下架");
        }
        return product;
    }

    public List<Product> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productMapper.selectBatchIds(ids);
    }

    public Product create(ProductSaveRequest request) {
        Product product = new Product();
        product.setProductName(request.productName());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setSales(0);
        product.setImageUrl(request.imageUrl());
        product.setDetailText(request.detailText());
        product.setStatus(1);
        product.setVersion(0);
        product.setDeleted(0);
        product.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        product.setUpdatedAt(product.getCreatedAt());
        productMapper.insert(product);
        return product;
    }

    public Product update(Long id, ProductSaveRequest request) {
        Product product = detail(id);
        product.setProductName(request.productName());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setDetailText(request.detailText());
        product.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        productMapper.updateById(product);
        return product;
    }

    public void delete(Long id) {
        Product product = detail(id);
        product.setStatus(0);
        product.setDeleted(1);
        productMapper.updateById(product);
    }
}
