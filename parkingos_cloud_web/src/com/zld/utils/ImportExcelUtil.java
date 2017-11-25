/**
 * @author drh
 * Excel数据导入数据库
 * @version 1.0
 */
package com.zld.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class ImportExcelUtil {

	/**
	 * 操作Excel表格的功能类
	 */


	/**
	 * 导入报表Excel数据，生成用户表的数据库导入语句
	 *        String formFileName：上传的文件名（获取后缀）判断是2007（.xlsx）还是2003（.xls）
	 *        int isTitle:是否有标题，有则是1，无则0
	 * @return ArrayList<Object[]>
	 * @throws Exception,Set<String> set
	 */
	public static ArrayList<Object[]> generateUserSql(InputStream in,String formFileName,int isTitle)
			throws Exception {
        Workbook wb;
        Sheet sheet;
        Row row;
		//FileInputStream in = null;
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		//Map<String, String> localMap = readLocal();
		//Set set =new HashSet<String>();
		try {

			//in = new FileInputStream(formFile);//将文件读入到输入流中

			//从输入流中获取WorkBook对象，加载选中的excel文件
			String suffix = formFileName.substring(formFileName.lastIndexOf("."));  // 文件后辍.

			//支持office2007
			if (".xlsx".equals(suffix.toLowerCase())) {
				wb = new XSSFWorkbook(in);
			}
			else{
				//支持office2003
				wb = new HSSFWorkbook(in);
			}

			for (int i=0; i<wb.getNumberOfSheets(); i++) {//获取每个Sheet表
				sheet = wb.getSheetAt(i);
				if(sheet!=null){
					int count = i+1;
					System.err.println(">>>>>文件行数 ："+sheet.getPhysicalNumberOfRows());
					for (int j=isTitle; j<sheet.getPhysicalNumberOfRows(); j++) {//获取每行，j=isTitle表示从第j行开始获取数据
//	            		 Object[] valStr = new String[row.getPhysicalNumberOfCells()];//用数组来存放每一行的数据，9表示每一行的数据不能超过9，可以<=9
						ArrayList<Object> arrayList = new ArrayList<Object>();
						row = sheet.getRow(j);
						StringBuffer str = new StringBuffer();

						//System.out.println("第"+j+"行：长度："+row.getPhysicalNumberOfCells()+",getLastCellNum:"+row.getLastCellNum());
						for (int k=0; k<row.getLastCellNum(); k++) {//获取每个单元格
							Cell cell = row.getCell(k);
							if((k<1||k>2))
								if(cell!=null)
									cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							String content = getCellFormatValue(cell).trim();
							arrayList.add(content);
						}
						//调整字段顺序，避免出现org.postgresql.util.PSQLException: 未设定参数值 *的内容。
//						if(arrayList.size()<4){
//							continue;
//						}

						//company_name,parking_type,address,city,parking_total,longitude,latitude,create_time,update_time,state,type,mobile,remarks,chanid,groupid
						System.out.println(arrayList);
						Object[] values = arrayList.toArray();
						list.add(values);
					}
				}
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 根据HSSFCell类型设置数据
	 * @param cell
	 * @return
	 */
	private static String getCellFormatValue(Cell cell) {
		String cellvalue = "";
		if (cell != null) {
			// 判断当前Cell的Type
            //System.out.println(cell.getCellType());

			switch (cell.getCellType()) {
				// 如果当前Cell的Type为NUMERIC
				case HSSFCell.CELL_TYPE_NUMERIC:
//                    cellvalue = String.valueOf(cell.getNumericCellValue());
//                    System.out.println(cellvalue);
//                    if(cellvalue.length()>7){
//                        //方法2：这样子的data格式是不带带时分秒的：2011-10-12
//                        Date date = cell.getDateCellValue();
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                        cellvalue = sdf.format(date);
//                    }else if(cellvalue.indexOf(".")!=-1){
//                        cellvalue =cellvalue.substring(0,cellvalue.indexOf("."));
//                    }
//                    break;
				case HSSFCell.CELL_TYPE_FORMULA: {
					// 判断当前的cell是否为Date
					if (true){
						//	HSSFDateUtil.isCellDateFormatted(cell)) {
						// 如果是Date类型则，转化为Data格式

						//方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
						//cellvalue = cell.getDateCellValue().toLocaleString();

						//方法2：这样子的data格式是不带带时分秒的：2011-10-12
						Date date = cell.getDateCellValue();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						cellvalue = sdf.format(date);

					}
					// 如果是纯数字
					else {
						// 取得当前Cell的数值
						cellvalue = String.valueOf(cell.getNumericCellValue());
					}
					break;
				}
				// 如果当前Cell的Type为STRIN
				case HSSFCell.CELL_TYPE_STRING:
					// 取得当前的Cell字符串
					cellvalue = cell.getRichStringCellValue().getString();
					break;
				// 默认的Cell值
				default:
					cellvalue = " ";
			}
		} else {
			cellvalue = "";
		}
		return cellvalue;

	}




	public static void main(String[] args) {
		try {
//			System.err.println(readLocal().size());
			File file = new File("C:\\test.xlsx");
			ArrayList<Object[]> values = generateUserSql(new FileInputStream(file), "test.xlsx", 1);
			System.out.println(values);
			//String sql = "insert into com_info_tb(company_name,resume,create_time,longitude,latitude,type,state,city) values(?,?,?,?,?,?,?,?)";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
