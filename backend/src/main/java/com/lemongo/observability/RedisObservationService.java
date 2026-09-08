package com.lemongo.observability;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Keeps lightweight realtime counters in Redis. Redis failures never break the request.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisObservationService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MINUTE = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final ZoneId ASIA_SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final StringRedisTemplate redis;

    public void recordUser(Long userId) {
        if (userId == null) {
            return;
        }
        runQuietly(() -> {
            LocalDateTime now = LocalDateTime.now(ASIA_SHANGHAI);
            String date = DATE.format(now);
            String minute = MINUTE.format(now);
            String onlineKey = "lemongo:online:" + date;
            String minuteKey = "lemongo:active:minute:" + date + ":" + userId;
            String requestKey = "lemongo:user:request:" + date + ":" + userId;

            redis.opsForZSet().add(onlineKey, String.valueOf(userId), now.atZone(ASIA_SHANGHAI).toEpochSecond());
            redis.expire(onlineKey, Duration.ofDays(2));
            redis.opsForSet().add(minuteKey, minute);
            redis.expire(minuteKey, Duration.ofDays(2));
            redis.opsForValue().increment(requestKey);
            redis.expire(requestKey, Duration.ofDays(2));
        }, "user observation");
    }

    public void recordApi(Long apiId, int success, int error) {
        runQuietly(() -> {
            String date = DATE.format(LocalDateTime.now(ASIA_SHANGHAI));
            increment("lemongo:api:request:" + date + ":" + apiId);
            if (error > 0) {
                increment("lemongo:api:error:" + date + ":" + apiId);
            }
            if (success > 0) {
                increment("lemongo:api:success:" + date + ":" + apiId);
            }
        }, "api observation");
    }

    public void recordModule(Long moduleId, int success, int error) {
        runQuietly(() -> {
            String date = DATE.format(LocalDateTime.now(ASIA_SHANGHAI));
            increment("lemongo:module:request:" + date + ":" + moduleId);
            if (error > 0) {
                increment("lemongo:module:error:" + date + ":" + moduleId);
            }
            if (success > 0) {
                increment("lemongo:module:success:" + date + ":" + moduleId);
            }
        }, "module observation");
    }

    public Long onlineUserCount() {
        try {
            long[] range = onlineWindow();
            Long count = redis.opsForZSet()
                    .count("lemongo:online:" + DATE.format(LocalDateTime.now(ASIA_SHANGHAI)),
                            range[0], range[1]);
            return count == null ? 0L : count;
        } catch (RedisConnectionFailureException ex) {
            return 0L;
        }
    }

    public Set<Long> onlineUserIds() {
        try {
            long[] range = onlineWindow();
            Set<String> members = redis.opsForZSet()
                    .rangeByScore("lemongo:online:" + DATE.format(LocalDateTime.now(ASIA_SHANGHAI)),
                            range[0], range[1]);
            if (members == null) {
                return Set.of();
            }
            return members.stream().map(Long::valueOf).collect(Collectors.toSet());
        } catch (RedisConnectionFailureException ex) {
            return Set.of();
        }
    }

    private void increment(String key) {
        redis.opsForValue().increment(key);
        redis.expire(key, Duration.ofDays(2));
    }

    private void runQuietly(Runnable action, String operation) {
        try {
            action.run();
        } catch (RedisConnectionFailureException ex) {
            log.debug("Redis unavailable while {}", operation);
        }
    }

    private long[] onlineWindow() {
        LocalDateTime now = LocalDateTime.now(ASIA_SHANGHAI);
        return new long[]{
                now.minusMinutes(5).atZone(ASIA_SHANGHAI).toEpochSecond(),
                now.atZone(ASIA_SHANGHAI).toEpochSecond()
        };
    }
}
