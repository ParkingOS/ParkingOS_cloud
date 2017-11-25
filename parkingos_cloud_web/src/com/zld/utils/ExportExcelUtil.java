package com.zld.utils;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.*;
import jxl.write.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class ExportExcelUtil {

	static Logger logger = Logger.getLogger(ExportExcelUtil.class);

	public  String excelName="报表";
	public  String[] headBody = null;
	public  List<List<String>> bodyList = null;
	public  List<Map<String,String>> mulitHeadList =null;
	public Map<String,String> headInfo=null;
	/**
	 *
	 * @param excelName 文件名
	 * @param headBody 表头
	 * @param bodyList 内容
	 * @param isEncrypt 电话是否加密
	 */
	public ExportExcelUtil(String excelName,String[] headBody,List<List<String>> bodyList){
		this.excelName=excelName;
		this.headBody = headBody;
		this.bodyList = bodyList;
	}
	public void createExcelFile(OutputStream os) throws IOException {
		try {
			//创建一个文件
			WritableWorkbook workbook = Workbook.createWorkbook(os);
			//使用第一张工作表
			WritableSheet sheet = workbook.createSheet(excelName, 0);
			CellView cellView = new CellView();
			//cellView.setAutosize(true); //设置自动大小
			cellView.setSize(4050);

			//创建多表头
	        /*
	         *  通过writablesheet.mergeCells(int x,int y,int m,int n);来实现的。
 				表示将从第x+1列，y+1行到m+1列，n+1行合并 (四个点定义了两个坐标，左上角和右下角)
 				结果是合并了m-x+1行，n-y+1列，两者乘积就是合并的单元格数量。
	         */
			Integer start =0;
			if(headInfo!=null&&!headInfo.isEmpty()){
				start++;
				Integer length  = Integer.valueOf(headInfo.get("length"));
				sheet.mergeCells(0, 0, length, 0);
				WritableFont font = new WritableFont(WritableFont.ARIAL,14,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);

				WritableCellFormat wc = new WritableCellFormat(font);
				// 设置居中
				wc.setAlignment(Alignment.CENTRE);
				// 设置边框线
				wc.setBorder(Border.ALL, BorderLineStyle.THIN);

				// 设置单元格的背景颜色
				//  wc.setBackground(jxl.format.Colour.YELLOW);
				Label cell= new Label(0, 0,headInfo.get("content"),wc);

				sheet.addCell(cell);
			}
			if(mulitHeadList!=null){
				Integer preKey =0;
				for(Map<String,String> map :mulitHeadList){
					Integer length  = Integer.valueOf(map.get("length"));
					sheet.mergeCells(preKey, start, preKey+length,start);
					WritableCellFormat wc = new WritableCellFormat();
					// 设置居中
					wc.setAlignment(Alignment.CENTRE);
					// 设置边框线
					wc.setBorder(Border.ALL, BorderLineStyle.THIN);
					// 设置单元格的背景颜色
					wc.setBackground(jxl.format.Colour.YELLOW);
					Label cell= new Label(preKey,start,map.get("content"),wc);

					sheet.addCell(cell);
					preKey += length+1;
				}
				start++;
			}
			//创建表头
			for (int i=0;i<headBody.length;i++) {
				//三个参数分别表示col+1列，row+1行，标题内容是title。
				WritableCellFormat wc = new WritableCellFormat();
				// 设置居中
				wc.setAlignment(Alignment.CENTRE);
				// 设置边框线
				wc.setBorder(Border.ALL, BorderLineStyle.THIN);
				wc.setBackground(jxl.format.Colour.GRAY_25);
				wc.isShrinkToFit();
				Label cell= new Label(i, start, headBody[i],wc);
				//cellView.setSize(headBody[i].length()*600);
				sheet.setColumnView(i, cellView);//根据内容自动设置列宽
				sheet.addCell(cell);
			}
			//插入数据
			if(bodyList != null) {
				logger.info("开始创建文件");
				//WritableFont wf = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD, false);
				//DisplayFormat displayFormat = NumberFormats.TEXT;
				//WritableCellFormat format = new WritableCellFormat(wf,displayFormat);
				for(int i = 0,j=start+1; i < bodyList.size(); i++,j++) {
					//获取写入数据
					List<String > dateList=bodyList.get(i);
					//写入数据
					for(int k=0 ;k<dateList.size();k++){
						String value = dateList.get(k);//处理导出客户电话后有特殊字符的问题
						value = (value==null||value.equals("null"))?"":value;
//	        			if(Check.isNumber(value)||Check.isDouble(value)){
//	        				jxl.write.Number number = new jxl.write.Number(k,j, Double.parseDouble(value),format);
//	        				sheet.addCell(number);
//	        			}else {
						Label label = new Label(k,j,value);
						sheet.addCell(label);
//						}
					}
				}
			}
			logger.info("创建文件结束");
			//关闭对象，释放资源
			workbook.write();
			workbook.close();
			os.close();
		} catch (Exception e) {
			os.close();
			logger.error(e);
		}
	}
}
