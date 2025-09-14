package com.sky.controller.user;


import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端-地址簿接口")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 添加地址
     * @param addressBook
     */
    @PostMapping
    @ApiOperation("添加地址")
    public Result<String> add(@RequestBody AddressBook addressBook) {
        log.info("新增地址：{}", addressBook);
        addressBookService.add(addressBook);
        return Result.success();
    }

    /**
     * 查看地址列表
     */
    @GetMapping("/list")
    @ApiOperation("查看地址列表")
    public Result<List<AddressBook>> list() {
        List<AddressBook> list = addressBookService.list();
        return Result.success(list);
    }

    /**
     * 查看默认地址
     */
    @GetMapping("/default")
    @ApiOperation("查看默认地址")
    public Result<AddressBook> getDefaultAddress() {
        AddressBook addressBook = addressBookService.getDefaultAddress();
        return Result.success(addressBook);
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }
    /**
     * 修改地址
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result<String> update(@RequestBody AddressBook addressBook) {
        log.info("修改地址：{}", addressBook);
        addressBookService.update(addressBook);
        return Result.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping
    @ApiOperation("删除地址")
    public Result<String> delete(@RequestParam Long id) {
        log.info("删除地址：{}", id);
        addressBookService.delete(id);
        return Result.success();
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result<String> setDefault(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址：{}", addressBook);
        addressBookService.setDefault(addressBook);
        return Result.success();
    }
}
