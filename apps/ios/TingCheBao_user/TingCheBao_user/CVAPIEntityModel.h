//
//  CVAPIEntityModel.h
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "CVAPIRequestModel.h"

@interface CVAPIEntityModel : CVAPIRequestModel

@property (nonatomic, retain) NSString* key;

- (id)initWithKey:(NSString*)key;

/* Can be overridden by the subclass if subclass is non-standard */

- (void)load;

/* The following can be overridden by the subclass */

- (NSString*) getAPIPath;
- (NSString*) getMethd;
- (NSString*) getAction;

/* Generic way to get cache key for CVAPIEntityModel */

- (NSString *) getCacheKey;

@end
