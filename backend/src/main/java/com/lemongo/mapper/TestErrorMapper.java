package com.lemongo.mapper;

import org.apache.ibatis.annotations.Select;

public interface TestErrorMapper {

    @Select("""
            SELECT id FROM missing_table_for_error_demo LIMIT 1
            """)
    Object simulateDatabaseFailure();
}
