//
//  TParkMonthDetailBottomView.m
//  TingCheBao_user
//
//  Created by apple on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthDetailBottomView.h"
#import "TAPIUtility.h"

@interface TParkMonthDetailBottomView()

@property(nonatomic, retain) UILabel* parkInfoLabel;
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UILabel* addressLabel;
@property(nonatomic, retain) UIView* line2View;
@property(nonatomic, retain) UIButton* phoneButton ;
@property(nonatomic, retain) UIView* line3View;
@property(nonatomic, retain) UIView* commentView;
@property(nonatomic, retain) UIImageView* priseImgView;
@property(nonatomic, retain) UILabel* priseLabel;
@property(nonatomic, retain) UIImageView* projectImgView;
@property(nonatomic, retain) UILabel* projectLabel;
@property(nonatomic, retain) UILabel* commentLabel;
@property(nonatomic, retain) UIImageView* commentImgView;

@property(nonatomic, retain) UIView* line4View;
@property(nonatomic, retain) UILabel* produceInfoLabel;
@property(nonatomic, retain) UIView* line5View;
@property(nonatomic, retain) UILabel* descriptionLabel;

@property(nonatomic, retain) UIView* line6View;
@property(nonatomic, retain) UILabel* knowHeaderLabel;
@property(nonatomic, retain) UIView* line7View;
@property(nonatomic, retain) UILabel* knowLabel;

@property(nonatomic, retain) UIWebView* webView;

@end
@implementation TParkMonthDetailBottomView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        _parkInfoLabel = [[UILabel alloc] init];
        _parkInfoLabel.backgroundColor = [UIColor clearColor];
        _parkInfoLabel.textColor = [UIColor grayColor];
        _parkInfoLabel.text = @"停车场信息";
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];
        _nameLabel.textColor = [UIColor blackColor];
        _nameLabel.text = @"";
        
        _addressLabel = [[UILabel alloc] init];
        _addressLabel.backgroundColor = [UIColor clearColor];
        _addressLabel.textColor = gray_color;
        _addressLabel.text = @"";
        
        _phoneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_phoneButton setBackgroundImage:[UIImage imageNamed:@"phones.png"] forState:UIControlStateNormal];
        
        _commentView = [[UIView alloc] init];
        UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture:)];
        [_commentView addGestureRecognizer:tapGesture];
        
        _priseImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_praise.png"]];
        
        _priseLabel = [[UILabel alloc] init];
        _priseLabel.backgroundColor = [UIColor clearColor];
        _priseLabel.textColor = gray_color;
        
        _projectImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_disparage.png"]];
        
        _projectLabel = [[UILabel alloc] init];
        _projectLabel.backgroundColor = [UIColor clearColor];
        _projectLabel.textColor = gray_color;
        
        _commentLabel = [[UILabel alloc] init];
        _commentLabel.backgroundColor = [UIColor clearColor];
        _commentLabel.textColor = gray_color;
        _commentLabel.textAlignment = NSTextAlignmentRight;
        
        _commentImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_arrow_grey.png"]];
        
        _produceInfoLabel = [[UILabel alloc] init];
        _produceInfoLabel.backgroundColor = [UIColor clearColor];
        _produceInfoLabel.textColor = gray_color;
        _produceInfoLabel.text = @"产品信息";
        
        _descriptionLabel = [[UILabel alloc] init];
        _descriptionLabel.backgroundColor = [UIColor clearColor];
        _descriptionLabel.textColor = [UIColor blackColor];
        _descriptionLabel.text = @"本车场环境优雅，车位多，价格优惠！欢迎光临!";
        
        _knowHeaderLabel = [[UILabel alloc] init];
        _knowHeaderLabel.backgroundColor = [UIColor clearColor];
        _knowHeaderLabel.textColor = gray_color;
        _knowHeaderLabel.text = @"购买须知";
        
        
        _knowLabel = [[UILabel alloc] init];
        _knowLabel.backgroundColor = [UIColor clearColor];
        _knowLabel.textColor = gray_color;
        _knowLabel.text = @"购买须知";
        _knowLabel.numberOfLines = 0;
        
        _webView = [[UIWebView alloc] init];
        NSURLRequest* request = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:@"http://s.tingchebao.com/zld/presume.jsp"]];
        [_webView loadRequest:request];
        
        [_commentView addSubview:_priseImgView];
        [_commentView addSubview:_priseLabel];
        [_commentView addSubview:_projectImgView];
        [_commentView addSubview:_projectLabel];
        [_commentView addSubview:_commentLabel];
        [_commentView addSubview:_commentImgView];
        
        [self addSubview:_parkInfoLabel];
        [self addSubview:_nameLabel];
        [self addSubview:_addressLabel];
        [self addSubview:_phoneButton];
        [self addSubview:_commentView];
        [self addSubview:_produceInfoLabel];
        [self addSubview:_descriptionLabel];
        [self addSubview:_knowHeaderLabel];
        [self addSubview:_knowLabel];
        [self addSubview:_webView];
        
        //添加所有  lineView
        [self createLineViews];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _parkInfoLabel.frame = CGRectMake(0, 0, 200, 30);
    _line1View.frame = CGRectMake(0, _parkInfoLabel.bottom, self.width, 0.5);
    _nameLabel.frame = CGRectMake(0, _line1View.bottom, self.width, 30);
    _addressLabel.frame = CGRectMake(0, _nameLabel.bottom, self.width, 30);
    _line2View.frame = CGRectMake(self.width - 33, _addressLabel.top, 1, 30);
    _phoneButton.frame = CGRectMake(self.width - 28, _addressLabel.top + 2, 25, 25);
    
    _line3View.frame = CGRectMake(0, _addressLabel.bottom, self.width, 0.5);
    _commentView.frame = CGRectMake(0, _line3View.bottom, self.width, 30);
    _priseImgView.frame = CGRectMake(4, 5, 20, 20);
    _priseLabel.frame = CGRectMake(_priseImgView.right + 4, 0, 60, 30);
    _projectImgView.frame = CGRectMake(_priseLabel.right + 4, 5, 20, 20);
    _projectLabel.frame = CGRectMake(_projectImgView.right + 4, 0, 60, 30);
    _commentLabel.frame = CGRectMake(self.width - 150, 0, 130, 30);
    _commentImgView.frame = CGRectMake(_commentLabel.right, 5, 20, 20);
    
    _line4View.frame = CGRectMake(0, _commentView.bottom, self.width, 2);
    _produceInfoLabel.frame = CGRectMake(0, _line4View.bottom, self.width, 30);
    _line5View.frame = CGRectMake(0, _produceInfoLabel.bottom, self.width, 0.5);
    _descriptionLabel.frame = CGRectMake(0, _line5View.bottom, self.width, 30);
    
    _line6View.frame = CGRectMake(0, _descriptionLabel.bottom, self.width, 2);
    _knowHeaderLabel.frame = CGRectMake(0, _line6View.bottom, self.width, 30);
    
    _line7View.frame = CGRectMake(0, _knowHeaderLabel.bottom, self.width, 0.5);
    CGSize size = T_TEXTSIZE(_knowLabel.text, _knowLabel.font);
    _knowLabel.frame = CGRectMake(0, _line7View.bottom, self.width, size.height);
    
    _webView.frame = CGRectMake(0, _knowLabel.bottom, self.width, 200);
}

