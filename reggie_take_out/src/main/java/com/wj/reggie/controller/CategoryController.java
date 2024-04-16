package com.wj.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wj.reggie.common.R;
import com.wj.reggie.entity.Category;
import com.wj.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author wj
 * @version 1.0
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新規分類
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新規分類に成功しました");
    }
    /**
     * ページネーションクエリ
     * @param page
     * @param pageSize
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
// ページネーションオブジェクトの生成
        Page<Category> pageInfo=new Page<>(page,pageSize);
        // 条件構築オブジェクト
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
// ソート条件の追加：sortで昇順ソート
        queryWrapper.orderByAsc(Category::getSort);
    // ページネーションクエリ実行
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);

    }

    /**
     *  IDに基づいて分類を削除する
     * @param id
     * @return
     */

    @DeleteMapping
    public R<String> delete(Long id){
        log.info("分類を削除します、ID：{}",id);

        categoryService.remove(id);
        return R.success("分類情報が削除されました");
    }
//IDに基づいて分類情報を更新します
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("分類情報を更新します：{}",category);
        categoryService.updateById(category);
        return R.success("分類情報を更新しました");
    }
}
