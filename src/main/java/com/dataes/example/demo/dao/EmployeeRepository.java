package com.dataes.example.demo.dao;
 
/**
 * Created by 19130 on 2018/5/16.
 */
import com.dataes.example.demo.entity.Employee;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
/**
 * @author linzhiqiang
 */
@Component
public interface EmployeeRepository extends ElasticsearchRepository<Employee,String>{
 
    /**
     * 查询雇员信息
     * @param id
     * @return
     */
    Employee queryEmployeeById(String id);
}