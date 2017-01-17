//
//  TLocationInfoViewController.m
//  
//
//  Created by apple on 15/6/26.
//
//

#import "TLocationInfoViewController.h"
#import "TAPIUtility.h"
#import "EMAlertView.h"
#import "TLocationViewController.h"
#import "TCommentNoteViewController.h"
#import <MobileCoreServices/MobileCoreServices.h>

#define button_width 70
#define bottom_height 50

@interface TLocationInfoViewController ()<UIActionSheetDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, MBProgressHUDDelegate>

@property(nonatomic, retain) UIImageView* imageView;

@property(nonatomic, retain) UIButton* levelButton;
@property(nonatomic, retain) UIButton* locationButton;
@property(nonatomic, retain) UILabel* timeLabel;
@property(nonatomic, retain) UIButton* photoButton;
@property(nonatomic, retain) UIImageView* whiteLineView;
@property(nonatomic, retain) UITextView* noteTextView;
@property(nonatomic, retain) UIButton* noteButton;

@property(nonatomic, retain) UIBarButtonItem* rightBarItem;

@end

@implementation TLocationInfoViewController

- (id)init {
    if (self = [super init]) {

        self.view.backgroundColor = [UIColor blackColor];
        
        _imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height - bottom_height)];
        _imageView.backgroundColor = [UIColor whiteColor];
        
        _levelButton = [UIButton buttonWithType:UIButtonTypeCustom];
//        [_levelButton setTitle:@"停车楼层" forState:UIControlStateNormal];
//        [_levelButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _levelButton.titleLabel.font = [UIFont systemFontOfSize:13];
        [_levelButton setBackgroundImage:[UIImage imageNamed:@"location_level.png"] forState:UIControlStateNormal];
        _levelButton.frame = CGRectMake(self.view.width - 5 - button_width, self.view.height/2, button_width, button_width);
        [_levelButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _locationButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_locationButton setBackgroundImage:[UIImage imageNamed:@"location_info.png"] forState:UIControlStateNormal];
        _locationButton.frame = CGRectMake(self.view.width - 5 - button_width, self.view.height - bottom_height - 10 - button_width, button_width, button_width);
        [_locationButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _timeLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, self.view.height - bottom_height - 10 - 20, 150, 20)];
        _timeLabel.text = @"";
        _timeLabel.textColor = RGBCOLOR(221, 202, 49);
        
        _photoButton = [UIButton buttonWithType:UIButtonTypeCustom];
        
        UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"location_refresh.png"]];
        imgView.frame = CGRectMake((50-20)/2, 5, 20, 20);
        
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 25, 50, 20)];
        label.text = @"重拍";
        label.textColor = [UIColor whiteColor];
        label.font = [UIFont systemFontOfSize:14];
        label.textAlignment = NSTextAlignmentCenter;
        [_photoButton addSubview:imgView];
        [_photoButton addSubview:label];
        _photoButton.frame = CGRectMake(10, self.view.height - bottom_height, 50, bottom_height);
        [_photoButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _whiteLineView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"white_line.png"]];
        _whiteLineView.frame = CGRectMake(_photoButton.right, _photoButton.top + (bottom_height - 40)/2, 2, 40);
        
        _noteTextView = [[UITextView alloc] initWithFrame:CGRectMake(_whiteLineView.right, _photoButton.top, self.view.width - _whiteLineView.right, bottom_height)];
        _noteTextView.textColor = [UIColor whiteColor];
        _noteTextView.backgroundColor = [UIColor clearColor];
        
        _noteButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _noteButton.frame = _noteTextView.frame;
        imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"location_add.png"]];
        CGSize labelSize = T_TEXTSIZE(@"添加详细说明", [UIFont systemFontOfSize:17]);
        NSLog(@"size-%lf", labelSize.width);
        imgView.frame = CGRectMake((_noteButton.width - 20 - labelSize.width - 10)/2, (bottom_height - 20)/2, 20, 20);
        
        label = [[UILabel alloc] initWithFrame:CGRectMake(imgView.right + 10, imgView.top, labelSize.width, 20)];
        label.text = @"添加详细说明";
        label.textColor = [UIColor whiteColor];
        label.font = [UIFont systemFontOfSize:16];
        label.textAlignment = NSTextAlignmentCenter;
        [_noteButton addSubview:imgView];
        [_noteButton addSubview:label];
        
        [_noteButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self.view addSubview:_imageView];
        [self.view addSubview:_levelButton];
        [self.view addSubview:_locationButton];
        [self.view addSubview:_timeLabel];
        [self.view addSubview:_photoButton];
        [self.view addSubview:_whiteLineView];
        [self.view addSubview:_noteTextView];
        [self.view addSubview:_noteButton];
        
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"停车标记";
    self.titleView.textColor = [UIColor whiteColor];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    //初始化数据
    [self initData];
    
    [self.navigationController.navigationBar setBarTintColor:[UIColor blackColor]];
    [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleLightContent;
    [self setNeedsStatusBarAppearanceUpdate];
    
    self.navigationItem.rightBarButtonItem = self.rightBarItem;
    
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController.navigationBar setBarTintColor:RGBCOLOR(254, 254, 254)];
    [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleDefault;
    [self setNeedsStatusBarAppearanceUpdate];
}