- (void)createLineViews {
    for (int i = 0; i< 7; i++) {
        UIView* view = [[UIView alloc] init];
        view.backgroundColor = light_white_color;
        [self addSubview:view];
        
        if (!_line1View)
            _line1View = view;
        else if (!_line2View)
            _line2View = view;
        else if (!_line3View)
            _line3View = view;
        else if (!_line4View)
            _line4View = view;
        else if (!_line5View)
            _line5View = view;
        else if (!_line6View)
            _line6View = view;
        else if (!_line7View)
            _line7View = view;
    }
}

- (void)setItem:(TParkMonthDetailItem *)item monthItem:(TParkMonthItem*)monthItem {
    _item = item;
    _monthItem = monthItem;
    
    _nameLabel.text = item.company_name;
    _addressLabel.text = item.address;
    _priseLabel.text = item.praiseNum;
    _projectLabel.text = item.disparageNum;
    
    _commentLabel.text = [NSString stringWithFormat:@"%@人评价", item.commentnum];
    if ([_item.resume isEqualToString:@""]) {
        _descriptionLabel.text = @"本车场环境优雅，车位多，价格优惠！欢迎光临!";
    } else {
        _descriptionLabel.text = _item.resume;
    }
    
    //组成一个长的字符串
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[_item.limitday integerValue]];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd之前"];
    NSString* validDay = [formatter stringFromDate:date];
    
    NSString* fixedString = [_monthItem.reserved isEqualToString:@"0"] ? @"车位不固定，但能保定包月用户到现场有车位可停！" : @"车位固定，专用车位，一个独享";
    
    NSString* know = [NSString stringWithFormat:@"有效期:\n%@\n使用时间:\n日间(%@)\n车位是否固定:\n%@\n使用规则:\n", validDay, _monthItem.limittime, fixedString];
    NSMutableAttributedString* knowAttributeString = [[NSMutableAttributedString alloc] initWithString:know];
    NSRange range1 = [know rangeOfString:@"有效期:"];
    NSRange range2 = [know rangeOfString:@"使用时间:"];
    NSRange range3 = [know rangeOfString:@"车位是否固定:"];
    NSRange range4 = [know rangeOfString:@"使用规则:"];
    
    [knowAttributeString addAttributes:@{NSForegroundColorAttributeName : orange_color} range:range1];
    [knowAttributeString addAttributes:@{NSForegroundColorAttributeName : orange_color} range:range2];
    [knowAttributeString addAttributes:@{NSForegroundColorAttributeName : orange_color} range:range3];
    [knowAttributeString addAttributes:@{NSForegroundColorAttributeName : orange_color} range:range4];
    _knowLabel.attributedText = knowAttributeString;
}

- (void)handleTapGesture:(UIGestureRecognizer*)gesture {
    if ([_item.mobile isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"该停车场暂未提供电话"];
        return;
    }
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", _item.mobile]]];
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
