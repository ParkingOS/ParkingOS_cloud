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


import com.google.gson.Gson;

import java.util.TimeZone;


public class SensorInterface{
	private static int[] wCRCTalbeAbs = {
        0x0000, 0xCC01, 0xD801, 0x1400, 0xF001, 0x3C00, 0x2800, 0xE401, 
        0xA001, 0x6C00, 0x7800, 0xB401, 0x5000, 0x9C01, 0x8801, 0x4400, 
    };
	private static int CalCrcByte(Byte da,int nCrc)
    {

        int wCRC = nCrc;
        wCRC = wCRCTalbeAbs[(da ^ wCRC) & 15] ^ (wCRC >> 4);
        wCRC = wCRCTalbeAbs[((da >> 4) ^ wCRC) & 15] ^ (wCRC >> 4);


        return wCRC & 0xffff;
    }

	private static String[] strNameList = {
            "\"DT\"",
            ",\"TID\"",
             ",\"PATH\"",
            "\",\"SEN\"",
        };
	private static int ShowChar2Bit6(char cc)
    {
        if (cc >= '0' && cc <= '9')
            return cc - '0';
        else if (cc >= 'A' && cc <= 'Z')
            return cc - 'A' + 10;
        else if (cc >= 'a' && cc <= 'z')
            return cc - 'a' + 10 + 26;
        else if (cc == '+')
            return 10 + 26 + 26;
        else
            return 10 + 26 + 26 + 1;
    }
	private static char Bit42Byte(int c)
    {
        if (c <= 9)
            return (char)(c + '0');
        if (c == 0xA)
            return '-';
        if (c == 0xB)
            return '\"';
        if (c == 0xC)
            return ',';
        if (c == 0xD)
            return ':';
        if (c == 0xE)
            return '!';
        return ' ';
    }

	private static String StringOutZip(String strIn)
    {
        int nVal = 0;
        int nBit = 0;
        String strOut = "";
        for(int i=0; i<strIn.length(); i++)
        {
        	char c = strIn.charAt(i);
            nVal <<= 6;
            nVal += ShowChar2Bit6(c);
            nBit += 6;

            while (nBit >= 4)
            {
                int nn = nVal >> (nBit - 4);
                nn &= 0xF;
                strOut += Bit42Byte(nn);
                nBit -= 4;
            }
        }
        String[] strList = strOut.split("!");
        strOut = "";
        for (int i=0; i<strList.length; i++)
        {
        	String str = strList[i];
        	StringBuilder strItem = new StringBuilder("{:" + str);
            int nStart = 0;
            int nInsert = 0;
            while (true)
            {
                int nPoint = strItem.indexOf(":", nStart);
                if (nPoint >= 0)
                {
                    if(nInsert >= 2)
                        strItem = strItem.insert(nPoint + 1,"\"");
                    strItem = strItem.insert(nPoint, strNameList[nInsert]);
                    nStart = nPoint + strNameList[nInsert].length() + 1;
                    if(nInsert == 3)
                    {
                        strItem = strItem.insert(strItem.length(),"\"");
                        break;
                    }
                }
                else
                {
                    break;
                }
                nInsert++;
            }
            strItem = strItem.insert(strItem.length(),"}");
            if (strOut != "")
                strOut += ',';
            strOut += strItem;
        }

        return strOut;
    }

	private static String JObjectOutZip(String strJson)
    {
        try
        {
        	Gson gson = new Gson();
        	CJsonFromNetGate jc = gson.fromJson(strJson, CJsonFromNetGate.class);

            if (jc.CRC != -1) //和
            {
                int nStart = strJson.indexOf("{");
                int nEnd = strJson.indexOf(",\"CRC\"");
                int nCrc = 0;
                for (int i = nStart + 1; i < nEnd; i++)
                {
                	nCrc = CalCrcByte((byte)strJson.charAt(i), nCrc);
                }
                if (nCrc == jc.CRC)
                {
                	String strMag = jc.getMAG();
                    if(strMag != null)
                    {
                    	  String strZip = StringOutZip(strMag);
                    	  strJson = strJson.replace("\"" + strMag + "\"", "[" + strZip + "]");
                    	  
                    }
                    return strJson;
                }
                else
                {

                }
            }
            else
            {
            }
        }
        catch (Exception ee)
        {
        }
        return null;

    }
    
    public static CNetGateMsg GetSiteMsg(String strJson){
    	try
    	{
    		Gson gson = new Gson();
    		if(strJson.indexOf("CRC") > 0 )
    		{
    			
		    	String str = JObjectOutZip(strJson);
		    	if(str != null)
		    	{
	
					CNetGateMsg sm = gson.fromJson(str, CNetGateMsg.class);
					long lTime = sm.NOW;
					if(sm.MAG != null)
					{
						for(CSensorMsg sensorMsg : sm.MAG){
							lTime += sensorMsg.DT;
							sensorMsg.DT = lTime;
						}
					}
					sm.VAR = 2;
					return sm;
		    	}
    		}
    		else
    		{
    			CJsonFromNetgateV0 jc = gson.fromJson(strJson, CJsonFromNetgateV0.class);
    			CNetGateMsg sm = new CNetGateMsg(jc);
    			sm.VAR = 0;
    			return sm;
    		}
    	}
    	catch (Exception ee)
        {
        }
        return null;

    }
    private static String JsonOutWithCRC(String strIn)
    {
        int nCrc = 0xFFFF;

        for (int i = 0; i < strIn.length(); i++)
        {
        	nCrc = CalCrcByte((byte)strIn.charAt(i), nCrc);
        }
        return "{" + strIn + ",\"CRC\":" + nCrc + "}\r\n";
    }
    static int MAX_POINT = 4096 *128;
    public static int getMaxPoint()
    {
    	return MAX_POINT;
    }
    public static long GetTimeTicks()
    {
    	long lTime = System.currentTimeMillis() / 1000; 	//UTC时间
        lTime += TimeZone.getDefault().getRawOffset() / 1000;	//本地时间
        
        return lTime;
    }
    public static String GetReadString(int nPoint)
    {
        if (nPoint >= 0)
            nPoint &= (MAX_POINT - 1);
        else
            nPoint = MAX_POINT;
        long lTime = GetTimeTicks(); 	//UTC时间
        String strOut = "\"NOW\":" + lTime
        		+ ",\"POT\":"+ nPoint
        		+ ",\"SD1\":0,\"SD2\":0";
        return JsonOutWithCRC(strOut);
    }
}
