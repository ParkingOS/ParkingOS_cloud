//
//  TCommentItem.m
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TCommentItem.h"

@implementation TCommentItem

+(TCommentItem*)getItemWithDictionary:(NSDictionary*)dic {
    if (!dic)
        return nil;
    TCommentItem* item = [[TCommentItem alloc] init];
    item.time = [dic objectForKey:@"ctime"];
    item.user = [dic objectForKey:@"user"];
    if (item.user.length > 4) {
        item.user = [item.user stringByReplacingCharactersInRange:NSMakeRange(2, [item.user length] - 4) withString:@"***"];
    }
    item.oldUser = [dic objectForKey:@"user"];
    item.info = [dic objectForKey:@"info"];
    return item;
}

@end
