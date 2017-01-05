package com.zld.dao;

import java.util.List;
import java.util.Map;

public interface P4ReadDao {
	
	public List<Map<String, Object>> getAll(String sql,Object[] values );
	
	public Long getLong(String sql,Object[] values );
	
	public String getField(String sql,Object[] values );
	
	public List<Map<String, Object>> getPage(String sql,Object[] values, int pageNum,int pageSize);
	
	//²éÄ³Ò»×Ö¶Î
	public Object getObject(String sql,Object[] values,Class type);
	
	public <T> List<T> getPOJOList(String sql, Object[] values, Class<T> type);
	
	public <T> T getPOJO(String sql, Object[] values, Class<T> type);
}
