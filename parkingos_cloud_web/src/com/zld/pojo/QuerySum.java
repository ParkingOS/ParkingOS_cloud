package com.zld.pojo;

import com.zld.service.PgOnlyReadService;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

public class QuerySum implements Callable<Map> {
	private String sql;
	private ArrayList<Object> params;
	private PgOnlyReadService readService;
	public QuerySum(PgOnlyReadService readService, String sql,
					ArrayList<Object> params){
		this.sql = sql;
		this.params = params;
		this.readService = readService;
	}
	@Override
	public Map call() throws Exception {
		Map result = null;
		try {
			result = readService.getMap(sql, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
