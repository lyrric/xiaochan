SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for location
-- ----------------------------
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location`  (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                             `user_id` int NOT NULL COMMENT '用户ID',
                             `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标识，如：公司',
                             `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地址',
                             `city_code` int NOT NULL COMMENT '城市区编码',
                             `latitude` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '纬度',
                             `longitude` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '经度',
                             `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '位置信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for monitor_config
-- ----------------------------
DROP TABLE IF EXISTS `monitor_config`;
CREATE TABLE `monitor_config`  (
                                   `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提醒规则：STORE_ACTIVITY-指定门店, MINIMUM_PAY-最小实付',
                                   `user_id` int NOT NULL COMMENT '用户ID',
                                   `location_id` bigint NULL DEFAULT NULL COMMENT '位置信息ID',
                                   `start_hour` int NOT NULL COMMENT '运行开始时间(小时)',
                                   `end_hour` int NOT NULL COMMENT '运行结束时间(小时)',
                                   `weeks` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '运行星期配置，从1开始，多个以逗号分隔，如：1,2,3,4,5,6,7',
                                   `ext_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '门店提醒扩展配置（JSON格式）',
                                   `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE-启用, DISABLE-停用',
                                   `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                   `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除, 1-已删除',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                   INDEX `idx_location_id`(`location_id` ASC) USING BTREE,
                                   INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '监控配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for store_pushed_history
-- ----------------------------
DROP TABLE IF EXISTS `store_pushed_history`;
CREATE TABLE `store_pushed_history`  (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `user_id` int NOT NULL DEFAULT 0 COMMENT '用户ID',
                                         `notify_config_id` int NOT NULL DEFAULT 0 COMMENT '通知配置ID',
                                         `notify_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知类型：STORE_ACTIVITY-指定门店, MINIMUM_PAY-最小实付',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门店名称',
                                         `store_id` int NOT NULL COMMENT '门店ID',
                                         `if_new` tinyint(1) NULL DEFAULT 0 COMMENT '是否是新店：0-否, 1-是',
                                         `open_hours` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营业时间，如 10:00-22:00',
                                         `promotion_id` int NULL DEFAULT NULL COMMENT '活动ID（同一个门店每日不同）',
                                         `type` int NULL DEFAULT NULL COMMENT '平台类型：1-美团, 2-饿了么, 3-京东',
                                         `start_time` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动开始时间，格式如 08:00',
                                         `end_time` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动结束时间，格式如 21:00',
                                         `left_number` int NULL DEFAULT NULL COMMENT '剩余数量',
                                         `distance` int NULL DEFAULT NULL COMMENT '距离，单位米',
                                         `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '满多少返',
                                         `rebate_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '返的金额',
                                         `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '门店图片URL',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                         INDEX `idx_notify_config_id`(`notify_config_id` ASC) USING BTREE,
                                         INDEX `idx_store_id`(`store_id` ASC) USING BTREE,
                                         INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
                                         INDEX `idx_promotion_id`(`promotion_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '门店推送历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for task_exec_history
-- ----------------------------
DROP TABLE IF EXISTS `task_exec_history`;
CREATE TABLE `task_exec_history`  (
                                      `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `user_id` int NOT NULL DEFAULT 0 COMMENT '用户ID',
                                      `notify_config_id` int NOT NULL DEFAULT 0 COMMENT '通知配置ID',
                                      `notify_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知类型：STORE_ACTIVITY-指定门店, MINIMUM_PAY-最小实付',
                                      `start_time` datetime NOT NULL COMMENT '开始时间',
                                      `end_time` datetime NOT NULL COMMENT '结束时间',
                                      `notify_store_count` int NOT NULL DEFAULT 0 COMMENT '通知门店数量',
                                      `success` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否成功：0-失败, 1-成功',
                                      `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                      INDEX `idx_notify_config_id`(`notify_config_id` ASC) USING BTREE,
                                      INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务执行记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
                         `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'token',
                         `spt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'spt',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `idx_token`(`token` ASC) USING BTREE,
                         UNIQUE INDEX `idx_spt`(`spt` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'user表' ROW_FORMAT = Dynamic;

-- 2026年7月10日 支持corn表达式
SET FOREIGN_KEY_CHECKS = 1;
ALTER TABLE `monitor_config`
    ADD COLUMN `cron` VARCHAR(50) NULL DEFAULT NULL COMMENT '自定义 cron 表达式'
        AFTER `weeks`;

ALTER TABLE `monitor_config`
    MODIFY COLUMN `start_hour` INT NULL COMMENT '运行开始时间',
    MODIFY COLUMN `end_hour` INT NULL COMMENT '运行结束时间',
    MODIFY COLUMN `weeks` VARCHAR(50) NULL COMMENT '运行星期配置';

-- 2026年7月23日 添加门店库存记录表
CREATE TABLE `store_inventory_history` (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                           `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门店名称',
                                           `unique_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门店唯一标识',
                                           `inventory` int NOT NULL COMMENT '库存数量',
                                           `store_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店类型',
                                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           PRIMARY KEY (`id`),
                                           KEY `idx_unique_time` (`unique_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店库存记录';

-- 2026年7月24日 添加收藏门店表
CREATE TABLE `favorite_store` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `user_id` int NOT NULL COMMENT '用户ID',
                                `location_id` bigint NOT NULL COMMENT '位置信息ID',
                                `uniq_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门店唯一标识',
                                `store_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门店类型：XC_MANJIAN、XC_MTSJ',
                                `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店图片URL',
                                `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门店名称',
                                `type` int DEFAULT NULL COMMENT '平台类型：1-美团, 2-饿了么, 3-京东',
                                `distance` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '距离',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-未删除, 1-已删除',
                                PRIMARY KEY (`id`) USING BTREE,
                                KEY `idx_user_id_location_id_store_type` (`user_id`,`location_id`,`store_type`),
                                KEY `idx_unique_id` (`uniq_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏门店表';

-- 2026年7月24日 门店库存记录增加 sku 字段
ALTER TABLE `store_inventory_history`
    ADD COLUMN `sku_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '活动id/SkuID',
    ADD COLUMN `sku_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '活动名称/Sku名称',
    ADD KEY `idx_sku_id` (`sku_id`);

-- 2026年7月29日 门店推送历史字段对齐 StoreInfo：store_id 改为 uniq_id（字符串），promotion_id 改为字符串，
-- 删除 if_new/open_hours，新增 store_type_enum/distance_str/rebate_ratio/rebate_max
ALTER TABLE `store_pushed_history`
    CHANGE COLUMN `store_id` `uniq_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门店唯一ID（storeId or wm_poi_id）',
    MODIFY COLUMN `promotion_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动ID（同一个门店每日不同）',
    DROP COLUMN `if_new`,
    DROP COLUMN `open_hours`,
    ADD COLUMN `store_type_enum` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '门店类型：XC_MANJIAN-小蚕满减, XC_MTSJ-小蚕美团赏金, WM_MANJIAN-歪卖满减, WM_MTSJ-歪卖美团赏金' AFTER `uniq_id`,
    ADD COLUMN `distance_str` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '距离自带单位，如 1.2km' AFTER `distance`,
    ADD COLUMN `rebate_ratio` decimal(10, 2) NULL DEFAULT NULL COMMENT '返现百分比（仅美团赏金）' AFTER `rebate_price`,
    ADD COLUMN `rebate_max` decimal(10, 2) NULL DEFAULT NULL COMMENT '返现最高返金额（仅美团赏金）' AFTER `rebate_ratio`,
    RENAME INDEX `idx_store_id` TO `idx_uniq_id`;

-- 2026年7月29日 门店推送历史删除 distance 字段，统一使用 distance_str
ALTER TABLE `store_pushed_history`
    DROP COLUMN `distance`;

-- 2026年7月29日 监控配置添加门店类型字段，默认小蚕满减
ALTER TABLE `monitor_config`
    ADD COLUMN `store_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'XC_MANJIAN' COMMENT '门店类型：XC_MANJIAN-小蚕满减, WM_MANJIAN-歪卖满减'
        AFTER `ext_config`;

-- 2026年7月30日 user表添加歪麦token字段，默认为空
ALTER TABLE `user`
    ADD COLUMN `waimai_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '歪麦token'
        AFTER `spt`;


-- 2026年8月6日 新增rebate_condition_str字段
ALTER TABLE `store_pushed_history`
    ADD COLUMN `rebate_condition_str` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '返现条件字符串' AFTER `uniq_id`;

-- 2026年8月7日 新增batch_id字段，用于标识批量插入的批次
ALTER TABLE `store_pushed_history`
    ADD COLUMN `batch_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批量插入批次ID（UUID去-）' AFTER `id`;

-- 2026年8月7日 新增消息批次记录表
CREATE TABLE `message_batch_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `batch_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '批次ID（多个以逗号分割，已去重）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id` ASC),
    INDEX `idx_create_time` (`create_time` ASC)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息批次记录';

-- 2026年8月7日 门店推送历史新增 location_id 字段
ALTER TABLE `store_pushed_history`
    ADD COLUMN `location_id` bigint NULL DEFAULT NULL COMMENT '位置信息ID' AFTER `notify_config_id`,
    ADD INDEX `idx_location_id` (`location_id` ASC);

