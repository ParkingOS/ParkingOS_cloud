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
	
	public int X0;//�ų�X��ֵ
	public int Y0;//�ų�Y��ֵ
	public int Z0;//�ų�Z��ֵ
	public int BusyRate = 0;	//�г����� ǧ�ֱ�
	
	public Date DT;	//ʱ��
	public String PATH;
	public int ID;//��λ̽����ID
	public int X;//�ų�Xֵ
	public int Y;//�ų�Yֵ
	public int Z;//�ų�Zֵ
	public int Q;//�ź�����
	public int N;//���ͱ��
	public int D = -1;//����ֵ��cm���������ϳ�λ̽������
	public int V;//��ŵ�ѹ
	public int CV;//���ص�ŵ�ѹ
	public int GV;//�м�����ŵ�ѹ
	public String GS;//�м���״̬
	public String getID() {
		return "TB" + ID;
	}
	public void OnUpdate(int x,int y, int z, int d)
	{
		int mag = (int) Math.floor(Math.sqrt(Math.pow(x - X0, 2) + Math.pow(y - Y0, 2) + Math.pow(z - Z0, 2)));
		if(d >= 0)
		{
			int magLast = (int) Math.floor(Math.sqrt(Math.pow(X - X0, 2) + Math.pow(Y - Y0, 2) + Math.pow(Z - Z0, 2)));
			
			int rate = BusyRateCalc.CalcProbability(mag, d);	//����һ���г�����
			int rateAmend = BusyRateCalc.CalcProbabilityAmend(magLast,mag,D,d,BusyRate,rate);	//�����ۺ��г�����
			
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
		java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy��MM��dd HH:mm:ss");
		return format.format(DT);
	}
	@Override
	public String toString() {
		java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy��MM��dd HH:mm:ss");
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
