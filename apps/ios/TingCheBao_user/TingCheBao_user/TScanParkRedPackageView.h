//
//  TScanParkRedPackageView.h
//  
//
//  Created by apple on 15/7/22.
//
//

//扫描收费员红包二维码会弹出这个页面
#import "TPopView.h"
#import "TScanParkRedPackageItem.h"

typedef void(^scanParkRedParkageHandle)();
@interface TScanParkRedPackageView : TPopView

@property(nonatomic, copy) scanParkRedParkageHandle completeHandle;
@property(nonatomic, retain) TScanParkRedPackageItem* item;

@end
