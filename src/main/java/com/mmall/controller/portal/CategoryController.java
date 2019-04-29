package com.mmall.controller.portal;

import com.mmall.common.ServerResponse;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 〈〉
 *
 * @author liu
 * @create 2019/3/11
 * @since 1.0.0
 */
@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "categoryId", defaultValue = "0")Integer categoryId){

        return iCategoryService.getCategory(categoryId);

    }
}
