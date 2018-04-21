package com.zldpark.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.zldpark.dao.DataBaseDao;


@Repository
public class DataBaseImpl extends JdbcTemplate implements DataBaseDao {
	@Autowired
	@Override
	public void setDataSource(@Qualifier("dataSource")DataSource dataSource) {
		// TODO Auto-generated method stub
		super.setDataSource(dataSource);
	}
	public int update(String sql, Object[] values) {
		int r = super.update(sql, values);
		if(sql.trim().substring(0, 6).toUpperCase().equals("INSERT") 
				&& r == 0){//分区表插入数据时，因为插入了字表，父表受影响行数为0
			r = 1;
		}
		return r;
	}

	public int bathInsert(String sql, List<Object[]> lists, int[] columnTypes) {
		final List<Object[]> valus = lists;
	 	final int [] argTypes = columnTypes;
		BatchPreparedStatementSetter bpss= new BatchPreparedStatementSetter(){
		      public void setValues(PreparedStatement ps, int i) throws SQLException {
		    	   Object[] obj = valus.get(i);
		  	       try{
		  	    	   for(int j = 0;j<obj.length;j++){
		  	    		   if(obj[j]==null||obj[j].toString().equals("null")||obj[j].toString().equals("")){
		  	    			   ps.setNull(j+1, argTypes[j]);
		  	    			   continue;
		  	    		   }
		  	    		   if(argTypes[j]==4){
		  	    			   ps.setLong(j+1,Long.parseLong(obj[j].toString()));
		  	    		   }else if(argTypes[j]==91){
		  	    			   ps.setDate(j+1, (Date)obj[j]);
		  	    		   }else if(argTypes[j]==3){
		  	    			   ps.setDouble(j+1, (Double)obj[j]);
		  	    		   } else {
		  	    			   ps.setString(j+1,(String)obj[j]);
		  	    		   }
		  	    	   }
		  	       } catch(Exception e){
		  	    	   e.printStackTrace();
		  	       }
		      }
		  	  public int getBatchSize(){
		  	       return valus.size();
		  	  }
	 	};
		int reslut[] = batchUpdate(sql,bpss);
		return reslut.length;
	}

	public Long getLong(String sql, Object[] values) {
		return queryForObject(sql,values,Long.class);
	}

	public List getAll(String sql, Object[] values) {
		return queryForList(sql,values);
	}

	public Object getObject(String sql, Object[] values, Class type) {
		return queryForObject(sql, values,type);
	}
	
	public Map getPojo(String sql,Object[] values){
		List<Map<String,Object>> list = queryForList(sql,values);
		if(list!=null&&list.size()>0)
			return list.get(0);
		return null;
	}

}
