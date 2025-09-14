package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    /**
     * 添加地址
     * @param addressBook
     */
    void add(AddressBook addressBook);


    /**
     * 查询当前用户的所有地址
     */
    List<AddressBook> list();

    /**
     * 查询默认地址
     */
    AddressBook getDefaultAddress();

    /**
     * 根据id查询地址
     * @param id
     */
    AddressBook getById(Long id);

    /**
     * 修改地址
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 删除地址
     * @param id
     */
    void delete(Long id);

    /**
     * 设置默认地址
     */
    void setDefault(AddressBook addressBook);
}
