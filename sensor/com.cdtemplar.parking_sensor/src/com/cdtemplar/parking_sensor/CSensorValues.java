// The MIT License (MIT)
// Copyright (C) 2016 by Lixiong <lx@cdtemplar.com>,http://www.cdtemplar.com
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.cdtemplar.parking_sensor;
import java.util.Date;

public class CSensorValues {
	public String getSensorKey()
	{
		return "TB" + ID;
	}
	
	public int X0;//磁场X初值
	public int Y0;//磁场Y初值
	public int Z0;//磁场Z初值
	public int BusyRate = 0;	//有车概率 千分比
	
	public Date DT;	//时间
	public String PATH;
	public int ID;//车位探测器ID
	public int X;//磁场X值
	public int Y;//磁场Y值
	public int Z;//磁场Z值
	public int Q;//信号质量
	public int N;//发送编号
	public int D = -1;//距离值（cm），仅复合车位探测器有
	public int V;//电磁电压
	public int CV;//网关电磁电压
	public int GV;//中继器电磁电压
	public String GS;//中继器状态
	public String getID() {
		return "TB" + ID;
	}
	public void OnUpdate(int x,int y, int z, int d)
	{
		int mag = (int) Math.floor(Math.sqrt(Math.pow(x - X0, 2) + Math.pow(y - Y0, 2) + Math.pow(z - Z0, 2)));
		if(d >= 0)
		{
			int magLast = (int) Math.floor(Math.sqrt(Math.pow(X - X0, 2) + Math.pow(Y - Y0, 2) + Math.pow(Z - Z0, 2)));
			
			int rate = BusyRateCalc.CalcProbability(mag, d);	//计算一般有车概率
			int rateAmend = BusyRateCalc.CalcProbabilityAmend(magLast,mag,D,d,BusyRate,rate);	//计算综合有车概率
			
			BusyRate = rateAmend;
		}
		else
		{
			if(mag < 100)
				BusyRate = 0;
			else
				BusyRate = 1000;
		}
		X=x;
		Y=y;
		Z=z;
		D=d;
	}
	public String getTimeString()
	{
		java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy－MM－dd HH:mm:ss");
		return format.format(DT);
	}
	@Override
	public String toString() {
		java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy－MM－dd HH:mm:ss");
		return "CSensorValues [ID=" + ID 
				+ ", DT=" + getTimeString()
				+ ", RATE=" + BusyRate  
				+ ", X0=" + X0 
				+ ", y0=" + Y0 
				+ ", z0=" + Z0
				+ ", X=" + X 
				+ ", Y=" + Y 
				+ ", Z=" + Z
				+ ", Q=" + Q 
				+ ", N=" + N 
				+ ", D=" + D 
				+ ", V=" + V 
				+ ", CV=" + CV 
				+ ", GV=" + GV 
				+ ", GS=" + GS
				+ ", PATH=" + PATH + "]";
	}
}
