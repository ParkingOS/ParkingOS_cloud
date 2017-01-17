//
//  TCommentItem.h
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TCommentItem : NSObject

@property(nonatomic, retain) NSString* time;
@property(nonatomic, retain) NSString* user;//带*的车牌号
@property(nonatomic, retain) NSString* info;
@property(nonatomic, retain) NSString* oldUser;//完整的车牌号

//{"info":"测试评论收费员","ctime":"1434187870","user":"京N78532"}
+(TCommentItem*)getItemWithDictionary:(NSDictionary*)dic;

@end
