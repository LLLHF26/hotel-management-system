package com.lhf.hotel.order.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_request")
public class ServiceRequest {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 发起客户ID */
    private Long customerId;

    /** 客户姓名 */
    private String customerName;

    /** 所属房间ID */
    private Long roomId;

    /** 房间号 */
    private String roomNumber;

    /** 服务类型：打扫 / 送物 / 维修 / 其他 */
    private String type;

    /** 备注 */
    private String remark;

    /** 状态：待处理 / 已处理 / 已取消 */
    private String status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理备注 */
    private String handleRemark;

    /** 处理时间 */
    private LocalDateTime handleTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
