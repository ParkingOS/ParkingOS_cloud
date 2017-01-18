package com.zld.pcloud_sensor.service;


public interface MemcacheService {
	/**
	 * 获取锁名称
	 * @param key
	 * @return
	 */
	public String getLock(Object key);
	
	/**
	 * 添加分布式锁,永久锁
	 * add方法，成功返回true，当数据已经存在时，返回false，应用于分布式锁
	 * @param key 锁标志
	 * @return
	 */
	public boolean lock(String key);
	
	/**
	 * 添加分布式锁,有过期时间
	 * add方法，成功返回true，当数据已经存在时，返回false，应用于分布式锁
	 * @param key 锁标志
	 * @param exp 过期时间（单位秒）
	 * @return
	 */
	public boolean lock(String key, int exp);
	
	/**
	 * 删除锁
	 * @param key
	 * @return
	 */
	public boolean delete(String key);
	
	/**
	 * set命令不但可以简单添加，如果set的key已经存在，该命令可以更新该key所对应的原来的数据，也就是实现更新的作用。
	 * @param key
	 * @param value
	 * @param exp
	 * @return
	 */
	public boolean set(String key, Object value, int exp);
	
	/**
	 * set命令不但可以简单添加，如果set的key已经存在，该命令可以更新该key所对应的原来的数据，也就是实现更新的作用。
	 * @param key
	 * @param value
	 * @param exp
	 * @return
	 */
	public boolean set(String key, Object value);
	
	/**
	 * 获取内容
	 * @param key
	 * @return
	 */
	public <T> T get(String key, Class<T> type);
	
	/**
	 * 获取内容
	 * @param key
	 * @return
	 */
	public String get(String key);
}
