package com.zld.impl;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.zld.dao.P4ReadDao;
@Repository
public class P4ReadImpl extends JdbcTemplate implements P4ReadDao {
	@Autowired
	@Override
	public void setDataSource(@Qualifier("p4read")DataSource dataSource) {
		// TODO Auto-generated method stub
		super.setDataSource(dataSource);
	}
	
	@Override
	public List<Map<String, Object>> getAll(String sql, Object[] values) {
		System.err.println(sql);
		List<Map<String, Object>> list =  queryForList(sql,values);
		return list;
	}

	@Override
	public Long getLong(String sql, Object[] values) {
		return queryForLong(sql,values);
	}

	@Override
	public String getField(String sql, Object[] values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> getPage(String sql, Object[] values,
			int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getObject(String sql, Object[] values, Class type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> getPOJOList(String sql, Object[] values, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getPOJO(String sql, Object[] values, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

}
