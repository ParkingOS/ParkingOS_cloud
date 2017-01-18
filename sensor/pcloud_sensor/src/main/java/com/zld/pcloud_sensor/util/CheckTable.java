package com.zld.pcloud_sensor.util;

public class CheckTable {
	static int[][] m_nssPointMag = {
		{0, 1}, 					//0 ~ 1%
	    {10, 1}, 
	    {40, 10},
	    {120, 83},
	    {150, 98},
	    {200, 99},						//200 ~ 99%
	    {300, 99},	
	};
	
	static int[][] m_nssPointIR = {
		{0, 99}, 			//0 ~ 99%
	    {10, 99}, 
	    {30, 98},
	    {40, 80},
	    {65, 10},
	    {75, 3},
	    {100, 1},				//100 ~ 1%	
	    {110, 1}
	};
	
	/**
	 * 获取概率
	 * @param nssPoint
	 * @param nIn
	 * @return
	 */
	public static int ReadTable2Value(int[][] nssPoint, int nIn){
	    int nStart = 0;
	    if (nssPoint.length < 2)
	        return 0;
	    if (nIn > nssPoint[0][0]) {
	        for (nStart = 0; nStart < nssPoint.length - 2; nStart++) {
	            if (nIn > nssPoint[nStart][0] && nIn <= nssPoint[nStart + 1][0]) {
	                break;
	            }
	        }
	    }
	    int nn = (nIn - nssPoint[nStart][0]) * (nssPoint[nStart][1] - nssPoint[nStart + 1][1]) / (nssPoint[nStart][0] - nssPoint[nStart + 1][0]);
	    nn += nssPoint[nStart][1];
	    return nn;
	}
	
	/**
	 * 返回0~1000，小于500表示无车 大于等于500表示有车
	 * @param nMag 磁场变化值 nMag=sqrt((X-X0)^2 + (Y-Y0)^2 + (Z-Z0)^2)
	 * @param nIR 红外距离值
	 * @return
	 */
	public static int CalcProbability(int nMag, int nIR) {
	    int nMagL = ReadTable2Value(m_nssPointMag, nMag);
	    int nIRL = ReadTable2Value(m_nssPointIR, nIR);
	    int nn = nMagL * 55 + nIRL * 45;
	    return (nn + 5) / 10;
	}
}
