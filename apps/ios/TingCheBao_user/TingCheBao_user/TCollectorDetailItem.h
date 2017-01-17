//
//  TCollectorDetailItem.h
//  
//
//  Created by apple on 15/6/18.
//
//

#import <Foundation/Foundation.h>

@interface TCollectorDetailItem : NSObject

@property(nonatomic, retain) NSString* rcount;//打赏数
@property(nonatomic, retain) NSString* money;//打赏金额
@property(nonatomic, retain) NSString* scount;//服务次数
@property(nonatomic, retain) NSString* wcount;//最近一周服务次数
@property(nonatomic, retain) NSString* ccount;//评论数
@property(nonatomic, retain) NSString* mobile;//手机号


+ (TCollectorDetailItem*)getItemFromDic:(NSDictionary*)dic;

@end
