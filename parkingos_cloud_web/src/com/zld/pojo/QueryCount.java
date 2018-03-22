package com.zld.pojo;

import com.zld.service.PgOnlyReadService;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * 开启线程查询数量
 * @author whx
 */
public class QueryCount implements Callable<Long> {
	private String sql;
	private ArrayList<Object> params;
	private PgOnlyReadService readService;
	public QueryCount(PgOnlyReadService readService, String sql,
					  ArrayList<Object> params){
		this.sql = sql;
		this.params = params;
		this.readService = readService;
	}
	@Override
	public Long call() throws Exception {
		Long result = null;
		try {
			result = readService.getCount(sql, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
