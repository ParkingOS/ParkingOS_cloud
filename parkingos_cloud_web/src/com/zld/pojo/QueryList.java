package com.zld.pojo;

import com.zld.service.PgOnlyReadService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 开启线程查询列表
 * @author whx
 */
public class QueryList implements Callable<List> {
	private String sql;
	private ArrayList<Object> params;
	private int pageNum;
	private int pageSize;
	private PgOnlyReadService readService;
	public QueryList(PgOnlyReadService readService, String sql,
					 ArrayList<Object> params, int pageNum, int pageSize){
		this.sql = sql;
		//这里需要用到深拷贝，否则简单的赋值只是引用的同一个对象，而getAll会改变params的值
		this.params = (ArrayList<Object>) params.clone();
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.readService = readService;
	}
	@Override
	public List call() throws Exception {
		List<Map<String, Object>> result = null;
		try {
			result = readService.getAll(sql, params, pageNum, pageSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
