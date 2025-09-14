package com.sky.mapper;


import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    /**
     * 插入数据
     * @param addressBook
     */
    @Insert("insert into address_book(user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) " +
        "values(#{userId}, #{consignee}, #{sex}, #{phone}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}, #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    void insert(AddressBook addressBook);

    /**
     * 查询地址列表
     */
    @Select("select * from address_book where user_id = #{userId}")
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 查询默认地址
     */
    @Select("select * from address_book where user_id = #{userId} and is_default = 1")
    AddressBook getDefaultAddress(Long userId);

    /**
     * 根据id查询地址
     */
    @Select("select * from address_book where id = #{id}")
    AddressBook getById(Long id);

    /**
     * 修改地址
     */
    void update(AddressBook addressBook);

    /**
     * 删除地址
     */
    @Delete("delete from address_book where id = #{id}")
    void delete(Long id);

    /**
     * 清除默认地址
     */
    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void clearDefault(Long userId);
}
