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
import java.util.List;


public class CJsonFromNetgateV0 {
	public int CID;//集中器ID
	public String PATH;//"10-11-12",路径，表示通过采集器10到11到12到达集中器
	public int N;//集中器发送编号
	public String SQ;//采集器信号质量
	public String Err;//错误码，正常下无值
	public int V;//电压
	public List<CSensorValues> MAG;//车位探测器信息组
	public String getCID() {
		return "TB" + CID;
	}
	@Override
	public String toString() {
		return "CJsonFromNetgateV0 [CID=" + CID
				+ ", PATH=" + PATH 
				+ ", N=" + N
				+ ", SQ=" + SQ 
				+ ", Err=" + Err 
				+ ", V=" + V 
				+ ", MAG=" + MAG + "]";
	}
}
