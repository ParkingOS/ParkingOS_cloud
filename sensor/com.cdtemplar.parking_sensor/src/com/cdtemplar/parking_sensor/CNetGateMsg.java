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
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class CNetGateMsg {
	public int CID;
	public long NOW;
	public int POT;
	public int NUM;
	public int PD;
	public int VAR = 0;		//�汾��
	public List<CSensorMsg> MAG;
	
	public int CRC = -1;
	public String getNetgateKey()
	{
		return "TB" + CID;
	}
	public CNetGateMsg(CJsonFromNetgateV0 jc) 
	{
		// TODO �Զ����ɵĹ��캯�����
		CID = jc.CID;
		if(jc.Err != null)
			return;
		NOW = SensorInterface.GetTimeTicks();
		POT = -1;
		NUM = 0;
		PD = -1;
		
		int CV = jc.V;

		LinkedList<CSensorMsg> MagList = new LinkedList<CSensorMsg>();
		String strSQ = jc.SQ.replace('|', ',');
		if(strSQ == "")
		{
			String[] strSQList = strSQ.split(",");
			
			if(strSQList.length >= 3)
			{
				strSQ = strSQList[0] + ","
						+ strSQList[1] + ","
						+ (Integer.parseInt(strSQList[2]) + 200);
			}
		}
		else
		{
			strSQ = "0";
		}
		if(jc.MAG != null)
		{
			for(CSensorValues csv : jc.MAG){
				CSensorMsg csm = new CSensorMsg();
				csm.DT = NOW;
				csm.TID = csv.ID;
				csm.PATH = jc.PATH;
				
				csm.SEN = "" + csv.X
						+ "," + csv.Y
						+ "," + csv.Z
						+ "," + csv.D
						+ "," + csv.Q
						+ "," + 0
						+ "," + csv.V
						+ "," + (jc.V) / 10
						+ "," + strSQ;	
				MagList.add(csm);
			}
		}
		else
		{
			CSensorMsg csm = new CSensorMsg();
			csm.DT = NOW;
			csm.TID = 0;
			csm.PATH = jc.PATH;
			
			csm.SEN = "" + 0
					+ "," + 0
					+ "," + 0
					+ "," + 0
					+ "," + 0
					+ "," + 0
					+ "," + 0
					+ "," + (jc.V) / 10
					+ "," + strSQ;	
			MagList.add(csm);
		}
		MAG = MagList;
	}
	public int SensorNum()
	{

		if(MAG == null)
			return 0;
		else
			return MAG.size();
	}
	public CSensorValues getSensorValues(int index){
		CSensorMsg sm = MAG.get(index);
		if(sm != null)
		{
			CSensorValues sv = new CSensorValues();
			sv.DT = new Date( sm.DT * 1000L - TimeZone.getDefault().getRawOffset());
			sv.PATH = sm.PATH + "-" + CID;
			sv.ID = sm.TID;//������ID
			sm.SEN = sm.SEN.replace(" ", "");	//ȥ�����ܵĿո�
			String[] strValues = sm.SEN.split(",");
			if(strValues.length >= 7)	//����0�����ݲ��ǳ�����
			{
				sv.X = Integer.parseInt(strValues[0]);//�ų�Xֵ
				sv.Y = Integer.parseInt(strValues[1]);//�ų�Yֵ
				sv.Z = Integer.parseInt(strValues[2]);//�ų�Zֵ
				sv.D = Integer.parseInt(strValues[3]);//����ֵ��cm���������ϳ�λ̽������
				sv.Q = Integer.parseInt(strValues[4]);//�ź�����
				sv.N = Integer.parseInt(strValues[5]);//���ͱ��
				sv.V = Integer.parseInt(strValues[6]);//��ŵ�ѹ
				sv.CV = Integer.parseInt(strValues[7]);//���ص�ŵ�ѹ
				if(strValues.length >= 11 && sm.PATH != "")
				{
					sv.GS = strValues[8] + "|" + strValues[9];	//�м���״̬
					sv.GV = Integer.parseInt(strValues[10]);	//�м�����ŵ�ѹ
				}
			}
			return sv;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "CNetGateMsg [CID=" + CID 
				+", NOW=" + NOW
				+", POT=" + POT
				+", NUM=" + NUM
				+", PD=" + PD
				+", MAG=" + MAG
				+", CRC=" + CRC
				+ "]";
	}
}
