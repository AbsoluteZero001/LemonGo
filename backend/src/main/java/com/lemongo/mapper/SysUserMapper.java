package com.lemongo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemongo.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Update("""
            UPDATE sys_user
            SET last_visit_time = NOW(),
                last_active_time = NOW(),
                activity_score = LEAST(100, activity_score + 1)
            WHERE id = #{userId}
            """)
    int touchActivity(@Param("userId") Long userId);

    @Update("""
            UPDATE sys_user
            SET nickname = #{nickname},
                email = #{email},
                phone = #{phone},
                updated_at = NOW()
            WHERE id = #{userId}
            """)
    int updateProfile(
            @Param("userId") Long userId,
            @Param("nickname") String nickname,
            @Param("email") String email,
            @Param("phone") String phone);

    @Update("""
            UPDATE sys_user
            SET online_status = 1,
                last_visit_time = NOW(),
                last_active_time = NOW()
            WHERE id = #{userId}
            """)
    int touchPresence(@Param("userId") Long userId);

    @Update("""
            UPDATE sys_user
            SET online_status = 0
            WHERE id = #{userId}
            """)
    int markOffline(@Param("userId") Long userId);
}
