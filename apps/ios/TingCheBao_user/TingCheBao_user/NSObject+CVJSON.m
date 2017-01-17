//
//  NSObject+CVJSON.m
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "NSObject+CVJSON.h"

@implementation NSObject (CVJSON)

-(NSString*)jsonValue {
    NSString *json = @"";
    if ([NSJSONSerialization isValidJSONObject:self]) {
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self options:NSJSONWritingPrettyPrinted error:&error];
        if(!error) {
            json =[[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        }
        else
            NSLog(@"JSON parse error: %@", error);
    }
    else
        NSLog(@"Not a valid JSON object: %@", self);
    return json;
}

@end
