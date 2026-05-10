package ${package.BaseEntity};

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
* <p>
* 基础实体类，包含公共字段
* </p>
*
* @author ${author}
* @since ${date}
*/
@Getter
@Setter
public class BaseEntity {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑删除(0-正常 1-删除)
     */
    @Schema(description = "逻辑删除(0-正常 1-删除)")
    @TableLogic
    @TableField("is_deleted")
    private Byte deleted;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    @TableField("create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(value = "create_at", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    @TableField("update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField(value = "update_at", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}