//
//  TShareItem.m
//  TingCheBao_user
//
//  Created by apple on 14/12/25.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TShareItem.h"

@implementation TShareItem

+ (TShareItem*) getItemFromDic:(NSDictionary*)dic {
    TShareItem* item = [[TShareItem alloc] init];
    item.imgurl = [dic objectForKey:@"imgurl"];
    item.title = [dic objectForKey:@"title"];
    item.descri = [dic objectForKey:@"description"];
    item.url = [dic objectForKey:@"url"];
    return item;
}

@end
