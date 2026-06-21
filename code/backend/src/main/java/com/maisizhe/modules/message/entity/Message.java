package com.maisizhe.modules.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类
 * 
 * @author MaiSiZhe Team
 */
@Data
@TableName("im_message")
public class Message {
    
    /**
     * 消息ID(雪花算法生成)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 会话ID(私聊:p_uid1_uid2, 群聊:g_gid)
     */
    @TableField("chat_id")
    private String chatId;
    
    /**
     * 会话内序列号(连续递增)
     */
    @TableField("seq_id")
    private Long seqId;
    
    /**
     * 发送者ID(0表示AI机器人)
     */
    @TableField("from_uid")
    private Long fromUid;
    
    /**
     * 接收者ID(私聊时有效)
     */
    @TableField("to_uid")
    private Long toUid;
    
    /**
     * 群组ID(群聊时有效)
     */
    @TableField("group_id")
    private Long groupId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型:0-文本,1-图片,2-文件,3-AI摘要,99-AI思考中
     */
    @TableField("msg_type")
    private Integer msgType;
    
    /**
     * 消息状态:0-发送中,1-已发送,2-已送达,3-已读,4-发送失败
     */
    @TableField("msg_status")
    private Integer msgStatus;
    
    /**
     * @的用户ID列表(JSON数组)
     */
    @TableField("mentioned_users")
    private String mentionedUsers;
    
    /**
     * 是否已撤回:0-否,1-是
     */
    @TableField("is_recalled")
    private Integer isRecalled;
    
    /**
     * 撤回时间
     */
    @TableField("recall_time")
    private LocalDateTime recallTime;
    
    /**
     * 扩展数据(JSON格式)
     */
    @TableField("extra_data")
    private String extraData;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除:0-未删除,1-已删除
     */
    @TableField("deleted")
    private Integer deleted;
}
