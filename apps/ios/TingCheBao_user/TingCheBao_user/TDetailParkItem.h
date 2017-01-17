//
//  TDetailParkItem.h
//  TingCheBao_user
//
//  Created by apple on 14-9-3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TDetailParkItem : NSObject

@property(nonatomic, retain) NSString* total;//总共停车数
@property(nonatomic, retain) NSString* freeSpace;//空闲车位

@property(nonatomic, retain) NSString* updatetime;//更新时间
@property(nonatomic, retain) NSString* parking_type;//0 地面 1 地下 2 路边 3地下/地面混合
@property(nonatomic, retain) NSString* hasPraise;//0 贬过 -1 无，1 赞过
@property(nonatomic, retain) NSString* currentPrice;//价格
@property(nonatomic, retain) NSString* stop_type;//0 平面 1 立体
@property(nonatomic, retain) NSString* disparageNum;
@property(nonatomic, retain) NSString* praiseNum;//赞 数量

@property(nonatomic, retain) NSString* address;//地址
@property(nonatomic, retain) NSString* descri;
//@property(nonatomic, retain) NSString* description;//描述
//5种支持, 按下面顺序排列
@property(nonatomic, retain) NSString* nfc;
@property(nonatomic, retain) NSString* etc;
@property(nonatomic, retain) NSString* book;
@property(nonatomic, retain) NSString* navi;//是否支持室内导航
@property(nonatomic, retain) NSString* monthlyPay;

@property(nonatomic, retain) NSString* photoUrl;//照片地址
@property(nonatomic, retain) NSString* mobile;//手机

+ (TDetailParkItem*)getItemFromDictionary:(NSDictionary*)dictionary;

//{"total":"50","updatetime":"2014-09-02 10:11","parking_type":"1","etc":"0","hasPraise":"-1","currentPrice":"1.0元/15分钟","navi":"0","stop_type":"0","disparageNum":"0","freeSpace":"11","praiseNum":"0","monthlyPay":"1","address":"北京市海淀区上地三街26号","description":"本车场环境优雅，车位多，价格优惠！欢迎光临！","book":"1","nfc":"1","photoUrl":["parkpics/3_1407378785.jpeg"],"mobile":""}

@end
