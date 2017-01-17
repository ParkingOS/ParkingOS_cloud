//
//  CVAPIRequestModel.h
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//
#import "CVAPIRequestModel.h"

#define PAGE_LIMIT  10

#define PARAM_FIRST_PAGE @"F"
#define PARAM_LAST_PAGE @"L"
#define PARAM_NEXT_PAGE @"N"
#define PARAM_PREV_PAGE @"P"

#define ITEM_OBJECT @"obj"

@class CVAPIListModel;

@interface CVAPIListModel : CVAPIRequestModel <NSCoding>

@property(nonatomic, assign) NSInteger pageIdx;
@property(nonatomic, retain) NSString* page;// the value is F/L/N/P
@property(nonatomic, retain) NSMutableArray* items;
@property(nonatomic, retain) NSMutableSet* keySetOfContactItems;
@property(nonatomic, retain) NSMutableArray* nextItems;
@property(nonatomic, retain) NSMutableArray* prevItems;
@property (nonatomic, assign) BOOL hasMore;
@property(nonatomic, assign) NSInteger total;

@property (nonatomic, assign) BOOL isActionMode;
@property (nonatomic, retain) NSMutableSet* selectedKeys;
@property (nonatomic, retain) NSString* filter;
@property (nonatomic, retain) NSString* status;
@property (nonatomic, retain) NSString* type;
@property (nonatomic, retain) NSString* sortBy;
@property (nonatomic, retain) NSString* order;
@property (nonatomic, retain) NSString* searchText;

- (void)loadMore:(BOOL)more;

- (void)updateWithFilter:(NSString*)filter;
- (void)updateWithStatus:(NSString *)status;
- (void)updateWithType:(NSString*)type;
- (void)search:(NSString *)searchText;
- (void)sortByField:(NSString*)sortBy;
- (void)sortByField:(NSString*)sortBy withOrder:(NSString*)order;

@end
