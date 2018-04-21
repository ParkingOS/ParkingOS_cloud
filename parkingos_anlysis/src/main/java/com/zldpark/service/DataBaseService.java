package com.zldpark.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zldpark.dao.DataBaseDao;
import com.zldpark.utils.StringUtils;


@Service
public class DataBaseService {
	private Logger logger = Logger.getLogger(DataBaseService.class);

	@Autowired
	private DataBaseDao databasedao;
	

	public List getAll(String sql,Object[] values ){
		System.err.println("params:"+StringUtils.objArry2String(values));
		return databasedao.getAll(sql, values);
	}
	
	public List getAllMap(String sql,List<Object> values ){
		Object[] objects= null;
		if(values!=null&&values.size()>0){
			objects=new Object[values.size()];
			for(int i=0;i<objects.length;i++){
				objects[i]=values.get(i);
			}
		}
		return databasedao.getAll(sql, objects);
	}
	
	public List getAll(String sql,List<Object> values,int pageNum,int pageSize ){
		if(values==null)
			values= new ArrayList<Object>();
		if(pageSize>0){
			int end = pageSize;
			int start = (pageNum - 1) * pageSize;
			sql =sql +" limit ? offset ?";
			
			values.add(end);
			values.add(start);
		}
		Object[] objects= null;
		if(values.size()>0){
			objects=new Object[values.size()];
			for(int i=0;i<objects.length;i++){
				objects[i]=values.get(i);
			}
		}
		return databasedao.getAll(sql, objects);
	}
	
	public Map getPojo(String sql,Object[] values){
		Map object = null;
		try {
			object = databasedao.getPojo(sql+" order by id desc limit 1 ", values);
		} catch (Exception e) {
			logger.error("查询错误，没有记录：sql:"+sql+":params:"+StringUtils.objArry2String(values));
		}
		return object;
	}
	
	public Map getMap(String sql,Object[] values){
		Map object = null;
		try {
			object = databasedao.getPojo(sql, values);
		} catch (Exception e) {
			logger.error("查询错误，没有记录：sql:"+sql+":params:"+StringUtils.objArry2String(values));
		}
		return object;
	}
	public Map getMap(String sql,List values){
		Object[] valObjects =null;
		if(values!=null){
			valObjects= new Object[values.size()];
			for(int i=0;i<values.size();i++){
				valObjects[i]=values.get(i);
			}
		}
		return getMap(sql, valObjects);
	}
	
	
	public int update(String sql,Object[] values){
		return databasedao.update(sql, values);
	}
	
	public int update(String sql,List values){
		Object [] _values =null;
		if(values!=null&&values.size()>0){
			_values=new Object[values.size()];
			for(int i=0;i<values.size();i++){
				_values[i]=values.get(i);
			}
		}
		//System.err.println(sql+",params:"+StringUtils.objArry2String(_values));
		//return 0;
		return databasedao.update(sql, _values);
	}
	
	public int updateParamList(String sql,List<Object> values){
		Object[] valObjects =null;
		if(values!=null){
			valObjects= new Object[values.size()];
			for(int i=0;i<values.size();i++){
				valObjects[i]=values.get(i);
			}
		}
		return databasedao.update(sql, valObjects);
	}
	
	public boolean bathUpdate(List params){
		boolean r = true;
		for(int i=0;i<params.size();i++){
			Map map = (Map)params.get(i);
			String sql = map.get("sql")+"";
			Object[] values = (Object[])map.get("values");
			int result = databasedao.update(sql, values);
			if(result!=1){
				r = false;
				break;
			}
		}
		return r;
	}

	public boolean bathUpdate2(List params){
		boolean r = true;
		for(int i=0;i<params.size();i++){
			Map map = (Map)params.get(i);
			String sql = map.get("sql")+"";
			Object[] values = (Object[])map.get("values");
			int result = databasedao.update(sql, values);
			if(result < 0){
				r = false;
				break;
			}
		}
		return r;
	}
	
	public boolean bathUpdate_order_3(List params){
		boolean r = true;
		for(int i=0;i<params.size();i++){
			Map map = (Map)params.get(i);
			String sql = map.get("sql")+"";
			Object[] values = (Object[])map.get("values");
			int result = databasedao.update(sql, values);
			if(result < 0){
				r = false;
				break;
			}
		}
		return r;
	}
	
	public int bathInsert(String sql,List<Object[]> lists,int[] argTypes){
		return databasedao.bathInsert(sql, lists, argTypes);
	}
	
	public Long getLong(String sql,Object[] values){
		return databasedao.getLong(sql, values);
	}
	
	public Object getObject(String sql ,Object[] values ,Class type){
		Object object = null;
		try {
			object = databasedao.getObject(sql+" order by id desc limit 1", values, type);
		} catch (Exception e) {
			logger.error("查询错误，没有记录：sql:"+sql+":params:"+StringUtils.objArry2String(values));
		}
		return object;
	}
	
	public Long getCount(String sql,List<Object> values){
		Object[] valObjects =null;
		if(values!=null){
			valObjects= new Object[values.size()];
			for(int i=0;i<values.size();i++){
				valObjects[i]=values.get(i);
			}
		}
		return getLong(sql, valObjects);
	}
}
