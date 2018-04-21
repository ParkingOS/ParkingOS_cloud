package com.zldpark.impl;

import com.zldpark.dao.PgOnlyReadDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PgOnlyReadImpl extends JdbcTemplate implements PgOnlyReadDao {
	
	@Autowired
	@Override
	public void setDataSource(@Qualifier("dataOnlyRead")DataSource dataSource) {
		// TODO Auto-generated method stub
		super.setDataSource(dataSource);
	}
	public List getAll(String sql,Object[] values ) {
		List list = queryForList(sql,values);
		return list;
	}
	
	public Long getLong(String sql,Object[] values ){
		return queryForObject(sql,values,Long.class);
		
	}
	public String getField(String sql, Object[] values) {
		queryForObject(sql, values, String.class);
		return null;
	}
	public List getPage(String sql, Object[] values, int pageNum, int pageSize) {
		int end = pageSize;
		int start = (pageNum - 1) * pageSize;
		sql =sql +" limit ? offset ?";
		int length =2;
		
		Object [] _params=null;
		if(values!=null){
			length= values.length+2;
			_params=new Object[length];
			for(int i=0;i<values.length;i++){
				_params[i]=values[i];
			}
			_params[length-2]=end;
			_params[length-1]=start;
		}else {
			_params=new Object[2];
			_params[0]=end;
			_params[1]=start;
		}
		return getAll(sql, _params);
	}
	public Object getObject(String sql, Object[] values, Class type) {
		return queryForObject(sql, values,type);
	}
	
	public <T> List<T> getPOJOList(String sql, Object[] values, Class<T> type){
		RowMapper rm = BeanPropertyRowMapper.newInstance(type);
		return query(sql, values, rm);
	}
	
}
