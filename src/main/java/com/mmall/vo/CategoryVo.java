package com.mmall.vo;

import java.util.List;

/**
 * 〈〉
 *
 * @author liu
 * @create 2019/3/11
 * @since 1.0.0
 */
public class CategoryVo {
    private Integer id;
    private String name;
    private List<CategoryVo> categoryVoList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CategoryVo> getCategoryVoList() {
        return categoryVoList;
    }

    public void setCategoryVoList(List<CategoryVo> categoryVoList) {
        this.categoryVoList = categoryVoList;
    }
}
