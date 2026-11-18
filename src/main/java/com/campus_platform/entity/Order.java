package com.campus_platform.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Order {
    private Long id;            // 订单ID
    private String orderNo;     // 订单号(UUID)
    private Long buyerId;       // 买家ID
    private Long itemId;        // 商品ID
    private BigDecimal amount;  // 订单金额
    private Integer status;     // 状态：0待支付 1已支付 2已完成
    private Date createTime;    // 下单时间
    private Date updateTime;    // 更新时间

    //补充部分：
    private String itemName;  //商品名称
    private String itemImage;  //商品图片

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemImage() { return itemImage; }
    public void setItemImage(String itemImage) { this.itemImage = itemImage; }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}