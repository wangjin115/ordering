package com.wj.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wj.reggie.common.R;
import com.wj.reggie.entity.Employee;
import com.wj.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @author wj
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * ログイン
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1 ページで送信されたパスワードをmd5で暗号化する。
        String password=employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());

        //2 ページで送信されたユーザー名に基づいてデータベースに問い合わせる。
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3 問い合わせがない場合はログイン失敗を返す
        if(emp == null){
            return R.error("ログイン失敗");
        }

        //4 パスワードの比較、食い違う場合はログイン失敗を返す
        if(!emp.getPassword().equals(password)){
            return R.error("ログイン失敗");
        }

        //5 従業員のステータスをチェックし、ステータスが無効の場合はアカウント無効を返す
        if(emp.getStatus()==0){
            return R.error("アカウント無効");
        }

        //6 ログインに成功し、従業員IDをセッションに保存し、ログイン成功を返す。
        request.getSession().setAttribute("employee",emp.getId());

        //セッションメカニズムを通じて、
        // サーバーは異なるリクエスト間でユーザーの状態を維持し、
        // ユーザー認証や権限管理などの機能を実装することができる。
        return R.success(emp);

    }

    /**
     * logout
     * @param request
     * @return
     */
    @PostMapping("/logout")

    public R<String> logout(HttpServletRequest request){
    //セッションに保存されている、現在ログインしている従業員のIDをクリアする。
        request.getSession().removeAttribute("employee");

        return R.success("ログアウト");
    }

    /**
     * 従業員を追加する
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("従業員を追加すると、従業員情報は{}",employee.toString());

        ///初期パスワード123456を設定し、md5暗号化処理が必要
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        // 現在ログインしているユーザーのIDを取得する
//
//        Long empId=(Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新規従業員を成功に増加する");
    }

    /**
     * 员工信息分页查询
     * 従業員情報のページング検索
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        // ログ出力: ページ、ページサイズ、名前
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //构造分页构造器，它表示了分页信息，包括当前页码和每页大小。
        //ページ情報の生成: 現在のページ番号とページサイズを指定
        Page pageInfo=new Page(page,pageSize);

        //构造查询条件的构造器
//LambdaQueryWrapper 是 MyBatis-Plus 提供的一个查询条件构造器，它能够方便地构造复杂的查询条件。
        // クエリ条件の構築
        // LambdaQueryWrapper: MyBatis-Plusが提供するクエリ条件ビルダーで、複雑なクエリ条件を簡単に構築できる
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper();
        //添加一个过滤条件
       //这是在查询条件中添加了一个模糊查询条件。
        // StringUtils.isNotEmpty(name) 判断了传入的姓名是否为空，如果不为空则添加模糊查询条件，使用了 like 方法进行模糊匹配。
// フィルタ条件の追加
        // StringUtils.isNotEmpty(name)で渡された名前が空でない場合、部分一致クエリ条件を追加。likeメソッドを使用して部分一致を行う。
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);

        //这是在查询条件中添加了一个降序排序条件。Employee::getUpdateTime 表示按照 Employee 类中的 updateTime 属性进行降序排序。
//// 降順ソート条件の追加: EmployeeクラスのupdateTimeプロパティで降順ソート
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //这是执行查询操作，调用了 employeeService 的 page 方法，传入了分页信息和查询得到的信息，执行数据库查询操作，结果赋值给pageInfo
        // クエリの実行: employeeServiceのpageメソッドを呼び出し、ページ情報とクエリ条件を渡す。データベースのクエリ操作が実行され、結果がpageInfoに設定される。
       employeeService.page(pageInfo,queryWrapper);
//pageInfo是一个对象引用，它指向一个Page对象，该对象在内存中存在。当employeeService.page方法执行后，它会修改这个对象的状态，也就是说，它会将查询结果设置到这个对象中。
//        返回查询结果，将查询得到的分页信息 pageInfo 封装到 R 对象中，并返回给调用方。
        // pageInfoはPageオブジェクトへの参照であり、メモリ内に存在する。employeeService.pageメソッドが実行されると、このオブジェクトの状態が変更される。つまり、このメソッドはクエリ結果をこのオブジェクトに設定する。
        // クエリ結果を返す: ページ情報pageInfoをRオブジェクトにラップし、呼び出し元に返す。
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * IDに基づいて従業員情報を変更する
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        long id=Thread.currentThread().getId();
        log.info("线程id为：{}",id);
//        Long empId = (Long)request.getSession().getAttribute("employee");
//
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("従業員情報が更新された");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("IDによる従業員情報の照会");
        Employee employee =employeeService.getById(id);
        if (employee!=null){
            return R.success(employee);
        }
        return R.error("対応する従業員情報が問合せされません");

    }

}