#pragma mark private

- (void)initData {
    //从本地取照片和层
    _imageView.image = [TAPIUtility getLoactionImage];
    if (GL(save_location_level)) {
        [_levelButton setTitle:GL(save_location_level) forState:UIControlStateNormal];
        [_levelButton setBackgroundImage:[UIImage imageNamed:@"location_gray.png"] forState:UIControlStateNormal];
    } else {
        [_levelButton setTitle:nil forState:UIControlStateNormal];
        [_levelButton setBackgroundImage:[UIImage imageNamed:@"location_level.png"] forState:UIControlStateNormal];
    }
    _timeLabel.text = GL(save_location_time);
    if (GL(save_location_right)) {
        [_locationButton setBackgroundImage:[UIImage imageNamed:@"location_open.png"] forState:UIControlStateNormal];
    } else {
        [_locationButton setBackgroundImage:[UIImage imageNamed:@"location_info.png"] forState:UIControlStateNormal];
    }
    _noteTextView.text = GL(save_location_note);
    if (GL(save_location_note) && ![GL(save_location_note) isEqualToString:@""]) {
        for (UIView* subview in _noteButton.subviews) {
            subview.hidden = YES;
        }
    } else {
        for (UIView* subview in _noteButton.subviews) {
            subview.hidden = NO;
        }
    }
}

- (UIBarButtonItem*)rightBarItem {
    UIButton* itemButton = [UIButton buttonWithType:UIButtonTypeCustom];
    itemButton.frame = CGRectMake(0, 0, 40, 40);
    
    UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"white_transh.png"]];
    imgView.frame = CGRectMake((40 - 22)/2, (40 - 25)/2, 22, 25);
    
    [itemButton addSubview:imgView];
    [itemButton addTarget:self action:@selector(deleteButtonTouch) forControlEvents:UIControlEventTouchUpInside];
    
   return [[UIBarButtonItem alloc] initWithCustomView:itemButton];
}

- (void)buttonTouched:(UIButton*)button {
    if (button == _levelButton) {
        UIActionSheet* sheet = [[UIActionSheet alloc] initWithTitle:@"选择楼层" delegate:self cancelButtonTitle:@"取消" destructiveButtonTitle:nil otherButtonTitles:@"5层",
                                                         @"4层",
                                                         @"3层",
                                                         @"2层",
                                                         @"1层",
                                                         @"-1层",
                                                         @"-2层",
                                                         @"-3层", nil];
        [sheet showInView:self.view];
        
    } else if (button == _photoButton) {
        if (![TAPIUtility checkPhotoAuthorization]) {
            return;
        }
        
        UIImagePickerController* picker = [[UIImagePickerController alloc] init];
        picker.sourceType = UIImagePickerControllerSourceTypeCamera;
        picker.mediaTypes = @[(NSString*)kUTTypeImage];
        picker.delegate = self;
        [self presentViewController:picker animated:YES completion:nil];
        
    } else if (button == _locationButton) {
        
        location_mode mode;
        if (GL(save_location_right)) {
            mode = location_mode_show;
        } else {
            mode = location_mode_choose;
        }
        
        TLocationViewController* vc = [[TLocationViewController alloc] initWithMode:mode];
        [self.navigationController pushViewController:vc animated:YES];
    } else if (button == _noteButton) {
        TCommentNoteViewController* vc = [[TCommentNoteViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
    }
}

- (void)deleteButtonTouch {
    [EMAlertView showAlertWithTitle:@"提示" message:@"确认要删除吗?" completionBlock:^(NSUInteger buttonIndex, EMAlertView *alertView) {
        if (buttonIndex == 1) {
            [TAPIUtility saveLocationImage:nil];
            SL(nil, save_location_level);
            SL(nil, save_location_time);
            SL(nil, save_location_note);
            SL(nil, save_location_lat);
            SL(nil, save_location_log);
            SL(nil, save_location_right);
            
            [TAPIUtility alertMessage:@"删除成功!" success:YES toViewController:self];
        }
    } cancelButtonTitle:@"取消" otherButtonTitles:@"确认", nil];
    

}

#pragma mark UIActionSheetDelegate

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != actionSheet.cancelButtonIndex) {
        //保存到本地
        SL([actionSheet buttonTitleAtIndex:buttonIndex], save_location_level);
        
        [_levelButton setBackgroundImage:[UIImage imageNamed:@"location_gray.png"] forState:UIControlStateNormal];
        [_levelButton setTitle:[actionSheet buttonTitleAtIndex:buttonIndex] forState:UIControlStateNormal];
    }
}

#pragma mark UIImagePickerControllerDelegate

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    UIImage* image = (UIImage*) [info objectForKey:UIImagePickerControllerOriginalImage];
    [picker dismissViewControllerAnimated:YES completion:nil];
    
    
    _imageView.image = image;
    
    [TAPIUtility saveLocationImage:image];
    SL([self getNowTime], save_location_time);
}

- (NSString*)getNowTime {
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"yyyy-MM-dd HH:mm";
    return [formatter stringFromDate:[NSDate new]];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark MBProgressHUDDelegate

- (void)hudWasHidden:(MBProgressHUD *)hud {
    [self.navigationController popViewControllerAnimated:YES];
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
