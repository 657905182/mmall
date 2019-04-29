package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import com.mmall.util.JedisUtils;
import com.mmall.util.JsonUtil;
import com.mmall.vo.CategoryVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

/**
 * 〈〉
 *
 * @author liu
 * @create 2019/2/27
 * @since 1.0.0
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null && StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("参数传递错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0) {
            this.clearCategoryRedis(category.getId());
            return ServerResponse.createBySuccessMessage("添加分类成功");
        }
        return ServerResponse.createByErrorMessage("添加分类失败");
    }

    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null && StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("参数传递错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) {
            this.clearCategoryRedis(category.getId());
            return ServerResponse.createBySuccessMessage("更新分类名称成功");
        }
        return ServerResponse.createByErrorMessage("更新分类名称失败");
    }

    public ServerResponse<List<Category>> getChildrenParalleCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询节点及其子节点
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet, categoryId);
        List<Integer> categoryList = Lists.newArrayList();
        if (categoryId != null) {
            for (Category categoryItem : categorySet) {
                categoryList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归算法，找出子节点
     * @param categorySet
     * @param categoryId
     * @return
     */
    public Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : categoryList) {
            findChildCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }

    public ServerResponse getCategory(Integer categoryId) {
        Jedis j = null;
        CategoryVo categoryVo = null;
        String value = null;
        try {
            //1.从redis获取分类信息
            try {
                //1.1获取连接
                j = JedisUtils.getJedis();

                //1.2 获取数据 判断数据是否为空
                value = j.get(Const.REDIS.CATEGORY_LIST+categoryId);
                //1.3 若不为空,直接返回数据
                if (value != null) {
                    logger.info("从缓存中获取");
                    categoryVo = (CategoryVo) JsonUtil.json2Object(value, CategoryVo.class);
                    return ServerResponse.createBySuccess(categoryVo);
                }
            } catch (Exception e) {
                logger.error("与缓存服务器连接失败",e);
            }

            //2 redis中 若无数据,则从mysql数据库中获取  别忘记将数据并放入redis中
            categoryVo = this.listCatogory(categoryId);
            if (categoryVo == null) {
                return ServerResponse.createByErrorMessage("没有该类别");
            }
            value = JsonUtil.object2json(categoryVo);
            //3.将value放入redis中
            try {
                j.set(Const.REDIS.CATEGORY_LIST+categoryId, value);
                logger.info("已经将数据放入缓存中");
            } catch (Exception e) {
                logger.error("数据存入缓存失败",e);
            }
        } catch (Exception e) {
            logger.error("缓存服务器异常",e);
        } finally {
            //释放jedis
            JedisUtils.closeJedis(j);
            return ServerResponse.createBySuccess(categoryVo);
        }


    }

    private void clearCategoryRedis(Integer categoryId) {
        Jedis j =null;
        try {
            //1.从redis获取分类信息
            try {
                //1.1获取连接
                j = JedisUtils.getJedis();
                if(j.isConnected()){
                    j.del(Const.REDIS.CATEGORY_LIST+categoryId);
                }
            } catch (Exception e) {
                logger.error("更新redis失败",e);
            }
        } finally {
            //释放jedis
            JedisUtils.closeJedis(j);
        }
    }

    public CategoryVo listCatogory(Integer categoryId) {
        CategoryVo categoryVo = new CategoryVo();
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category == null && categoryId != 0) {
            return null;
        }
        if (categoryId == 0) {
            category = new Category();
            category.setId(0);
            category.setName(null);
        }
        List<CategoryVo> categoryVoChildrenList = Lists.newArrayList();
        List<Category> categoryChildrenList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryChildrenList)){
            categoryVo = this.assembleCategoryVo(category, null);
            return  categoryVo;
        }
        for (Category categoryChildren : categoryChildrenList) {
            CategoryVo categoryVoChildren = listCatogory(categoryChildren.getId());
            categoryVoChildrenList.add(categoryVoChildren);
        }
        categoryVo = this.assembleCategoryVo(category, categoryVoChildrenList);
        return categoryVo;
    }


    private CategoryVo assembleCategoryVo(Category category,List<CategoryVo> categoryVoChildrenList) {
        CategoryVo categoryVo = new CategoryVo();
        categoryVo.setId(category.getId());
        categoryVo.setName(category.getName());
        categoryVo.setCategoryVoList(categoryVoChildrenList);
        return categoryVo;
    }
}
