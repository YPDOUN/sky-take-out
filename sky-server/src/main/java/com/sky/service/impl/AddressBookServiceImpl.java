package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    private static final Integer DEFAULT_ADDRESS = 1;
    private static final Integer NOT_DEFAULT_ADDRESS = 0;

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 添加地址
     * @param addressBook
     */
    @Override
    public void add(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());//设置当前用户ID

        //判断当前用户是否添加过默认地址
        Long userId = BaseContext.getCurrentId();
        AddressBook queryResult = addressBookMapper.getDefaultAddress(userId);
        if (queryResult == null) {
            addressBook.setIsDefault(DEFAULT_ADDRESS);
        } else {
            addressBook.setIsDefault(NOT_DEFAULT_ADDRESS);
        }

        addressBookMapper.insert(addressBook);
    }

    /**
     * 查询地址列表
     */
    @Override
    public List<AddressBook> list() {
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        return addressBookMapper.list(addressBook);
    }

    /**
     * 查询默认地址
     */
    @Override
    public AddressBook getDefaultAddress() {
        Long userId = BaseContext.getCurrentId();
        return addressBookMapper.getDefaultAddress(userId);
    }

    /**
     * 根据id查询地址
     */
    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        return addressBook;
    }

    /**
     * 修改地址
     */
    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    /**
     * 删除地址
     */
    @Override
    public void delete(Long id) {
        addressBookMapper.delete(id);
    }

    /**
     * 设置默认地址
     */
    @Override
    public void setDefault(AddressBook addressBook) {
        //先将当前用户的所有地址修改为非默认
        Long userId = BaseContext.getCurrentId();
        addressBookMapper.clearDefault(userId);

        //设置当前地址为默认
       addressBook = addressBookMapper.getById(addressBook.getId());
       addressBook.setIsDefault(DEFAULT_ADDRESS);
       addressBookMapper.update(addressBook);
    }
}
