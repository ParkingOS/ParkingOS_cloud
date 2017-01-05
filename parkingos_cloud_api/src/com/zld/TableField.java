package com.zld;

/**
 * 表字段
 * @author Gecko
 *
 */
public class TableField {


	private String name;
	private int fieldType;
	private int fieldLength;
	private boolean isInsertNull;
	private boolean isUpdateNull;
	
	
	public String getName() {
		return name;
	}
	public int getFieldType() {
		return fieldType;
	}
	public int getFieldLength() {
		return fieldLength;
	}
	public boolean isInsertNull() {
		return isInsertNull;
	}
	public boolean isUpdateNull() {
		return isUpdateNull;
	}
	
	public void setUpdateNull(boolean isUpdateNull) {
		this.isUpdateNull = isUpdateNull;
	}
	
	/**
	 * 
	 * @param name 字段名称
	 * @param fieldType 字段类型  ：2枚举 3浮点，4整数，5长整数 ，12字符串
	 * @param fieldLength 字段字节长度，整数时不限长度，传入0，浮点为保留小数点数，字符串为实际字符长度，一个汉字为两个字节
	 * @param isInsertNull 注册时可否为空
	 * @param isUpdateNull 更新时可否为空
	 */
	public TableField(String name, int fieldType, int fieldLength,
			boolean isInsertNull, boolean isUpdateNull) {
		super();
		this.name = name;
		this.fieldType = fieldType;
		this.fieldLength = fieldLength;
		this.isInsertNull = isInsertNull;
		this.isUpdateNull = isUpdateNull;
	}
	
}
