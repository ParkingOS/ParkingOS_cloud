package com.zld.impl;

import com.zld.dao.DataBaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


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
		return queryForLong(sql,values);
	}

	public List getAll(String sql, Object[] values) {
		return queryForList(sql,values);
	}

	public Object getObject(String sql, Object[] values, Class type) {
		return queryForObject(sql, values,type);
	}

	public Map getPojo(String sql,Object[] values){
		List<Map> list = queryForList(sql,values);
		if(list!=null&&list.size()>0)
			return list.get(0);
		return null;
	}

	@Override
	public <T> List<T> getPOJOList(String sql, Object[] values, Class<T> type) {
		RowMapper rm = ParameterizedBeanPropertyRowMapper.newInstance(type);
		return query(sql, values, rm);
	}
	@Override
	public <T> T getPOJO(String sql, Object[] values, Class<T> type) {
		try {
			RowMapper rm = ParameterizedBeanPropertyRowMapper.newInstance(type);
			return (T)queryForObject(sql, values, rm);
		} catch (EmptyResultDataAccessException e) {//没有查到数据时抛出EmptyResultDataAccessException异常
			return null;
		}
	}

}
