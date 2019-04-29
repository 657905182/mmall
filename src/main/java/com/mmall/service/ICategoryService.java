package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * 〈〉
 *
 * @author liu
 * @create 2019/2/27
 * @since 1.0.0
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParalleCategory(Integer categoryId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);

    //CategoryVo listCatogory(Integer categoryId);

    ServerResponse getCategory(Integer categoryId);
}
