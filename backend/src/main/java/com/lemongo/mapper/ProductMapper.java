package com.lemongo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemongo.entity.Product;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    @Update("""
            UPDATE product
            SET stock = stock - #{quantity},
                sales = sales + #{quantity},
                version = version + 1,
                updated_at = NOW()
            WHERE id = #{productId} AND stock >= #{quantity} AND deleted = 0
            """)
    int reduceStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Select("""
            SELECT DISTINCT category
            FROM product
            WHERE deleted = 0 AND status = 1
            ORDER BY category
            """)
    List<String> selectCategories();
}
