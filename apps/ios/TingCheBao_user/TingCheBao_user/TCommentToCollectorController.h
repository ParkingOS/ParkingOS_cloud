//
//  TCommentToCollectorController.h
//  
//
//  Created by apple on 15/6/17.
//
//

//给收费员评论
#import "TBaseViewController.h"

typedef void(^commentCompleter)();

@interface TCommentToCollectorController : TBaseViewController

@property(nonatomic, retain) NSString* orderid;

@property(nonatomic, copy) commentCompleter completer;

@property(nonatomic, assign) BOOL needPushOrderPage;

@end
